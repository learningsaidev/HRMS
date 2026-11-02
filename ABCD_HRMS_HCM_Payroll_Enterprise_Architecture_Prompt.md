# ABCD Enterprise HRMS, HCM & Payroll --- Master Architecture Prompt

## Role

Act as a principal enterprise architect, Java/Spring Boot lead,
payroll-domain specialist, database architect, security architect, and
DevOps engineer. Design an enterprise-grade HRMS, HCM and payroll
platform for **ABCD**. Use Workday, ADP and Oracle HCM only as
functional reference points for enterprise patterns; do not claim
knowledge of their proprietary internals or copy proprietary designs.

**First produce the architecture pack. Do not dump code immediately.**
After the architecture is reviewed and approved, implement one phase at
a time with complete, runnable files.

## 1. Mandatory technology

-   Java 11 only.
-   Spring Boot 2.7.x, selecting and pinning a Java-11-compatible patch
    version. Explain its maintenance/EOL implications and security
    considerations.
-   Spring MVC, Spring Data JPA, Hibernate, Bean Validation.
-   PostgreSQL, Flyway migrations.
-   Apache Kafka for asynchronous domain events.
-   Redis for selective caching.
-   Docker and Docker Compose.
-   Maven multi-module project.
-   Spring Security with OAuth2/OIDC and JWT validation.
-   OpenAPI/Swagger.
-   JUnit 5, Mockito, Testcontainers.
-   Micrometer, Prometheus-compatible metrics, structured logging and
    distributed tracing.

Do not use Java 17+ syntax/APIs. Pin compatible Spring Cloud and library
versions. PostgreSQL is the source of truth; Redis is never
authoritative. Kafka does not replace database transactions. Use Docker
Compose locally; describe production deployment separately. Kubernetes
is optional future scope.

## 2. Business scope

Build for company ABCD, with an extensible data model for multiple legal
entities, business units, departments, locations and potentially
multiple client companies. Design tenant/company isolation from the
beginning.

### 2.1 Workforce and headcount

-   Total headcount and
    active/inactive/terminated/on-leave/probation/pending-joining
    counts.
-   Headcount by legal entity, department, location, designation,
    manager and employment type.
-   Employee directory with search, filters, pagination and
    permission-controlled exports.
-   Organization chart and reporting hierarchy.
-   Define exact headcount rules for future joiners, notice period,
    leave and termination.

### 2.2 Employee master and lifecycle

-   Create, view, update, search, deactivate and rehire employees.
-   Configurable employee ID generation.
-   Personal/contact/emergency/address details; job, department,
    location, grade, manager, employment type, joining date, probation,
    confirmation, notice and exit details.
-   Bank and tax details with encryption, masking and field-level
    authorization.
-   Employment statuses: Draft, Preboarding, Active, Probation,
    Confirmed, On Leave, Notice Period, Suspended, Terminated, Retired.
-   Effective-dated employment, salary, department, manager, designation
    and location changes.
-   Employee history, audit trail, bulk import with validation report,
    employee self-service update requests.

### 2.3 Attendance and working hours

-   Shifts, rosters, calendars, holidays, weekly offs, grace periods,
    overtime, late/early marks, missed punches and regularization.
-   Clock-in/out integration or manual entry.
-   Timesheet submission and manager approval.
-   Attendance import adapter for biometric/time systems.
-   Separate raw punches, calculated attendance, approved timesheets and
    payroll-ready summaries.
-   Monthly attendance lock before payroll.

### 2.4 Leave management

-   Configurable Casual, Sick, Earned/Privilege, Unpaid, Comp-Off,
    Maternity/Paternity and company-specific leave types.
-   Policies by legal entity, location, employee category and service
    period.
-   Opening balance, accrual, carry-forward, lapse, encashment,
    adjustment and closing balance.
-   Apply, approve, reject, withdraw, cancel, delegate and multi-level
    approval.
-   Half-day, date overlap, holiday/weekend and negative-balance
    validation.
-   Employee/manager/HR leave balance views and team calendar.
-   Maintain an immutable leave transaction ledger; do not rely only on
    an editable balance field.
-   Integrate unpaid leave and encashment with payroll.

### 2.5 Onboarding and background verification

-   HR creates pre-employee/candidate record; secure candidate
    onboarding link.
-   Candidate enters required details and uploads documents; validation,
    HR review, correction requests, checklist and completion.
-   Consent and privacy notice records; duplicate detection; employee ID
    generation and activation on joining date.
-   Configurable BGV checks: identity, address, education, previous
    employment, references and lawful checks where authorized.
-   Verification states: Not Started, Pending Consent, Initiated, In
    Progress, Clear, Review Required, Discrepancy, Failed, Cancelled.
-   Vendor adapters plus manual reviewer workflow, evidence references,
    reviewer and decision audit.
-   Obtain consent before checks; restrict BGV results to authorized
    roles; never auto-reject solely from an unreviewed vendor response.
-   Store documents in private object storage; define retention/deletion
    policy.

### 2.6 Compensation and salary configuration

-   Effective-dated CTC, gross, basic, HRA, allowances, flexible
    benefits, reimbursements, bonus, incentives, commission, overtime
    and other components.
