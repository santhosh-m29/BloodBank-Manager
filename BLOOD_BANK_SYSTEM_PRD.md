# PRODUCT REQUIREMENTS DOCUMENT
## Blood Bank Management System

---

# 1. Project Overview

The **Blood Bank Management System** is a Java-based blood management application designed to manage the complete flow of blood from **donor → blood bank → hospital → patient**.

The system maintains two levels of blood inventory:

```text
                 DONOR
                   │
                   ▼
             BLOOD DONATION
                   │
                   ▼
          ┌──────────────────┐
          │    BLOOD BANK    │
          │  Central Stock   │
          └────────┬─────────┘
                   │
             Blood Transfer
                   │
                   ▼
          ┌──────────────────┐
          │     HOSPITAL     │
          │   Local Stock    │
          └────────┬─────────┘
                   │
              Issue Blood
                   │
                   ▼
                PATIENT
```

The blood bank acts as the **central blood storage facility**, while every hospital maintains its own **local inventory**.

Patients are treated through their hospital. They do not directly receive blood from the blood bank.

The application demonstrates Java OOP concepts including:

- Abstraction
- Inheritance
- Encapsulation
- Polymorphism
- Composition
- Association
- Collections
- File handling
- Authentication
- Validation
- Transaction management

---

# 2. Main Objectives

The system must:

1. Manage donor information.
2. Allow donors to initiate blood donation.
3. Perform laboratory testing before blood is registered into usable blood-bank stock.
4. Maintain centralized blood-bank inventory.
5. Maintain separate inventory for every hospital.
6. Allow hospital staff to create patients.
7. Allow hospitals to create blood requests.
8. Prioritize urgent blood requests.
9. Automatically determine whether hospital stock is sufficient.
10. Allow blood banks to transfer blood to hospitals.
11. Allow hospitals to issue blood to patients.
12. Maintain complete transaction history.
13. Track blood-unit status and expiry.
14. Provide alerts for low stock and expiry.
15. Generate reports.
16. Persist application data using files.
17. Provide role-based login and authorization.

---

# 3. User Roles

The application contains four user-facing roles:

| Role | Purpose |
|---|---|
| Donor | Donates blood to the blood bank |
| Patient | Views personal/request information |
| Hospital | Manages patients, local inventory and blood requests |
| Blood Bank | Manages donations, testing, inventory and hospital transfers |

Internally, the hospital role is represented by `HospitalStaff`, while the blood-bank role is represented by `BloodBankAdmin`.

The user does **not** need to think of Hospital Staff and Hospital as two separate login roles.

---

# 4. Core System Architecture

The system follows:

```text
PERSON
│
├── DONOR
├── PATIENT
└── STAFF
     │
     ├── HOSPITAL STAFF
     └── BLOOD BANK ADMIN
```

Organizations:

```text
ORGANIZATION
│
├── BLOOD BANK
└── HOSPITAL
```

Transactions:

```text
TRANSACTION
│
├── BLOOD DONATION
├── BLOOD REQUEST
└── BLOOD TRANSFER
```

Inventory:

```text
BLOOD BANK
     │
     └── Inventory
          └── BloodUnit[]

HOSPITAL
     │
     └── Inventory
          └── BloodUnit[]
```

---

# 5. Person

`Person` is an abstract base class.

## Attributes

```text
personId: String
name: String
age: int
gender: String
phoneNumber: String
address: String
```

## Methods

```text
getDetails(): String
updateDetails(): void
displayDetails(): void
```

## Responsibilities

`Person` stores information common to all people in the system.

The following classes inherit from it:

```text
Person
├── Donor
├── Patient
└── Staff
```

IDs must be unique.

Age and phone number must be validated before storing.

---

# 6. Staff

`Staff` is an abstract class extending `Person`.

## Attributes

```text
employeeId: String
facilityId: String
```

## Method

```text
displayStaffDetails(): void
```

`facilityId` identifies the facility where the staff member works.

For hospital staff:

```text
facilityId → Hospital ID
```

For blood-bank administrators:

```text
facilityId → Blood Bank ID
```

---

# 7. HospitalStaff

`HospitalStaff` extends `Staff`.

## Attribute

```text
department: String
```

## Methods

```text
createPatient(): void
requestBlood(): void
issueBlood(): void
```

