# Product Requirements Document — Blood Bank Management System

## 1. Purpose

Build a **complete, runnable Blood Bank Management System** based strictly on the supplied `JAVA.drawio` class diagram.

The implementation must cover **every class, attribute, method, inheritance relationship, login flow, validation rule, transaction flow, inventory operation, alert, report, persistence operation, and menu flow** represented by the class diagram.

This is not a UI mockup or partial prototype. Implement the actual application logic end-to-end.

### Primary goal

Create a robust Java application in which:

- users can authenticate according to their role;
- donors can check eligibility and view donation history;
- hospital staff can create patients, request blood, issue blood, inspect local stock, and restock;
- blood-bank administrators can register donations, initiate lab tests, approve/reject requests, and dispatch transfers;
- hospitals can submit requests and inspect request history and inventory;
- blood units are tracked through collection, testing, inventory, expiry, issue, and transfer;
- emergency requests receive priority treatment;
- low-stock and expiry alerts are generated;
- reports can be generated and exported;
- data persists between application runs;
- all validation and error cases are handled cleanly.

Do not remove functionality simply because the diagram declares methods as `void`. A method declared `void` should still perform its business operation and update the appropriate domain state.

---

# 2. Source of Truth

The supplied class diagram is the primary structural specification.

Do not rename or remove the diagram's classes, core attributes, or public methods without a compelling implementation reason.

The diagram contains the following classes:

### Abstract/base classes

1. `Person`
2. `Staff`
3. `Organization`
4. `Transaction`

### Person hierarchy

5. `HospitalStaff`
6. `BloodBankAdmin`
7. `Donor`
8. `Patient`

### Organization hierarchy

9. `BloodBank`
10. `Hospital`

### Inventory and blood domain

11. `Inventory`
12. `BloodUnit`

### Transaction hierarchy

13. `BloodDonation`
14. `BloodRequest`
15. `EmergencyRequest`
16. `BloodTransfer`

### Utility/service classes

17. `LoginManager`
18. `AlertManager`
19. `ReportGenerator`
20. `FileManager`
21. `Validation`

### Application classes

22. `Menu`
23. `Main`

---

# 3. Class Diagram Contract

## 3.1 Person

`Person` is abstract.

### Protected attributes

- `personId: String`
- `name: String`
- `age: int`
- `gender: String`
- `phoneNumber: String`
- `address: String`

### Methods

- `getDetails(): String`
- `updateDetails(): void`
- `displayDetails(): void`

### Requirements

- Store common personal information.
- `getDetails()` returns a complete formatted representation.
- `updateDetails()` must support updating editable personal information with validation.
- `displayDetails()` must print/render the person's details.
- IDs must be unique.
- Age and phone number must be validated.

---

# 4. Staff Hierarchy

## 4.1 Staff

`Staff` is abstract and extends `Person`.

### Protected attributes

- `employeeId: String`
- `facilityId: String`

### Method

- `displayStaffDetails(): void`

### Requirements

- Maintain staff identity and facility association.
- Staff IDs must be unique.
- Facility IDs must reference a valid hospital or blood bank.

---

## 4.2 HospitalStaff

Extends `Staff`.

### Private attribute

- `department: String`

### Methods

- `createPatient(): void`
- `requestBlood(): void`
- `issueBlood(): void`
- `checkLocalStock(): void`
- `restock(): void`

### Requirements

Hospital staff can:

1. Create/register a patient.
2. Create blood requests for patients.
3. View/search the hospital's local blood inventory.
4. Issue blood to a patient only when the request is approved and stock is available.
5. Request restocking from the blood bank when local stock is insufficient.
6. Maintain department information.
7. Only operate on the hospital associated with their `facilityId`.

---

## 4.3 BloodBankAdmin

Extends `Staff`.

### Private attribute

- `adminLevel: String`

### Methods

- `registerDonation(): void`
- `initiateLabTest(): boolean`
- `approveRequest(): void`
- `dispatchTransfer(): void`

### Requirements

Blood-bank administrators can:

1. Register donor donations.
2. Initiate testing for collected blood.
3. Mark tested units as approved/rejected according to test results.
4. Review and approve valid blood requests.
5. Reject invalid/unavailable requests.
6. Dispatch approved transfers from the blood bank.
7. Perform administrator-level inventory operations.
8. Respect admin-level authorization where applicable.

---

# 5. Donor

`Donor` extends `Person`.

### Private attributes