-   Pay grades/bands, salary ranges, employee salary structure,
    fixed/variable components and revision history.
-   Component configuration: formula, taxable flag, statutory
    applicability, accounting mapping, display order and effective
    dates.
-   Proration for joining, exit, unpaid leave and mid-period revisions.
-   Maker-checker approval for salary changes and bulk revisions.
-   Retroactive changes and arrears.
-   Never hard-code salary components, tax rates or statutory
    percentages in Java code.

### 2.7 Payroll engine and lifecycle

Implement a controlled lifecycle: 1. Payroll calendar and period. 2.
Payroll group selection. 3. Collect approved attendance, leave,
compensation, benefits, reimbursements, loans and deductions. 4.
Validate inputs and create exception report. 5. Preview/simulate. 6.
Calculate gross-to-net. 7. Calculate tax and statutory contributions. 8.
Maker-checker review and approval. 9. Lock payroll. 10. Generate payment
batch. 11. Import payment status and reconcile. 12. Publish payslips.
13. Export accounting journals. 14. Close period.

Statuses: Draft, Inputs Pending, Validating, Validation Failed,
Calculating, Calculated, Review Required, Pending Approval, Approved,
Locked, Payment Initiated, Partially Paid, Paid, Reconciliation
Required, Closed, Cancelled.

Support monthly/weekly/biweekly schedules, payroll groups, proration,
overtime, unpaid leave, arrears, retro pay, off-cycle payroll,
bonus/incentives, final settlement, simulations, reruns with versioning,
period freeze/lock, variance reports, exception handling and calculation
explanation per employee/component.

Calculation pipeline: - Resolve effective-dated compensation and payroll
configuration. - Resolve approved attendance/leave. - Calculate payable
days and proration. - Calculate earnings and employer contributions. -
Calculate pre-tax deductions and taxable income. - Calculate tax
withholding using versioned jurisdiction rules. - Calculate post-tax
deductions and net pay. - Validate negative/abnormal values. - Persist
immutable calculation lines and payslip data.

Use `BigDecimal`, never `double`/`float`, for money. Store currency
code, precision and rounding policy. Payroll runs must store the exact
versions of policies and rules used so historical payslips remain
reproducible.

### 2.8 Payslips

-   Monthly payslip with earnings, deductions, employer contributions,
    tax, gross/net, payable days, payment status and YTD totals.
-   PDF generation, employee self-service access, access audit and
    controlled reissue/correction with version history.
-   Annual tax and compensation statements.

### 2.9 Tax, statutory deductions, benefits and loans

-   Configurable jurisdiction, tax year and effective-dated rules.
-   For India-first support, design configurable TDS/income tax, tax
    regime/declarations/proofs, PF, ESI where applicable, Professional
    Tax by state, LWF where applicable, gratuity estimates/settlement
    and bonus where applicable.
-   Do not invent or hard-code statutory rates, slabs, thresholds or
    eligibility. Mark rules as requiring validation against current
    official sources and qualified payroll/tax professionals before
    production.
-   Tax projection and actual withholding; employee/employer
    contribution breakdown.
-   Recurring/one-time deductions, loans, advances, installment
    schedules, reimbursements, benefit deductions, employer
    contributions, deduction priority/limits and termination recovery.
-   Avoid duplicate tax calculations: define one authoritative
    calculation contract and ownership.

### 2.10 Salary credit, debit, reversal and reconciliation

-   Payment batches from approved, locked payroll only.
-   Bank file export/provider adapter; payment status: Created,
    Submitted, Processing, Successful, Failed, Returned, Reversed,
    Reconciled.
-   UTR/bank reference, partial payment, retries, salary credits,
    authorized recoveries/debits and reversals.
-   Idempotency keys and duplicate payment prevention.
-   Maker-checker for batch release, adjustments and reversal.
-   Reconcile payroll liability, payment batch, bank statement and
    general ledger.
-   Use immutable financial transactions and compensating entries; never
    delete/overwrite completed payments.
-   Define double-entry ledger/journal, debit/credit accounts, balancing
    validation and accounting export.

### 2.11 Reports

Headcount, joiners/leavers, attendance, overtime, leave
balances/utilization, pending approvals, onboarding/BGV, payroll
register, gross-to-net, department cost, tax/statutory reports,
deductions/loans, payment failures/reconciliation, final settlement and
audit reports. Support secure asynchronous CSV/XLSX/PDF exports.

## 3. Microservices --- target architecture

Use **8 independently deployable business services**. Each owns its
database/schema and data. No service may read/write another service's
tables directly. Explain why microservices add operational complexity
and keep boundaries domain-driven.

1.  **identity-service** --- authentication integration, users,
    employee-user mapping, RBAC/ABAC, roles, permissions,
    MFA/identity-provider integration, service identities.
2.  **workforce-service** --- company/legal entities, business units,
    departments, locations, employee master, lifecycle, reporting
    hierarchy, employment history, headcount and employee import.
3.  **time-leave-service** --- shifts, calendars, attendance,
    timesheets, overtime, leave
    policies/applications/approvals/accruals/ledger and payroll-ready
    summaries.
