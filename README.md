# ABCD Corporation — Enterprise HCM, HRMS & Double-Entry Payroll Platform

An enterprise-grade, multi-tenant Human Capital Management (HCM), Human Resource Management System (HRMS), and Double-Entry Payroll Accounting Platform designed for **ABCD Corporation**. 

The platform supports high-volume, compliant, and mathematically strict workforce management, automated Gross-to-Net payroll execution, Indian statutory tax computation (New & Old regimes per Section 115BAC), balanced General Ledger posting, and real-time structured log streaming to Splunk.

---

## 🏛️ Platform Architecture Overview

```
                      +------------------------------------------+
                      |         ABCD Enterprise Web SPA          |
                      |          (Angular 15+ / Port 4200)       |
                      +--------------------+---------------------+
                                           |
                              HTTP / REST with Bearer JWT
                              & X-Correlation-Id Header
                                           |
        +----------------------------------+----------------------------------+
        |                                                                     |
        v                                                                     v
+-----------------------+   +-----------------------+     +-----------------------+
|   identity-service    |   |   workforce-service   |     |  time-leave-service   |
|      (Port 8081)      |   |      (Port 8082)      |     |      (Port 8083)      |
+-----------+-----------+   +-----------+-----------+     +-----------+-----------+
            |                           |                             |
            +---------------------------+-----------------------------+
                                        |
        +-------------------------------+-----------------------------+
        |                               |                             |
        v                               v                             v
+-----------------------+   +-----------------------+     +-----------------------+
| onboarding-bgv-service|   | tax-benefits-service  |     | payment-ledger-service|
|      (Port 8084)      |   |      (Port 8086)      |     |      (Port 8087)      |
+-----------+-----------+   +-----------+-----------+     +-----------+-----------+
            |                           |                             |
            +---------------------------+-----------------------------+
                                        |
        +-------------------------------+-----------------------------+
        |                                                             |
        v                                                             v
+-----------------------+                                 +-----------------------+
|  notification-service |                                 |    payroll-service    |
|      (Port 8088)      |                                 |      (Port 8089)      |
+-----------+-----------+                                 +-----------+-----------+
            |                                                         |
            +---------------------------+-----------------------------+
                                        |
                   Asynchronous Domain Events & Log Streaming
                                        |
                   +--------------------+--------------------+
                   |                                         |
                   v                                         v
       +-----------------------+                 +-----------------------+
       |     Apache Kafka      |                 |   Splunk Enterprise   |
       |  (KRaft / Port 9092)  |                 | (HEC 8088 / UI 8000)  |
       +-----------------------+                 +-----------------------+
```

---

## 📦 Service Inventory & Port Mappings

| Service Name | Port | Description & Responsibilities | Key Endpoints |
| :--- | :--- | :--- | :--- |
| **`identity-service`** | `8081` | Authentication, Argon2id passwords, JWT issuance, RBAC permission resolution, security audit trail | `/api/v1/auth/login`<br>`/api/v1/auth/validate` |
| **`workforce-service`** | `8082` | Legal entities, department tree, cost centers, employee lifecycle (active, probation, resigned), dynamic ID sequencing | `/api/v1/workforce/employees`<br>`/api/v1/workforce/departments` |
| **`time-leave-service`** | `8083` | Biometric clock in/out, shift assignment, timesheets, double-entry immutable leave ledgers, monthly payroll lock | `/api/v1/attendance/punch`<br>`/api/v1/attendance/lock`<br>`/api/v1/leaves/apply` |
| **`onboarding-bgv-service`** | `8084` | Preboarding invitations, magic links, digital consent audit, document vault, 5-pillar BGV verification, Day-1 conversion | `/api/v1/onboarding/cases`<br>`/api/v1/onboarding/portal/consent`<br>`/api/v1/bgv/initiate` |
| **`tax-benefits-service`** | `8086` | Old vs New tax regimes (Sec 115BAC), Chapter VI-A investment proofs (80C, 80D, HRA), reducing-balance loan EMI calculator | `/api/v1/tax/declarations`<br>`/api/v1/tax/loans` |
| **`payment-ledger-service`** | `8087` | Double-Entry General Ledger journal entries, balance invariant check ($\sum D = \sum C$), disbursement batches, return saga | `/api/v1/ledger/journals`<br>`/api/v1/ledger/payments/disburse` |
| **`notification-reporting-service`** | `8088` | Notification dispatch (email/SMS/in-app), Kafka event consumers, executive KPI & workforce analytics dashboard | `/api/v1/reporting/dashboard/executive`<br>`/api/v1/notifications` |
| **`payroll-service`** | `8089` | Gross-to-Net calculation engine, salary structures, statutory deductions (PF, PT, ESI, TDS), payslips, outbox events | `/api/v1/payroll/runs`<br>`/api/v1/payroll/runs/{id}/calculate`<br>`/api/v1/payroll/payslips` |
| **`abcd-hrms-ui`** | `4200` | Angular 15+ Single Page Application with persona switcher, live Splunk telemetry badge, and financial workbenches | `http://localhost:4200` |

