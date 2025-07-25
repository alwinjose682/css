# CSS: Cash Settlement System

Financial institutions have backoffice systems that perform various tasks after a trade is booked, such as: confirmation, settlement, accounting, reconciliation etc.

**C**ash **S**ettlement **S**ystem(**CSS**) consists of (micro)services that perform some of the core functions of a *backoffice settlement system*, namely:

    - Cashflow enrichment
    - Cashflow confirmation
    - Cashflow netting or aggregation into Payment(realtime on cashflow confirmation)
    - Payment release

**NOTE:**

- The CSS project is divided into multiple modules each having a separate git repository.
- This repository groups together all the CSS repositories and does not contain any code.
- Detailed documentation for each component along with its code is within the component's specific repository. Example, for documentation and code of cashflow-consumer, check [cashflow-consumer]
  repository
- All CSS components like DB, Cache, Kafka etc can be run together in a single machine.

**What is completed?**

- [**Cashflow Consumer**][cashflow-consumer]      : A component that processes cashflows from upstream system. Cashflow processing involves mainly- receiving, validating, enriching and persisting
  cashflows to the database
- [**Upstream Simulator**][fo-simulator]           : Simulates an fo-system for CSS. Creates cashflows for various trade types according to a limited domain specific rules
- [**Reference Data Generator**][refdata-generator]      : Generates reference data relevant to CSS like Counterparty, Nostro, SSI etc
- [**Database and Cache Data Loader**][db-cache-data-loader]   : Loads the generated reference data in database and in memory cache
- [**Infrastructure used by CSS**][css-infra]              : Configurations, SQL files etc for **Database**, **Ignite Cache** and **Kafka**

[cashflow-consumer]: https://github.com/alwinjose682/css-cashflow-consumer/

[fo-simulator]: https://github.com/alwinjose682/css-fo-simulator/

[refdata-generator]: https://github.com/alwinjose682/css-refdata-generator/

[db-cache-data-loader]: https://github.com/alwinjose682/css-db-cache-data-loader/

[css-infra]: https://github.com/alwinjose682/css-infra/

**What is pending?**

- confirmation-consumer
- netting-service
- payment-release

**How to Build and Run CSS?**

[Check the build and run steps](#Building-and-Running-CSS-Components)

[Also, check "starting and stopping Cashflow Generators"](#REST-endpoints-to-start-or-stop-Cashflow-Generators)

## Maven Modules

### Core modules

| Module                 | Description                                                                                                                                                                                                                                                  |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| css-config/            | Configuration files for all css services                                                                                                                                                                                                                     |
| css-infra/ignite-cache | In-Memory Cache(Apache Ignite) that holds the reference data                                                                                                                                                                                                 |
| css-infra/oracle-db    | Oracle FREE DB. Also contains all the SQL DDL statements                                                                                                                                                                                                     |
| css-infra/h2-server    | H2 DB. To be used instead of Oracle DB to save computing resources                                                                                                                                                                                           |
| refdata-generator      | A reference data generator to generate reference data relevant to CSS like Counterparty, Nostro, SSI etc. The reference data is generated according to a limited set of backo office specific business rules.                                                |
| db-cache-data-loader   | Loads reference data in database and cache. Uses 'refdata-generator' module to generate reference data. Also contains all the SQL DDL statements used to create the cache. The cache can be operated via both SQL and Java APIs                              |
| fo-simulator           | Front office simulator that continuously generates cashflows using mutiple concurrent producers. The cashflows are generated according to a limited set of business rules that are required to cover various scenarios that a real settlement system handles |
| cashflow-consumer      | Consumes cashflow, enriches, persists and generates un-net events for already netted cashflow                                                                                                                                                                |
| confirmation-consumer  | Consumes confirmation events, confirms cashflow and generates net and un-net events                                                                                                                                                                          |
| netting-service        | Performs netting or aggregation of confirmed cashflows published by confirmation-consumer                                                                                                                                                                    |

### Important Shared modules

| Module                                                | Description                                                                                  |
|-------------------------------------------------------|----------------------------------------------------------------------------------------------|
| css-lib/css-parent/                                   | parent pom for all CSS components                                                            |
| css-lib/css-scripts/                                  | scripts to build and run css components                                                      |
| css-lib/css-shared/tx-template/                       | Spring Transaction template. Can be used instead of @Transactional/declarative transaction   |
| css-lib/data-generator-shared/                        | Shared data generator libs. Contains mainly the generic base classes and token/ID generators |
| css-lib/css-shared/domain-shared/cashflow/            | Shared cashflow domain objects                                                               |
| css-lib/css-shared/domain-shared/reference-data/      | Shared reference data domain objects                                                         |
| css-lib/css-shared/model-shared/reference-data-model/ | The reference data classes that can be serialized/deserialized from/to the ignite cache      |

## Building and Running CSS Components

**Requirements**

- Java: version 24
- Build tool: maven
- Preferred OS: Linux or Mac OS
- Container provider: podman or docker
- 6 GB RAM (8GB if Oracle DB is used)
- Use the build and start scripts as explained below and place all the modules in this CSS repo in the same directory

**NOTE:**

- All CSS components like DB, Cache, Kafka etc can be run together in a single machine. The build is run as per the configs in the local profile
- The build and start scripts are linux bash scripts. To build and run in windows, it is required to write equivalent windows scripts
- By default the database used is Oracle DB. Few config changes and a code change are required to use H2 DB
- As of now, the H2 DB files are configured to be written to the home directory of the user: /home/<user>

### Steps to build

**Containerized components:**

Container registries used:
cat ~/.config/containers/registries.conf -> unqualified-search-registries = ['docker.io','ghrc.io','quay.io','container-registry.oracle.com']

1. cd css-infra/ignite-cache
    1) podman build -t alw.io/ignite:latest .

**Non-Containerized components:**

2. cd css-lib/css-scripts/build
    1. ./buildAll.sh

   **OR**

    1. ./install.sh css-lib/css-shared
    2. ./install.sh css-lib/data-generator-shared
    3. ./install.sh refdata-generator
    4. ./install.sh css-infra/ignite-cache
    5. ./install.sh db-cache-data-loader
    6. ./install.sh fo-simulator
    7. ./install.sh cashflow-consumer
    8. ./install.sh css-infra/h2-server (If H2 database is used)

### Steps to setup Oracle Database

1) podman run -d -p 127.0.0.1:1521:1521 -e ORACLE_PWD=sysdbapass container-registry.oracle.com/database/express:21.3.0-xe
2) Note the container id from above command. The same container needs to be used when running the application
2) Copy this entire directory: 'css-infra/oracle-db/install__1_0' to the container(anywhere, even in /tmp). <br/> ex: podman cp css-infra/oracle-db/install__1_0 <container-id>:/tmp
3) Run the script: css-infra/oracle-db/install__1_0/install.sh
4) The script outputs the information of users, tables, indexes, sequences etc that are created. You may check it to verify that the DB objects are created