- `bloodGroup: String`
- `haemoglobin: double`
- `weight: double`
- `lastDonationDate: String`
- `eligible: boolean`

### Methods

- `checkEligibility(): boolean`
- `viewDonationHistory(): void`

### Requirements

Donor eligibility must be calculated rather than blindly trusting the stored `eligible` flag.

At minimum validate:

- supported blood group;
- valid age;
- valid weight;
- haemoglobin value;
- previous donation date;
- minimum donation interval;
- absence of invalid/missing donor data.

Use sensible configurable business rules rather than magic numbers scattered throughout the code.

The eligibility result must be shown clearly to the user.

A donor must not be allowed to register a donation if they are currently ineligible.

---

# 6. Patient

`Patient` extends `Person`.

### Private attributes

- `bloodGroup: String`
- `disease: String`
- `doctorName: String`
- `unitsRequired: int`
- `hospitalId: String`

### Method

- `viewRequestStatus(): void`

### Requirements

- Patients belong to a hospital.
- Blood group must be valid.
- Units required must be positive.
- Patient request status must be retrievable.
- A patient may have multiple historical requests.
- Patient information must not be exposed to unauthorized users.

---

# 7. Organization Hierarchy

## 7.1 Organization

Abstract class.

### Protected attributes

- `organizationId: String`
- `organizationName: String`
- `address: String`
- `contactNumber: String`

### Methods

- `displayOrganization(): void`
- `updateOrganization(): void`

### Requirements

- Common organization information must be reusable by both `BloodBank` and `Hospital`.
- Organization IDs must be unique.
- Contact information must be validated.
- Update operations must preserve identity.

---

# 8. BloodBank

Extends `Organization`.

### Attributes

- `managerName: String`
- `inventory: Inventory`

### Methods

- `addBloodUnit(): void`
- `removeBloodUnit(): void`
- `transferBlood(): void`
- `viewInventory(): void`

### Requirements

The blood bank is the central inventory facility.

It must support:

- adding valid blood units;
- removing/issuing blood units;
- transferring blood to hospitals;
- inventory searching;
- inventory display;
- low-stock monitoring;
- expiry monitoring;
- maintaining accurate stock counts.

Blood that has failed lab testing or has expired must never be issued.

---

# 9. Hospital

Extends `Organization`.

### Attributes

- `hospitalType: String`
- `emergencyContact: String`
- `inventory: Inventory`
- `staffs: HospitalStaff[]`

### Methods

- `sendBloodRequest(): void`
- `viewRequestHistory(): void`
- `viewInventory(): void`

### Requirements

Hospitals must:

- manage local inventory;
- have one or more staff members;
- submit blood requests;
- submit emergency requests;
- view request history;
- view local stock;
- receive approved blood transfers;
- issue blood to patients.

Hospital staff must be linked to their hospital.

---

# 10. Inventory

### Attributes

- `bloodUnits: BloodUnit[]`
- `totalStock: int`

### Methods

- `addBloodUnit(): void`
- `removeBloodUnit(): void`
- `searchBloodGroup(): BloodUnit[]`
- `checkLowStock(): void`
- `displayInventory(): void`

### Requirements

Inventory must be treated as a real domain component, not merely a list.

It must:

- maintain blood units;
- maintain correct total stock;
- support blood-group searches;
- prevent negative stock;
- prevent duplicate blood-unit IDs;
- remove only available/valid units;
- ignore expired/rejected units for usable-stock calculations;
- support FIFO-style selection where appropriate;
- report exact stock by blood group;
- identify low-stock groups;
- display unit-level information when requested.

### Stock accounting

For a blood group:

`available stock = sum(quantity of usable BloodUnit records for that group)`

Never rely solely on `totalStock` if it can be derived from unit records.

---

# 11. BloodUnit

### Attributes

- `bloodUnitId: String`
- `bloodGroup: String`
- `quantity: int`
- `collectionDate: String`
- `expiryDate: String`
- `status: String`

### Methods

- `runLabTests(): boolean`
- `isExpired(): boolean`
- `updateQuantity(): void`
- `displayBloodUnit(): void`

### Requirements

Each blood unit must have:

- unique ID;
- valid blood group;
- positive quantity;
- collection date;
- expiry date;
- lifecycle status.

Suggested statuses:

