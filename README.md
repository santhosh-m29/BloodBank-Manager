# Blood Bank Management System (BBMS)

A complete, runnable, and robust academic-grade Blood Bank Management System implemented in Java. The application is strictly based on the specifications of the `BLOOD_BANK_SYSTEM_PRD.md` and the `JAVA.drawio` class diagram contract.

---

## 1. System Architecture

The project is structured in a clean layered architecture under the `bloodbank` package:

```text
src/
└── bloodbank/
    ├── model/
    │   ├── Person.java (abstract)
    │   ├── Staff.java (abstract extends Person)
    │   ├── HospitalStaff.java (extends Staff)
    │   ├── BloodBankAdmin.java (extends Staff)
    │   ├── Donor.java (extends Person)
    │   ├── Patient.java (extends Person)
    │   ├── Organization.java (abstract)
    │   ├── BloodBank.java (extends Organization)
    │   ├── Hospital.java (extends Organization)
    │   ├── Inventory.java (composed in BloodBank & Hospital)
    │   ├── BloodUnit.java
    │   ├── Transaction.java (abstract)
    │   ├── BloodDonation.java (extends Transaction)
    │   ├── BloodRequest.java (extends Transaction)
    │   ├── EmergencyRequest.java (extends BloodRequest)
    │   └── BloodTransfer.java (extends Transaction)
    │
    ├── service/ / util/ / ui/
    │   ├── LoginManager.java (multi-role authentication & session state)
    │   ├── AlertManager.java (expiry & low-stock check)
    │   ├── ReportGenerator.java (inventory, donation, and requests report generator & exporter)
    │   ├── FileManager.java (flat-file persistence layer mapping data/ files)
    │   ├── Validation.java (central validation rules)
    │   └── Menu.java (navigable console-based user interface)
    │
    ├── Main.java (entry point & system life cycle)
    └── TestFlow.java (automated integration test runner)
```

---

## 2. Default Seed & Demo Credentials

When the system is started for the first time, it automatically creates default seed data in the `data/` directory. You can log in with the following credentials to test different role dashboards:

| Role | Username | Password | Purpose / Dashboard Access |
| :--- | :--- | :--- | :--- |
| **Blood Bank Admin** | `admin` | `admin123` | Register donations, initiate lab tests, approve requests, dispatch transfers, view alerts, and generate reports. |
| **Hospital Staff (St. Jude)** | `hosp1` | `hosp123` | Register patients, submit blood requests, check local hospital stock, issue blood, request restock. |
| **Hospital Staff (Grace)** | `hosp2` | `hosp123` | Hospital staff console for Grace Clinic. |
| **Donor** | `donor` | `donor123` | Check eligibility, view donation history, update details, change password. |
| **Patient** | `patient` | `patient123` | View request status and history, update profile, change password. |

---

## 3. Setup and Run Instructions

### Prerequisites
- Java Development Kit (JDK) 8 or higher (e.g. Java 17).
- Terminal or PowerShell.

### Compilation
Compile all Java files into a output directory (`bin`):
```powershell
javac -d bin src/bloodbank/*.java
```

### Run the Interactive Application
To launch the interactive console UI:
```powershell
java -cp bin bloodbank.Main
```

### Run the Automated Integration Tests
To run the automated workflow integration test:
```powershell
java -cp bin bloodbank.TestFlow
```

---

## 4. Completed Workflows & Business Rules

### A. Donor Registration & Donation
- **Eligibility Check**: Automatically validates if the donor is between 18 and 65, weighs >= 50.0 kg, has hemoglobin >= 12.5 g/dL, and has a donation interval >= 90 days from the previous donation date.
- **Lab Testing**: When a donation is registered, a blood unit is created in `PENDING_TEST` status. Administrators can initiate screening tests for pathogens (HIV, Hepatitis B/C, Syphilis) to set the unit status to `AVAILABLE` (usable stock) or `TEST_FAILED` (discarded).

### B. Request, Transfer & Issue Flow
- **Standard Request**: Hospital staff can request blood for a patient. Status starts as `Pending`.
- **Emergency Request**: Priority requests with high/critical urgency levels bypass stock threshold restrictions using priority overrides.
- **Transfer Dispatch**: Admins can approve requests and dispatch transfers. This removes units from the central blood bank's inventory and moves them into the hospital's local inventory.
- **Issuing**: Hospital staff can issue units from local stock to patients only when approved.

### C. Alerts & Reports
- **AlertManager**: Automatically checks for expiry (near expiry within 7 days) and low-stock levels (below 10 units) across all facilities.
- **ReportGenerator**: Generates formatted tables for inventory stock, donation logs, requests, and transfers. Exports them directly as `.txt` files in the `reports/` folder.

### D. File Persistence
- All state changes are serialized cleanly to human-readable comma-separated flat files under the `data/` directory (`admin.txt`, `hospitalstaff.txt`, `donors.txt`, `patients.txt`, `hospitals.txt`, `bloodbanks.txt`, `bloodunits.txt`, `donations.txt`, `requests.txt`, `transfers.txt`) and survive application restart.