## Responsibilities

Hospital staff can:

- Create patients.
- Create blood requests.
- View hospital inventory.
- Check blood availability.
- Issue blood to patients.
- Request blood from the blood bank.
- View request history.

The hospital staff member can only operate on the hospital associated with their `facilityId`.

---

# 8. BloodBankAdmin

`BloodBankAdmin` extends `Staff`.

## Attribute

```text
adminLevel: String
```

## Methods

```text
registerDonation(): void
initiateLabTest(): boolean
approveRequest(): void
dispatchTransfer(): void
```

## Responsibilities

Blood-bank administrators manage:

- Blood donation registration.
- Laboratory testing.
- Blood-unit approval/rejection.
- Blood-bank inventory.
- Hospital blood requests.
- Transfer approval.
- Blood dispatch.

---

# 9. Donor

`Donor` extends `Person`.

## Attributes

```text
bloodGroup: String
haemoglobin: double
weight: double
lastDonationDate: String
eligible: boolean
```

## Required donor functionality

The donor's main operation is:

```text
donate()
```

The donor profile must provide access to:

```text
viewProfile()
```

The profile itself should display:

- Personal information.
- Blood group.
- Weight.
- Haemoglobin.
- Last donation date.
- Eligibility/status information.
- Donation history.

Therefore, a separate standalone:

```text
viewDonationHistory()
```

operation is **not required** in the updated design.

Similarly:

```text
checkEligibility()
```

is **not part of the donor-facing workflow**.

Eligibility is handled as part of the donation/laboratory process.

---

# 10. Donor Donation Workflow

The correct donation sequence is:

```text
DONOR
  │
  │ donate()
  ▼
Blood Collection
  │
  ▼
Blood Unit Created
  │
  ▼
Lab Testing
  │
  ├── FAIL → Reject/Discard
  │
  └── PASS
       │
       ▼
BloodBankAdmin.registerDonation()
       │
       ▼
Blood Bank Inventory
```

## Detailed workflow

### Step 1 — Donor initiates donation

The donor logs in and chooses:

```text
Donate Blood
```

This invokes:

```text
Donor.donate()
```

---

### Step 2 — Blood is collected

A donation transaction is created.

The system records:

- Donor ID
- Blood group
- Quantity
- Donation date
- Transaction ID

---

### Step 3 — Blood unit enters testing

The collected blood must **not immediately become usable blood-bank inventory**.

The unit is marked as awaiting laboratory testing.

Example:

```text
Status = PENDING_TEST
```

---

### Step 4 — Laboratory test

The blood-bank administrator initiates laboratory testing:

```text
BloodBankAdmin.initiateLabTest()
```

The result is:

```text
PASS
```

or

```text
FAIL
```

---

### Step 5 — Failed test

If testing fails:

```text
Blood Unit
    ↓
TEST FAILED
    ↓
REJECTED / DISCARDED
```

The blood must never enter usable inventory.

---

### Step 6 — Passed test

If testing passes:

```text
Blood Unit
    ↓
TEST PASSED
    ↓
BloodBankAdmin.registerDonation()
    ↓
Blood Bank Inventory
```

Only after successful testing does:

```text
registerDonation()
```

complete the registration of the usable donation.

---

# 11. Patient

`Patient` extends `Person`.

## Attributes

```text
bloodGroup: String
disease: String
doctorName: String
unitsRequired: int
hospitalId: String
```

## Method

```text
viewRequestStatus(): void
```

Patients are created by hospital staff.

Patients belong to exactly one hospital.

---

# 12. Patient Workflow

A patient does not directly request blood from the blood bank.

The workflow is:

```text
Hospital Staff
      │
      ▼
Create Patient
      │
      ▼
Patient requires blood
      │
      ▼
Hospital creates BloodRequest
      │
      ▼
Hospital Local Inventory
      │
      ├── Enough blood
      │      ↓
      │   Issue to Patient
      │
      └── Not enough
             ↓
       Request Blood Bank
             ↓
       Blood Transfer
             ↓
       Hospital Inventory
             ↓
       Issue to Patient
```

The patient login is primarily for viewing:

- Profile
- Request status
- Request history
- Relevant blood-request information

---

# 13. Organization

`Organization` is an abstract class.