- `COLLECTED`
- `PENDING_TEST`
- `TEST_PASSED`
- `TEST_FAILED`
- `AVAILABLE`
- `RESERVED`
- `ISSUED`
- `TRANSFERRED`
- `EXPIRED`
- `DISCARDED`

Do not hard-code status strings throughout the application. Use enums/constants where possible while preserving the diagram's public API.

### Lab testing

`runLabTests()` must perform a deterministic application-level validation/test workflow.

The UI must clearly show:

- test initiated;
- test result;
- reason/result status where appropriate;
- whether the unit entered usable inventory.

A failed unit must never enter available stock.

### Expiry

`isExpired()` must compare the expiry date with the current date.

Expired units must automatically become unusable.

---

# 12. Transaction Hierarchy

## 12.1 Transaction

Abstract class.

### Protected attributes

- `transactionId: String`
- `transactionDate: String`
- `status: String`

### Method

- `displayTransaction(): void`

### Requirements

Every donation, request, emergency request, and transfer must have:

- unique transaction ID;
- timestamp/date;
- lifecycle status;
- audit information.

---

# 13. BloodDonation

Extends `Transaction`.

### Attributes

- `donorId: String`
- `bloodGroup: String`
- `quantity: int`

### Methods

- `recordDonation(): void`
- `updateInventory(): void`

### Donation flow

1. Authenticate donor/admin/staff as appropriate.
2. Select donor.
3. Check donor eligibility.
4. Create donation transaction.
5. Collect blood.
6. Create one or more `BloodUnit` records.
7. Mark units as pending testing.
8. Run lab tests.
9. Only passed units become available.
10. Update inventory.
11. Update donor's last donation date.
12. Store donation history.
13. Generate relevant alerts/reports.

Do not add untested blood to usable inventory.

---

# 14. BloodRequest

Extends `Transaction`.

### Attributes

- `patientId: String`
- `hospitalId: String`
- `bloodGroup: String`
- `unitsRequested: int`
- `urgency: String`

### Methods

- `checkAvailability(): boolean`
- `approveRequest(): void`
- `rejectRequest(): void`

### Request lifecycle

Suggested statuses:

- `PENDING`
- `UNDER_REVIEW`
- `APPROVED`
- `REJECTED`
- `PARTIALLY_FULFILLED`
- `FULFILLED`
- `CANCELLED`

### Requirements

Before approval:

- patient must exist;
- hospital must exist;
- blood group must be valid;
- units requested must be positive;
- hospital must be authorized to request;
- stock must be checked;
- expired/rejected blood must not count.

---

# 15. EmergencyRequest

Extends `BloodRequest`.

### Attributes

- `urgencyLevel: String`
- `levels: String[]`
- `isEmergency: boolean`

### Methods

- `priorityDispatch(): void`
- `overrideThreshold(): void`

### Requirements

Emergency requests must be prioritized over normal requests.

Suggested priority levels:

- `LOW`
- `NORMAL`
- `HIGH`
- `CRITICAL`

Emergency requests may use threshold overrides only according to clearly defined business rules.

`overrideThreshold()` must be audited and must never create negative inventory.

The UI should visibly distinguish emergency requests from standard requests.

---

# 16. BloodTransfer

Extends `Transaction`.

### Attributes

- `sourceFacilityId: String`
- `destinationFacilityId: String`
- `bloodGroup: String`
- `unitsTransferred: int`

### Methods

- `transferUnits(): void`
- `recordTransfer(): void`

### Transfer flow

1. Verify source facility.
2. Verify destination hospital.
3. Verify requested blood group.
4. Verify approved request.
5. Check usable source inventory.
6. Reserve/select units.
7. Deduct source inventory.
8. Add units to destination inventory.
9. Record transfer transaction.
10. Update request status.
11. Generate confirmation/audit record.

Transfers must be atomic from the application's perspective: if the destination update fails, source inventory must not be permanently deducted.

---

# 17. LoginManager

### Attributes

- `username: String`
- `password: String`

### Methods

- `authenticateUser(): boolean`
- `changePassword(): void`
- `logoutUser(): void`

## Authentication requirements

Implement a real role-based authentication flow.

Supported roles:

- `DONOR`
- `PATIENT`
- `HOSPITAL_STAFF`
- `BLOOD_BANK_ADMIN`

If the implementation requires a system-level hospital/blood-bank account, support that through the same authorization model rather than a separate insecure mechanism.

### Login screen

The login flow must support:

- username;
- password;
- authentication;
- invalid credential handling;
- role resolution;
- session state;
- logout;
- password change.