4.  **onboarding-bgv-service** --- candidate onboarding, checklists,
    document metadata, consent, BGV cases, vendor adapters and review
    workflow.
5.  **payroll-service** --- payroll calendars/groups, salary components,
    effective-dated compensation, rule versions, payroll
    inputs/validation, calculation, runs, approval, lock,
    retro/off-cycle and payslip data.
6.  **tax-benefits-service** --- tax declarations/proofs, tax rule
    configuration, statutory deductions, PF/ESI/PT/LWF configuration,
    loans, advances, reimbursements, benefits and deduction schedules.
7.  **payment-ledger-service** --- payment batches, bank/provider
    adapters, salary credits/failures/returns/debits/reversals, ledger
    and reconciliation.
8.  **notification-reporting-service** --- email/SMS/in-app
    notifications, templates, reminders, delivery status, read-optimized
    reporting projections and report generation.

Also define optional API Gateway, object storage, identity provider
(e.g. standards-based OIDC provider), secrets manager, observability
stack and scheduler. Audit events must be emitted by every service and
retained in a protected audit store; notification/reporting is not the
sole audit system.

## 4. Service communication and Kafka

-   REST for synchronous user commands/queries; Kafka for domain events
    and asynchronous workflows.
-   OpenAPI REST contracts and versioned event schemas.
-   Event envelope: eventId, eventType, schemaVersion, aggregateId,
    companyId/legalEntityId, occurredAt UTC, producer, correlationId,
    causationId, trace context, minimal payload.
-   Example events: `EmployeeCreated.v1`, `EmployeeActivated.v1`,
    `EmployeeTerminated.v1`, `TimesheetSubmitted.v1`,
    `TimesheetApproved.v1`, `AttendancePeriodLocked.v1`,
    `LeaveApplied.v1`, `LeaveApproved.v1`, `LeaveBalanceChanged.v1`,
    `OnboardingStarted.v1`, `OnboardingCompleted.v1`,
    `BgvStatusChanged.v1`, `CompensationChanged.v1`,
    `PayrollRunRequested.v1`, `PayrollRunCalculated.v1`,
    `PayrollApproved.v1`, `PayrollLocked.v1`, `PayslipPublished.v1`,
    `PaymentBatchCreated.v1`, `SalaryPaymentSucceeded.v1`,
    `SalaryPaymentFailed.v1`, `SalaryPaymentReversed.v1`,
    `PayrollPeriodClosed.v1`.
-   Transactional Outbox for DB change + event publication;
    Inbox/processed-event table and idempotent consumers.
-   Retry with exponential backoff/jitter, DLQ, safe replay procedure,
    event ordering/partition keys, schema compatibility and monitoring.
-   Never publish bank/identity/tax data unnecessarily; send identifiers
    and minimum required data.
-   Do not claim Kafka exactly-once solves cross-service database
    consistency.
-   Use Saga/process manager for onboarding, payroll approval/payment
    and termination. Define compensation actions and manual-intervention
    states.

## 5. Database and domain design

Produce per-service ER diagrams and Flyway DDL for relevant tables,
including: `company`, `legal_entity`, `business_unit`, `department`,
`location`, `employee`, `employment`, `employment_history`,
`manager_relationship`, `job_position`, `pay_group`, `pay_calendar`,
`leave_type`, `leave_policy`, `leave_ledger`, `attendance_punch`,
`timesheet`, `shift`, `onboarding_case`, `employee_document`,
`consent_record`, `bgv_case`, `compensation_plan`, `salary_component`,
`employee_compensation`, `payroll_period`, `payroll_run`,
`payroll_run_employee`, `payroll_calculation_line`, `tax_declaration`,
`tax_rule_version`, `deduction`, `loan`, `loan_installment`, `payslip`,
`payment_batch`, `payment_transaction`, `ledger_account`,
`journal_entry`, `journal_line`, `approval_workflow`, `approval_task`,
`audit_event`, `outbox_event`, `inbox_event`.

Rules: - UUID or documented globally unique IDs. - Consistent
`company_id` and `legal_entity_id` isolation. - Database constraints,
foreign keys, indexes and unique keys. - `@Version` optimistic locking
for concurrent updates. - Effective start/end dates for policies,
salary, tax and employment changes. - UTC timestamps; explicit
company/location time zones; `LocalDate` for payroll/leave business
dates. - Monetary columns as `NUMERIC`, never floating-point. -
Created/updated metadata and audit references. - No production
`ddl-auto=update`; use Flyway. - No cross-service joins. Shared
PostgreSQL cluster is acceptable initially, but separate service-owned
databases/schemas and credentials are required. - Define retention,
archival and purge for sensitive documents and employee data.

## 6. APIs and workflows

Provide a complete API catalogue with method, path, DTOs, validation,
authorization, pagination, error format and idempotency requirements.
Example endpoints: - Workforce: `POST/GET /api/v1/employees`,
`GET/PATCH /api/v1/employees/{id}`, `GET /api/v1/workforce/headcount`. -
Leave/time: `POST /api/v1/leaves`, `POST /api/v1/leaves/{id}/approve`,
`POST /api/v1/leaves/{id}/reject`,
`GET /api/v1/employees/{id}/leave-balances`, timesheet submit/approve
and attendance lock endpoints. - Onboarding/BGV: onboarding create,
document upload, complete, BGV initiate and review endpoints. - Payroll:
create period/run, validate, calculate, submit-for-approval, approve,
lock, employee calculation details and payslip endpoints. - Payment:
create/submit batch, reverse payment, reconciliation endpoints.

