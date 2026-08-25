package bloodbank;

import java.time.LocalDate;
import java.util.ArrayList;

public class TestFlow {
    public static void main(String[] args) {
        Main.initializeSystem();
        
        System.out.println("\n--- RUNNING COMPLETE REFACTORED BBMS INTEGRATION TEST ---");
        
        // 1. Clear state for testing
        Main.admins.clear();
        Main.hospitalStaffs.clear();
        Main.donors.clear();
        Main.patients.clear();
        Main.hospitals.clear();
        Main.bloodBanks.clear();
        Main.donations.clear();
        Main.requests.clear();
        Main.transfers.clear();
        Main.inventory.getBloodUnits().clear();

        // 2. Setup Central Blood Bank and Hospitals
        String bbId = "BB_TEST_001";
        BloodBank bloodBank = new BloodBank(bbId, "Central Blood Bank Test", "101 Center St", "555-0101", "Dr. Alan", Main.inventory);
        Main.bloodBanks.add(bloodBank);

        String hospId = "HOSP_TEST_001";
        Hospital hospital = new Hospital(hospId, "City General Test Hosp", "202 Health Rd", "555-0202", "Private", "911", new Inventory());
        Main.hospitals.add(hospital);

        // 3. Register Accounts (Admin, Staff, Donor, Patient)
        System.out.println("\nStep 1: Registering accounts...");
        BloodBankAdmin admin = new BloodBankAdmin("ADM_TEST_001", "Admin Alice", 38, "Female", "9998887776", "BB HQ", "admin_t", "pass", "BLOOD_BANK_ADMIN", "EMP_A01", bbId, "SuperAdmin");
        Main.admins.add(admin);

        HospitalStaff staff = new HospitalStaff("STF_TEST_001", "Nurse Ned", 29, "Male", "8887776665", "City Hosp", "staff_t", "pass", "HOSPITAL_STAFF", "EMP_S01", hospId, "Emergency");
        Main.hospitalStaffs.add(staff);
        hospital.setStaffs(new HospitalStaff[]{staff});

        Donor donor = new Donor("DON_TEST_001", "Donor Dave", 25, "Male", "7776665554", "123 Main St", "donor_t", "pass", "DONOR", "A+", 14.2, 70.0, "");
        Main.donors.add(donor);

        Patient patient = new Patient("PAT_TEST_001", "Patient Pam", 52, "Female", "6665554443", "456 Side St", "patient_t", "pass", "PATIENT", "A+", "Severe Loss", "Dr. Carter", 2, hospId);
        Main.patients.add(patient);

        // 4. Verify Donor Eligibility
        System.out.println("\nStep 2: Checking donor eligibility...");
        boolean isEligible = donor.isEligible();
        System.out.println("Donor " + donor.getName() + " Eligibility: " + (isEligible ? "ELIGIBLE" : "INELIGIBLE"));
        if (!isEligible) {
            System.err.println("Test Failed: Seed donor should be eligible.");
            System.exit(1);
        }

        // 5. Donation Flow
        System.out.println("\nStep 3: Recording donation...");
        String donationTxnId = "DON_TXN_001";
        BloodDonation donation = new BloodDonation(donationTxnId, LocalDate.now().toString(), "Pending", donor.getPersonId(), donor.getBloodGroup(), 3);
        donation.recordDonation();
        Main.donations.add(donation);

        String unitId = "UNIT_TEST_001";
        BloodUnit unit = new BloodUnit(unitId, donor.getBloodGroup(), 3, LocalDate.now().toString(), LocalDate.now().plusDays(42).toString(), "PENDING_TEST");
        Main.inventory.addBloodUnit(unit);
        System.out.println("Recorded 3 units of A+ donation. Unit ID: " + unitId + " Status: " + unit.getStatus());

        // 6. Lab Testing Flow
        System.out.println("\nStep 4: Running lab tests...");
        boolean testPassed = unit.runLabTests();
        if (!testPassed || !"AVAILABLE".equals(unit.getStatus())) {
            System.err.println("Test Failed: Lab tests should pass and set status to AVAILABLE.");
            System.exit(1);
        }
        System.out.println("Central Inventory usable stock of A+: " + Main.inventory.getStockForGroup("A+") + " units.");

        // 7. Request Submission
        System.out.println("\nStep 5: Submitting blood request from Hospital...");
        String reqTxnId = "REQ_TXN_001";
        BloodRequest request = new BloodRequest(reqTxnId, LocalDate.now().toString(), "Pending", patient.getPersonId(), hospId, "A+", 2, "NORMAL");
        Main.requests.add(request);
        System.out.println("Request submitted. Status: " + request.getStatus() + " Urgency: " + request.getUrgency());

        // 8. Request Approval
        System.out.println("\nStep 6: Admin approves request...");
        boolean isAvailable = request.checkAvailability();
        if (!isAvailable) {
            System.err.println("Test Failed: Central inventory should have enough stock.");
            System.exit(1);
        }
        request.approveRequest();
        System.out.println("Request status: " + request.getStatus());

        // 9. Dispatch Transfer
        System.out.println("\nStep 7: Admin dispatches blood transfer...");
        String transId = "TRANS_TXN_001";
        BloodTransfer transfer = new BloodTransfer(transId, LocalDate.now().toString(), "Pending", bbId, hospId, request.getBloodGroup(), request.getUnitsRequested());
        transfer.transferUnits();
        Main.transfers.add(transfer);

        // Move units
        int unitsToMove = request.getUnitsRequested();
        BloodUnit[] availableUnits = Main.inventory.searchBloodGroup(request.getBloodGroup());
        for (BloodUnit bu : availableUnits) {
            if (unitsToMove <= 0) break;
            if (bu.getQuantity() <= unitsToMove) {
                unitsToMove -= bu.getQuantity();
                Main.inventory.removeBloodUnit(bu.getBloodUnitId());
            } else {
                bu.updateQuantity(bu.getQuantity() - unitsToMove);
                unitsToMove = 0;
            }
        }
        // Add to hospital
        BloodUnit hospUnit = new BloodUnit("HOSP_UNIT_001", request.getBloodGroup(), request.getUnitsRequested(), LocalDate.now().toString(), LocalDate.now().plusDays(42).toString(), "AVAILABLE");
        hospital.getInventory().addBloodUnit(hospUnit);
        request.setStatus("COMPLETED");

        System.out.println("Transfer complete. Central Stock A+: " + Main.inventory.getStockForGroup("A+") + " | Hospital Stock A+: " + hospital.getInventory().getStockForGroup("A+"));

        // 10. Issue Blood to Patient
        System.out.println("\nStep 8: Hospital staff issues blood to patient...");
        int localStock = hospital.getInventory().getStockForGroup("A+");
        if (localStock >= request.getUnitsRequested()) {
            // Deduct
            hospital.getInventory().removeBloodUnit("HOSP_UNIT_001");
            System.out.println("Issued " + request.getUnitsRequested() + " units to patient: " + patient.getName());
            System.out.println("Hospital Stock A+ after issue: " + hospital.getInventory().getStockForGroup("A+"));
        } else {
            System.err.println("Test Failed: Hospital stock should have enough units.");
            System.exit(1);
        }

        // 11. Alerts & Reports
        System.out.println("\nStep 9: Running Alerts...");
        Main.alertManager.checkLowStock();
        Main.alertManager.checkExpiry();

        System.out.println("\nStep 10: Generating and exporting Reports...");
        Main.reportGenerator.generateInventoryReport();
        Main.reportGenerator.generateDonationReport();
        Main.reportGenerator.generateRequestReport();
        Main.reportGenerator.exportReport();

        // 12. Save & Load (Persistence Verification)
        System.out.println("\nStep 11: Testing Persistence Layer...");
        FileManager fm = new FileManager("data_test/");
        fm.saveData();

        // Clear in-memory
        Main.admins.clear();
        Main.hospitalStaffs.clear();
        Main.donors.clear();
        Main.patients.clear();
        Main.hospitals.clear();
        Main.bloodBanks.clear();
        Main.donations.clear();
        Main.requests.clear();
        Main.transfers.clear();
        Main.inventory.getBloodUnits().clear();

        System.out.println("In-memory lists cleared. Loading from files...");
        fm.loadData();

        System.out.println("\nVerification of loaded data:");
        System.out.println(" - Admins loaded: " + Main.admins.size() + " (Expected: 1)");
        System.out.println(" - Staff loaded: " + Main.hospitalStaffs.size() + " (Expected: 1)");
        System.out.println(" - Donors loaded: " + Main.donors.size() + " (Expected: 1)");
        System.out.println(" - Patients loaded: " + Main.patients.size() + " (Expected: 1)");
        System.out.println(" - Donations loaded: " + Main.donations.size() + " (Expected: 1)");
        System.out.println(" - Requests loaded: " + Main.requests.size() + " (Expected: 1)");
        System.out.println(" - Transfers loaded: " + Main.transfers.size() + " (Expected: 1)");

        if (Main.donors.isEmpty() || !Main.donors.get(0).getName().equals("Donor Dave")) {
            System.err.println("Test Failed: Loaded donor name mismatch.");
            System.exit(1);
        }

        System.out.println("\n--- AUTOMATED INTEGRATION TEST COMPLETED SUCCESSFULLY ---");
    }
}