## Attributes

```text
organizationId: String
organizationName: String
address: String
contactNumber: String
```

## Methods

```text
displayOrganization(): void
updateOrganization(): void
```

Two organizations extend it:

```text
Organization
├── BloodBank
└── Hospital
```

---

# 14. BloodBank

`BloodBank` extends `Organization`.

## Attributes

```text
managerName: String
inventory: Inventory
```

## Methods

```text
addBloodUnit(): void
removeBloodUnit(): void
transferBlood(): void
viewInventory(): void
```

The blood bank maintains the **central blood inventory**.

Its inventory is the primary source from which hospitals receive blood.

---

# 15. Hospital

`Hospital` extends `Organization`.

## Attributes

```text
hospitalType: String
emergencyContact: String
inventory: Inventory
staffs: HospitalStaff[]
```

## Methods

```text
sendBloodRequest(): void
viewRequestHistory(): void
viewInventory(): void
```

Each hospital has its **own independent local blood inventory**.

Example:

```text
Blood Bank
├── O+ : 50
├── A+ : 40
└── B+ : 30

Hospital A
├── O+ : 5
├── A+ : 2
└── B+ : 4

Hospital B
├── O+ : 8
├── A+ : 6
└── B+ : 1
```

Hospital A cannot use Hospital B's inventory directly.

If Hospital A needs additional blood, it requests it from the blood bank.

---

# 16. Inventory

## Attributes

```text
bloodUnits: BloodUnit[]
totalStock: int
```

## Methods

```text
addBloodUnit(): void
removeBloodUnit(): void
searchBloodGroup(): BloodUnit[]
checkLowStock(): void
displayInventory(): void
```

Inventory is used by both:

```text
BloodBank
Hospital
```

Every inventory contains actual `BloodUnit` records.

---

# 17. BloodUnit

## Attributes

```text
bloodUnitId: String
bloodGroup: String
quantity: int
collectionDate: String
expiryDate: String
status: String
```

## Methods

```text
runLabTests(): boolean
isExpired(): boolean
displayBloodUnit(): void
```

Each blood unit must have a unique ID.

Example:

```text
BU1001
Blood Group: O+
Quantity: 1
Collection Date: 06-10-2026
Expiry Date: ...
Status: AVAILABLE
```

---

# 18. Blood Unit Lifecycle

A blood unit follows:

```text
COLLECTED
    ↓
PENDING_TEST
    ↓
LAB TEST
   / \
 FAIL PASS
  ↓     ↓
REJECTED TEST_PASSED
          ↓
       AVAILABLE
          ↓
      TRANSFERRED
          ↓
       HOSPITAL
          ↓
        ISSUED
```

Expired units become unusable.

Possible statuses include:

```text
COLLECTED
PENDING_TEST
TEST_PASSED
TEST_FAILED
AVAILABLE
TRANSFERRED
ISSUED
EXPIRED
REJECTED
```

---

# 19. Transaction

`Transaction` is an abstract base class.

## Attributes

```text
transactionId: String
transactionDate: String
status: String
```

## Method

```text
displayTransaction(): void
```

The transaction hierarchy is:

```text
Transaction
├── BloodDonation
├── BloodRequest
└── BloodTransfer
```

Every transaction receives a unique transaction ID.

---

# 20. BloodDonation

`BloodDonation` extends `Transaction`.

## Attributes

```text
donorId: String
bloodGroup: String
quantity: int
```

## Methods

```text
recordDonation(): void
updateInventory(): void
```

The donation transaction records the movement of blood from donor collection into the blood-bank processing workflow.

Important:

```text
Donation ≠ immediately usable inventory
```

The blood must first pass laboratory testing.

---

# 21. Blood Request

`BloodRequest` extends `Transaction`.

## Attributes

```text
patientId: String
hospitalId: String
bloodGroup: String
unitsRequested: int
urgency: String
```

## Methods

```text
checkAvailability(): boolean
approveRequest(): void
rejectRequest(): void
priorityDispatch(): void
```

A request is created by hospital staff for a patient.

---

# 22. Blood Request Priority

Priority belongs to the **BloodRequest**, not permanently to the patient.

The request contains:

```text
urgency: String
```

The system uses a priority `HashMap`.

Example:

```java
HashMap<String, Integer> priorityMap;
```

with:

```text
CRITICAL → 1
HIGH     → 2
MEDIUM   → 3
LOW      → 4
```

Lower numerical value means higher priority.

The hospital staff determines the urgency of the request based on the patient's requirement.

The system then uses the priority when processing pending requests.

---

# 23. Hospital Blood Request Workflow

## Step 1 — Patient is created

Hospital staff creates the patient.

Example:

```text
Patient ID: P1001
Blood Group: O+
Disease: Severe blood loss
Doctor: Dr. Kumar
Units Required: 4
Hospital: H001
```

---

## Step 2 — Hospital creates request

Hospital staff creates:

```text
BloodRequest
```

with:

```text
Patient ID
Hospital ID
Blood Group
Units Required
Urgency
```

---

## Step 3 — Check local hospital inventory

The system first checks the hospital's own inventory.

Example:

```text
Required = 4 O+
Hospital stock = 6 O+
```

Result:

```text
AVAILABLE
```

No blood-bank transfer is required.

---

# 24. Local Stock Sufficient

If:

```text
Required = 4
Available = 6
```

then:

```text
Hospital Inventory
6 → 2
```

The hospital can issue blood directly to the patient.

Flow:

```text
Patient Request
      ↓
Hospital Inventory
      ↓
Issue Blood
      ↓
Patient
```

The blood bank is not involved.

---

# 25. Local Stock Insufficient

Example:

```text
Patient requires = 5 O+
Hospital has = 2 O+
```

The system determines:

```text
Shortage = 5 - 2
        = 3 units
```

The hospital requests:

```text
3 O+
```

from the blood bank.

Flow:

```text
Patient
   ↓
Hospital Request
   ↓
Check Local Stock
   ↓
2 available / 3 shortage
   ↓
Blood Bank Request
   ↓
Blood Bank
   ↓
Transfer 3 units
   ↓
Hospital Inventory
   ↓
Issue 5 units to patient
```

The blood bank does **not** directly issue the blood to the patient.

---

# 26. Partial Availability

If the hospital has some of the required blood, the system can use the available local stock and request the shortage.

Example:

```text
Required = 5
Hospital = 2
Shortage = 3
```

The hospital can:

```text
Use 2 local units
+
Request 3 from Blood Bank
```

This prevents unnecessary transfers of blood that is already available locally.

---

# 27. Blood Transfer

`BloodTransfer` extends `Transaction`.

## Attributes

```text
sourceFacilityId: String
destinationFacilityId: String
bloodGroup: String
unitsTransferred: int
```

## Methods

```text
transferUnits(): void
recordTransfer(): void
```

The normal transfer direction is:

```text
Blood Bank
     ↓
Hospital
```

The transfer must record:

- Source facility
- Destination facility
- Blood group
- Quantity
- Transaction ID
- Date
- Status

---

# 28. Blood Transfer Workflow

```text
Hospital detects shortage
          ↓
Creates blood request
          ↓
Blood Bank receives request
          ↓
Admin checks central inventory
          ↓
Approve / Reject
          ↓
Select valid blood units
          ↓
Dispatch transfer
          ↓
Blood Bank inventory decreases
          ↓
Hospital inventory increases
          ↓
Transfer recorded
          ↓
Hospital can issue blood
```

Only usable blood can be transferred.

Expired or failed units must never be transferred.

---

# 29. Blood Bank Request Approval

When a hospital requests blood from the blood bank:

```text
REQUEST
   ↓
PENDING
   ↓
BloodBankAdmin reviews
   ↓
 ┌─────────────┐
 │             │
APPROVE       REJECT
 │             │
 ▼             ▼
TRANSFER      END
```

The administrator checks:

- Hospital validity
- Patient validity
- Blood group
- Required quantity
- Request urgency
- Central inventory availability
- Blood-unit validity

---

# 30. Issue Blood to Patient

Blood is issued **from the hospital inventory**.

Workflow:

```text
Hospital Staff
      ↓
Select Patient
      ↓
Select Blood Request
      ↓
Check Request
      ↓
Check Hospital Inventory
      ↓
Check Blood Unit Status
      ↓
Check Expiry
      ↓
Issue Blood
      ↓
Update Inventory
      ↓
Update Request Status
```