Define state machines and sequence diagrams for employee onboarding,
BGV, leave, timesheet approval, attendance-to-payroll, payroll
calculation/approval/lock, payment and reversal.

Approval requirements: - Configurable approval chains,
manager/HR/payroll/finance levels, delegation, escalation, reminders,
due dates, rejection reason and resubmission. - Immutable approval
history. - Prevent self-approval where configured. - Separation of
duties: payroll maker must not be sole approver/releaser; payment
release and reversal require separate authorization.

## 7. Security and privacy

Treat employee, compensation, tax, bank and BGV data as highly
sensitive. - Spring Security with OAuth2/OIDC and JWT validation;
standards-based identity provider, no homemade cryptography. - RBAC plus
scoped ABAC for company/legal entity/department/location/manager. - MFA
for HR, payroll, finance and admins. - Least privilege, deny by default,
service-to-service authentication. - TLS in transit; encryption at rest;
KMS/secrets manager for keys. - Application-level
encryption/tokenization for sensitive fields as appropriate. - Mask
bank/tax/identity values; field-level authorization. - Private document
storage, file type/size validation, malware scanning, expiring download
links. - CORS allow-list, rate limits, secure headers, validation,
output encoding, parameterized queries. - CSRF strategy documented for
browser/session vs stateless bearer-token clients. - Audit sensitive
reads and writes. - Never log credentials, tokens, bank numbers, tax
IDs, identity numbers, payslip contents or BGV evidence. - Secrets out
of Git; dependency/container scanning and SAST/DAST. - Threat model:
account takeover, privilege escalation, payroll tampering, duplicate
payment, insider threat, data leakage, Kafka spoofing, malicious uploads
and cross-company data access.

## 8. Caching and performance

Use Redis selectively for reference/configuration data, organization
hierarchy, dashboard aggregates and carefully scoped employee summaries.
Use version-aware cache keys for policies and salary configuration. -
Cache-aside, TTL, invalidation, stampede protection and event-driven
invalidation. - Authorization scope must be part of sensitive cache
keys. - Cache is never source of truth. - Do not make cached payroll
calculations or leave balances authoritative. - Optimize indexes, query
plans, projections, pagination and batch operations before caching. -
Prevent N+1 queries; define fetch plans. - Remote-call timeouts, circuit
breakers, bulkheads and bounded retries. - Define performance SLOs and
load-test employee search, dashboard and payroll volume.

## 9. Payroll integrity and accounting controls

-   Deterministic, repeatable, versioned payroll calculations.
-   Approval and lock are separate operations.
-   Locked payroll cannot be silently edited.
-   Corrections use new runs/adjustments/reversals with traceability.
-   Idempotency for every payment command.
-   Prevent duplicate employee-period payments with constraints and
    validation.
-   Payment release separated from payroll calculation/approval.
-   Reversal uses compensating ledger entries.
-   Debit and credit journal totals must balance.
-   Manual adjustments require reason, actor, timestamp and approval.
-   Simulation must never create real payment instructions.
-   Period close requires exception and reconciliation checks.
-   Provide illustrative sample gross-to-net calculations, clearly label
    all sample statutory rates as fictional---not legal rates.

## 10. Observability and operations

Provide JSON logs, correlation IDs across REST/Kafka/jobs, metrics for
API latency/errors, Kafka lag, payroll duration, calculation exceptions,
pending approvals, payment failures and reconciliation mismatches;
distributed tracing; health/readiness/liveness; alerts; backup/restore;
Kafka retention; disaster recovery RPO/RTO assumptions; graceful
shutdown; scheduled-job locking; runbooks for stuck payroll, DLQ, failed
payment and reconciliation mismatch.

## 11. Testing

Use JUnit 5, Mockito and Testcontainers. Include unit, repository, API,
security, Kafka, outbox/inbox, contract, end-to-end, concurrency,
property/parameterized, performance and security tests.

Mandatory scenarios: - Mid-month join/exit and salary revision. - Unpaid
leave, missing attendance and retroactive adjustment. - Tax declaration
changes after preview. - Payroll rerun and locked-period edit attempt. -
Concurrent leave requests. - Duplicate Kafka event and duplicate payment
request. - Payment failure/retry and successful payment reversal. -
Manager accessing out-of-scope employee. - Cross-company data access
attempt. - Ledger imbalance and reconciliation mismatch.

## 12. Docker and repository

Provide Docker Compose for PostgreSQL, Kafka, Redis, identity provider
if selected, gateway if selected, and services. Include health checks,
volumes, networks, environment variables, `.env.example` placeholders,
Java 11-compatible Dockerfiles, local commands, migration commands and
reset procedure. Never commit real secrets.

Suggested repository:

``` text
abcd-hrms/
├── pom.xml
├── README.md
├── ARCHITECTURE.md
├── docker-compose.yml
├── .env.example
├── docs/
│   ├── domain-model.md
│   ├── service-boundaries.md
│   ├── event-catalog.md
│   ├── api-catalog.md
│   ├── security-threat-model.md
│   ├── payroll-calculation.md
│   ├── deployment-runbook.md
│   └── diagrams/
├── platform/
│   ├── common-observability/
│   ├── common-security/
│   └── common-messaging/
├── identity-service/
├── workforce-service/
├── time-leave-service/
├── onboarding-bgv-service/
├── payroll-service/
├── tax-benefits-service/
├── payment-ledger-service/
└── notification-reporting-service/
```

Use layered/hexagonal architecture in each service: `api`,
`application`, `domain`, `infrastructure`, `config`, `exception`.
Controllers stay thin; DTOs are used at API boundaries; entities are
never exposed directly. Shared modules contain technical utilities only,
not shared business entities/repositories.

## 13. Diagrams required

Create Mermaid diagrams for: 1. System context. 2.
Microservice/container architecture. 3. REST/Kafka communication. 4.
Employee onboarding and BGV sequence. 5. Leave request/approval. 6.
Attendance-to-payroll flow. 7. Payroll calculation/approval/lock. 8.
Payment/reversal/reconciliation. 9. Kafka event flow. 10. Per-service
database ownership and ER model. 11. Security trust boundaries. 12.
Docker deployment architecture.

Explain every diagram in plain language.

## 14. Implementation deliverables and order

**A --- Architecture pack first:** executive summary, assumptions,
functional/non-functional requirements, service boundaries, data
ownership matrix, Mermaid diagrams, API catalogue, event catalogue,
workflow state machines, security, caching, payroll controls,
deployment, risks and ADRs.

**B --- Database design:** per-service ER diagrams, table definitions,
Flyway migrations, indexes, constraints and ABCD seed data.

**C --- Runnable foundation:** parent and service POMs, Java 11
application classes, profiles (`local`, `test`, `prod`), standard
API/error format, validation, security, OpenAPI, Docker, observability
and CI outline.

**D --- Implement in phases:** 1. Identity/access. 2. Organization and
employee master. 3. Attendance and leave. 4. Onboarding and BGV. 5.
Compensation configuration. 6. Payroll calculation and payslips. 7. Tax,
benefits, deductions and loans. 8. Payment, ledger and reconciliation.
9. Notifications and reporting. 10. Hardening, integration, performance
and security tests.

For every phase provide business flow, exact package structure, complete
source files with paths, Flyway migrations, API examples, Kafka
contracts/producers/consumers, tests, exact Maven/Docker commands and
verification steps. No omitted imports/configuration/dependencies; no
pseudocode presented as runnable code; preserve compatibility with Java
11 and previous phases.

**E --- Frontend integration contract:** define employee self-service,
manager dashboard, HR admin, payroll maker/approver,
finance/reconciliation and configuration screens with API and
authorization requirements.

## 15. Engineering principles and anti-patterns

Apply DDD, bounded contexts, SOLID, Clean/Hexagonal Architecture, high
cohesion/low coupling, KISS, Twelve-Factor principles, explicit state
machines, idempotency, outbox/inbox, Saga, optimistic locking, versioned
APIs/events, backward-compatible migrations, observability,
secure-by-default and privacy-by-design.

Avoid shared unrestricted database access, cross-service 2PC, long
synchronous service chains, business logic in controllers, JPA entities
in API responses, hard-coded statutory rates, floating-point money,
silent locked-payroll edits, deleting financial history, secrets in Git,
unbounded Kafka retries, cache as source of truth and premature service
proliferation.

## 16. Assumptions and questions

Document assumptions for country/jurisdiction, currency, legal entities,
employee categories, pay frequencies, attendance source, leave policy,
payroll cutoff/pay date, bank integration, tax rules, accounting/ERP,
BGV vendor, document storage, employee count/peak payroll volume,
availability and RPO/RTO.

Do not block the initial architecture on every unanswered item. State
safe assumptions and mark configurable areas. Assume India-first only if
clearly labeled as an assumption. Do not give legal/tax advice; validate
statutory rules against current official sources and qualified
professionals before production.

## 17. First response instructions

Start with **Deliverable A --- Architecture Pack** only. Include: 1.
Executive summary. 2. Assumptions and open questions. 3. System context
and Mermaid microservice architecture. 4. Eight services,
responsibilities and database ownership. 5. REST/Kafka interaction and
key event flows. 6. Employee, leave, onboarding, payroll and payment
workflows. 7. Core data model. 8. Security, caching, reliability, audit
and financial controls. 9. Repository structure. 10. Phased
implementation roadmap. 11. Risks and architecture decisions.

Then stop and wait for approval before Deliverable B or code.

Think like an enterprise product architect. Explain what to build, why,
which service owns each datum, how failures recover, how payroll is
protected from unauthorized changes, and how financial records are
reconciled. The design must be detailed enough for a Java developer to
implement and maintain.


# 18. Complete Angular frontend — mandatory