Never display stored passwords.

Do not store plaintext passwords if a persistent credential store is implemented. Use password hashing.

### Role authorization

After login, users must only see and access operations allowed for their role.

Example:

| Role | Main capabilities |
|---|---|
| Donor | profile, eligibility, donation history |
| Patient | profile, request status |
| Hospital Staff | patient creation, requests, issuing, stock, restocking |
| Blood Bank Admin | donations, lab testing, approvals, transfers, inventory, reports |

---

# 18. AlertManager

### Attributes

- `lowStockThreshold: int`
- `expiryAlertDays: int`

### Methods

- `checkLowStock(): void`
- `checkExpiry(): void`
- `generateAlert(): void`

### Requirements

Generate alerts for:

1. Low stock by blood group.
2. Blood units approaching expiry.
3. Expired blood units.
4. Failed lab tests where relevant.
5. Critical/emergency request situations where appropriate.

Thresholds must be configurable.

Alerts should include:

- type;
- severity;
- facility;
- blood group/unit if applicable;
- message;
- date/time;
- resolved/unresolved state where useful.

---

# 19. ReportGenerator

### Attributes

- `reportTitle: String`
- `generatedDate: String`

### Methods

- `generateInventoryReport(): void`
- `generateDonationReport(): void`
- `generateRequestReport(): void`
- `exportReport(): void`

### Reports

Implement at least:

### Inventory report

Include:

- blood group;
- usable quantity;
- expired quantity;
- pending-test quantity;
- low-stock status;
- unit information when requested.

### Donation report

Include:

- donation ID;
- donor;
- blood group;
- quantity;
- date;
- test result;
- final status.

### Request report

Include:

- request ID;
- patient;
- hospital;
- blood group;
- quantity;
- urgency;
- status;
- request date;
- fulfillment information.

### Export

Support a practical export format such as CSV and/or TXT.

Reports must be generated from current persisted application data.

---

# 20. FileManager

### Attribute

- `filePath: String`

### Methods

- `saveData(): void`
- `loadData(): void`
- `appendData(): void`
- `deleteRecord(): void`

### Requirements

Implement persistence.

The application must not lose all data when it exits.

At minimum persist:

- users/accounts;
- people;
- staff;
- hospitals;
- blood banks;
- donors;
- patients;
- blood units;
- inventory;
- donations;
- requests;
- emergency requests;
- transfers;
- alert/audit information.

Use a clean persistence strategy appropriate for the project.

If no database is required, JSON/CSV/serialized files may be used, but structure the persistence layer so it can later be replaced with a database.

Do not scatter file-writing logic across domain classes.

---

# 21. Validation

### Attribute

- `bloodGroups: String[]`

### Methods

- `validateBloodGroup(): boolean`
- `validatePhoneNumber(): boolean`
- `validateAge(): boolean`
- `validateDate(): boolean`

### Requirements

Centralize validation.

Supported blood groups:

- `A+`
- `A-`
- `B+`
- `B-`
- `AB+`
- `AB-`
- `O+`
- `O-`

Validate:

- blood groups;
- phone numbers;
- age;
- dates;
- positive quantities;
- IDs;
- required strings;
- expiry dates after collection dates;
- valid staff/facility references.

All invalid user input must produce a useful error message and allow correction instead of crashing the application.

---

# 22. Menu

### Attribute

- `choice: Integer`

### Methods

- `displayMainMenu(): void`
- `staffMenu(): void`
- `donorMenu(): void`
- `patientMenu(): void`
- `hospitalMenu(): void`

### Requirements

Build a complete navigable console UI.

## Main menu

Provide:

1. Login
2. Register user/account where appropriate
3. Exit

## Donor menu

Provide:

1. View profile
2. Update profile
3. Check eligibility
4. View donation history
5. Change password
6. Logout

## Patient menu

Provide:

1. View profile
2. Update profile
3. View request status/history
4. Change password
5. Logout

## Hospital staff menu

Provide:

1. Create patient
2. Create blood request
3. Create emergency blood request
4. Check local stock
5. Issue blood
6. Request restock
7. View request history
8. View hospital inventory
9. Change password
10. Logout

## Blood-bank admin menu

Provide:

1. Register donation
2. Initiate lab test
3. View pending tests
4. Approve request
5. Reject request
6. Dispatch transfer
7. View blood-bank inventory
8. Add/remove blood unit
9. View alerts
10. Generate inventory report
11. Generate donation report
12. Generate request report
13. Manage facilities/users where applicable
14. Change password
15. Logout