Once issued:

```text
BloodUnit.status = ISSUED
```

The inventory quantity is reduced.

---

# 31. Complete End-to-End Example

Suppose:

```text
Patient needs 5 O+
Hospital has 2 O+
Blood Bank has 20 O+
```

### Step 1

Hospital creates patient.

### Step 2

Hospital creates:

```text
BloodRequest
O+
5 units
HIGH urgency
```

### Step 3

Hospital checks local inventory:

```text
2 available
```

### Step 4

Shortage:

```text
5 - 2 = 3
```

### Step 5

Hospital requests 3 units from blood bank.

### Step 6

Blood-bank admin approves.

### Step 7

Blood bank transfers 3 units.

```text
Blood Bank: 20 → 17
Hospital:   2 → 5
```

### Step 8

Hospital issues 5 units to patient.

```text
Hospital: 5 → 0
```

### Step 9

Request becomes:

```text
FULFILLED
```

All transactions are stored.

---

# 32. LoginManager

## Attributes

```text
username: String
password: String
```

## Methods

```text
authenticateUser(): boolean
changePassword(): void
logoutUser(): void
```

Supported roles:

```text
DONOR
PATIENT
HOSPITAL
BLOOD_BANK
```

Internally:

```text
HOSPITAL → HospitalStaff
BLOOD_BANK → BloodBankAdmin
```

---

# 33. Authentication Flow

```text
START
  ↓
LOGIN
  ↓
Enter username/password
  ↓
Authenticate
  ↓
 ┌───────────────┐
 │               │
SUCCESS         FAILURE
 │               │
 ▼               ▼
Role Menu      Error
```

After login, the system must only expose functions allowed for that role.

---

# 34. Donor Menu

The donor dashboard should contain:

```text
1. View Profile
2. Update Profile
3. Donate Blood
4. Change Password
5. Logout
```

The profile displays donation history as part of the donor's information.

There is no need for a separate:

```text
Check Eligibility
```

menu operation.

---

# 35. Patient Menu

```text
1. View Profile
2. Update Profile
3. View Request Status
4. View Request History
5. Change Password
6. Logout
```

Patients cannot:

- create blood requests;
- modify hospital inventory;
- approve requests;
- transfer blood.

---

# 36. Hospital Menu

```text
1. Create Patient
2. View Patients
3. Create Blood Request
4. Create Emergency/Urgent Request
5. Check Local Inventory
6. View Inventory
7. Issue Blood
8. Request Blood from Blood Bank
9. View Request History
10. View Transfer History
11. Change Password
12. Logout
```

Hospital staff can only access their own hospital's inventory.

---

# 37. Blood Bank Menu

```text
1. View Pending Donations
2. Initiate Lab Test
3. Register Approved Donation
4. View Blood Bank Inventory
5. View Hospital Requests
6. Approve Request
7. Reject Request
8. Dispatch Blood Transfer
9. View Transfer History
10. View Alerts
11. Generate Reports
12. Change Password
13. Logout
```

---

# 38. AlertManager

## Attributes

```text
lowStockThreshold: int
expiryAlertDays: int
```

## Methods

```text
checkLowStock(): void
checkExpiry(): void
generateAlert(): void
```

The system should generate alerts for:

### Low stock

Example:

```text
WARNING:
O+ blood stock at Hospital H001 is below threshold.
```

### Expiry

Example:

```text
WARNING:
Blood Unit BU1005 expires soon.
```

### Expired blood

Expired blood must immediately become unavailable for issue/transfer.

---

# 39. ReportGenerator

## Attributes

```text
reportTitle: String
generatedDate: String
```

## Methods

```text
generateInventoryReport(): void
generateDonationReport(): void
generateRequestReport(): void
exportReport(): void
```

### Inventory Report

Contains:

- Facility
- Blood group
- Available quantity
- Blood-unit IDs
- Expiry information
- Low-stock status

### Donation Report

Contains:

- Donation ID
- Donor ID
- Blood group
- Quantity
- Date
- Lab result
- Registration status

### Request Report

Contains:

- Request ID
- Patient ID
- Hospital ID
- Blood group
- Units requested
- Urgency
- Status
- Date

Reports can be exported using a suitable file format such as CSV/TXT.

---