Build a complete, production-oriented Angular frontend for the entire ABCD HRMS, HCM and Payroll platform. The frontend is a first-class deliverable, not a mockup or optional future enhancement. It must integrate with the eight Spring Boot microservices through the API Gateway and their documented REST APIs.

## 18.1 Frontend technology and compatibility

- Use **Angular 17** as the baseline, with a pinned compatible Node.js/npm version. If the backend team selects a different Angular major, explain the compatibility and support trade-offs before changing it.
- TypeScript with strict mode enabled; RxJS for asynchronous streams.
- Angular Router with lazy-loaded feature routes.
- Reactive Forms for all business forms and validation.
- Angular Material and Angular CDK for accessible enterprise components, tables, dialogs, menus, date pickers and navigation. Use a consistent theme and design-token system.
- SCSS for styling; responsive layouts for desktop, tablet and mobile.
- Use a maintainable state-management approach: start with Angular services/signals or RxJS for local/feature state; introduce NgRx only where cross-feature complexity justifies it. Explain the choice.
- Use HttpClient, typed API clients, HTTP interceptors and centralized error handling.
- Use OpenAPI-generated TypeScript clients where practical, or maintain typed API service classes generated from the documented API contracts.
- Do not put secrets, client secrets or trusted authorization decisions in browser code.
- Do not store access tokens in localStorage by default. Prefer a secure OIDC Authorization Code + PKCE flow and a backend-for-frontend / secure HttpOnly-cookie approach where appropriate. Explain the selected browser authentication architecture, CSRF protection and token renewal.
- The Angular app must be independently buildable and runnable through documented commands and Docker.

## 18.2 User roles and role-specific portals

Implement navigation, dashboards, actions and data visibility based on server-enforced permissions. Hiding a button in Angular is not authorization; every backend endpoint must enforce access independently.

Provide these experiences:

1. **Employee Self-Service (ESS)**
   - My profile and permitted profile update requests.
   - Personal information, job details and organization details.
   - Attendance, punches, timesheets and regularization.
   - Apply for leave, withdraw/cancel requests and view leave ledger/balances.
   - Holiday calendar and team calendar where permitted.
   - Payslips, annual statements, tax declarations and proof uploads.
   - Compensation statements where authorized.
   - Benefits, loans, advances, reimbursements and deduction schedules.
   - Onboarding checklist, document uploads and BGV consent/status where applicable.
   - Notifications, tasks, approval status and help/support links.

2. **Manager Portal**
   - Team dashboard and authorized team headcount.
   - Team attendance, missing punches, timesheet approvals and overtime.
   - Leave requests with approve/reject and reason.
   - Team leave calendar and authorized leave balances.
   - Employee onboarding task progress where assigned.
   - Team changes and manager approvals.
   - Delegation, pending tasks, reminders and approval history.
   - No access to salary, tax, bank or BGV details unless separately granted.

3. **HR Admin Portal**
   - Workforce overview, headcount analytics and organization structure.
   - Employee create/edit/lifecycle workflows and bulk import.
   - Departments, locations, designations, positions and reporting hierarchy.
   - Employee document checklist and profile correction workflow.
   - Onboarding cases, document verification and BGV case review with restricted permissions.
   - Attendance policies, leave types, leave policies, accruals and adjustments.
   - Approval workflows, delegation and escalations.
   - Employee lifecycle reports, joiners/leavers and audit search.
   - Configurable access to sensitive fields.

4. **Payroll Maker Portal**
   - Payroll calendars, pay groups and payroll periods.
   - Input collection status and missing-input exceptions.
   - Compensation and salary component configuration within assigned scope.
   - Payroll preview, simulation, calculation run and employee-level calculation explanation.
   - Gross-to-net breakdown, variance analysis and exception resolution.
   - Arrears, retro pay, off-cycle runs and final settlement.
   - Payslip preview and controlled publishing.
   - Maker cannot approve/release their own payroll when separation-of-duties policy prohibits it.

5. **Payroll Approver Portal**
   - Payroll run review and comparison with prior periods.
   - Headcount, gross, deductions, tax, employer contributions and net-pay totals.
   - Exception and variance drill-down.
   - Approval/rejection with mandatory reason where configured.
   - Lock payroll only with required permission and workflow state.
   - Immutable approval history and evidence.

6. **Finance and Payment Portal**
   - Approved payroll payment batches.
   - Payment file generation and provider/bank submission status.
   - Salary credit status, failed/returned payments and retry eligibility.
   - Authorized debit/recovery and reversal workflows.
   - Maker-checker controls for release and reversal.
   - Bank statement import, UTR matching and reconciliation.
   - Ledger journals, debit/credit totals and accounting exports.
   - Period close checklist and unresolved reconciliation exceptions.

7. **System Administrator Portal**
   - User and role administration, permission mapping and identity-provider configuration references.
   - Company/legal entity and tenant configuration.
   - Reference data, feature flags and integration health.
   - Notification templates and delivery monitoring.
   - Kafka consumer/DLQ operational visibility with tightly restricted actions.
   - Audit, security events and service health dashboards.
   - No ability to bypass payroll financial controls merely by being a technical administrator.