The menu must never expose unauthorized operations.

---

# 23. Main

### Methods

- `main(args: String[]): void`
- `initializeSystem(): void`
- `closeApplication(): void`

### Requirements

`Main` must:

1. Initialize configuration.
2. Load persisted data.
3. Initialize services/managers.
4. Ensure required default facilities/admin account exist.
5. Launch the main menu.
6. Handle the application lifecycle.
7. Save data safely before exit.
8. Close resources cleanly.

The application must start with a single standard command.

---

# 24. Complete Business Workflows

## Workflow A — Donor registration and donation

1. User logs in as donor or authorized staff/admin.
2. Donor profile is displayed.
3. Eligibility is checked.
4. If ineligible, donation stops with the reason.
5. If eligible, donation is registered.
6. Blood units are created.
7. Units enter `PENDING_TEST`.
8. Admin initiates lab testing.
9. Test passes → unit becomes usable.
10. Test fails → unit becomes rejected/discarded.
11. Inventory is updated.
12. Donor history is updated.
13. Donation transaction is stored.

---

## Workflow B — Patient requests blood

1. Hospital staff logs in.
2. Staff selects/creates patient.
3. Staff selects blood group.
4. Staff enters required units.
5. Staff chooses normal/emergency request.
6. System validates all information.
7. Availability is checked.
8. Request is stored as pending.
9. Admin reviews request.
10. Admin approves/rejects.
11. Approved request becomes eligible for fulfillment.
12. Transfer/issue occurs.
13. Request status is updated.
14. Patient can view request status.

---

## Workflow C — Hospital receives blood

1. Hospital submits request.
2. Admin approves request.
3. Blood bank selects valid units.
4. Transfer transaction is created.
5. Source inventory decreases.
6. Destination inventory increases.
7. Transfer is recorded.
8. Request status changes appropriately.
9. Hospital can see received stock.

---

## Workflow D — Hospital issues blood to patient

1. Hospital staff logs in.
2. Staff selects patient.
3. Staff selects approved request.
4. System verifies remaining required units.
5. System verifies local inventory.
6. System verifies blood unit status/expiry.
7. Blood is issued.
8. Inventory decreases.
9. Request is updated.
10. Transaction/audit record is stored.

---

## Workflow E — Restock

1. Hospital staff checks local stock.
2. Low stock is detected.
3. Staff creates restock request.
4. Blood bank receives request.
5. Admin checks availability.
6. Admin approves/fulfills.
7. Blood transfer is dispatched.
8. Hospital inventory increases.
9. Blood bank inventory decreases.
10. Transfer and request history are updated.

---

## Workflow F — Emergency request

1. Hospital staff creates emergency request.
2. Emergency request is marked `isEmergency = true`.
3. Urgency level is assigned.
4. Request receives higher priority.
5. Admin sees emergency request at the top of the queue.
6. Threshold/availability rules are evaluated.
7. If allowed, priority dispatch occurs.
8. Transfer is recorded.
9. All overrides are audited.

---

# 25. Authentication and Access Control

Implement authorization centrally.

Do not rely only on hiding menu options.

Every sensitive service operation should verify authorization.

Examples:

- A donor cannot approve a blood request.
- A patient cannot modify hospital inventory.
- Hospital staff cannot perform blood-bank administrative actions.
- Only authorized staff can issue blood.
- Only authorized admins can approve/dispatch transfers.
- Users can only update their own profile unless they have administrative permission.

---

# 26. Data Relationships

Implement the relationships implied by the class diagram:

### Inheritance

- `Staff` → `Person`
- `Donor` → `Person`
- `Patient` → `Person`
- `HospitalStaff` → `Staff`
- `BloodBankAdmin` → `Staff`
- `BloodBank` → `Organization`
- `Hospital` → `Organization`
- `BloodDonation` → `Transaction`
- `BloodRequest` → `Transaction`
- `EmergencyRequest` → `BloodRequest`
- `BloodTransfer` → `Transaction`

### Composition/association

- `BloodBank` owns/uses an `Inventory`.
- `Hospital` owns/uses an `Inventory`.
- `Hospital` has `HospitalStaff[]`.
- `Inventory` contains `BloodUnit[]`.
- `BloodDonation` creates/updates `BloodUnit` records.
- `BloodRequest` references a `Patient` and `Hospital`.
- `BloodTransfer` references source and destination facilities.
- `AlertManager` observes inventory and expiry conditions.
- `ReportGenerator` reads application/domain data.
- `FileManager` persists application state.
- `LoginManager` manages authentication/session state.