# 40. FileManager

## Attributes

```text
filePath: String
```

## Methods

```text
saveData(): void
loadData(): void
appendData(): void
deleteRecord(): void
```

The application must persist data between executions.

Data that should be persisted includes:

- Users
- Donors
- Patients
- Staff
- Hospitals
- Blood banks
- Blood units
- Inventories
- Donations
- Requests
- Transfers
- Relevant history

The file-handling logic should remain centralized inside `FileManager`.

---

# 41. Validation

## Attributes

```text
bloodGroups: String[]
```

## Methods

```text
validateBloodGroup(): boolean
validatePhoneNumber(): boolean
validateAge(): boolean
validateDate(): boolean
```

Supported blood groups:

```text
A+
A-
B+
B-
AB+
AB-
O+
O-
```

Validation must prevent:

- Invalid blood groups
- Invalid phone numbers
- Invalid ages
- Invalid dates
- Negative quantities
- Empty mandatory fields
- Invalid IDs
- Invalid facility references

---

# 42. Data Relationships

## Inheritance

```text
Person
├── Staff
│    ├── HospitalStaff
│    └── BloodBankAdmin
├── Donor
└── Patient
```

```text
Organization
├── BloodBank
└── Hospital
```

```text
Transaction
├── BloodDonation
├── BloodRequest
└── BloodTransfer
```

## Composition / Association

```text
BloodBank
    └── Inventory
          └── BloodUnit[]

Hospital
    ├── Inventory
    │     └── BloodUnit[]
    │
    └── HospitalStaff[]
```

```text
BloodDonation
    └── Donor

BloodRequest
    ├── Patient
    └── Hospital

BloodTransfer
    ├── Source Facility
    └── Destination Facility
```

---

# 43. Complete Business Workflows

## Workflow A — Donation

```text
Donor Login
    ↓
View Profile
    ↓
Donate
    ↓
Blood Collected
    ↓
Blood Unit Created
    ↓
Pending Lab Test
    ↓
Blood Bank Admin Initiates Test
    ↓
 ┌───────────────┐
 │               │
 FAIL           PASS
 │               │
 ▼               ▼
Reject       Register Donation
                 ↓
            Add to Inventory
```

---

## Workflow B — Patient Blood Request

```text
Hospital Login
     ↓
Create Patient
     ↓
Patient Requires Blood
     ↓
Create Blood Request
     ↓
Assign Urgency
     ↓
Check Local Inventory
     ↓
 ┌──────────────────────┐
 │                      │
SUFFICIENT           INSUFFICIENT
 │                      │
 ▼                      ▼
Issue Locally       Request Blood Bank
                        ↓
                  Admin Approval
                        ↓
                     Transfer
                        ↓
                 Hospital Inventory
                        ↓
                   Issue to Patient
```

---

## Workflow C — Blood Transfer

```text
Hospital Request
       ↓
Blood Bank Admin
       ↓
Check Central Stock
       ↓
Approve
       ↓
Select Blood Units
       ↓
Transfer
       ↓
Blood Bank Stock -
       ↓
Hospital Stock +
       ↓
Record Transaction
```

---

## Workflow D — Patient Issue

```text
Approved Request
       ↓
Hospital Staff
       ↓
Check Local Stock
       ↓
Check Blood Unit
       ↓
Check Expiry
       ↓
Issue Blood
       ↓
Hospital Inventory -
       ↓
Request Updated
       ↓
Patient Receives Blood
```

---

# 44. Priority-Based Dispatch

Pending requests are maintained according to urgency.

Example:

```text
HashMap<String, Integer>

CRITICAL → 1
HIGH     → 2
MEDIUM   → 3
LOW      → 4
```

If the blood bank receives:

```text
Request A → LOW
Request B → CRITICAL
Request C → HIGH
```

the processing order should be:

```text
B → C → A
```

Priority must not bypass basic validity checks.

A request still requires valid:

- Patient
- Hospital
- Blood group
- Quantity
- Blood availability

---

# 45. Inventory Rules

The system must always maintain correct inventory.

### Rule 1

Inventory cannot become negative.

### Rule 2

Rejected blood cannot be used.

### Rule 3

Expired blood cannot be used.

### Rule 4

Untested blood cannot be used.

### Rule 5