---

## 💎 Financial Rigor & Architectural Guarantees

### 1. Zero Floating-Point Rounding Errors
- **`BigDecimal` Exclusively**: Floating-point numbers (`float`, `double`) are banned from all compensation, tax, loan, and ledger calculations.
- **Banker's Rounding**: All currency computations employ `RoundingMode.HALF_EVEN` at an internal precision of 4 decimal places, formatted to 2 decimals for statutory reports and payslips.
- **Centralized Money Utility**: Standardized calculations are enforced via `MoneyUtil` in `platform/common-core`.

### 2. Double-Entry General Ledger Invariant
- Every payroll transaction generates balanced Journal Vouchers (JV) with strict double-entry verification:
  $$\sum \text{Debit Amount} - \sum \text{Credit Amount} = 0.0000$$
- Any unbalanced transaction triggers an immediate transaction rollback with an `UnbalancedJournalEntryException`, preventing corrupted accounting records.

### 3. Transactional Outbox Pattern
- Microservices emit domain events (`hrms.payroll.events`, `hrms.bgv.events`, `hrms.payment.events`) using the Transactional Outbox pattern. Events are committed atomically within the same database transaction as the business entity, guaranteeing At-Least-Once Delivery to Apache Kafka.

### 4. Distributed Splunk Observability
- Every microservice streams structured JSON log events directly to **Splunk HTTP Event Collector (HEC)** using the custom `SplunkHecAppender`.
- Every incoming HTTP request is assigned a unique `X-Correlation-Id` via `CorrelationIdFilter`, propagated downstream through all REST and Kafka communications.

---

## 🛠️ Infrastructure Configuration

The platform relies on the pre-configured enterprise Docker stack at:
`d:\ai-projects\work\kafka-docker-image\docker-compose.yml`

| Component | Port | Credentials / Details |
| :--- | :--- | :--- |
| **PostgreSQL 16** | `5432` | Username: `postgres` \| Password: `postgres` \| DB: `postgres` |
| **Apache Kafka** | `9092` | KRaft Mode (Controller on 9093) \| Internal Listener: `kafka:29092` |
| **Kafka UI** | `8085` | Web management interface: `http://localhost:8085` |
| **Splunk Enterprise** | `8000` (UI) / `8088` (HEC) | Web: `admin` / `SplunkAdmin123!` \| HEC Token: `946654e2-6e83-4ae2-943a-a32ffef9305b` |

---

## 🚀 Step-by-Step Startup Runbook

### Step 1: Start Infrastructure Containers
```powershell
cd d:\ai-projects\work\kafka-docker-image
docker-compose up -d
```
Verify containers are running:
```powershell
docker ps
```
- PostgreSQL: `localhost:5432`
- Kafka: `localhost:9092`
- Splunk Web: `http://localhost:8000`
- Splunk HEC: `http://localhost:8088`
- Kafka UI: `http://localhost:8085`

### Step 2: Build All Backend Platform & Microservice Modules
Build and run unit tests for all 14 modules using JDK 11 and Maven:
```powershell
cd d:\ai-projects\work\HRMS
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& set PATH=C:\Program Files\Java\jdk-11\bin;%PATH%&& D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd clean install -DskipTests"
```

