package bloodbank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private Integer choice;
    private Scanner scanner;
    private Validation val;

    public Menu() {
        this.choice = 0;
        this.scanner = new Scanner(System.in);
        this.val = new Validation();
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a decimal number.");
            }
        }
    }

    public void displayMainMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     BLOOD BANK MANAGEMENT SYSTEM - MAIN MENU     ");
            System.out.println("==================================================");
            System.out.println("1. Login");
            System.out.println("2. Register New User (Donor / Patient / Staff)");
            System.out.println("3. Exit System");
            System.out.println("==================================================");
            int choiceVal = readInt("Enter choice (1-3): ");
            this.choice = choiceVal;

            if (this.choice == 1) {
                loginFlow();
            } else if (this.choice == 2) {
                registrationFlow();
            } else if (this.choice == 3) {
                Main.closeApplication();
                System.out.println("Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void loginFlow() {
        System.out.println("\n--- User Login ---");
        String username = readString("Enter Username: ");
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        boolean authenticated = Main.loginManager.authenticateUser(username, password);
        if (authenticated) {
            Person user = Main.loginManager.getLoggedInUser();
            System.out.println("\nLogin Successful! Welcome, " + user.getName() + " [" + user.getRole() + "]");
            
            if ("BLOOD_BANK_ADMIN".equalsIgnoreCase(user.getRole())) {
                adminMenu();
            } else if ("HOSPITAL_STAFF".equalsIgnoreCase(user.getRole())) {
                staffMenu();
            } else if ("DONOR".equalsIgnoreCase(user.getRole())) {
                donorMenu();
            } else if ("PATIENT".equalsIgnoreCase(user.getRole())) {
                patientMenu();
            }
        } else {
            System.out.println("\nInvalid Username or Password. Login failed.");
        }
    }

    private void registrationFlow() {
        System.out.println("\n--- User Registration ---");
        System.out.println("Select Registration Role:");
        System.out.println("1. Donor");
        System.out.println("2. Patient");
        System.out.println("3. Hospital Staff");
        int regChoice = readInt("Enter choice (1-3): ");

        String name = readString("Enter Full Name: ");
        int age = readInt("Enter Age: ");
        String gender = readString("Enter Gender: ");
        String phone = readString("Enter Phone Number: ");
        while (!val.validatePhoneNumber(phone)) {
            System.out.println("Invalid phone (must be 10 digits).");
            phone = readString("Enter Phone Number: ");
        }
        String address = readString("Enter Address: ");
        String username = readString("Choose Username: ");
        // Ensure unique username
        while (isUsernameTaken(username)) {
            System.out.println("Username is already taken. Try another.");
            username = readString("Choose Username: ");
        }
        String password = readString("Choose Password: ");

        if (regChoice == 1) {
            String bg = readString("Blood Group (e.g. A+, O-): ");
            while (!val.validateBloodGroup(bg)) {
                System.out.println("Invalid blood group.");
                bg = readString("Blood Group: ");
            }
            double hb = readDouble("Hemoglobin level (g/dL): ");
            double weight = readDouble("Weight (kg): ");
            
            String id = "DON_" + System.currentTimeMillis();
            Donor newDonor = new Donor(id, name, age, gender, phone, address, username, password, "DONOR", bg, hb, weight, "");
            Main.donors.add(newDonor);
            System.out.println("Donor registered successfully! Your ID is: " + id);
            Main.saveDonorsToFile();
        } else if (regChoice == 2) {
            String bg = readString("Blood Group Required (e.g. B+, AB-): ");
            while (!val.validateBloodGroup(bg)) {
                System.out.println("Invalid blood group.");
                bg = readString("Blood Group: ");
            }
            String disease = readString("Disease/Reason for Request: ");
            String doctor = readString("Referring Doctor: ");
            int units = readInt("Units Required: ");
            
            // List available hospitals
            System.out.println("Available Hospitals:");
            for (Hospital h : Main.hospitals) {
                System.out.println(" - " + h.getOrganizationId() + ": " + h.getOrganizationName());
            }
            String hospitalId = readString("Enter Hospital ID: ");
            
            String id = "PAT_" + System.currentTimeMillis();
            Patient newPatient = new Patient(id, name, age, gender, phone, address, username, password, "PATIENT", bg, disease, doctor, units, hospitalId);
            Main.patients.add(newPatient);
            System.out.println("Patient registered successfully! Your ID is: " + id);
            Main.savePatientsToFile();
        } else if (regChoice == 3) {
            String employeeId = "EMP_" + System.currentTimeMillis();
            System.out.println("Available Hospitals:");
            for (Hospital h : Main.hospitals) {
                System.out.println(" - " + h.getOrganizationId() + ": " + h.getOrganizationName());
            }
            String hospitalId = readString("Enter Hospital ID: ");
            String dept = readString("Enter Department: ");
            
            String id = "STF_" + System.currentTimeMillis();
            HospitalStaff newStaff = new HospitalStaff(id, name, age, gender, phone, address, username, password, "HOSPITAL_STAFF", employeeId, hospitalId, dept);
            Main.hospitalStaffs.add(newStaff);
            
            // Add staff to the hospital record
            for (Hospital h : Main.hospitals) {
                if (h.getOrganizationId().equals(hospitalId)) {
                    List<HospitalStaff> list = new ArrayList<>(List.of(h.getStaffs()));
                    list.add(newStaff);
                    h.setStaffs(list.toArray(new HospitalStaff[0]));
                    Main.saveHospitalsToFile();
                    break;
                }
            }

            System.out.println("Hospital Staff registered successfully! Your ID is: " + id);
            Main.saveHospitalStaffsToFile();
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private boolean isUsernameTaken(String username) {
        for (BloodBankAdmin a : Main.admins) if (a.getUsername().equalsIgnoreCase(username)) return true;
        for (HospitalStaff s : Main.hospitalStaffs) if (s.getUsername().equalsIgnoreCase(username)) return true;
        for (Donor d : Main.donors) if (d.getUsername().equalsIgnoreCase(username)) return true;
        for (Patient p : Main.patients) if (p.getUsername().equalsIgnoreCase(username)) return true;
        return false;
    }

    // --- Donor Dashboard Menu ---
    public void donorMenu() {
        Donor donor = (Donor) Main.loginManager.getLoggedInUser();
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("                 DONOR DASHBOARD                  ");
            System.out.println("==================================================");
            System.out.println("Logged in as: " + donor.getName() + " | Blood Group: " + donor.getBloodGroup());
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. Check Eligibility");
            System.out.println("4. View Donation History");
            System.out.println("5. Change Password");
            System.out.println("6. Logout");
            System.out.println("==================================================");
            int choiceVal = readInt("Enter choice (1-6): ");
            this.choice = choiceVal;

            switch (this.choice) {
                case 1: donor.displayDetails(); break;
                case 2: donor.updateDetails(); break;
                case 3:
                    boolean elig = donor.isEligible();
                    System.out.println("\n>>> ELIGIBILITY STATUS: " + (elig ? "ELIGIBLE TO DONATE" : "NOT ELIGIBLE"));
                    if (!elig) {
                        System.out.println("Reason: You must meet all criteria (Age 18-65, Hb >= 12.5, Weight >= 50kg, and last donation >= 90 days ago).");
                    }
                    break;
                case 4: donor.viewDonationHistory(); break;
                case 5:
                    String newPass = readString("Enter New Password: ");
                    Main.loginManager.changePassword(newPass);
                    break;
                case 6:
                    Main.loginManager.logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // --- Patient Dashboard Menu ---
    public void patientMenu() {
        Patient patient = (Patient) Main.loginManager.getLoggedInUser();
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("                PATIENT DASHBOARD                 ");
            System.out.println("==================================================");
            System.out.println("Logged in as: " + patient.getName() + " | Hospital ID: " + patient.getHospitalId());
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. View Request Status/History");
            System.out.println("4. Change Password");
            System.out.println("5. Logout");
            System.out.println("==================================================");
            int choiceVal = readInt("Enter choice (1-5): ");
            this.choice = choiceVal;

            switch (this.choice) {
                case 1: patient.displayDetails(); break;
                case 2: patient.updateDetails(); break;
                case 3: patient.viewRequestStatus(); break;
                case 4:
                    String newPass = readString("Enter New Password: ");
                    Main.loginManager.changePassword(newPass);
                    break;
                case 5:
                    Main.loginManager.logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // --- Hospital Staff Dashboard Menu ---
    public void staffMenu() {
        HospitalStaff staff = (HospitalStaff) Main.loginManager.getLoggedInUser();
        Hospital hospital = null;
        for (Hospital h : Main.hospitals) {
            if (h.getOrganizationId().equals(staff.getFacilityId())) {
                hospital = h;
                break;
            }
        }
        
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("             HOSPITAL STAFF DASHBOARD             ");
            System.out.println("==================================================");
            System.out.println("Staff: " + staff.getName() + " | Facility: " + (hospital != null ? hospital.getOrganizationName() : "N/A"));
            System.out.println("1. Register Patient");
            System.out.println("2. Create Blood Request");
            System.out.println("3. Create Emergency Blood Request");
            System.out.println("4. Check Hospital Stock");
            System.out.println("5. Issue Blood to Patient");
            System.out.println("6. Request Restock from Blood Bank");
            System.out.println("7. View Request History");
            System.out.println("8. View Hospital Inventory");
            System.out.println("9. Change Password");
            System.out.println("10. Logout");
            System.out.println("==================================================");
            int choiceVal = readInt("Enter choice (1-10): ");
            this.choice = choiceVal;

            switch (this.choice) {
                case 1:
                    registrationFlow(); // Registration flow handles patient creation
                    break;
                case 2:
                    createBloodRequestFlow(staff, false);
                    break;
                case 3:
                    createBloodRequestFlow(staff, true);
                    break;
                case 4:
                    if (hospital != null) {
                        hospital.viewInventory();
                    } else {
                        System.out.println("Facility association missing.");
                    }
                    break;
                case 5:
                    issueBloodFlow(hospital);
                    break;
                case 6:
                    restockRequestFlow(staff);
                    break;
                case 7:
                    if (hospital != null) hospital.viewRequestHistory();
                    break;
                case 8:
                    if (hospital != null) hospital.viewInventory();
                    break;
                case 9:
                    String newPass = readString("Enter New Password: ");
                    Main.loginManager.changePassword(newPass);
                    break;
                case 10:
                    Main.loginManager.logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void createBloodRequestFlow(HospitalStaff staff, boolean isEmergency) {
        System.out.println("\n--- Create Blood Request ---");
        String pId = readString("Enter Patient ID: ");
        Patient patient = null;
        for (Patient p : Main.patients) {
            if (p.getPersonId().equals(pId)) {
                patient = p;
                break;
            }
        }
        if (patient == null) {
            System.out.println("Error: Patient not found.");
            return;
        }

        String bg = readString("Enter Blood Group Required: ");
        while (!val.validateBloodGroup(bg)) {
            System.out.println("Invalid blood group.");
            bg = readString("Enter Blood Group Required: ");
        }
        int qty = readInt("Enter Units Required: ");
        if (qty <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        String reqId = "REQ_" + System.currentTimeMillis();
        BloodRequest req;
        if (isEmergency) {
            System.out.println("Select Urgency Level:");
            System.out.println("1. LOW");
            System.out.println("2. NORMAL");
            System.out.println("3. HIGH");
            System.out.println("4. CRITICAL");
            int uCh = readInt("Choice (1-4): ");
            String urgencyLvl = "NORMAL";
            if (uCh == 1) urgencyLvl = "LOW";
            else if (uCh == 2) urgencyLvl = "NORMAL";
            else if (uCh == 3) urgencyLvl = "HIGH";
            else if (uCh == 4) urgencyLvl = "CRITICAL";
            
            req = new EmergencyRequest(reqId, LocalDate.now().toString(), "Pending", pId, staff.getFacilityId(), bg, qty, urgencyLvl);
        } else {
            req = new BloodRequest(reqId, LocalDate.now().toString(), "Pending", pId, staff.getFacilityId(), bg, qty, "NORMAL");
        }

        Main.requests.add(req);
        Main.saveRequestsToFile();
        System.out.println("Blood Request submitted successfully! Request ID: " + reqId);
    }

    private void issueBloodFlow(Hospital hospital) {
        if (hospital == null) return;
        System.out.println("\n--- Issue Blood to Patient ---");
        System.out.println("Select APPROVED blood request for hospital " + hospital.getOrganizationId() + ":");
        List<BloodRequest> approvedRequests = new ArrayList<>();
        for (BloodRequest br : Main.requests) {
            if (br.getHospitalId().equals(hospital.getOrganizationId()) && "APPROVED".equalsIgnoreCase(br.getStatus())) {
                approvedRequests.add(br);
            }
        }
        if (approvedRequests.isEmpty()) {
            System.out.println("No approved requests found for this hospital.");
            return;
        }

        for (int i = 0; i < approvedRequests.size(); i++) {
            BloodRequest req = approvedRequests.get(i);
            System.out.println((i + 1) + ". Request " + req.getTransactionId() + " (Patient ID: " + req.getPatientId() + ") - Group: " + req.getBloodGroup() + " | Units: " + req.getUnitsRequested());
        }

        int reqIndex = readInt("Select Request (1-" + approvedRequests.size() + "): ") - 1;
        if (reqIndex < 0 || reqIndex >= approvedRequests.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        BloodRequest selectedReq = approvedRequests.get(reqIndex);
        
        // Check local stock
        int localStock = hospital.getInventory().getStockForGroup(selectedReq.getBloodGroup());
        if (localStock < selectedReq.getUnitsRequested()) {
            System.out.println("Error: Insufficient local inventory of " + selectedReq.getBloodGroup() + ". Local stock: " + localStock + " units. Requested: " + selectedReq.getUnitsRequested());
            return;
        }

        // Deduct local units FIFO style
        int unitsToDeduct = selectedReq.getUnitsRequested();
        BloodUnit[] activeUnits = hospital.getInventory().searchBloodGroup(selectedReq.getBloodGroup());
        for (BloodUnit bu : activeUnits) {
            if (unitsToDeduct <= 0) break;
            if (bu.getQuantity() <= unitsToDeduct) {
                unitsToDeduct -= bu.getQuantity();
                bu.setStatus("ISSUED");
            } else {
                bu.updateQuantity(bu.getQuantity() - unitsToDeduct);
                unitsToDeduct = 0;
            }
        }

        selectedReq.setStatus("COMPLETED");
        
        // Record issue log
        System.out.println("Successfully issued " + selectedReq.getUnitsRequested() + " units of " + selectedReq.getBloodGroup() + " to Patient " + selectedReq.getPatientId());
        
        Main.saveRequestsToFile();
        Main.saveBloodUnitsToFile();
    }

    private void restockRequestFlow(HospitalStaff staff) {
        System.out.println("\n--- Request Restock from Blood Bank ---");
        String bg = readString("Blood Group to Restock: ");
        while (!val.validateBloodGroup(bg)) {
            System.out.println("Invalid blood group.");
            bg = readString("Blood Group to Restock: ");
        }
        int qty = readInt("Units to Request: ");
        if (qty <= 0) {
            System.out.println("Units must be positive.");
            return;
        }

        String reqId = "REQ_RESTOCK_" + System.currentTimeMillis();
        // Hospital restock uses "STAFF" or "HOSPITAL" as patientId
        BloodRequest req = new BloodRequest(reqId, LocalDate.now().toString(), "Pending", "HOSPITAL", staff.getFacilityId(), bg, qty, "NORMAL");
        Main.requests.add(req);
        Main.saveRequestsToFile();
        System.out.println("Restock request sent to central Blood Bank. ID: " + reqId);
    }

    // --- Blood Bank Admin Dashboard Menu ---
    public void adminMenu() {
        BloodBankAdmin admin = (BloodBankAdmin) Main.loginManager.getLoggedInUser();
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("            BLOOD BANK ADMIN DASHBOARD            ");
            System.out.println("==================================================");
            System.out.println("Admin: " + admin.getName() + " | Level: " + admin.getAdminLevel() + " | Facility: " + admin.getFacilityId());
            System.out.println("1. Register Donation");
            System.out.println("2. Initiate Lab Test");
            System.out.println("3. View Pending Tests");
            System.out.println("4. Approve Blood Request");
            System.out.println("5. Reject Blood Request");
            System.out.println("6. Dispatch Transfer (Fulfill Request)");
            System.out.println("7. View Blood-Bank Inventory");
            System.out.println("8. Add / Remove Blood Unit Manually");
            System.out.println("9. View Expiry & Low-Stock Alerts");
            System.out.println("10. Generate Inventory Report");
            System.out.println("11. Generate Donation Report");
            System.out.println("12. Generate Request Report");
            System.out.println("13. Manage Facilities / Users");
            System.out.println("14. Change Password");
            System.out.println("15. Logout");
            System.out.println("==================================================");
            int choiceVal = readInt("Enter choice (1-15): ");
            this.choice = choiceVal;

            switch (this.choice) {
                case 1: registerDonationFlow(); break;
                case 2: initiateLabTestFlow(); break;
                case 3: viewPendingTestsFlow(); break;
                case 4: approveRequestFlow(); break;
                case 5: rejectRequestFlow(); break;
                case 6: dispatchTransferFlow(); break;
                case 7:
                    System.out.println("\nCentral Inventory Stock:");
                    Main.inventory.displayInventory();
                    break;
                case 8: manualBloodUnitFlow(); break;
                case 9:
                    Main.alertManager.checkLowStock();
                    Main.alertManager.checkExpiry();
                    break;
                case 10: Main.reportGenerator.generateInventoryReport(); break;
                case 11: Main.reportGenerator.generateDonationReport(); break;
                case 12: Main.reportGenerator.generateRequestReport(); break;
                case 13: manageFacilitiesFlow(); break;
                case 14:
                    String newPass = readString("Enter New Password: ");
                    Main.loginManager.changePassword(newPass);
                    break;
                case 15:
                    Main.loginManager.logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void registerDonationFlow() {
        System.out.println("\n--- Register Blood Donation ---");
        String donorId = readString("Enter Donor ID: ");
        Donor donor = null;
        for (Donor d : Main.donors) {
            if (d.getPersonId().equalsIgnoreCase(donorId)) {
                donor = d;
                break;
            }
        }
        if (donor == null) {
            System.out.println("Donor not found. Please register the donor first.");
            return;
        }

        if (!donor.isEligible()) {
            System.out.println("Donation REJECTED! Donor is not eligible to donate blood.");
            System.out.println("Details: Weight: " + donor.getWeight() + "kg | Hb: " + donor.getHaemoglobin() + "g/dL | Last donation: " + donor.getLastDonationDate());
            return;
        }

        int qty = readInt("Enter Quantity collected (Units, e.g. 1 unit): ");
        if (qty <= 0) {
            System.out.println("Quantity must be positive.");
            return;
        }

        String txnId = "DON_TXN_" + System.currentTimeMillis();
        BloodDonation donation = new BloodDonation(txnId, LocalDate.now().toString(), "Pending", donorId, donor.getBloodGroup(), qty);
        donation.recordDonation();
        Main.donations.add(donation);
        
        // Create unit in central inventory, pending test
        String unitId = "UNIT_" + System.currentTimeMillis();
        BloodUnit unit = new BloodUnit(unitId, donor.getBloodGroup(), qty, LocalDate.now().toString(), LocalDate.now().plusDays(42).toString(), "PENDING_TEST");
        Main.inventory.addBloodUnit(unit);
        
        // Update donor records
        donor.setLastDonationDate(LocalDate.now().toString());
        donor.setEligible(donor.checkEligibility());

        System.out.println("Donation recorded successfully! Blood Unit ID: " + unitId + " registered in PENDING_TEST status.");
        
        Main.saveDonationsToFile();
        Main.saveBloodUnitsToFile();
        Main.saveDonorsToFile();
    }

    private void initiateLabTestFlow() {
        System.out.println("\n--- Initiate Lab Test ---");
        List<BloodUnit> pending = new ArrayList<>();
        for (BloodUnit bu : Main.inventory.getBloodUnits()) {
            if ("PENDING_TEST".equalsIgnoreCase(bu.getStatus())) {
                pending.add(bu);
            }
        }

        if (pending.isEmpty()) {
            System.out.println("No blood units pending lab tests.");
            return;
        }

        for (int i = 0; i < pending.size(); i++) {
            System.out.println((i + 1) + ". Unit: " + pending.get(i).getBloodUnitId() + " (Group: " + pending.get(i).getBloodGroup() + " | Qty: " + pending.get(i).getQuantity() + ")");
        }

        int index = readInt("Select unit to test (1-" + pending.size() + "): ") - 1;
        if (index < 0 || index >= pending.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        BloodUnit selected = pending.get(index);
        selected.runLabTests();
        Main.saveBloodUnitsToFile();
    }

    private void viewPendingTestsFlow() {
        System.out.println("\n--- Pending Lab Tests ---");
        boolean found = false;
        for (BloodUnit bu : Main.inventory.getBloodUnits()) {
            if ("PENDING_TEST".equalsIgnoreCase(bu.getStatus())) {
                System.out.println("Unit ID: " + bu.getBloodUnitId() + " | Group: " + bu.getBloodGroup() + " | Qty: " + bu.getQuantity() + " | Collected: " + bu.getCollectionDate());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No blood units pending testing.");
        }
    }

    private void approveRequestFlow() {
        System.out.println("\n--- Approve Blood Request ---");
        List<BloodRequest> pending = new ArrayList<>();
        
        // Show emergency requests first
        for (BloodRequest br : Main.requests) {
            if ("Pending".equalsIgnoreCase(br.getStatus()) && br instanceof EmergencyRequest) {
                pending.add(br);
            }
        }
        // Then standard requests
        for (BloodRequest br : Main.requests) {
            if ("Pending".equalsIgnoreCase(br.getStatus()) && !(br instanceof EmergencyRequest)) {
                pending.add(br);
            }
        }

        if (pending.isEmpty()) {
            System.out.println("No pending requests found.");
            return;
        }

        System.out.println("Select request to approve (Emergency requests shown at the top):");
        for (int i = 0; i < pending.size(); i++) {
            BloodRequest br = pending.get(i);
            String urgLabel = br instanceof EmergencyRequest ? "[EMERGENCY: " + ((EmergencyRequest) br).getUrgencyLevel() + "]" : "[NORMAL]";
            System.out.println((i + 1) + ". ID: " + br.getTransactionId() + " - " + urgLabel + " Facility: " + br.getHospitalId() + " | Patient: " + br.getPatientId() + " | Group: " + br.getBloodGroup() + " | Units: " + br.getUnitsRequested());
        }

        int index = readInt("Select Request (1-" + pending.size() + "): ") - 1;
        if (index < 0 || index >= pending.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        BloodRequest selected = pending.get(index);
        selected.approveRequest();
        Main.saveRequestsToFile();
    }

    private void rejectRequestFlow() {
        System.out.println("\n--- Reject Blood Request ---");
        List<BloodRequest> pending = new ArrayList<>();
        for (BloodRequest br : Main.requests) {
            if ("Pending".equalsIgnoreCase(br.getStatus())) {
                pending.add(br);
            }
        }

        if (pending.isEmpty()) {
            System.out.println("No pending requests found.");
            return;
        }

        for (int i = 0; i < pending.size(); i++) {
            BloodRequest br = pending.get(i);
            System.out.println((i + 1) + ". ID: " + br.getTransactionId() + " | Hospital: " + br.getHospitalId() + " | Group: " + br.getBloodGroup() + " | Units: " + br.getUnitsRequested());
        }

        int index = readInt("Select Request (1-" + pending.size() + "): ") - 1;
        if (index < 0 || index >= pending.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        BloodRequest selected = pending.get(index);
        selected.rejectRequest();
        Main.saveRequestsToFile();
    }

    private void dispatchTransferFlow() {
        System.out.println("\n--- Dispatch Blood Transfer (Fulfill Request) ---");
        List<BloodRequest> approved = new ArrayList<>();
        for (BloodRequest br : Main.requests) {
            if ("APPROVED".equalsIgnoreCase(br.getStatus())) {
                approved.add(br);
            }
        }

        if (approved.isEmpty()) {
            System.out.println("No approved requests available for dispatch.");
            return;
        }

        for (int i = 0; i < approved.size(); i++) {
            BloodRequest br = approved.get(i);
            System.out.println((i + 1) + ". ID: " + br.getTransactionId() + " | Hosp: " + br.getHospitalId() + " | Group: " + br.getBloodGroup() + " | Units: " + br.getUnitsRequested());
        }

        int index = readInt("Select Request (1-" + approved.size() + "): ") - 1;
        if (index < 0 || index >= approved.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        BloodRequest selectedReq = approved.get(index);
        
        // Find destination Hospital
        Hospital destHosp = null;
        for (Hospital h : Main.hospitals) {
            if (h.getOrganizationId().equals(selectedReq.getHospitalId())) {
                destHosp = h;
                break;
            }
        }
        if (destHosp == null) {
            System.out.println("Error: Destination hospital not found.");
            return;
        }

        // Verify stock in Central Inventory
        int avail = Main.inventory.getStockForGroup(selectedReq.getBloodGroup());
        if (avail < selectedReq.getUnitsRequested()) {
            // Check if emergency override applies
            if (selectedReq instanceof EmergencyRequest) {
                System.out.println("Warning: Central inventory has insufficient stock. Emergency requested: " + selectedReq.getUnitsRequested() + ", Available: " + avail);
                System.out.println("Executing Priority Dispatch / Threshold Override.");
                ((EmergencyRequest) selectedReq).overrideThreshold();
                ((EmergencyRequest) selectedReq).priorityDispatch();
            } else {
                System.out.println("Error: Insufficient central inventory stock. Dispatch failed.");
                return;
            }
        }

        // Fulfill and Transfer
        String sourceFacility = Main.bloodBanks.isEmpty() ? "CENTRAL_BB" : Main.bloodBanks.get(0).getOrganizationId();
        String txnId = "TRANS_" + System.currentTimeMillis();
        
        BloodTransfer transfer = new BloodTransfer(txnId, LocalDate.now().toString(), "Pending", sourceFacility, destHosp.getOrganizationId(), selectedReq.getBloodGroup(), selectedReq.getUnitsRequested());
        transfer.transferUnits();
        
        // Move units from central inventory to hospital inventory
        int unitsToMove = selectedReq.getUnitsRequested();
        BloodUnit[] activeUnits = Main.inventory.searchBloodGroup(selectedReq.getBloodGroup());
        
        // Safety check to deduct whatever is available if it was an emergency override with lower stock
        int transferCounter = 0;
        for (BloodUnit bu : activeUnits) {
            if (unitsToMove <= 0) break;
            int moveQty = Math.min(bu.getQuantity(), unitsToMove);
            
            // Central deduct
            if (bu.getQuantity() <= moveQty) {
                Main.inventory.removeBloodUnit(bu.getBloodUnitId());
            } else {
                bu.updateQuantity(bu.getQuantity() - moveQty);
            }
            
            // Destination add
            String newUnitId = "HOSP_UNIT_" + System.currentTimeMillis() + "_" + (transferCounter++);
            BloodUnit movedUnit = new BloodUnit(newUnitId, selectedReq.getBloodGroup(), moveQty, bu.getCollectionDate(), bu.getExpiryDate(), "AVAILABLE");
            destHosp.getInventory().addBloodUnit(movedUnit);
            
            unitsToMove -= moveQty;
        }

        transfer.recordTransfer();
        Main.transfers.add(transfer);
        
        // Update request status
        selectedReq.setStatus("COMPLETED");

        System.out.println("Transfer dispatched successfully! Destination inventory updated.");
        
        Main.saveRequestsToFile();
        Main.saveTransfersToFile();
        Main.saveBloodUnitsToFile();
    }

    private void manualBloodUnitFlow() {
        System.out.println("\n--- Programmatic Stock Management ---");
        System.out.println("1. Add Blood Unit manually (Tested/Passed)");
        System.out.println("2. Remove Blood Unit manually");
        int ch = readInt("Enter choice (1-2): ");
        
        if (ch == 1) {
            String id = "UNIT_M_" + System.currentTimeMillis();
            String bg = readString("Blood Group (e.g. A+): ");
            while (!val.validateBloodGroup(bg)) {
                bg = readString("Invalid. Enter Blood Group: ");
            }
            int qty = readInt("Quantity: ");
            String colDate = readString("Collection Date [YYYY-MM-DD]: ");
            String expDate = readString("Expiry Date [YYYY-MM-DD]: ");
            
            BloodUnit bu = new BloodUnit(id, bg, qty, colDate, expDate, "AVAILABLE");
            Main.inventory.addBloodUnit(bu);
            Main.saveBloodUnitsToFile();
            System.out.println("Blood Unit added to Central stock: " + id);
        } else if (ch == 2) {
            String unitId = readString("Enter Blood Unit ID to remove: ");
            boolean removed = Main.inventory.removeBloodUnit(unitId);
            if (!removed) {
                // Check hospital stocks
                for (Hospital h : Main.hospitals) {
                    if (h.getInventory().removeBloodUnit(unitId)) {
                        removed = true;
                        break;
                    }
                }
            }
            
            if (removed) {
                Main.saveBloodUnitsToFile();
                System.out.println("Blood unit removed successfully.");
            } else {
                System.out.println("Unit ID not found.");
            }
        }
    }

    private void manageFacilitiesFlow() {
        System.out.println("\n--- Facility Registry ---");
        System.out.println("1. View Registered Blood Banks");
        System.out.println("2. View Registered Hospitals");
        System.out.println("3. Register New Hospital");
        System.out.println("4. Register New Blood Bank");
        int ch = readInt("Enter choice (1-4): ");

        if (ch == 1) {
            System.out.println("\n--- Blood Banks ---");
            for (BloodBank bb : Main.bloodBanks) {
                bb.displayOrganization();
            }
        } else if (ch == 2) {
            System.out.println("\n--- Hospitals ---");
            for (Hospital h : Main.hospitals) {
                h.displayOrganization();
            }
        } else if (ch == 3) {
            String id = "HOSP_" + System.currentTimeMillis();
            String name = readString("Hospital Name: ");
            String addr = readString("Address: ");
            String contact = readString("Contact Number: ");
            String type = readString("Type (Private/Public): ");
            String emgContact = readString("Emergency Contact: ");
            
            Hospital h = new Hospital(id, name, addr, contact, type, emgContact, new Inventory());
            Main.hospitals.add(h);
            Main.saveHospitalsToFile();
            System.out.println("Hospital registered successfully! ID: " + id);
        } else if (ch == 4) {
            String id = "BB_" + System.currentTimeMillis();
            String name = readString("Blood Bank Name: ");
            String addr = readString("Address: ");
            String contact = readString("Contact Number: ");
            String manager = readString("Manager Name: ");
            
            BloodBank bb = new BloodBank(id, name, addr, contact, manager, new Inventory());
            Main.bloodBanks.add(bb);
            Main.saveBloodBanksToFile();
            System.out.println("Blood Bank registered successfully! ID: " + id);
        }
    }

    public void hospitalMenu() {
        System.out.println("Hospital management operations are integrated under Admin Dashboard Option 13.");
    }
}