Blood transferred from the blood bank must be removed from blood-bank usable stock.

### Rule 6

Transferred blood must be added to the destination hospital.

### Rule 7

Blood issued to a patient must be removed from hospital stock.

---

# 46. Error Handling

The application must handle:

- Invalid login
- Wrong password
- Duplicate IDs
- Invalid blood group
- Invalid age
- Invalid phone number
- Invalid date
- Invalid quantity
- Missing patient
- Missing hospital
- Missing donor
- Missing blood bank
- Insufficient stock
- Expired blood
- Failed laboratory test
- Unauthorized operation
- Invalid menu choice
- File loading errors
- File saving errors
- Report generation errors

Normal user errors must never crash the application.

---

# 47. Object-Oriented Requirements

The implementation must clearly demonstrate:

### Abstraction

Use:

```text
abstract Person
abstract Staff
abstract Organization
abstract Transaction
```

### Inheritance

Use the inheritance relationships represented in the class diagram.

### Encapsulation

Keep attributes private/protected as appropriate and expose controlled operations.

### Polymorphism

Use parent references where appropriate:

```java
Person person;
Staff staff;
Transaction transaction;
Organization organization;
```

### Composition

For example:

```text
Hospital → Inventory
BloodBank → Inventory
Inventory → BloodUnit[]
```

---

# 48. Suggested Java Project Structure

```text
src/
│
├── model/
│   ├── Person.java
│   ├── Staff.java
│   ├── HospitalStaff.java
│   ├── BloodBankAdmin.java
│   ├── Donor.java
│   ├── Patient.java
│   ├── Organization.java
│   ├── Hospital.java
│   ├── BloodBank.java
│   ├── Inventory.java
│   ├── BloodUnit.java
│   ├── Transaction.java
│   ├── BloodDonation.java
│   ├── BloodRequest.java
│   └── BloodTransfer.java
│
├── manager/
│   ├── LoginManager.java
│   ├── AlertManager.java
│   └── ReportGenerator.java
│
├── utility/
│   ├── Validation.java
│   └── FileManager.java
│
├── ui/
│   └── Menu.java
│
└── Main.java
```

The exact package structure may change, but responsibilities should remain separated.

---

# 49. Main Application Flow

`Main` contains:

```text
main(args)
initializeSystem()
closeApplication()
```

Startup:

```text
Application Start
       ↓
Initialize System
       ↓
Load Saved Data
       ↓
Initialize Blood Banks
       ↓
Initialize Hospitals
       ↓
Initialize Users
       ↓
Display Main Menu
```

Shutdown:

```text
Logout
  ↓
Close Application
  ↓
Save Data
  ↓
Close Files
  ↓
Exit
```

---

# 50. Main Menu

```text
================================
     BLOOD BANK MANAGEMENT
================================

1. Login
2. Exit

Enter choice:
```

After authentication, the system redirects the user according to role.

---

# 51. Security and Authorization

Authentication alone is not sufficient.

The application must also verify authorization.

Examples:

```text
DONOR
  ✗ Cannot approve blood requests

PATIENT
  ✗ Cannot modify inventory

HOSPITAL
  ✗ Cannot dispatch blood-bank transfers

BLOOD BANK
  ✓ Can approve and dispatch transfers
```

A hospital staff member can only access the hospital associated with their account.

A blood-bank admin can only perform administrative operations permitted by their role/admin level.

---

# 52. Persistence

The system must retain data after application shutdown.

At minimum:

```text
Users
People
Donors
Patients
Staff
Hospitals
Blood Banks
Blood Units
Inventories
Donations
Requests
Transfers
```

Startup:

```text
initializeSystem()
      ↓
FileManager.loadData()
      ↓
Restore objects
      ↓
Start application
```

Shutdown:

```text
closeApplication()
      ↓
FileManager.saveData()
      ↓
Exit
```

---

# 53. Testing Requirements

Important functionality must be tested.

## Donor tests

- Donor creation
- Profile display
- Donation creation
- Donation history
- Lab testing
- Failed donation
- Successful donation

## Inventory tests

- Add blood
- Remove blood
- Search blood group
- Insufficient stock
- Expiry
- Low stock

## Hospital tests

- Patient creation
- Blood request
- Local stock checking
- Blood issuing
- Blood-bank restocking