Do not duplicate the same object unnecessarily when a shared reference/ID relationship is more appropriate.

---

# 27. Suggested Architecture

Use a clean layered structure.

```text
src/
├── model/
│   ├── Person.java
│   ├── Staff.java
│   ├── HospitalStaff.java
│   ├── BloodBankAdmin.java
│   ├── Donor.java
│   ├── Patient.java
│   ├── Organization.java
│   ├── BloodBank.java
│   ├── Hospital.java
│   ├── Inventory.java
│   ├── BloodUnit.java
│   ├── Transaction.java
│   ├── BloodDonation.java
│   ├── BloodRequest.java
│   ├── EmergencyRequest.java
│   └── BloodTransfer.java
│
├── service/
│   ├── LoginManager.java
│   ├── AlertManager.java
│   ├── ReportGenerator.java
│   └── ...
│
├── util/
│   ├── Validation.java
│   ├── FileManager.java
│   └── ...
│
├── ui/
│   └── Menu.java
│
└── Main.java
```

The exact package structure may vary, but responsibilities must remain separated.

---

# 28. Object-Oriented Requirements

The implementation must visibly demonstrate the OOP concepts represented by the diagram.

Use:

- abstraction;
- inheritance;
- encapsulation;
- polymorphism;
- composition;
- association;
- method overriding;
- constructors;
- access modifiers;
- collections;
- enums where useful.

Avoid:

- one giant `Main` class;
- all business logic inside `Menu`;
- static global state for everything;
- duplicated validation;
- duplicated inventory logic;
- hard-coded fake responses;
- methods that merely print "success" without changing application state.

---

# 29. Error Handling

Handle at least:

- invalid login;
- duplicate username;
- duplicate IDs;
- invalid blood group;
- invalid age;
- invalid phone number;
- invalid date;
- invalid quantity;
- missing patient;
- missing donor;
- missing hospital;
- missing blood bank;
- unavailable stock;
- expired blood;
- rejected blood;
- failed lab test;
- unauthorized action;
- malformed persistence file;
- missing persistence file;
- failed report export;
- invalid menu choice.

The application must not terminate unexpectedly because of normal user input errors.

---

# 30. Persistence Requirements

On startup:

```text
initializeSystem()
    ↓
load persisted users
    ↓
load organizations
    ↓
load people
    ↓
load blood units/inventory
    ↓
load transactions
    ↓
load alerts/audit data
    ↓
launch Menu
```

On exit:

```text
closeApplication()
    ↓
validate pending state
    ↓
save all persistent state
    ↓
close files/resources
    ↓
terminate
```

Use atomic/defensive saving where practical so a failed save does not corrupt the entire dataset.

---

# 31. Seed Data

For first launch, create useful demo data so the application is immediately testable.

Include:

- at least one blood bank;
- at least two hospitals;
- at least one admin;
- at least one hospital staff member per hospital;
- at least one donor;
- at least one patient;
- representative blood units across several blood groups;
- example requests;
- example completed transaction history.

Clearly document the demo credentials in the README.

Do not hard-code production credentials.

---

# 32. UI/UX Requirements

The application is primarily a functional Java application.

Keep the interface clean and understandable.

Every screen should show:

- current role;
- current user/facility where relevant;
- clear menu options;
- success/error messages;
- confirmation before destructive actions;
- useful empty states.

Use formatted tables where helpful for:

- inventory;
- requests;
- donation history;
- transfer history;
- alerts;
- reports.

---

# 33. Testing Requirements

Create tests for all critical business logic.

### Unit tests

At minimum test:

- `Validation`
- donor eligibility;
- blood-unit expiry;
- inventory addition/removal;
- inventory search;
- low-stock detection;
- donation registration;
- lab-test pass/fail;
- request availability;
- request approval/rejection;
- emergency priority;
- blood transfer;
- issue-to-patient flow;
- authentication;
- authorization;
- persistence;
- report generation.

### Integration tests

Test complete workflows:

1. donor → donation → test → inventory;
2. patient → request → approval → transfer;
3. hospital → request → transfer → issue;
4. emergency request → priority dispatch;
5. application restart → persisted state restored.

---

