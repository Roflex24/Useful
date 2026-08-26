package my.help.finance.general.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.common.exception.ConflictException;
import my.help.finance.general.dto.*;
import my.help.finance.general.entity.*;
import my.help.finance.general.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FinanceSnapshotService {

    private final MonthlyFinanceSnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;
    private final CashbackRepository cashbackRepository;
    private final DepositRepository depositRepository;
    private final SecurityRepository securityRepository;
    private final ObjectMapper objectMapper;

    public void createSnapshotForPreviousMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstOfCurrentMonth = today.withDayOfMonth(1);
        LocalDate snapshotDate = firstOfCurrentMonth.minusDays(1);

        YearMonth snapshotYearMonth = YearMonth.from(snapshotDate);

        if (snapshotRepository.existsBySnapshotDate(snapshotDate)) {
            throw new ConflictException("Snapshot for " + snapshotYearMonth + " already exists");
        }

        log.info("Creating finance snapshot for {}", snapshotYearMonth);

        List<Account> allAccounts = accountRepository.findAll();
        int snapshotsCreated = 0;

        for (Account account : allAccounts) {
            List<Cashback> cashbacks = cashbackRepository.findByAccount(account);

            List<CashbackSnapshotDto> cashbackDtos = cashbacks.stream()
                    .map(cb -> new CashbackSnapshotDto(
                            cb.getId(),
                            cb.getCategory(),
                            cb.getPercentage()
                    ))
                    .collect(Collectors.toList());

            String cashbacksJson;
            try {
                cashbacksJson = objectMapper.writeValueAsString(cashbackDtos);
            } catch (Exception e) {
                log.error("Failed to serialize cashbacks for account {}", account.getId(), e);
                cashbacksJson = "[]";
            }

            String depositJson = null;
            if (account.getType() == AccountType.DEPOSIT || account.getType() == AccountType.SAVINGS) {
                Optional<Deposit> depositOpt = depositRepository.findByAccountId(account.getId());
                if (depositOpt.isPresent()) {
                    Deposit deposit = depositOpt.get();
                    DepositSnapshotDto depositDto = new DepositSnapshotDto(
                            deposit.getId(),
                            deposit.getEndDate(),
                            deposit.getInterestPaymentDate(),
                            deposit.getInterestRate()
                    );
                    try {
                        depositJson = objectMapper.writeValueAsString(depositDto);
                    } catch (Exception e) {
                        log.error("Failed to serialize deposit for account {}", account.getId(), e);
                    }
                }
            }

            String securitiesJson = null;
            if (account.getType() == AccountType.INVESTMENT) {
                List<Security> securities = securityRepository.findByAccountId(account.getId());
                List<SecuritySnapshotDto> securityDtos = securities.stream()
                        .map(s -> new SecuritySnapshotDto(
                                s.getId(),
                                s.getSecurityType(),
                                s.getTicker(),
                                s.getQuantity(),
                                s.getCurrentPrice(),
                                s.getFaceValue(),
                                s.getCouponRate(),
                                s.getMaturityDate()
                        ))
                        .collect(Collectors.toList());
                try {
                    securitiesJson = objectMapper.writeValueAsString(securityDtos);
                } catch (Exception e) {
                    log.error("Failed to serialize securities for account {}", account.getId(), e);
                    securitiesJson = "[]";
                }
            }

            MonthlyFinanceSnapshot snapshot = MonthlyFinanceSnapshot.builder()
                    .snapshotDate(snapshotDate)
                    .accountId(account.getId())
                    .bankName(account.getBankName())
                    .amount(account.getAmount())
                    .type(account.getType())
                    .comment(account.getComment())
                    .cashbacksJson(cashbacksJson)
                    .depositJson(depositJson)
                    .securitiesJson(securitiesJson)
                    .createdBy("SYSTEM_SCHEDULER")
                    .build();

            snapshotRepository.save(snapshot);
            snapshotsCreated++;
        }

        updateDenormalizedBankTotals(snapshotDate);

        log.info("Snapshot for {} completed. Created {} snapshots", snapshotYearMonth, snapshotsCreated);
    }

    private void updateDenormalizedBankTotals(LocalDate snapshotDate) {
        List<MonthlyFinanceSnapshot> snapshots = snapshotRepository.findBySnapshotDate(snapshotDate);

        Map<String, BigDecimal> totalsByBank = snapshots.stream()
                .collect(Collectors.groupingBy(
                        MonthlyFinanceSnapshot::getBankName,
                        Collectors.reducing(BigDecimal.ZERO, MonthlyFinanceSnapshot::getAmount, BigDecimal::add)
                ));

        for (MonthlyFinanceSnapshot snapshot : snapshots) {
            snapshot.setTotalAmountByBank(totalsByBank.get(snapshot.getBankName()));
        }

        snapshotRepository.saveAll(snapshots);
    }

    public List<SnapshotInfoDto> getAvailableSnapshots() {
        List<LocalDate> snapshotDates = snapshotRepository.findAllSnapshotDates();

        return snapshotDates.stream()
                .map(date -> {
                    YearMonth yearMonth = YearMonth.from(date);
                    List<MonthlyFinanceSnapshot> snapshots = snapshotRepository.findBySnapshotDate(date);
                    int totalCashbacks = snapshots.stream()
                            .mapToInt(s -> parseCashbacksCount(s.getCashbacksJson()))
                            .sum();

                    return new SnapshotInfoDto(
                            yearMonth,
                            date,
                            snapshots.size(),
                            totalCashbacks,
                            yearMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")))
                    );
                })
                .sorted((a, b) -> b.snapshotDate().compareTo(a.snapshotDate()))
                .collect(Collectors.toList());
    }

    public HistoricalDataResponseDto getHistoricalData(YearMonth yearMonth) {
        LocalDate snapshotDate = yearMonth.atEndOfMonth();

        List<MonthlyFinanceSnapshot> snapshots = snapshotRepository.findBySnapshotDate(snapshotDate);

        if (snapshots.isEmpty()) {
            log.warn("No snapshot found for {}", yearMonth);
            return null;
        }

        List<AccountRs> accounts = new ArrayList<>();

        for (MonthlyFinanceSnapshot snapshot : snapshots) {
            AccountRs accountDto = AccountRs.builder()
                    .id(snapshot.getAccountId())
                    .bankName(snapshot.getBankName())
                    .amount(snapshot.getAmount())
                    .type(snapshot.getType())
                    .comment(snapshot.getComment())
                    .build();

            List<CashbackRs> cashbacks = parseCashbacksToRs(
                    snapshot.getCashbacksJson(),
                    snapshot.getAccountId(),
                    snapshot.getBankName()
            );
            accountDto.setCashbacks(cashbacks);

            if (snapshot.getType() == AccountType.DEPOSIT || snapshot.getType() == AccountType.SAVINGS) {
                accountDto.setDepositInfo(parseDepositToDto(snapshot.getDepositJson()));
            }

            if (snapshot.getType() == AccountType.INVESTMENT) {
                List<SecurityRs> securities = parseSecuritiesToRs(
                        snapshot.getSecuritiesJson(),
                        snapshot.getAccountId(),
                        snapshot.getBankName()
                );
                accountDto.setSecurities(securities);
            }

            accounts.add(accountDto);
        }

        FinanceSummaryDto summary = buildSummaryFromSnapshots(snapshots);

        List<BankCashbackSummaryDto> cashbackSummary = buildCashbackSummaryFromSnapshots(snapshots);

        return new HistoricalDataResponseDto(
                snapshotDate,
                accounts,
                summary,
                cashbackSummary
        );
    }

    private FinanceSummaryDto buildSummaryFromSnapshots(List<MonthlyFinanceSnapshot> snapshots) {
        BigDecimal totalAmount = snapshots.stream()
                .map(MonthlyFinanceSnapshot::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> amountByBank = snapshots.stream()
                .collect(Collectors.groupingBy(
                        MonthlyFinanceSnapshot::getBankName,
                        Collectors.reducing(BigDecimal.ZERO, MonthlyFinanceSnapshot::getAmount, BigDecimal::add)
                ));

        Map<AccountType, BigDecimal> amountByType = snapshots.stream()
                .collect(Collectors.groupingBy(
                        MonthlyFinanceSnapshot::getType,
                        Collectors.reducing(BigDecimal.ZERO, MonthlyFinanceSnapshot::getAmount, BigDecimal::add)
                ));

        for (AccountType type : AccountType.values()) {
            amountByType.putIfAbsent(type, BigDecimal.ZERO);
        }

        return FinanceSummaryDto.builder()
                .totalAmount(totalAmount)
                .amountByBank(amountByBank)
                .amountByType(amountByType)
                .cashbackSummaryByBank(new HashMap<>())
                .bestCashbackByCategory(new HashMap<>())
                .build();
    }

    private List<BankCashbackSummaryDto> buildCashbackSummaryFromSnapshots(List<MonthlyFinanceSnapshot> snapshots) {
        Map<String, List<CashbackRs>> cashbacksByBank = new HashMap<>();

        for (MonthlyFinanceSnapshot snapshot : snapshots) {
            if (snapshot.getType() != AccountType.CARD) {
                continue;
            }

            List<CashbackRs> cashbacks = parseCashbacksToRs(
                    snapshot.getCashbacksJson(),
                    snapshot.getAccountId(),
                    snapshot.getBankName()
            );
            if (!cashbacks.isEmpty()) {
                cashbacksByBank.put(snapshot.getBankName(), cashbacks);
            }
        }

        List<BankCashbackSummaryDto> summary = new ArrayList<>();

        for (Map.Entry<String, List<CashbackRs>> entry : cashbacksByBank.entrySet()) {
            String bankName = entry.getKey();
            List<CashbackRs> cashbacks = entry.getValue();

            Map<String, BigDecimal> cashbackByCategory = cashbacks.stream()
                    .collect(Collectors.toMap(
                            CashbackRs::category,
                            CashbackRs::percentage,
                            (existing, replacement) -> existing
                    ));

            Optional<CashbackRs> bestCashback = cashbacks.stream()
                    .max(Comparator.comparing(CashbackRs::percentage));

            summary.add(new BankCashbackSummaryDto(
                    bankName,
                    cashbacks.size(),
                    bestCashback.map(CashbackRs::percentage).orElse(BigDecimal.ZERO),
                    bestCashback.map(CashbackRs::category).orElse("Нет"),
                    cashbackByCategory,
                    cashbacks
            ));
        }

        return summary;
    }

    public boolean shouldCreateSnapshot() {
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);

        if (today.equals(firstOfMonth)) {
            LocalDate lastDayOfPreviousMonth = firstOfMonth.minusDays(1);
            return !snapshotRepository.existsBySnapshotDate(lastDayOfPreviousMonth);
        }

        return false;
    }

    @Transactional(readOnly = true)
    public List<MonthlyDynamicsDto> getMonthlyDynamics() {
        List<MonthlyDynamicsDto> result = new ArrayList<>();

        List<LocalDate> snapshotDates = snapshotRepository.findAllSnapshotDates();

        for (LocalDate snapshotDate : snapshotDates) {
            YearMonth yearMonth = YearMonth.from(snapshotDate);
            HistoricalDataResponseDto historicalData = getHistoricalData(yearMonth);
            if (historicalData != null && historicalData.accounts() != null) {
                MonthlyDynamicsDto dto = buildMonthlyDynamicsFromAccounts(historicalData.accounts(), yearMonth);
                result.add(dto);
            }
        }

        MonthlyDynamicsDto currentMonthDto = getCurrentMonthDynamics();
        result.add(currentMonthDto);

        result.sort(Comparator.comparing(MonthlyDynamicsDto::month));

        return result;
    }

    private MonthlyDynamicsDto getCurrentMonthDynamics() {
        List<Account> currentAccounts = accountRepository.findAll();

        List<AccountRs> accountDtos = currentAccounts.stream()
                .map(account -> AccountRs.builder()
                        .id(account.getId())
                        .bankName(account.getBankName())
                        .amount(account.getAmount())
                        .type(account.getType())
                        .comment(account.getComment())
                        .build())
                .collect(Collectors.toList());

        YearMonth currentMonth = YearMonth.now();
        return buildMonthlyDynamicsFromAccounts(accountDtos, currentMonth);
    }

    private MonthlyDynamicsDto buildMonthlyDynamicsFromAccounts(List<AccountRs> accounts, YearMonth month) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal cardTotal = BigDecimal.ZERO;
        BigDecimal depositTotal = BigDecimal.ZERO;
        BigDecimal savingsTotal = BigDecimal.ZERO;
        BigDecimal investmentTotal = BigDecimal.ZERO;

        for (AccountRs account : accounts) {
            BigDecimal amount = account.getAmount() != null ? account.getAmount() : BigDecimal.ZERO;
            total = total.add(amount);

            if (account.getType() == AccountType.CARD) {
                cardTotal = cardTotal.add(amount);
            } else if (account.getType() == AccountType.DEPOSIT) {
                depositTotal = depositTotal.add(amount);
            } else if (account.getType() == AccountType.SAVINGS) {
                savingsTotal = savingsTotal.add(amount);
            } else if (account.getType() == AccountType.INVESTMENT) {
                investmentTotal = investmentTotal.add(amount);
            }
        }

        String monthLabel = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")));

        return new MonthlyDynamicsDto(
                month,
                monthLabel,
                total,
                cardTotal,
                depositTotal,
                savingsTotal,
                investmentTotal
        );
    }

    private List<CashbackRs> parseCashbacksToRs(String cashbacksJson, Long accountId, String bankName) {
        if (cashbacksJson == null) {
            return Collections.emptyList();
        }
        try {
            List<CashbackSnapshotDto> dtos = objectMapper.readValue(
                    cashbacksJson,
                    new TypeReference<>() {}
            );
            return dtos.stream()
                    .map(dto -> new CashbackRs(
                            dto.id(),
                            accountId,
                            bankName,
                            dto.category(),
                            dto.percentage()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to parse cashbacks JSON for account {}", accountId, e);
            return Collections.emptyList();
        }
    }

    private int parseCashbacksCount(String cashbacksJson) {
        if (cashbacksJson == null) {
            return 0;
        }
        try {
            List<CashbackSnapshotDto> dtos = objectMapper.readValue(
                    cashbacksJson,
                    new TypeReference<>() {}
            );
            return dtos.size();
        } catch (Exception e) {
            return 0;
        }
    }

    private DepositInfoDto parseDepositToDto(String depositJson) {
        if (depositJson == null) {
            return null;
        }
        try {
            DepositSnapshotDto depositDto = objectMapper.readValue(depositJson, DepositSnapshotDto.class);
            return new DepositInfoDto(
                    depositDto.id(),
                    depositDto.endDate(),
                    depositDto.interestPaymentDate(),
                    depositDto.interestRate()
            );
        } catch (Exception e) {
            log.error("Failed to parse deposit JSON", e);
            return null;
        }
    }

    private List<SecurityRs> parseSecuritiesToRs(String securitiesJson, Long accountId, String bankName) {
        if (securitiesJson == null) {
            return Collections.emptyList();
        }
        try {
            List<SecuritySnapshotDto> dtos = objectMapper.readValue(
                    securitiesJson,
                    new TypeReference<>() {}
            );
            return dtos.stream()
                    .map(dto -> new SecurityRs(
                            dto.id(),
                            accountId,
                            bankName,
                            dto.securityType(),
                            dto.ticker(),
                            dto.quantity(),
                            dto.currentPrice(),
                            dto.quantity() != null && dto.currentPrice() != null
                                    ? dto.quantity().multiply(dto.currentPrice())
                                    : BigDecimal.ZERO,
                            dto.faceValue(),
                            dto.couponRate(),
                            dto.maturityDate()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to parse securities JSON for account {}", accountId, e);
            return Collections.emptyList();
        }
    }
}