8. **Auditor / Read-only Portal**
   - Permission-scoped read-only access to payroll runs, approvals, payment reconciliation, audit history and reports.
   - Export actions separately permissioned and audited.
   - No mutation actions.

## 18.3 Angular modules / feature structure

Create lazy-loaded feature areas aligned to user workflows, not one Angular module per backend microservice blindly:

```text
abcd-hrms-ui/
├── src/
│   ├── app/
│   │   ├── core/
│   │   │   ├── auth/
│   │   │   ├── guards/
│   │   │   ├── interceptors/
│   │   │   ├── api/
│   │   │   ├── layout/
│   │   │   ├── navigation/
│   │   │   ├── error-handling/
│   │   │   └── telemetry/
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   ├── directives/
│   │   │   ├── pipes/
│   │   │   ├── validators/
│   │   │   ├── models/
│   │   │   └── utilities/
│   │   ├── features/
│   │   │   ├── dashboards/
│   │   │   ├── workforce/
│   │   │   ├── organization/
│   │   │   ├── attendance/
│   │   │   ├── leave/
│   │   │   ├── onboarding/
│   │   │   ├── background-verification/
│   │   │   ├── compensation/
│   │   │   ├── payroll/
│   │   │   ├── payslips/
│   │   │   ├── tax/
│   │   │   ├── benefits/
│   │   │   ├── deductions/
│   │   │   ├── loans/
│   │   │   ├── payments/
│   │   │   ├── ledger/
│   │   │   ├── reconciliation/
│   │   │   ├── approvals/
│   │   │   ├── reports/
│   │   │   ├── notifications/
│   │   │   ├── administration/
│   │   │   └── audit/
│   │   ├── app.routes.ts
│   │   └── app.config.ts
│   ├── assets/
│   ├── environments/
│   ├── styles/
│   └── theme/
├── angular.json
├── package.json
├── tsconfig.json
├── Dockerfile
└── nginx.conf
```

Use standalone components and feature-level route configuration where supported by the selected Angular version. Keep core singleton services separate from reusable shared UI components.

## 18.4 Required screens and interactions

For every feature, design and implement the complete screen set, not just a dashboard:

- List/search screens with server-side pagination, sorting, filters, column selection and export permissions.
- Create/edit/detail screens with reactive forms, validation, dirty-state protection and confirmation dialogs.
- Detail pages with tabs for overview, history, documents, approvals and audit trail as appropriate.
- Workflow screens with status timeline, current owner, next action, due date and full approval history.
- Bulk upload screens with downloadable templates, upload progress, row-level validation errors and retry of failed rows.
- Dashboard cards and charts with drill-down and permission-aware data.
- Empty, loading, error, access-denied and no-results states.
- Responsive mobile layouts for employee and manager self-service.
- Accessible keyboard navigation, visible focus, labels, contrast and screen-reader semantics.

Specific important screens:
- Login/SSO redirect, access denied and session-expired handling.
- Employee directory, employee profile, create employee wizard and employee lifecycle timeline.
- Organization chart and headcount dashboard.
- Attendance calendar, punch correction, timesheet and manager approval queue.
- Leave application wizard, leave balance ledger, policy configuration and approval inbox.
- Onboarding checklist, candidate portal, secure document upload and BGV review.
- Salary structure editor, component formula/configuration screen and compensation history.
- Payroll period dashboard, input validation, payroll simulation, calculation detail, variance comparison, approval, lock and payslip publishing.
- Employee payslip viewer and downloadable statement.
- Tax declaration, proof submission and tax projection.
- Benefits, loans, advances, reimbursements and deductions.
- Payment batch, bank submission, failed payment, reversal and reconciliation screens.
- Ledger journal and accounting export.
- Reports catalogue, report filters, export job status and download.
- Role/permission administration, approval workflow configuration and audit search.

## 18.5 Design system and UX quality

Create a polished, consistent enterprise HR platform:
- Professional, clean visual language suitable for HR, payroll and finance users.
- Persistent app shell with left navigation, top bar, company/legal-entity context, breadcrumbs, global search where authorized, notification center and user menu.
- Role-aware navigation and dashboard widgets.
- Consistent spacing, typography, colors, icons, form controls, status chips, tables, dialogs and page headers.
- Use status colors consistently but do not rely on color alone.
- Use confirmation and clear warning language for irreversible/high-impact actions such as payroll lock, payment release, debit and reversal.
- Show currency, date, time zone and number formats consistently based on company/user locale.
- Support configurable company branding and logo.
- Do not invent real employee data; use clearly labeled synthetic seed data in local development.
- Avoid cluttered dashboards, oversized cards, decorative charts without business value and inaccessible custom controls.

## 18.6 Frontend security

- OIDC Authorization Code Flow with PKCE or a documented secure BFF approach.
- Route guards and permission directives for user experience only; backend authorization remains authoritative.
- Permission checks at feature, action and field level.
- Never expose sensitive fields in HTML, browser logs, analytics or client-side error telemetry.
- Mask bank/tax/identity details by default and reveal only after authorized action.
- Sanitize untrusted content; avoid unsafe DOM APIs and `bypassSecurityTrust...` unless reviewed and justified.
- Validate file type/size client-side for usability and again on the server for security.
- Use secure download flows for payslips and documents; do not expose public object-storage URLs.
- Apply CSP, secure headers, allowed origins and CSRF protections at the edge/BFF.
- Clear sensitive in-memory state on logout/session expiry and handle idle timeout.
- Prevent accidental double submission; use idempotency keys for sensitive commands where supported.
- Audit sensitive downloads and exports on the backend.

