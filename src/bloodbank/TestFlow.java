package bloodbank;

import java.time.LocalDate;

public class TestFlow {
    public static void main(String[] args) {
        Main.initializeSystem();
        
        System.out.println("\n--- RUNNING AUTOMATED OOP INTEGRATION TEST ---");
        
        // Clear lists for a clean test run
        Main.donors.clear();
        Main.hospitals.clear();
        Main.recipients.clear();
        Main.donations.clear();
        Main.requests.clear();
        Main.transfers.clear();
        Main.inventory.getBloodUnits().clear();
        
        // 1. Register Donor
        String donorId = "DON_TEST_001";
        Donor donor = new Donor(donorId, "Alice Smith", 28, "Female", "1234567890", "456 Oak St", "A+", 13.5, 55.0, "");
        Main.donors.add(donor);
        Main.saveDonorsToFile();
        System.out.println("Step 1: Registered Donor - " + donor.getName() + " (Eligible: " + donor.isEligible() + ")");
        
        // 2. Register Hospital
        String hospId = "HOSP_TEST_001";
        Hospital hospital = new Hospital(hospId, "City General Hospital", "789 Pine Rd", "9876543210", "Private", "9998887776");
        Main.hospitals.add(hospital);
        Main.saveHospitalsToFile();
        System.out.println("Step 2: Registered Hospital - " + hospital.getOrganizationName());
        
        // 3. Register Recipient
        String recId = "REC_TEST_001";
        Recipient recipient = new Recipient(recId, "Bob Brown", 45, "Male", "5556667777", "101 Maple Ave", "A+", "Anemia", "Dr. Green", 2, hospId);
        Main.recipients.add(recipient);
        Main.saveRecipientsToFile();
        System.out.println("Step 3: Registered Recipient - " + recipient.getName());
        
        // 4. Record Donation
        String donationTxnId = "DON_TXN_TEST_001";
        BloodDonation donation = new BloodDonation(donationTxnId, LocalDate.now().toString(), "Completed", donorId, "A+", 3);
        Main.donations.add(donation);
        Main.saveDonationsToFile();
        
        BloodUnit unit = new BloodUnit("UNIT_TEST_001", "A+", 3, LocalDate.now().toString(), LocalDate.now().plusDays(42).toString());
        Main.inventory.addBloodUnit(unit);
        Main.saveBloodUnitsToFile();
        System.out.println("Step 4: Recorded 3 units donation of A+ from Alice Smith. Current Total Stock: " + Main.inventory.getTotalStock());
        
        // 5. Check Low Stock Alerts
        Main.alertManager.checkLowStock(Main.inventory);
        
        // 6. Request Blood (2 units of A+)
        String reqTxnId = "REQ_TXN_TEST_001";
        BloodRequest request = new BloodRequest(reqTxnId, LocalDate.now().toString(), "Pending", recId, hospId, "A+", 2);
        Main.requests.add(request);
        Main.saveRequestsToFile();
        System.out.println("Step 5: Submitted request for 2 units of A+. Status: " + request.getStatus());
        
        // 7. Process Request (Approve & Deduct stock)
        int initialStock = Main.inventory.getStockForGroup("A+");
        System.out.println("Stock of A+ before processing: " + initialStock + " units.");
        if (initialStock >= request.getUnitsRequested()) {
            request.approveRequest();
            // Deduct
            int unitsToDeduct = request.getUnitsRequested();
            java.util.List<BloodUnit> activeUnits = Main.inventory.searchBloodGroup("A+");
            for (BloodUnit bu : activeUnits) {
                if (unitsToDeduct <= 0) break;
                if (bu.getQuantity() <= unitsToDeduct) {
                    unitsToDeduct -= bu.getQuantity();
                    Main.inventory.removeBloodUnit(bu.getBloodUnitId());
                } else {
                    bu.updateQuantity(bu.getQuantity() - unitsToDeduct);
                    unitsToDeduct = 0;
                }
            }
            Main.saveRequestsToFile();
            Main.saveBloodUnitsToFile();
            System.out.println("Step 6: Approved request. Stock of A+ after processing: " + Main.inventory.getStockForGroup("A+") + " units.");
        } else {
            System.out.println("Step 6: Failed to approve request due to low stock.");
        }
        
        // 8. Generate Reports
        System.out.println("\nStep 7: Generating Inventory Report:");
        Main.reportGenerator.generateInventoryReport(Main.inventory);
        
        System.out.println("\nStep 8: Generating Request Report:");
        Main.reportGenerator.generateRequestReport(Main.requests);
        
        System.out.println("\n--- AUTOMATED INTEGRATION TEST COMPLETED SUCCESSFULLY ---");
    }
}
