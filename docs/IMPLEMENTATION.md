# Implementation and requirements coverage

> **Implementation Status**: Current milestone implements approximately 50% of the proposed Blood Bank Management System. The current implementation covers authentication, donor registration, blood donation, laboratory validation, donation registration, blood-bank inventory, patient creation, blood-request creation, and basic local availability checking. Hospital blood transfer, reservation/fulfillment, blood issue, advanced alerts, reporting, and failure/recovery workflows are planned for Phase 2.

The current PRD ([BLOOD_BANK_SYSTEM_PRD.md](file:///D:/Repos/BloodBank-Manager/BLOOD_BANK_SYSTEM_PRD.md)) and `JAVA.drawio` supplied in the workspace describe the complete intended system. They are preserved without modifying their contents or checking their acceptance boxes on the user's behalf. This document defines the actual Java codebase milestone coverage for Phase 1 (~50% implementation) and clearly delineates the boundaries deferred to Phase 2.

## Milestone Scoping & Requirements Mapping

| PRD sections | Scope | Implementation Status | Verification |
| --- | --- | --- | --- |
| 1–8, 11, 13–17, 19–22, 27, 42, 47–48 | Phase 1 & 2 Core | All 15 core model classes, four abstract bases, typed inheritance, facility composition, validated attributes, permanent IDs | Compilation with Java 17; model validation; authorization & persistence tests |
| 9–10, 18, 20, 34 | Phase 1 | Donor initiation; profile with history; collection → pending test → pass/fail → separate registration | `donationAndAuthentication` |
| 32–37, 50–51 | Phase 1 | Four role menus; hashed credentials; old-password verification; logout; service-level role, facility, and admin-level checks | `donationAndAuthentication`, `authorizationAndValidation`, console checks |
| 12, 23 | Phase 1 | Hospital staff creates patient; patient accounts persisted; hospital-scoped patient access | `phase1WorkflowAndPersistence`, `authorizationAndValidation` |
| 24–25 | Phase 1 | Blood request creation; blood group, units, patient, hospital, and urgency recorded; local inventory availability check | `phase1WorkflowAndPersistence` |
| 21–22 | Phase 1 | Four-level priority HashMap mapping (`CRITICAL`: 1, `HIGH`: 2, `MEDIUM`: 3, `LOW`: 4) | `priorityAndPhase2Guardrails` |
| 40–41, 46, 49, 52 | Phase 1 | Centralized file storage, atomic snapshots for Phase-1 entities, rollback on failed save, process lock, no reseed on corruption, EOF handling | `persistenceAndFailure`, `phase1WorkflowAndPersistence`, `consolePhase1` |
| 26, 30–31 | Phase 2 (Deferred) | Hospital stock fulfillment; blood issue to patient (`ISSUED`); request fulfillment (`FULFILLED`) | Throws `UnsupportedOperationException`; documented in `Phase2DeferredTest` |
| 28–29, 44–45 | Phase 2 (Deferred) | Bank approval; central unit reservation; exact-unit transfer dispatch; reservation release on rejection | Throws `UnsupportedOperationException`; documented in `Phase2DeferredTest` |
| 38–39 | Phase 2 (Deferred) | Low-stock and near-expiry automated alert evaluations; facility text report generation and export | Throws `UnsupportedOperationException`; documented in `Phase2DeferredTest` |
| 43, 53–56 | Phase 1 & 2 | Unified service API across console and tests; state changes and audit events save atomically for active operations | Regression suite (`SystemTest.java`) |

The Phase-1 regression test suite covers **121 regression assertions** across Phase-1 workflows and Phase-2 guardrails. Run `build.ps1 -Test` for current test results.

## State Transitions (Phase 1 vs. Phase 2)

```text
Donation Lifecycle (Phase 1 - Active):
  PENDING_TEST -> TEST_PASSED -> REGISTERED (Blood bank available stock)
               -> REJECTED

Blood Unit Lifecycle:
  [Phase 1] PENDING_TEST -> TEST_PASSED -> AVAILABLE (Quarantine -> Usable)
                         -> REJECTED
  [Phase 2] AVAILABLE -> TRANSFERRED -> ISSUED (Deferred to Phase 2)
  [Phase 2] Unissued stock automatic expiry batching (Deferred to Phase 2)

Blood Request Lifecycle:
  [Phase 1] PENDING (Recorded, persisted, evaluated against hospital local stock)
  [Phase 2] PENDING -> APPROVED -> READY -> FULFILLED (Deferred to Phase 2)
  [Phase 2] PENDING / APPROVED -> REJECTED (Deferred to Phase 2)
  [Phase 2] READY -> PENDING (Recheck after local expiry - Deferred to Phase 2)

Blood Transfer Lifecycle:
  [Phase 2] PENDING -> DISPATCHED (Deferred to Phase 2)
```

- **Donation & Quarantine**: Collection creates traceable `PENDING_TEST` records in quarantined inventory. Only upon laboratory `PASS` and explicit administrative `registerDonation()` do units enter usable `AVAILABLE` inventory.
- **Local Availability Checking**: In Phase 1, `checkAvailability()` calculates whether hospital local stock is sufficient or has a shortage, without executing automated reservation locks or inter-facility transfers.
- **Explicit Deferral Guardrails**: Rather than pretending Phase-2 operations succeed, service and model methods throw `UnsupportedOperationException("...planned for Phase 2...")` when transfer, reservation, fulfillment, issuance, alerts, or report exports are invoked.

## Transaction & Persistence Boundary

`BloodBankService` authenticates the actor, copies the current state, validates the operation against that copy, applies all inventory/history changes, appends an audit event, checks entity relationships, saves one atomic snapshot to `data/v2/system-v2.dat`, and only then replaces its authoritative in-memory state. Errors before the final replacement reject the operation and leave memory unchanged.

The following Phase-1 entities are fully persisted across application restarts:
- Users & credential hashes (salt + PBKDF2-HMAC-SHA256)
- Facilities (Blood banks and hospitals)
- Donors and patient records
- Blood donations and laboratory test results/notes
- Blood units (both quarantined `PENDING_TEST` and active `AVAILABLE` units)
- Blood requests and local availability state
- Append-only audit log records for all Phase-1 events

## Diagram API Interpretation & Preservation

All 15 classes from the class diagram and PRD are retained in the codebase with their declared attributes, inheritance, and method signatures:

- `Donor.donate`, `HospitalStaff` operations, and `BloodBankAdmin` operations delegate to an explicitly supplied `BloodBankService`; the service owns authorization, isolation, and persistence.
- `BloodUnit.runLabTests(boolean)` requires a recorded laboratory test result.
- `BloodRequest.priorityDispatch()` provides the HashMap priority (`CRITICAL`: 1, `HIGH`: 2, `MEDIUM`: 3, `LOW`: 4).
- `Inventory.totalStock` is calculated dynamically from unit records, not stored as an independent counter.
- Phase-2 model methods (`BloodBank.transferBlood`, `BloodTransfer.transferUnits`, `BloodUnit.reserve`, `BloodUnit.release`, `BloodUnit.issue`, `Inventory.reserved`, `Inventory.release`, `Inventory.expire`, `Inventory.checkLowStock`, `AlertManager`, `ReportGenerator`) remain in place with their declared signatures, throwing `UnsupportedOperationException`.

## Test Suites

1. **Phase-1 Regression Suite (`SystemTest.java`)**:
   - `donationAndAuthentication`: Valid/invalid login, session management, old-password verification, donor profile/history, collection, donation intervals, quarantined units, lab pass/fail, and donation registration.
   - `phase1WorkflowAndPersistence`: Hospital patient creation, blood request creation, local availability check, atomic snapshot persistence across reloads.
   - `priorityAndPhase2Guardrails`: Priority HashMap evaluation and guardrail assertions confirming that all Phase-2 methods (transfers, reservations, releases, fulfillment, blood issuance, alert manager, report generator) fail fast with `UnsupportedOperationException`.
   - `authorizationAndValidation`: Hospital-scoped patient access, facility isolation, duplicate ID checks, input validations (age, phone, blood group, urgency, quantity).
   - `persistenceAndFailure`: Atomic rollback on failed disk writes, corrupt snapshot preservation without overwriting, and process lock enforcement.
   - `consolePhase1`: Console menu navigation, recovery from invalid user input, EOF handling.

2. **Phase-2 Specifications (`Phase2DeferredTest.java`)**:
   - Preserves test specifications for Phase-2 requirements (inter-facility transfers, exact-unit arithmetic, priority reservations, low-stock/expiry alerts, report exports) without claiming they are implemented in Phase 1.

## Known Boundaries

- Legacy flat files remain readable as historical source files but are not silently converted to the stricter version-2 schema.
- The first launch creates a local demonstration dataset. There is no database, web interface, network service, real laboratory integration, component compatibility matching, or clinical certification.
- No external library is required. The build targets Java 17; a Java 17-compatible JDK is needed to execute it.
