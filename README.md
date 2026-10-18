# Useful — четыре независимых сервиса

| Сервис | Адрес | PostgreSQL для запуска из IDE | Содержимое |
|---|---|---|---|
| common | http://localhost:8080 | localhost:5433/common | Главная страница, вакансии, даты, мотивация; собственный Redis |
| finance | http://localhost:8081 | localhost:5434/finance | Финансы, инвестиции, Avito, AI-чат и история |
| food | http://localhost:8082 | localhost:5435/food | Продукты, питание, рационы |
| kanban | http://localhost:8083 | localhost:5436/kanban | Проекты, задачи, планы, события, идеи, Telegram-бот |

Каждый сервис содержит собственные контроллеры, HTML-страницы, сущности и репозитории,
собирается в исполняемый JAR и работает без остальных приложений. Вызовов между
сервисами нет. Кнопки на главной странице common перенаправляют браузер на другие
сервисы; их отключение не мешает работе common. Общего родительского POM нет.
У каждого сервиса свой самостоятельный POM без блока `parent`, версии библиотек,
настройки плагинов, Maven Wrapper и Dockerfile. Для согласования библиотек Spring
импортируется внешний `spring-boot-dependencies` BOM; POM соседних сервисов не используется.

## Docker

```powershell
docker compose up -d --build
```

Запустить только один сервис и его собственную базу:

```powershell
docker compose up -d --build food
```

Аналогично используются `common`, `finance`, `kanban`. Только common требует Redis.
Базы используют отдельные контейнеры, пользователей и volumes. Данные старого
контейнера `postgres` автоматически не переносятся и не удаляются.

Мониторинг: `docker compose --profile monitoring up -d prometheus grafana`.
Prometheus доступен на порту 9090, Grafana — 3000 (первый вход admin/admin).
В Grafana адрес источника Prometheus: `http://prometheus:9090`.

## Запуск из IDE / Java

Нужен JDK 21 или новее; JAVA_HOME должен указывать на JDK.
Для одного сервиса сначала поднимите его инфраструктуру:

```powershell
docker compose up -d food-db
.\mvnw.cmd -f food/pom.xml spring-boot:run
```

Для common: `docker compose up -d common-db common-redis`.
Для остальных аналогично: `finance-db` или `kanban-db`.
В IDE запускайте `UsefulApplication`, `FinanceApplication`, `FoodApplication` или
`KanbanApplication`. Порты по умолчанию различаются, поэтому приложения можно запускать вместе.

Сборка одного сервиса без сборки соседей:

```powershell
.\mvnw.cmd -f food/pom.xml clean package
java -jar food/target/food-0.0.1-SNAPSHOT.jar
```

Каждую папку сервиса можно скопировать в отдельный репозиторий и собирать самостоятельно:

```powershell
cd food
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
# Или собрать образ из этой же папки:
docker build -t useful-food .
```

Сборка и тесты всех сервисов из корня (PowerShell):

```powershell
foreach ($service in 'common', 'finance', 'food', 'kanban') {
    .\mvnw.cmd -f "$service/pom.xml" clean verify
    if ($LASTEXITCODE -ne 0) { throw "Build failed: $service" }
}
```

На Linux/macOS используйте `sh ./mvnw`. В IDE импортируйте четыре отдельных `pom.xml`.
Корневой Maven Wrapper оставлен только для удобства вызовов с `-f`; в каждом сервисе
есть собственный Wrapper. Для запуска достаточно JAR и инфраструктуры выбранного сервиса.

Swagger каждого сервиса: `/swagger-ui/index.html`. Метрики: `/actuator/prometheus`.
Проверка состояния: `/actuator/health` на том же порту, что и приложение.

## Настройки

Каждый процесс поддерживает `SERVER_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`.
Локальные значения пользователя/пароля совпадают с именем сервиса.
В Compose пароль задаётся отдельно: `COMMON_DB_PASSWORD`, `FINANCE_DB_PASSWORD`,
`FOOD_DB_PASSWORD`, `KANBAN_DB_PASSWORD` (при первом создании соответствующего volume).

В common: `REDIS_HOST`, `REDIS_PORT`; адреса для браузерных переходов:
`FINANCE_URL`, `FOOD_URL`, `KANBAN_URL`. При доступе с другого компьютера задайте
внешние адреса вместо localhost. API страниц обращаются к собственному origin.
Резервное копирование `/util/base/backup` использует подключение только к базе common;
для него требуется установленный `pg_dump` (путь задаётся свойством `backup.pg-dump`).
Стандартный образ приложения не содержит PostgreSQL CLI.

В finance: `OPENAI_BASE_URL`, `OPENAI_API_KEY`, `OPENAI_MODEL`.
Локальная модель: `docker compose -f docker-compose1.yml up -d`.
Модель нужна для запросов к AI, а не для запуска finance. Чат находится по адресу
`http://localhost:8081/page/ai/chat`; его инструменты и данные теперь в одном сервисе.
Автоматизация Avito через мышь/клавиатуру требует запуска finance в графической сессии
с `HEADLESS=false`; в серверном контейнере эти функции недоступны.
Отдельный `AvitoVisitorBot` использует `FINANCE_URL` (по умолчанию localhost:8081).

Telegram-бот находится в kanban и по умолчанию выключен. Для включения задайте
`TELEGRAM_ENABLED=true`, `TELEGRAM_USERNAME`, `TELEGRAM_TOKEN`.

## Данные и переход со старой базы

По умолчанию сохранён существующий режим `DDL_AUTO=update`, `LIQUIBASE_ENABLED=false`.
Hibernate создаёт таблицы только своего сервиса. Исторические Liquibase changelog
разделены по владельцам; они не описывают всю текущую модель. Их нельзя использовать
как полную схему с `DDL_AUTO=validate` без дополнительных миграций.
CSV в `src/main/resources/database` — исторические выгрузки, автоматически не импортируются.

Перед переключением существующей установки остановите запись в старое приложение,
сделайте резервную копию PostgreSQL и перенесите таблицы в отдельные базы с помощью
`pg_dump`/`pg_restore`, сохранив идентификаторы и значения последовательностей:

- common: `vacancies_per_day`, историческая `weather_forecasts`.
- finance: `accounts`, `cashbacks`, `deposits`, `securities`, `monthly_finance_snapshots`,
  `currency_rates`, `key_rates`, `bond_daily_data`, `deposit_rates`, `apartments`,
  `chat_session`, `spring_ai_chat_memory`.
- food: `products`, `products_per_day`, `nutrients_per_day`, `diets`, `diet_items`.
- kanban: `projects`, `columns`, `tasks`, `project_metric`, `strategic_plan`, `plan_tasks`,
  `events`, `ideas`, историческая `completed_task`.

Не подключайте четыре приложения к одной старой базе. Сначала проверьте перенос
на копии и сравните количество записей. Существующая база этой переработкой не изменяется.

Тесты запускают каждый сервис с отдельной H2-базой, проверяют собственные сущности,
отсутствие чужих таблиц и HTTP-страницу без запущенных соседних сервисов.