## Blood-bank tests

- Donation registration
- Lab testing
- Request approval
- Request rejection
- Transfer

## Authentication tests

- Valid login
- Invalid login
- Role authorization
- Password change
- Logout

---

# 54. Acceptance Criteria

The project is considered complete when:

- [ ] All classes from the updated class diagram exist.
- [ ] All diagram attributes are implemented.
- [ ] All diagram methods are implemented.
- [ ] Correct inheritance is implemented.
- [ ] Donor can log in.
- [ ] Donor can view profile.
- [ ] Donation history appears through the profile.
- [ ] Donor can initiate donation.
- [ ] Blood undergoes laboratory testing.
- [ ] Failed blood cannot enter usable inventory.
- [ ] Successful blood can be registered by the blood-bank admin.
- [ ] Blood bank maintains central inventory.
- [ ] Each hospital has separate local inventory.
- [ ] Hospital can create patients.
- [ ] Hospital can create blood requests.
- [ ] Requests have urgency/priority.
- [ ] Hospital checks local inventory first.
- [ ] Shortage can be requested from blood bank.
- [ ] Blood bank can approve/reject requests.
- [ ] Blood bank can transfer blood.
- [ ] Hospital receives transferred blood.
- [ ] Hospital can issue blood to patients.
- [ ] Blood inventory updates correctly.
- [ ] Expired blood cannot be issued.
- [ ] Low-stock alerts work.
- [ ] Expiry alerts work.
- [ ] Reports can be generated.
- [ ] Data persists after restart.
- [ ] Role-based authorization works.
- [ ] Invalid input does not crash the program.
- [ ] Complete workflows work end-to-end.

---

# 55. Final System Flow

The complete system can be summarized as:

```text
                         ┌─────────────┐
                         │    DONOR    │
                         └──────┬──────┘
                                │
                            donate()
                                │
                                ▼
                       ┌────────────────┐
                       │ BLOOD COLLECTED│
                       └───────┬────────┘
                               │
                               ▼
                        LABORATORY TEST
                         /            \
                      FAIL            PASS
                       │                │
                       ▼                ▼
                   REJECTED      REGISTER DONATION
                                      │
                                      ▼
                              ┌────────────────┐
                              │   BLOOD BANK   │
                              │ CENTRAL STOCK  │
                              └───────┬────────┘
                                      │
                              Blood Transfer
                                      │
                                      ▼
                              ┌───────────────┐
                              │    HOSPITAL   │
                              │ LOCAL INVENTORY│
                              └───────┬───────┘
                                      │
                               Blood Request
                                      │
                                      ▼
                                  PATIENT
                                      │
                                  Blood Issue
                                      │
                                      ▼
                              Patient Treatment
```

The fundamental rule of the system is:

> **The blood bank manages the central blood supply, while each hospital manages its own local stock and is responsible for issuing blood to its patients.**

Therefore, the complete operational chain is:

```text
DONOR
  ↓
DONATION
  ↓
LAB TEST
  ↓
BLOOD BANK
  ↓
HOSPITAL TRANSFER
  ↓
HOSPITAL INVENTORY
  ↓
PRIORITY-BASED REQUEST
  ↓
PATIENT
```

---

# 56. Final Implementation Principle

The project should not be implemented as a collection of independent classes.

Every operation must cause a real state change.

For example:

```text
Donor.donate()
        ↓
BloodDonation created
        ↓
BloodUnit created
        ↓
Lab test
        ↓
Admin registers successful donation
        ↓
BloodBank.inventory updated
```

Similarly:

```text
Hospital requests blood
        ↓
BloodRequest created
        ↓
Local inventory checked
        ↓
Shortage identified
        ↓
BloodBank approves
        ↓
BloodTransfer created
        ↓
BloodBank inventory decreases
        ↓
Hospital inventory increases
        ↓
Hospital issues blood
        ↓
Hospital inventory decreases
        ↓
Patient request completed
```

Every transaction, inventory change and status change must therefore be reflected consistently throughout the application.

## Definition of Done

The final application must be:

**Runnable → Persistent → Role-based → Object-oriented → Functionally connected → Validated → Tested**

It should function as a complete academic-grade Java Blood Bank Management System rather than a set of disconnected classes.