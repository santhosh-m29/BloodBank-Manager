# Blood Bank Management System

A Java 17 console application implementing the donor → laboratory → blood bank → hospital → patient workflows in [BLOOD_BANK_SYSTEM_PRD.md](BLOOD_BANK_SYSTEM_PRD.md). No external libraries, database, or build-tool installation is required.

## Build, run, and test

Install a JDK **17 or later** and make `java` and `javac` available on `PATH`.

```powershell
# Windows PowerShell, from the project directory
.\build.ps1 -Test
.\build.ps1 -Run

# Optional isolated data and report directories
.\build.ps1 -Run -DataDirectory D:\BloodBankData -ReportDirectory D:\BloodBankReports
```

```sh
# macOS / Linux
sh build.sh test
sh build.sh run
```

The scripts recursively compile into `build/classes` with Java 17 compatibility and compiler warnings enabled. After building, run directly with:

```sh
java -cp build/classes bloodbank.Main
```

The regression suite uses isolated temporary directories, makes assertions about real service operations, and never initializes or writes the application data directory. A test failure returns a nonzero exit status.

## First launch

A missing version-2 snapshot creates a demonstration dataset with one bank, two hospitals, five accounts, and **empty inventory**. Blood enters usable inventory through donation, lab testing, and registration.

| Role | Username | Initial password | Facility |
| --- | --- | --- | --- |
| Blood Bank | `admin` | `admin123` | `BB001` |
| Hospital | `hosp1` | `hosp1234` | `HOSP001` |
| Hospital | `hosp2` | `hosp1234` | `HOSP002` |
| Donor | `donor` | `donor123` | Donor `DON001`, A+ |
| Patient | `patient` | `patient123` | Patient `PAT001`, HOSP001 |

Change these demonstration passwords through each role's menu. Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes. Interactive terminals conceal password entry when Java's console is available; IDE/piped input uses the shared input reader.

## Walk through the complete workflow

1. Log in as `donor`. Select **Donate Blood**, choose `BB001`, and record the collected quantity. Copy the donation ID. **View Profile** includes donation history and eligibility information.
2. Log in as `admin`. **Initiate Lab Test** records an explicit `PASS` or `FAIL` result and a laboratory reference/note. A passed donation remains unavailable until **Register Approved Donation**. A failed donation is rejected and cannot be registered.
3. Log in as `hosp1`. Create a patient or use `PAT001`. **Create Blood Request** accepts a quantity and `CRITICAL`, `HIGH`, `MEDIUM`, or `LOW` urgency. The application reserves available local stock and displays the remaining shortage.
4. If there is a shortage, select **Request Blood from Blood Bank**, enter the request ID, and select `BB001`.
5. As `admin`, review requests in priority order, **Approve Request**, then **Dispatch Blood Transfer**. Approval reserves central units; dispatch moves only the shortage into the correct hospital.
6. As `hosp1`, **Issue Blood** for the request. The hospital's reserved units become `ISSUED`, and the request becomes `FULFILLED`.
7. As `patient`, inspect request status/history. Restart the application to verify that all records remain.

When local stock is sufficient, the request becomes `READY` immediately and the hospital can issue without bank approval. Issuing always requires the complete requested quantity. The system combines local stock with a shortage transfer; it does not partially issue a patient's request.

## Structure

```text
src/bloodbank/
  Main.java                     startup, process lock, shutdown
  model/                        people, organizations, inventories, transactions
  service/
    BloodBankService.java       authorization and atomic business operations
    SystemState.java            connected aggregate and consistency checks
    LoginManager.java           authentication/session lifecycle
    AlertManager.java           facility-specific stock and expiry alerts
    ReportGenerator.java        inventory/donation/request/transfer reports
    DemoData.java               first-launch demonstration setup
  utility/
    Validation.java             input validation
    Passwords.java              password hashing
    FileManager.java            filtered snapshot loading and atomic saving
  ui/Menu.java                  role menus and a single console reader
tests/bloodbank/service/SystemTest.java  workflow and regression assertions
docs/IMPLEMENTATION.md           requirements coverage and design decisions
```

`Person`, `Staff`, `Organization`, and `Transaction` are abstract. Facilities own independent inventories. Each physical blood unit has quantity 1, a permanent unique ID, donation provenance, test result, collection/expiry dates, reservation, and eventual recipient. A donation of N units creates N records. Transfers preserve those IDs and dates. Issued and rejected records remain available for history and reporting.

## Operational rules

- Role checks and facility checks run in the service layer, not just the menus. Returned data is detached from authoritative state.
- Donors initiate donations. Hospital staff create patient accounts only in their own hospital. Blood-bank administrators can register donors; `SUPER` administrators can provision facilities and staff.
- `STANDARD` administrators perform blood-bank operations only for their assigned bank. `READ_ONLY` administrators can view their bank's records but cannot change inventory or transactions.
- Reservations prevent competing requests from counting the same stock. Selection uses earliest expiry first. Fulfillable pending requests with higher priority are processed before lower-priority requests for the same bank and blood group; equal priority uses creation order.
- There is no emergency override for unavailable, expired, rejected, or untested blood.
- Expiry is exclusive: a unit is unusable **on its expiry date**. Reads immediately exclude expired units; the next successful mutation persists their `EXPIRED` status.
- If local stock expires before issue, use **Request Blood from Blood Bank / Recheck Stock** to reserve replacements and route the new shortage. If approved central stock expires, reject that request and create a replacement.
- Donor eligibility uses the existing project's academic defaults: age 18–65, weight at least 50 kg, haemoglobin at least 12.5, and 90 days between collections. Shelf life is 42 days; inventory uses exact blood-group matching. These are demonstration policies, not a clinical decision engine. Laboratory results are entered by authorized staff, not inferred by software.
- Alerts show low unreserved stock below 10 units and expiry within 7 days. Staff can export facility-scoped TXT reports with unique filenames.

## Persistence and legacy files

New application data is stored in **`data/v2/system-v2.dat`**. Each successful operation saves the entire connected state to a temporary file, flushes it, and atomically replaces the previous snapshot. A save error rejects the operation and leaves in-memory state unchanged. Loading errors stop startup instead of reseeding over existing data. A process lock prevents two instances from opening the same directory. Empty stock is never replenished automatically on restart.

The original `data/*.txt` and `reports/*.txt` files are preserved unchanged as legacy records. **They are not automatically migrated into version 2.** The old format lacks donation-to-unit and request-to-transfer links, and its `COMPLETED` request status conflates transfer and patient fulfillment. Importing those records as current usable stock would require reconstructing facts the files do not contain. Use a new version-2 dataset for the corrected workflows; retain legacy files for historical reference. Stale compiled `bin/` classes have been removed; use `build/classes`.

Back up `system-v2.dat` while the application is closed. Restore by copying the backup into a closed application's data directory. The snapshot includes sensitive profile information and should be stored in an appropriately protected local directory. This is a single-process academic application with a versioned Java snapshot format, not a multi-user production deployment.

See [implementation and test coverage](docs/IMPLEMENTATION.md) for the mapping to the PRD and diagram.