## 18.7 API integration and frontend reliability

- Route all service calls through the API Gateway or BFF; do not let the browser call internal service endpoints directly.
- Define environment-specific API base URLs without rebuilding business logic.
- Generate or maintain strongly typed request/response models from OpenAPI.
- Centralize authentication/session handling, correlation IDs, error mapping and retry policy.
- Do not automatically retry non-idempotent payroll/payment commands.
- Use bounded retries only for safe idempotent reads or explicitly idempotent commands.
- Display correlation/reference IDs in support-friendly error screens without exposing stack traces.
- Use server-side filtering/pagination for large employee and payroll datasets.
- Handle long-running payroll/report jobs with polling or server-sent events where appropriate; document the chosen mechanism.
- Use optimistic UI only for low-risk actions; show authoritative server state for approvals and financial operations.
- Provide a consistent API error contract and field-level validation mapping.

## 18.8 Frontend testing and quality gates

Use:
- Angular unit tests for components, services, pipes, validators, guards and state.
- HTTP tests for API clients and interceptors.
- Component tests for forms, tables, dialogs and role-aware rendering.
- End-to-end tests using Playwright or Cypress.
- Accessibility checks and keyboard-only workflow tests.
- Visual regression tests for core layouts where practical.
- Security tests for route bypass attempts, sensitive-field exposure and session expiry.
- Contract tests against OpenAPI examples.
- Performance tests for large tables, dashboard rendering and lazy-loaded routes.

Mandatory end-to-end journeys:
1. Employee signs in, views profile, applies for leave and tracks approval.
2. Manager reviews team attendance and approves/rejects leave.
3. HR creates employee, completes onboarding and starts BGV.
4. HR imports employees and resolves invalid rows.
5. Payroll maker configures a period, validates inputs and calculates payroll.
6. Payroll approver reviews variances, rejects or approves and locks the run.
7. Employee views and downloads the published payslip.
8. Finance submits a payment batch, handles a failed salary payment and performs an authorized reversal.
9. Finance reconciles bank transactions and closes the period.
10. Unauthorized manager attempts to access another department's employee or payroll data and is denied.

## 18.9 Angular Docker and delivery

- Add Angular UI to the root repository and Docker Compose architecture.
- Use a multi-stage Docker build: Node image to build the Angular production bundle, then a small Nginx image to serve static assets.
- Configure Nginx SPA route fallback, security headers, gzip/brotli where supported, cache rules for hashed assets and no-cache rules for runtime configuration as appropriate.
- Do not bake secrets into the frontend image.
- Provide local development proxy configuration to the API Gateway.
- Provide environment/runtime configuration strategy for dev, test, staging and production.
- Add lint, formatting, unit-test, production-build and dependency-audit commands to CI.
- Document deployment, rollback and cache invalidation.

## 18.10 Frontend deliverables

The implementation must include:
1. Complete Angular workspace and pinned dependencies.
2. Application shell, theme, navigation and responsive layouts.
3. Authentication integration and permission-aware routing.
4. All role-specific portals and feature screens listed above.
5. Typed API clients and integration with every required backend workflow.
6. Reactive forms, validation, tables, charts, dialogs, notifications and file-upload flows.
7. Loading, empty, error, unauthorized and session-expiry states.
8. Unit, component, accessibility and end-to-end tests.
9. Dockerfile, Nginx configuration, Docker Compose integration and environment configuration.
10. README with exact commands to install, run, test, build and deploy.
11. Screen-to-API mapping matrix and role-to-screen/action permission matrix.
12. Seed/demo data and a walkthrough of complete employee-to-payroll-to-payment journeys.

## 18.11 Updated implementation order

Update the implementation roadmap so the frontend is developed alongside backend capabilities:

1. Architecture pack, UX sitemap, role/permission matrix and design-system foundations.
2. Identity service + Angular application shell, SSO, session handling and role-aware navigation.
3. Workforce service + employee directory, employee profile, create/edit and headcount dashboard.
4. Time/leave service + attendance, timesheet, leave application, balance and approval screens.
5. Onboarding/BGV service + candidate onboarding, document upload, consent and restricted review screens.
6. Compensation + salary configuration and compensation history screens.
7. Payroll engine + payroll maker, preview, calculation details, variance, approval, lock and payslip screens.
8. Tax/benefits/deductions + employee declarations, proof upload and HR configuration screens.
9. Payment/ledger + finance batch, credit/failure/reversal and reconciliation screens.
10. Notifications/reporting + dashboards, reports, exports and audit screens.
11. Full end-to-end integration, accessibility, performance, security and production deployment hardening.

At every phase, deliver a working vertical slice: database migration + backend API + Kafka integration where needed + Angular screen + authorization + automated tests + Docker verification. Do not build the entire backend first and leave the UI as a disconnected mockup.
