package my.help.finance.general.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.general.dto.*;
import my.help.finance.general.entity.*;
import my.help.finance.general.mapper.AccountMapper;
import my.help.finance.general.repository.*;
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
public class FinanceSnapshotService {

    private final MonthlyFinanceSnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;
    private final CashbackRepository cashbackRepository;
    private final DepositRepository depositRepository;
    private final SecurityRepository securityRepository;
    private final AccountMapper accountMapper;
    private final ObjectMapper objectMapper;

    // ОБНОВИТЬ КОНСТРУКТОР
    public FinanceSnapshotService(
            MonthlyFinanceSnapshotRepository snapshotRepository,
            AccountRepository accountRepository,
            CashbackRepository cashbackRepository,
            DepositRepository depositRepository,
            SecurityRepository securityRepository,
            AccountMapper accountMapper) {
        this.snapshotRepository = snapshotRepository;
        this.accountRepository = accountRepository;
        this.cashbackRepository = cashbackRepository;
        this.depositRepository = depositRepository;
        this.securityRepository = securityRepository;
        this.accountMapper = accountMapper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Создать снимок за прошедший месяц (вызывать 1-го числа)
     */
    public void createSnapshotForPreviousMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstOfCurrentMonth = today.withDayOfMonth(1);
        LocalDate snapshotDate = firstOfCurrentMonth.minusDays(1);

        YearMonth snapshotYearMonth = YearMonth.from(snapshotDate);

        // Проверяем, не создавали ли уже снимок за этот месяц
        if (snapshotRepository.existsBySnapshotDate(snapshotDate)) {
            log.info("Snapshot for {} already exists, skipping", snapshotYearMonth);
            return;
        }

        log.info("Creating finance snapshot for {}", snapshotYearMonth);

        List<Account> allAccounts = accountRepository.findAll();
        int snapshotsCreated = 0;

        for (Account account : allAccounts) {
            // Получаем кешбеки счёта
            List<Cashback> cashbacks = cashbackRepository.findByAccount(account);

            // Конвертируем кешбеки в JSON
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
                        .map(s -> SecuritySnapshotDto.builder()
                                .id(s.getId())
                                .securityType(s.getSecurityType())
                                .ticker(s.getTicker())
                                .quantity(s.getQuantity())
                                .currentPrice(s.getCurrentPrice())
                                .faceValue(s.getFaceValue())
                                .couponRate(s.getCouponRate())
                                .maturityDate(s.getMaturityDate())
                                .build())
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

        // После создания всех снимков, обновляем денормализованные суммы по банкам
        updateDenormalizedBankTotals(snapshotDate);

        log.info("Snapshot for {} completed. Created {} snapshots", snapshotYearMonth, snapshotsCreated);
    }

    /**
     * Обновить денормализованные поля (суммы по банкам)
     */
    private void updateDenormalizedBankTotals(LocalDate snapshotDate) {
        List<MonthlyFinanceSnapshot> snapshots = snapshotRepository.findBySnapshotDate(snapshotDate);

        // Группируем по банку и считаем сумму
        Map<String, BigDecimal> totalsByBank = snapshots.stream()
                .collect(Collectors.groupingBy(
                        MonthlyFinanceSnapshot::getBankName,
                        Collectors.reducing(BigDecimal.ZERO, MonthlyFinanceSnapshot::getAmount, BigDecimal::add)
                ));

        // Обновляем каждый снимок
        for (MonthlyFinanceSnapshot snapshot : snapshots) {
            snapshot.setTotalAmountByBank(totalsByBank.get(snapshot.getBankName()));
        }

        snapshotRepository.saveAll(snapshots);
    }

    /**
     * Получить все доступные даты снимков (для UI селектора)
     */
    public List<SnapshotInfoDto> getAvailableSnapshots() {
        List<LocalDate> snapshotDates = snapshotRepository.findAllSnapshotDates();

        return snapshotDates.stream()
                .map(date -> {
                    YearMonth yearMonth = YearMonth.from(date);
                    List<MonthlyFinanceSnapshot> snapshots = snapshotRepository.findBySnapshotDate(date);
                    int totalCashbacks = snapshots.stream()
                            .mapToInt(s -> {
                                try {
                                    List<CashbackSnapshotDto> cashbacks = objectMapper.readValue(
                                            s.getCashbacksJson(),
                                            new TypeReference<>() {
                                            }
                                    );
                                    return cashbacks.size();
                                } catch (Exception e) {
                                    return 0;
                                }
                            })
                            .sum();

                    return SnapshotInfoDto.builder()
                            .yearMonth(yearMonth)
                            .snapshotDate(date)
                            .accountsCount(snapshots.size())
                            .cashbacksCount(totalCashbacks)
                            .formattedDate(yearMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("ru"))))
                            .build();
                })
                .sorted((a, b) -> b.getSnapshotDate().compareTo(a.getSnapshotDate()))
                .collect(Collectors.toList());
    }

    /**
     * Получить данные за конкретный месяц (из снимка)
     */
    public HistoricalDataResponseDto getHistoricalData(YearMonth yearMonth) {
        LocalDate snapshotDate = yearMonth.atEndOfMonth();

        List<MonthlyFinanceSnapshot> snapshots = snapshotRepository.findBySnapshotDate(snapshotDate);

        if (snapshots.isEmpty()) {
            log.warn("No snapshot found for {}", yearMonth);
            return null;
        }

        // Восстанавливаем счета из снимков
        List<AccountResponseDto> accounts = new ArrayList<>();

        for (MonthlyFinanceSnapshot snapshot : snapshots) {
            AccountResponseDto accountDto = AccountResponseDto.builder()
                    .id(snapshot.getAccountId())
                    .bankName(snapshot.getBankName())
                    .amount(snapshot.getAmount())
                    .type(snapshot.getType())
                    .comment(snapshot.getComment())
                    .build();

            // Восстанавливаем кешбеки
            try {
                List<CashbackSnapshotDto> cashbackDtos = objectMapper.readValue(
                        snapshot.getCashbacksJson(),
                        new TypeReference<>() {
                        }
                );

                List<CashbackResponseDto> cashbacks = cashbackDtos.stream()
                        .map(dto -> new CashbackResponseDto(
                                dto.id(),
                                snapshot.getAccountId(),
                                snapshot.getBankName(),
                                dto.category(),
                                dto.percentage()
                        ))
                        .collect(Collectors.toList());

                accountDto.setCashbacks(cashbacks);
            } catch (Exception e) {
                log.error("Failed to parse cashbacks for snapshot {}", snapshot.getId(), e);
                accountDto.setCashbacks(Collections.emptyList());
            }

            // Восстанавливаем информацию о депозите/накопительном счёте, если есть
            if ((snapshot.getType() == AccountType.DEPOSIT || snapshot.getType() == AccountType.SAVINGS)
                    && snapshot.getDepositJson() != null) {
                try {
                    DepositSnapshotDto depositDto = objectMapper.readValue(
                            snapshot.getDepositJson(),
                            DepositSnapshotDto.class
                    );
                    accountDto.setDepositInfo(new DepositInfoDto(
                            depositDto.id(),
                            depositDto.endDate(),
                            depositDto.interestPaymentDate(),
                            depositDto.interestRate()
                    ));
                } catch (Exception e) {
                    log.error("Failed to parse deposit for snapshot {}", snapshot.getId(), e);
                }
            }

            if (snapshot.getType() == AccountType.INVESTMENT && snapshot.getSecuritiesJson() != null) {
                try {
                    List<SecuritySnapshotDto> securityDtos = objectMapper.readValue(
                            snapshot.getSecuritiesJson(),
                            new TypeReference<>() {
                            }
                    );

                    List<SecurityResponseDto> securities = securityDtos.stream()
                            .map(dto -> SecurityResponseDto.builder()
                                    .id(dto.getId())
                                    .accountId(snapshot.getAccountId())
                                    .bankName(snapshot.getBankName())
                                    .securityType(dto.getSecurityType())
                                    .ticker(dto.getTicker())
                                    .quantity(dto.getQuantity())
                                    .currentPrice(dto.getCurrentPrice())
                                    .totalValue(dto.getQuantity() != null && dto.getCurrentPrice() != null
                                            ? dto.getQuantity().multiply(dto.getCurrentPrice())
                                            : BigDecimal.ZERO)
                                    .faceValue(dto.getFaceValue())
                                    .couponRate(dto.getCouponRate())
                                    .maturityDate(dto.getMaturityDate())
                                    .build())
                            .collect(Collectors.toList());

                    accountDto.setSecurities(securities);
                } catch (Exception e) {
                    log.error("Failed to parse securities for snapshot {}", snapshot.getId(), e);
                    accountDto.setSecurities(Collections.emptyList());
                }
            }

            accounts.add(accountDto);
        }

        // Формируем сводку
        FinanceSummaryDto summary = buildSummaryFromSnapshots(snapshots);

        // Формируем сводку по кешбеку
        List<BankCashbackSummaryDto> cashbackSummary = buildCashbackSummaryFromSnapshots(snapshots);

        return new HistoricalDataResponseDto(
                snapshotDate,
                accounts,
                summary,
                cashbackSummary
        );
    }

    /**
     * Построить сводку из снимков
     */
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

        // Добавляем нули для отсутствующих типов
        for (AccountType type : AccountType.values()) {
            amountByType.putIfAbsent(type, BigDecimal.ZERO);
        }

        // Сводка по кешбеку (будет отдельно)
        return FinanceSummaryDto.builder()
                .totalAmount(totalAmount)
                .amountByBank(amountByBank)
                .amountByType(amountByType)
                .cashbackSummaryByBank(new HashMap<>())
                .bestCashbackByCategory(new HashMap<>())
                .build();
    }

    /**
     * Построить сводку кешбека из снимков
     */
    private List<BankCashbackSummaryDto> buildCashbackSummaryFromSnapshots(List<MonthlyFinanceSnapshot> snapshots) {
        Map<String, List<CashbackResponseDto>> cashbacksByBank = new HashMap<>();

        for (MonthlyFinanceSnapshot snapshot : snapshots) {
            if (snapshot.getType() != AccountType.CARD) {
                continue;
            }

            try {
                List<CashbackSnapshotDto> cashbackDtos = objectMapper.readValue(
                        snapshot.getCashbacksJson(),
                        new TypeReference<>() {
                        }
                );

                List<CashbackResponseDto> cashbacks = cashbackDtos.stream()
                        .map(dto -> new CashbackResponseDto(
                                dto.id(),
                                snapshot.getAccountId(),
                                snapshot.getBankName(),
                                dto.category(),
                                dto.percentage()
                        ))
                        .collect(Collectors.toList());

                if (!cashbacks.isEmpty()) {
                    cashbacksByBank.put(snapshot.getBankName(), cashbacks);
                }
            } catch (Exception e) {
                log.error("Failed to parse cashbacks for bank {}", snapshot.getBankName(), e);
            }
        }

        List<BankCashbackSummaryDto> summary = new ArrayList<>();

        for (Map.Entry<String, List<CashbackResponseDto>> entry : cashbacksByBank.entrySet()) {
            String bankName = entry.getKey();
            List<CashbackResponseDto> cashbacks = entry.getValue();

            Map<String, BigDecimal> cashbackByCategory = cashbacks.stream()
                    .collect(Collectors.toMap(
                            CashbackResponseDto::category,
                            CashbackResponseDto::percentage,
                            (existing, replacement) -> existing
                    ));

            Optional<CashbackResponseDto> bestCashback = cashbacks.stream()
                    .max(Comparator.comparing(CashbackResponseDto::percentage));

            summary.add(new BankCashbackSummaryDto(
                    bankName,
                    cashbacks.size(),
                    bestCashback.map(CashbackResponseDto::percentage).orElse(BigDecimal.ZERO),
                    bestCashback.map(CashbackResponseDto::category).orElse("Нет"),
                    cashbackByCategory,
                    cashbacks
            ));
        }

        return summary;
    }

    /**
     * Проверить, нужно ли создать снимок (для планировщика)
     */
    public boolean shouldCreateSnapshot() {
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);

        // Если сегодня 1-е число и ещё не создан снимок за прошлый месяц
        if (today.equals(firstOfMonth)) {
            LocalDate lastDayOfPreviousMonth = firstOfMonth.minusDays(1);
            return !snapshotRepository.existsBySnapshotDate(lastDayOfPreviousMonth);
        }

        return false;
    }

    /**
     * Получить динамику по месяцам:
     * - данные из снимков (за прошлые месяцы)
     * - текущий месяц из живых данных (accounts)
     */
    @Transactional(readOnly = true)
    public List<MonthlyDynamicsDto> getMonthlyDynamics() {
        List<MonthlyDynamicsDto> result = new ArrayList<>();

        // 1. Получаем все даты снимков из истории
        List<LocalDate> snapshotDates = snapshotRepository.findAllSnapshotDates();

        // 2. Добавляем данные из каждого снимка
        for (LocalDate snapshotDate : snapshotDates) {
            YearMonth yearMonth = YearMonth.from(snapshotDate);
            HistoricalDataResponseDto historicalData = getHistoricalData(yearMonth);
            if (historicalData != null && historicalData.accounts() != null) {
                MonthlyDynamicsDto dto = buildMonthlyDynamicsFromAccounts(historicalData.accounts(), yearMonth);
                result.add(dto);
            }
        }

        // 3. Добавляем текущий месяц (живые данные из БД)
        MonthlyDynamicsDto currentMonthDto = getCurrentMonthDynamics();
        result.add(currentMonthDto);

        // 4. Сортируем по месяцу (от старых к новым)
        result.sort(Comparator.comparing(MonthlyDynamicsDto::month));

        return result;
    }

    /**
     * Получить данные за текущий месяц из живых счетов
     */
    private MonthlyDynamicsDto getCurrentMonthDynamics() {
        List<Account> currentAccounts = accountRepository.findAll();

        List<AccountResponseDto> accountDtos = currentAccounts.stream()
                .map(account -> AccountResponseDto.builder()
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

    /**
     * Из списка счетов собираем статистику по типам
     */
    private MonthlyDynamicsDto buildMonthlyDynamicsFromAccounts(List<AccountResponseDto> accounts, YearMonth month) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal cardTotal = BigDecimal.ZERO;
        BigDecimal depositTotal = BigDecimal.ZERO;
        BigDecimal savingsTotal = BigDecimal.ZERO;
        BigDecimal investmentTotal = BigDecimal.ZERO;

        for (AccountResponseDto account : accounts) {
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

        String monthLabel = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("ru")));

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
}