# 34. Acceptance Criteria

The project is complete only when all of the following are true:

- [ ] Every class in the supplied diagram exists.
- [ ] Every diagram attribute is represented.
- [ ] Every diagram method is implemented.
- [ ] Inheritance relationships are implemented.
- [ ] Authentication works.
- [ ] Role-based authorization works.
- [ ] Donor workflow works end-to-end.
- [ ] Patient workflow works end-to-end.
- [ ] Hospital staff workflow works end-to-end.
- [ ] Blood-bank admin workflow works end-to-end.
- [ ] Blood donation workflow works.
- [ ] Lab testing works.
- [ ] Inventory is accurately maintained.
- [ ] Expiry detection works.
- [ ] Low-stock detection works.
- [ ] Normal blood requests work.
- [ ] Emergency requests work.
- [ ] Approval/rejection works.
- [ ] Blood transfers work.
- [ ] Hospital issuing works.
- [ ] Restocking works.
- [ ] Alerts work.
- [ ] Reports work.
- [ ] Report export works.
- [ ] File persistence works.
- [ ] Validation works.
- [ ] Invalid input does not crash the application.
- [ ] Logout works.
- [ ] Password changes work.
- [ ] Data survives application restart.
- [ ] Tests cover critical workflows.
- [ ] README explains setup, execution, architecture, credentials, and workflows.

---

# 35. Definition of Done

Do not consider the task complete after creating classes or screens.

The final implementation must be:

1. **Runnable**
2. **Persistent**
3. **Role-aware**
4. **Functionally connected**
5. **Tested**
6. **Consistent with the class diagram**
7. **Free from placeholder business logic**

Every button/menu option must perform a real operation.

Every transaction must update the relevant state.

Every state change must be reflected in the relevant inventory/history/report.

---

# 36. Implementation Rules for the Vibecoder

Follow these rules while implementing:

1. **Read the entire repository before making architectural decisions.**
2. Treat this PRD and `JAVA.drawio` as the specification.
3. Inspect existing files and preserve useful existing work.
4. Do not silently remove classes or features.
5. Do not replace the class diagram with a completely different architecture.
6. Keep class names and public methods aligned with the diagram.
7. If an implementation detail is ambiguous, choose the simplest robust solution consistent with the domain.
8. Do not ask for confirmation for routine implementation decisions.
9. Do not leave TODOs for core functionality.
10. Do not use fake/mock data as a substitute for real business logic.
11. Use real persisted state.
12. Keep secrets/passwords out of source code where possible.
13. Centralize validation.
14. Centralize authorization.
15. Keep business logic out of UI classes.
16. Add meaningful error handling.
17. Add tests for important workflows.
18. Update the README with exact run instructions.
19. After implementation, run the application and tests.
20. Fix compile errors, runtime errors, and broken workflows before declaring completion.

---

# 37. Final Verification Checklist

Before finishing, perform a complete manual walkthrough.

### Authentication

- Login as donor.
- Login as patient.
- Login as hospital staff.
- Login as blood-bank admin.
- Try incorrect credentials.
- Change password.
- Logout.
- Verify role restrictions.

### Donor

- View details.
- Update details.
- Check eligibility.
- Register donation.
- View donation history.

### Patient

- View details.
- Update details.
- View request status.

### Hospital staff

- Create patient.
- Submit normal request.
- Submit emergency request.
- View inventory.
- Check stock.
- Issue blood.
- Request restock.
- View history.

### Blood-bank admin

- Register donation.
- Initiate lab test.
- Verify passed/failed units.
- View inventory.
- Approve request.
- Reject request.
- Dispatch transfer.
- View alerts.
- Generate all reports.
- Export reports.

### System

- Restart application.
- Verify data remains.
- Verify inventory counts remain correct.
- Verify transactions remain.
- Verify no expired/rejected blood can be issued.
- Verify no unauthorized role can bypass permissions.

---

# 38. Expected Deliverables

Produce:

- complete Java source code;
- all classes from the diagram;
- clean package structure;
- persistence implementation;
- authentication/authorization;
- complete console menus;
- business workflows;
- validation;
- alerts;
- reports;
- tests;
- seed/demo data;
- README;
- sample exported reports where useful.

The final result should feel like a **complete academic-grade Blood Bank Management System**, not a collection of disconnected Java classes.

**Priority order:** correctness → complete functionality → class-diagram compliance → persistence → validation/security → testing → UI polish.
