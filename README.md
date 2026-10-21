 <div align="center">

<img src="https://capsule-render.vercel.app/api?type=rect&height=230&text=SENTINEL&fontSize=76&fontColor=F8FAFC&fontAlignY=40&desc=REAL-TIME%20FRAUD%20DETECTION%20PLATFORM&descSize=17&descAlignY=65&color=0:080D1C,50:172554,100:4338CA" width="100%" alt="SENTINEL — Real-Time Fraud Detection Platform" />

### SEE THE SIGNAL. EXPLAIN THE ALERT.

**Real-time transaction monitoring powered by Java, Apache Kafka, and Hadoop MapReduce.**

An engineering project exploring event streaming, explainable detection rules, and distributed analytics.

<br/>

[![Java 11](https://img.shields.io/badge/JAVA-11-F59E0B?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Kafka](https://img.shields.io/badge/APACHE-KAFKA-27272A?style=for-the-badge&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Hadoop](https://img.shields.io/badge/APACHE-HADOOP-0284C7?style=for-the-badge&logo=apachehadoop&logoColor=white)](https://hadoop.apache.org/)
[![Docker](https://img.shields.io/badge/DOCKER-COMPOSE-2563EB?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/BUILD-MAVEN-C2410C?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

<br/>

**01 — STREAM** &nbsp; → &nbsp; **02 — DETECT** &nbsp; → &nbsp; **03 — EXPLAIN** &nbsp; → &nbsp; **04 — ANALYZE**

<br/>

[Architecture](#-architecture) · [Features](#-features) · [Quick Start](#-quick-start) · [Detection Rules](#-detection-rules)

**Created by Akanksha Deshmukh**

</div>

---

> [!IMPORTANT]
> SENTINEL is an educational portfolio project using synthetic transaction data and illustrative rules. It is not a production fraud prevention service. An alert indicates a rule match, not confirmed fraud.

## ◈ Overview

SENTINEL demonstrates how a transaction monitoring system can ingest events, evaluate detection rules, publish structured alerts, and analyze historical transaction activity.

| Real-time pipeline | Batch analytics |
|:--|:--|
| Java + Apache Kafka | Hadoop MapReduce |
| Transaction event processing | Account-level aggregation |
| Rule-based alert generation | Historical transaction summaries |

## ✦ Features

<table>
<tr>
<td width="50%" valign="top">

### ⚡ Event Streaming

Generate synthetic transactions in Java and transport them through Kafka.

</td>
<td width="50%" valign="top">

### 🛡️ Explainable Detection

Evaluate transactions using configurable rules and readable reason codes.

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🔔 Structured Alerts

Publish matched transactions to a dedicated Kafka alert topic.

</td>
<td width="50%" valign="top">

### 📊 Batch Analytics

Aggregate transaction counts, total amounts, and maximum amounts by account.

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🧪 Testing

Use JUnit to verify selected detection behaviors.

</td>
<td width="50%" valign="top">

### 🧰 Local Environment

Use Docker Compose and Kafka UI to inspect the local streaming setup.

</td>
</tr>
</table>

## ◈ Architecture

```mermaid
flowchart TB
    subgraph RT["01  /  REAL-TIME PIPELINE"]
        direction LR
        P["Java Producer"] --> T[("Kafka<br/>transactions")]
        T --> C["Java Consumer<br/>Detection Engine"]
        C --> D{"Rule matched?"}
        D -->|No| N["No alert"]
        D -->|Yes| A[("Kafka<br/>fraud-alerts")]
    end

    subgraph BA["02  /  BATCH ANALYTICS"]
        direction LR
        F["Transaction JSONL"] --> M["Hadoop MapReduce"]
        M --> R["Account Aggregates"]
    end

    subgraph OBS["03  /  LOCAL OBSERVABILITY"]
        K["Kafka Broker"] --> U["Kafka UI"]
    end

    classDef source fill:#172554,stroke:#818CF8,color:#FFFFFF
    classDef process fill:#312E81,stroke:#A5B4FC,color:#FFFFFF
    classDef alert fill:#881337,stroke:#FB7185,color:#FFFFFF
    classDef analytics fill:#064E3B,stroke:#34D399,color:#FFFFFF
    classDef neutral fill:#1E293B,stroke:#64748B,color:#FFFFFF

    class P,T source
    class C,D process
    class A alert
    class F,M,R analytics
    class N,K,U neutral
```

### Transaction decision flow

```mermaid
flowchart TD
    S(["Transaction received"]) --> V{"Valid input?"}
    V -->|No| X["Handle invalid event"]
    V -->|Yes| R["Evaluate detection rules"]
    R --> D{"Any rule matched?"}
    D -->|No| N["Continue without alert"]
    D -->|Yes| A["Build alert with reason codes"]
    A --> P["Publish alert to Kafka"]
    P --> C["Commit offset after acknowledgement"]
    X --> E(["Next event"])
    N --> E
    C --> E

    classDef start fill:#172554,stroke:#818CF8,color:#FFFFFF
    classDef decision fill:#78350F,stroke:#FBBF24,color:#FFFFFF
    classDef alert fill:#881337,stroke:#FB7185,color:#FFFFFF
    classDef success fill:#064E3B,stroke:#34D399,color:#FFFFFF

    class S start
    class V,D decision
    class A,P alert
    class N,C,E success
```

*The flowcharts describe the intended workflow. Confirm that your implementation handles invalid events, Kafka acknowledgements, and offset commits as illustrated.*

## ✦ Detection Rules

The documented default threshold is `1000.00`, configurable through `FRAUD_AMOUNT_THRESHOLD`.

| Rule | Reason code |
|:--|:--|
| Amount meets or exceeds the threshold | `HIGH_AMOUNT` |
| Amount is missing or null | `MISSING_AMOUNT` |
| Amount cannot be parsed | `INVALID_AMOUNT` |
| Amount is negative | `NEGATIVE_AMOUNT` |
| Transaction ID is missing | `MISSING_TRANSACTION_ID` |
| Account ID is missing | `MISSING_ACCOUNT_ID` |

A high-value transaction may be legitimate. These rules demonstrate detection logic rather than establish real-world fraud.

## ⚡ Quick Start

### Prerequisites

- JDK 11
- Maven 3.8+
- Docker Desktop or Docker Engine with Compose
- Hadoop 3.3.x for batch analytics

### 01 — Start the environment

```bash
docker compose up -d
docker compose ps
```

Open Kafka UI if configured:

[http://localhost:8080](http://localhost:8080)

### 02 — Build the project

```bash
mvn clean package
```

### 03 — Start the consumer

```bash
java -jar consumer/target/consumer-1.0.0.jar
```

### 04 — Produce transactions

Open another terminal:

```bash
java -jar producer/target/producer-1.0.0.jar
```

### 05 — Inspect alerts

In Kafka UI, open the `fraud-alerts` topic and inspect published messages and their reason codes.

### 06 — Run tests

```bash
mvn test
```

### 07 — Stop services

```bash
docker compose down
```

> **Setup note:** These commands assume the JAR names, topic configuration, and environment variables match your actual source code. Verify them before publishing the README.

## ◈ Hadoop MapReduce

The batch analytics job processes newline-delimited JSON transaction records and summarizes activity by account.

```bash
hadoop jar mapreduce/target/mapreduce-1.0.0.jar \
  com.akanksha.fraud.mapreduce.TransactionAggregationJob \
  sample-data/transactions.jsonl \
  output/account-aggregates
```

Expected aggregate fields:

- Transaction count
- Total transaction amount
- Maximum transaction amount

Run this command with Hadoop configured for your environment. The output directory must not already exist.

## ◈ Technology Stack

| Technology | Responsibility |
|:--|:--|
| Java 11 | Event generation and detection logic |
| Apache Kafka | Event transport and alert streaming |
| Hadoop MapReduce | Historical aggregation |
| Maven | Build and dependencies |
| Docker Compose | Local infrastructure |
| JUnit | Automated tests |

## ◈ Project Structure

```text
real-time-fraud-detection-system/
├── producer/
├── consumer/
│   └── src/test/
├── mapreduce/
├── sample-data/
├── docker-compose.yml
├── pom.xml
└── README.md
```

The directory overview is abbreviated. Expand it if readers need to navigate specific Java source files.

## ✦ Reliability & Engineering

- The producer's delivery settings and consumer offset management should be documented according to the actual code.
- At-least-once processing can produce duplicate alerts if a crash occurs between alert publication and offset commitment.
- The single-broker development environment is not highly available.
- Malformed-event handling, retries, and dead-letter processing should be verified before claiming production-grade reliability.

## ◈ Roadmap

- [ ] Spring Boot REST API for alert search
- [ ] PostgreSQL persistence
- [ ] Responsive monitoring dashboard
- [ ] Retry and dead-letter handling
- [ ] Alert deduplication
- [ ] Schema validation
- [ ] Account-based velocity rules
- [ ] Throughput and consumer-lag monitoring
- [ ] CI pipeline and dependency scanning
- [ ] Evaluation using labeled data and false-positive metrics

## 🔐 Responsible Use

- Use synthetic transaction data.
- Never commit credentials or sensitive financial information.
- Treat alerts as investigation signals, not proof of fraud.
- Do not use illustrative rules for real financial decisions.

## License

No license has been selected yet. Add an appropriate `LICENSE` file if you intend to grant others permission to reuse the project.

---

<div align="center">

<img src="https://capsule-render.vercel.app/api?type=rect&height=120&section=footer&text=ENGINEERED%20FOR%20CLARITY&fontSize=25&fontColor=F8FAFC&fontAlignY=52&color=0:080D1C,100:4338CA" width="100%" alt="Engineered for clarity" />

**SENTINEL**

*Streaming systems · Distributed processing · Explainable detection*

**Akanksha Deshmukh**

[![GitHub](https://img.shields.io/badge/GitHub-Explore_the_Code-181717?style=for-the-badge&logo=github)](https://github.com/akankshad1712)

</div>