### Steps to run

**NOTE:**

- The CSS components use commonly used port numbers like 8080...8095, 10800 etc
- Logs will be written in a directory named 'logs' in the css project's root directory. *Example: cashflow-consumer logs will be written in '<css-root-dir>/logs/<profile>/cashflow-consumer'*
- Use the start scripts to run the CSS Components inorder to have the appropriate jvm args and commandline parameters

| Component       | Command                                                                                                                                                                                                                                            |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Oracle Database | (If Oracle database is used) <br/> podman start <container-id> (container-id is the oracle DB container-id that was setup in the previous step)                                                                                                    | 
| Ignite Cache    | podman run -d --rm -p 127.0.0.1:8095:8080,127.0.0.1:10800:10800 alw.io/ignite                                                                                                                                                                      | 
| Kafka           | podman run -d --rm -p 127.0.0.1:9092:9092 docker.io/apache/kafka:4.0.0                                                                                                                                                                             |
| Schema Registry | podman run -d --rm --net=host -e SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS=PLAINTEXT://localhost:9092 -e SCHEMA_REGISTRY_HOST_NAME=localhost -e SCHEMA_REGISTRY_LISTENERS=http://localhost:8995 docker.io/confluentinc/cp-schema-registry:7.9.1 |
|                 | ***---   Wait for 1 or 2 minutes for above components to start and become active   ---***                                                                                                                                                          |
| CSS Components  | cd css-lib/css-scripts/app<br/> 1) ./start.sh css-infra/h2-server (If H2 DB is used) <br/> 2) ./start.sh db-cache-data-loader (NOTE: Data load may take more than 5 mins) <br/> 3) ./start.sh fo-simulator <br/> 4) ./start.sh cashflow-consumer   |

#### REST endpoints to start or stop Cashflow Generators

      Fo-Simulator has Cashflow-Generators that creates and publishes FoCashMessages continously to a Kafka topic.
      Starting and stopping Cashflow-Generators are facilitated via below REST endpoints:
         Start : http://localhost:8081/cashflow/generators/start/all
         Stop  : http://localhost:8081/cashflow/generators/stop/all
      But, they are NOT started when fo-simulator starts. Instead, cashflow-consumer once started, invokes the REST endpoint to start the generators
      At any time, Cashflow-Generators can be stopped and later resumed via the REST endpoints
