package my.help.finance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import my.help.finance.dto.*;
import my.help.finance.entity.Account;
import my.help.finance.entity.AccountType;
import my.help.finance.entity.Cashback;
import my.help.finance.entity.MonthlyFinanceSnapshot;
import my.help.finance.mapper.AccountMapper;
import my.help.finance.repository.AccountRepository;
import my.help.finance.repository.CashbackRepository;
import my.help.finance.repository.MonthlyFinanceSnapshotRepository;
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
    private final AccountMapper accountMapper;
    private final ObjectMapper objectMapper;

    public FinanceSnapshotService(MonthlyFinanceSnapshotRepository snapshotRepository, AccountRepository accountRepository, CashbackRepository cashbackRepository, AccountMapper accountMapper) {
        this.snapshotRepository = snapshotRepository;
        this.accountRepository = accountRepository;
        this.cashbackRepository = cashbackRepository;
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
        LocalDate lastDayOfPreviousMonth = firstOfCurrentMonth.minusDays(1);
        LocalDate snapshotDate = lastDayOfPreviousMonth;

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
                    .map(cb -> CashbackSnapshotDto.builder()
                            .id(cb.getId())
                            .category(cb.getCategory())
                            .percentage(cb.getPercentage())
                            .build())
                    .collect(Collectors.toList());

            String cashbacksJson;
            try {
                cashbacksJson = objectMapper.writeValueAsString(cashbackDtos);
            } catch (Exception e) {
                log.error("Failed to serialize cashbacks for account {}", account.getId(), e);
                cashbacksJson = "[]";
            }

            MonthlyFinanceSnapshot snapshot = MonthlyFinanceSnapshot.builder()
                    .snapshotDate(snapshotDate)
                    .accountId(account.getId())
                    .bankName(account.getBankName())
                    .amount(account.getAmount())
                    .type(account.getType())
                    .comment(account.getComment())
                    .cashbacksJson(cashbacksJson)
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
                                            new TypeReference<List<CashbackSnapshotDto>>() {}
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
                        new TypeReference<List<CashbackSnapshotDto>>() {}
                );

                List<CashbackResponseDto> cashbacks = cashbackDtos.stream()
                        .map(dto -> CashbackResponseDto.builder()
                                .id(dto.getId())
                                .accountId(snapshot.getAccountId())
                                .bankName(snapshot.getBankName())
                                .category(dto.getCategory())
                                .percentage(dto.getPercentage())
                                .build())
                        .collect(Collectors.toList());

                accountDto.setCashbacks(cashbacks);
            } catch (Exception e) {
                log.error("Failed to parse cashbacks for snapshot {}", snapshot.getId(), e);
                accountDto.setCashbacks(Collections.emptyList());
            }

            accounts.add(accountDto);
        }

        // Формируем сводку
        FinanceSummaryDto summary = buildSummaryFromSnapshots(snapshots);

        // Формируем сводку по кешбеку
        List<BankCashbackSummaryDto> cashbackSummary = buildCashbackSummaryFromSnapshots(snapshots);

        return HistoricalDataResponseDto.builder()
                .snapshotDate(snapshotDate)
                .accounts(accounts)
                .summary(summary)
                .cashbackSummary(cashbackSummary)
                .build();
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
                        new TypeReference<List<CashbackSnapshotDto>>() {}
                );

                List<CashbackResponseDto> cashbacks = cashbackDtos.stream()
                        .map(dto -> CashbackResponseDto.builder()
                                .id(dto.getId())
                                .accountId(snapshot.getAccountId())
                                .bankName(snapshot.getBankName())
                                .category(dto.getCategory())
                                .percentage(dto.getPercentage())
                                .build())
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
                            CashbackResponseDto::getCategory,
                            CashbackResponseDto::getPercentage,
                            (existing, replacement) -> existing
                    ));

            Optional<CashbackResponseDto> bestCashback = cashbacks.stream()
                    .max(Comparator.comparing(CashbackResponseDto::getPercentage));

            summary.add(BankCashbackSummaryDto.builder()
                    .bankName(bankName)
                    .totalCashbackCategories(cashbacks.size())
                    .bestCashbackPercentage(bestCashback.map(CashbackResponseDto::getPercentage).orElse(BigDecimal.ZERO))
                    .bestCashbackCategory(bestCashback.map(CashbackResponseDto::getCategory).orElse("Нет"))
                    .cashbackByCategory(cashbackByCategory)
                    .activeCashbacks(cashbacks)
                    .build());
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
     * Создать начальный снимок (при первом запуске)
     */
    public void createInitialSnapshot() {
        LocalDate lastDayOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1).minusDays(1);

        if (!snapshotRepository.existsBySnapshotDate(lastDayOfLastMonth)) {
            log.info("Creating initial snapshot for {}", lastDayOfLastMonth);
            createSnapshotForPreviousMonth();
        }
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
            if (historicalData != null && historicalData.getAccounts() != null) {
                MonthlyDynamicsDto dto = buildMonthlyDynamicsFromAccounts(historicalData.getAccounts(), yearMonth);
                result.add(dto);
            }
        }

        // 3. Добавляем текущий месяц (живые данные из БД)
        MonthlyDynamicsDto currentMonthDto = getCurrentMonthDynamics();
        result.add(currentMonthDto);

        // 4. Сортируем по месяцу (от старых к новым)
        result.sort(Comparator.comparing(MonthlyDynamicsDto::getMonth));

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
        BigDecimal investmentTotal = BigDecimal.ZERO;

        for (AccountResponseDto account : accounts) {
            BigDecimal amount = account.getAmount() != null ? account.getAmount() : BigDecimal.ZERO;
            total = total.add(amount);

            if (account.getType() == AccountType.CARD) {
                cardTotal = cardTotal.add(amount);
            } else if (account.getType() == AccountType.DEPOSIT) {
                depositTotal = depositTotal.add(amount);
            } else if (account.getType() == AccountType.INVESTMENT) {
                investmentTotal = investmentTotal.add(amount);
            }
        }

        String monthLabel = month.format(DateTimeFormatter.ofPattern("LLLL yyyy", new Locale("ru")));

        return MonthlyDynamicsDto.builder()
                .month(month)
                .monthLabel(monthLabel)
                .totalAmount(total)
                .cardAmount(cardTotal)
                .depositAmount(depositTotal)
                .investmentAmount(investmentTotal)
                .build();
    }
}