### Step 3: Run the Microservices
Start services in separate terminal sessions or background processes:
```powershell
# 1. Identity Service (Port 8081)
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& cd d:\ai-projects\work\HRMS\identity-service && D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd spring-boot:run"

# 2. Workforce Service (Port 8082)
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& cd d:\ai-projects\work\HRMS\workforce-service && D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd spring-boot:run"

# 3. Time & Leave Service (Port 8083)
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& cd d:\ai-projects\work\HRMS\time-leave-service && D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd spring-boot:run"

# 4. Onboarding & BGV Service (Port 8084)
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& cd d:\ai-projects\work\HRMS\onboarding-bgv-service && D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd spring-boot:run"

# 5. Tax & Benefits Service (Port 8086)
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& cd d:\ai-projects\work\HRMS\tax-benefits-service && D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd spring-boot:run"

# 6. Payment & Ledger Service (Port 8087)
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& cd d:\ai-projects\work\HRMS\payment-ledger-service && D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd spring-boot:run"

# 7. Notification & Reporting Service (Port 8088)
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& cd d:\ai-projects\work\HRMS\notification-reporting-service && D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd spring-boot:run"

# 8. Payroll Service (Port 8089)
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& cd d:\ai-projects\work\HRMS\payroll-service && D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd spring-boot:run"
```

### Step 4: Run the Angular 15+ Frontend SPA
```powershell
cd d:\ai-projects\work\HRMS\abcd-hrms-ui
npm install
npm start
```
Open your browser at `http://localhost:4200`.

---

## 🧑‍💼 Pre-Configured Demo Personas

The UI features a quick-switch **Persona Bar** allowing immediate evaluation of role-based capabilities without manual credential entry:

1. **Executive / Super Admin (`admin@abcd.com` - Role: `ROLE_SYSTEM_ADMIN`)**:
   - Access: System settings, full platform observability, Kafka telemetry, and audit logs.
2. **HR Operations Manager (`hr.admin@abcd.com` - Role: `ROLE_HR_ADMIN`)**:
   - Access: Workforce management, preboarding candidate invitations, BGV clearance console.
3. **Payroll Specialist (`payroll.maker@abcd.com` - Role: `ROLE_PAYROLL_MAKER`)**:
   - Access: Gross-to-Net execution, monthly attendance cutoff lock, proration, and payslip locking.
4. **Finance Director (`finance.director@abcd.com` - Role: `ROLE_FINANCE_DIRECTOR`)**:
   - Access: Double-entry General Ledger audit, payment disbursement sanction, reversal saga management.
5. **Software Engineer (`john.doe@abcd.com` - Role: `ROLE_EMPLOYEE`)**:
   - Access: ESS portal, biometric punch simulator, leave balance ledger, tax regime simulator (Sec 115BAC), payslip download.

---

## 🔍 Splunk Telemetry & Verification

To verify that structured events are reaching Splunk HEC:
```powershell
curl.exe -X POST http://localhost:8088/services/collector/event `
  -H "Authorization: Splunk 946654e2-6e83-4ae2-943a-a32ffef9305b" `
  -d "{\"event\": {\"service\": \"workforce-service\", \"level\": \"INFO\", \"message\": \"Testing Splunk HEC integration from local Docker\"}}"
```
In the Splunk Enterprise UI (`http://localhost:8000`, `admin:SplunkAdmin123!`), run the search query:
```spl
index="main" | table _time host correlationId serviceName level message
```
You will observe real-time audit logs from all microservices, tagged with `correlationId`, `userId`, `tenantId`, and `serviceName`.

---

## 🧪 Testing & Validation Suite

Run the full automated test suite covering all 14 Maven modules:
```powershell
cmd.exe /c "set JAVA_HOME=C:\Program Files\Java\jdk-11&& set PATH=C:\Program Files\Java\jdk-11\bin;%PATH%&& D:\Softwares\apache-maven-3.8.7\bin\mvn.cmd test"
```
**Test Coverage Highlights:**
- **Gross-to-Net Calculation Engine**: Validates exact earnings, deductions, PF, PT, ESI, TDS calculations, and LOP proration with 0.0001 precision.
- **Double-Entry General Ledger Balance Validation**: Validates invariant $\sum D == \sum C$ and ensures rejection of unbalanced JVs.
- **Tax Amortization & Reducing Balance**: Tests exact monthly EMI calculation and interest balance amortisation.
- **Immutable Leave Ledger**: Verifies balance debits, accruals, and overlap prevention.
- **Background Verification Pipeline**: Tests candidate invitation, document upload, and clearance transition state machine.

---

## 📜 Compliance & Security Notes
- **Data Protection**: Sensitive personal information (Aadhaar, PAN, Bank Accounts) is encrypted using AES-256-GCM (`common-security`). First 8 digits of Aadhaar are automatically masked in all UI representations and API logs per UIDAI guidelines.
- **Tamper-Evident Auditing**: Financial records and journal vouchers are append-only; update and delete operations on finalized payroll and ledger entries are prohibited by domain guards.

