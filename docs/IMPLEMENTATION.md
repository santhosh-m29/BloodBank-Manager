# Implementation and requirements coverage

The current PRD and `JAVA.drawio` supplied in the workspace are the design references. They are preserved without modifying their contents or checking their acceptance boxes on the user's behalf.

## Requirements mapping

| PRD sections | Implementation | Verification |
| --- | --- | --- |
| 1–8, 11, 13–17, 19–22, 27, 42, 47–48 | All 15 core model classes, four abstract bases, typed inheritance, facility composition, validated attributes, permanent IDs | Compilation; model validation; authorization and persistence tests |
| 9–10, 18, 20, 34 | Donor initiation; profile with history; collection → pending test → pass/fail → separate registration | `donationAndAuthentication` |
| 12, 23–26, 30–31 | Hospital creates patient; local reservation first; shortage only; ready → hospital issue → fulfilled | `endToEndAndPersistence` |
| 21–22, 28–29, 44–45 | Four-level priority HashMap; bank approval; reservation; exact-unit transfer; rejection releases reservations | `priorityAndReservations` |
| 32–37, 50–51 | Four role menus; hashed credentials; old-password verification; logout; service-level role, facility, and admin-level checks | `donationAndAuthentication`, `authorizationAndValidation`, console checks |
| 38–39 | Low-stock and expiry alerts, facility inventory/unit detail, donation lab/registration details, request and transfer reports | `reportsAndConsole` |
| 40–41, 46, 49, 52 | Centralized file storage, strict input validation, atomic snapshots, rollback, process lock, no reseed on corruption, EOF handling | `expiryAndFailure`, `authorizationAndValidation`, `reportsAndConsole` |
| 43, 53–56 | Connected workflows use the same service API in console and tests; state changes and audit entries save together | Complete regression suite |

The suite initially covers 128 assertions. Run `build.ps1 -Test` for the current assertion count and result.

## State transitions

```text
Donation: PENDING_TEST -> TEST_PASSED -> REGISTERED
                      -> REJECTED

Blood unit: PENDING_TEST -> TEST_PASSED -> AVAILABLE -> TRANSFERRED -> ISSUED
                        -> REJECTED
            Unissued, nonrejected stock becomes EXPIRED at expiry.

Request: PENDING -> READY -> FULFILLED                 (local stock sufficient)
         PENDING -> APPROVED -> READY -> FULFILLED     (shortage transferred)
         PENDING / APPROVED -> REJECTED
         READY -> PENDING                            (recheck after local expiry)

Transfer: PENDING -> DISPATCHED                       (exact units moved)
```

Collection and the initial `PENDING_TEST` state are one atomic operation. Units awaiting testing are stored as quarantined records in the bank inventory, but every stock calculation excludes them. Each lab entry covers all units in that donation and records its result reference and staff actor. `TEST_FAILED` is represented by lab result `FAIL` and terminal unit/donation status `REJECTED`.

Reservations are separate from lifecycle statuses, so `AVAILABLE`/`TRANSFERRED` describe physical state while `reservedFor` prevents competing allocations. Reports distinguish total usable stock from unreserved available stock. No stock quantity is stored independently of unit records.

## Transaction boundary

`BloodBankService` authenticates the actor, copies the current state, validates the operation against that copy, applies all inventory/history changes, appends an audit event, checks relationships, saves one atomic snapshot, and only then replaces its authoritative in-memory state. Errors before the final replacement cannot leave a half-transfer, partially issued request, or misleading success. Returned records and inventory views do not expose the authoritative state.

The application holds a data-directory process lock. Console operations are sequential. It is not intended for concurrent network requests. Password hashing uses the JDK's PBKDF2-HMAC-SHA256 with random 16-byte salts, 120,000 iterations, and 256-bit keys. Snapshot deserialization is filtered and schema-versioned.

## Diagram API interpretation

The diagram's model operations are implemented with explicit inputs rather than hidden console reads or global `Main` state. In particular:

- `Donor.donate`, `HospitalStaff` operations, and `BloodBankAdmin` operations delegate to an explicitly supplied `BloodBankService`; the service owns authentication and persistence.
- `BloodUnit.runLabTests(boolean)` requires a recorded result. A parameterless routine cannot determine actual laboratory safety.
- Availability, expiry, registration, transfers, and alerts accept an explicit date. Production uses the local system clock; tests use a controlled clock.
- `BloodRequest.priorityDispatch()` provides the HashMap priority used by service scheduling. An emergency subtype and stock-threshold override are deliberately absent from the updated hierarchy.
- `Inventory.totalStock` is calculated from units, not stored as a second mutable counter.
- Console and report operations use returned strings/collections; convenience display methods remain on the main models.
- `FileManager` saves/restores the whole aggregate instead of rewriting independent CSV files. Append-only audit events are part of each snapshot. Historical donation, transfer, and issue records are retained rather than exposed through a generic destructive record-deletion menu.

These signatures make the operations testable while keeping the responsibilities and inheritance in the updated design. Persistence, credentials, IDs, and immutable clinical associations have no unrestricted public setters.

## Test scenarios

- Valid/invalid login, failed-login session clearing, old-password verification, logout, persisted password change, absence of plaintext passwords.
- Donor profile/history, collection, interval validation, pending units, lab pass, separate registration, repeat registration/test rejection, lab failure.
- PRD arithmetic: bank 20/hospital 2; request 5; transfer exactly 3; bank 17/hospital 5; issue 5; hospital 0; restart and patient visibility.
- Sufficient local stock with no transfer; double dispatch/issue rejection; preserved unit IDs and issued history.
- CRITICAL/HIGH/LOW ordering, reservations, release on rejection, insufficient emergency stock, rollback on rejected approval.
- Cross-hospital patient/request boundaries, cross-bank admin boundaries, patient restrictions, read-only and standard admin limits.
- Patient creation/login, duplicate IDs, invalid age/phone/blood group/quantity/urgency/date, NaN measurements, commas in profile fields.
- Expiry on the exact boundary, expired reserved central stock, expired local stock and restock routing, low stock and near-expiry alerts.
- Failed filesystem writes with memory rollback, corrupt snapshot preservation, exclusive lock, reports, invalid console input, and EOF.

## Known boundaries

- Legacy flat files remain readable as historical source files but are not silently converted to the stricter version-2 schema. See README for why automatic association would be ambiguous.
- The first launch creates a local demonstration dataset. There is no database, web interface, network service, real laboratory integration, component compatibility matching, or clinical certification.
- No external library is required. The build targets Java 17; a Java 17-compatible JDK is needed to execute it.
