# CSS: Cash Settlement System

Financial institutions have backoffice systems that perform various tasks after a trade is booked, namely: confirmation, settlement, accounting, reconciliation etc.

**C**ash **S**ettlement **S**ystem(**CSS**) consists of (micro)services that perform some of the core functions of a *backoffice settlement system*, namely:

    - Cashflow enrichment
    - Cashflow confirmation
    - Cashflow netting or aggregation(realtime)
    - Payment release

## Maven Modules

### Core modules

    | Module                        | Description |
    |-------------------------------|-------------|
    | css-config/                   | Configuration files for all css services |
    | css-infra/ignite-cache        | In-Memory Cache(Apache Ignite) that holds the reference data |
    | css-infra/oracle-db           | Oracle FREE DB. Also contains all the SQL DDL statements |
    | css-infra/h2-server           | H2 DB. To be used instead of Oracle DB to save computing resources |
    | refdata-generator             | A reference data generator to generate reference data relevent to CSS like Counterparty, Nostro, SSI etc |
    | db-cache-data-loader          | Loads reference data in database and cache. Uses 'refdata-generator' module to generate reference data. Also contains all the SQL DDL statements used to create the cache. The cache can be operated via both SQL and Java APIs |
    | fo-simulator                  | Front office simulator that continuously generates cashflows using mutiple concurrent producers. The cashflows are generated according to a limited set of business rules that are required to cover various scenarios that a real settlement system handles |
    | cashflow-consumer             | Consumes cashflow, enriches, persists and generates un-net events for already netted cashflow |
    | confirmation-consumer         | Consumes confirmation events, confirms cashflow and generates net and un-net events |
    | netting-service               | Performs netting or aggregation of confirmed cashflows published by confirmation-consumer |

### Important Shared modules

    | css-lib/css-parent/                                         | parent pom for all CSS components |
    | css-lib/css-scripts/                                        | scripts to build and run css components |
    | css-lib/css-shared/tx-template/                             | Spring Transaction template. Can be used instead of @Transactional/declarative transaction |
    | css-lib/data-generator-shared/                              | Shared data generator libs. Contains mainly the generic base classes and token/ID generators |
    | css-lib/css-shared/domain-shared/cashflow/                  | Shared cashflow domain objects |
    | css-lib/css-shared/domain-shared/reference-data/            | Shared reference data domain objects |
    | css-lib/css-shared/model-shared/reference-data-model/       | The reference data classes that can be serialized/deserialized from/to the ignite cache |

## Building and Running CSS Components

Following are the steps to build:

Containerized components:

1. cd css-infra/ignite-cache
    1) podman build -t alw.io/ignite:latest .

Non-Containerized components:

2. cd css-lib/css-scripts/build
    1) /install.sh css-lib/css-shared/
    2) /install.sh css-lib/data-generator-shared/
    3) /install.sh refdata-generator
    4) /install.sh db-cache-data-loader
    5) /install.sh fo-simulator
    6) /install.sh css-infra/h2-server

Following are the steps to run:

1) podman run --rm -d -p 127.0.0.1:8095:8080 alw.io/ignite
2) cd css-lib/css-scripts/app
    1) /start.sh css-infra/h2-server
    2) /start.sh db-cache-data-loader
    3) /start.sh fo-simulator

NOTE: Following components are work in progress and cannot be run as of now:
* cashflow-consumer
* confirmation-consumer
* netting-service
