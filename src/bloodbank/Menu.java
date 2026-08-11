package bloodbank;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private Scanner scanner;
    private Validation val;

    public Menu() {
        this.scanner = new Scanner(System.in);
        this.val = new Validation();
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readPassword(String prompt) {
        System.out.print(prompt);
        StringBuilder password = new StringBuilder();
        try {
            // Put terminal in raw mode to disable input buffering and echo
            Process p = new ProcessBuilder("/bin/sh", "-c", "stty -icanon min 1 -echo < /dev/tty").start();
            p.waitFor();
            
            while (true) {
                int ch = System.in.read();
                if (ch == -1 || ch == '\n' || ch == '\r') {
                    System.out.println();
                    break;
                } else if (ch == 127 || ch == 8) { // Backspace or Delete
                    if (password.length() > 0) {
                        password.deleteCharAt(password.length() - 1);
                        System.out.print("\b \b");
                    }
                } else if (ch == 3 || ch == 4) { // Ctrl+C or Ctrl+D
                    new ProcessBuilder("/bin/sh", "-c", "stty icanon echo < /dev/tty").start().waitFor();
                    System.exit(0);
                } else {
                    password.append((char) ch);
                    System.out.print("*");
                }
            }
        } catch (Exception e) {
            // Fallback for non-interactive / IDE console
            return scanner.nextLine().trim();
        } finally {
            try {
                // Ensure terminal cooked mode is restored
                new ProcessBuilder("/bin/sh", "-c", "stty icanon echo < /dev/tty").start().waitFor();
            } catch (Exception ignored) {}
        }
        return password.toString();
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
            System.out.println("      BLOOD BANK MANAGEMENT SYSTEM - MAIN MENU    ");
            System.out.println("==================================================");
            System.out.println("1. Administrator Login");
            System.out.println("2. Exit System");
            System.out.println("==================================================");
            int choice = readInt("Enter choice (1-2): ");

            if (choice == 1) {
                adminLoginFlow();
            } else if (choice == 2) {
                Main.closeApplication();
                System.out.println("Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void adminLoginFlow() {
        System.out.println("\n--- Administrator Login ---");
        String username = readString("Enter Username: ");
        String password = readPassword("Enter Password: ");

        boolean authenticated = Main.loginManager.authenticateUser(username, password, Main.ADMIN_FILE);
        if (authenticated) {
            System.out.println("\nLogin Successful! Welcome, " + Main.loginManager.getLoggedInAdmin().getName() + " (" + Main.loginManager.getLoggedInAdmin().getRole() + ")");
            adminMenu();
        } else {
            System.out.println("\nInvalid Username or Password. Login failed.");
        }
    }

    public void adminMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("             ADMINISTRATOR DASHBOARD              ");
            System.out.println("==================================================");
            System.out.println("1. Donor Management");
            System.out.println("2. Recipient Management");
            System.out.println("3. Hospital Management");
            System.out.println("4. Blood Donation Management");
            System.out.println("5. Blood Requests Processing");
            System.out.println("6. Blood Transfer Management");
            System.out.println("7. Alert & Expiry System");
            System.out.println("8. Report Generation & Export");
            System.out.println("9. Change Password");
            System.out.println("10. Logout");
            System.out.println("==================================================");
            int choice = readInt("Enter choice (1-10): ");

            switch (choice) {
                case 1: donorMenu(); break;
                case 2: recipientMenu(); break;
                case 3: hospitalMenu(); break;
                case 4: donationMenu(); break;
                case 5: requestMenu(); break;
                case 6: transferMenu(); break;
                case 7: alertMenu(); break;
                case 8: reportsMenu(); break;
                case 9:
                    String newPass = readPassword("Enter New Password: ");
                    Main.loginManager.changePassword(newPass, Main.ADMIN_FILE);
                    break;
                case 10:
                    Main.loginManager.logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // --- Module 2: Donor Management ---
    public void donorMenu() {
        while (true) {
            System.out.println("\n--- Donor Management ---");
            System.out.println("1. Register Donor");
            System.out.println("2. Update Donor Details");
            System.out.println("3. Delete Donor");
            System.out.println("4. Search Donor");
            System.out.println("5. View All Donors");
            System.out.println("6. Check Donation Eligibility");
            System.out.println("7. Back to Dashboard");
            int choice = readInt("Enter choice (1-7): ");

            switch (choice) {
                case 1: registerDonorFlow(); break;
                case 2: updateDonorFlow(); break;
                case 3: deleteDonorFlow(); break;
                case 4: searchDonorFlow(); break;
                case 5: viewAllDonors(); break;
                case 6: checkEligibilityFlow(); break;
                case 7: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void registerDonorFlow() {
        System.out.println("\n--- Register New Donor ---");
        String id = "DON_" + System.currentTimeMillis();
        String name = readString("Name: ");
        int age = readInt("Age: ");
        String gender = readString("Gender: ");
        String phone = readString("Phone Number: ");
        while (!val.validatePhoneNumber(phone)) {
            System.out.println("Invalid Phone Number (must be 10 digits).");
            phone = readString("Phone Number: ");
        }
        String address = readString("Address: ");
        String bg = readString("Blood Group (e.g. A+, O-, AB+): ");
        while (!val.validateBloodGroup(bg)) {
            System.out.println("Invalid Blood Group.");
            bg = readString("Blood Group: ");
        }
        double hb = readDouble("Hemoglobin (g/dL): ");
        double wt = readDouble("Weight (kg): ");
        String lastDate = readString("Last Donation Date (YYYY-MM-DD, or leave blank if none): ");
        if (!lastDate.isEmpty() && !val.validateDate(lastDate)) {
            System.out.println("Warning: Invalid date format. Last donation date cleared.");
            lastDate = "";
        }

        Donor d = new Donor(id, name, age, gender, phone, address, bg, hb, wt, lastDate);
        Main.donors.add(d);
        Main.saveDonorsToFile();
        
        System.out.println("\nDonor Registered Successfully!");
        d.displayDetails();
        d.registerDonor();
    }

    private void updateDonorFlow() {
        System.out.println("\n--- Update Donor Details ---");
        String id = readString("Enter Donor ID: ");
        Donor donor = findDonorById(id);
        if (donor == null) {
            System.out.println("Donor not found.");
            return;
        }

        System.out.println("Current details: " + donor.getDetails());
        String name = readString("New Name (leave blank to keep current): ");
        if (name.isEmpty()) name = donor.getName();

        int age = readInt("New Age (enter 0 to keep current): ");
        if (age <= 0) age = donor.getAge();

        String gender = readString("New Gender (leave blank to keep current): ");
        if (gender.isEmpty()) gender = donor.getGender();

        String phone = readString("New Phone (leave blank to keep current): ");
        if (phone.isEmpty()) {
            phone = donor.getPhoneNumber();
        } else {
            while (!val.validatePhoneNumber(phone)) {
                System.out.println("Invalid Phone Number (must be 10 digits).");
                phone = readString("New Phone: ");
            }
        }

        String address = readString("New Address (leave blank to keep current): ");
        if (address.isEmpty()) address = donor.getAddress();

        double hb = readDouble("New Hemoglobin (enter 0 to keep current): ");
        if (hb <= 0) hb = donor.getHaemoglobin();

        double wt = readDouble("New Weight (enter 0 to keep current): ");
        if (wt <= 0) wt = donor.getWeight();

        String lastDate = readString("New Last Donation Date (YYYY-MM-DD, leave blank to keep current): ");
        if (lastDate.isEmpty()) {
            lastDate = donor.getLastDonationDate();
        } else if (!val.validateDate(lastDate)) {
            System.out.println("Warning: Invalid date format. Setting to previous: " + donor.getLastDonationDate());
            lastDate = donor.getLastDonationDate();
        }

        donor.updateDetails(name, age, gender, phone, address);
        donor.updateMedicalDetails(hb, wt, lastDate);
        Main.saveDonorsToFile();
        System.out.println("Donor details updated successfully.");
    }

    private void deleteDonorFlow() {
        System.out.println("\n--- Delete Donor ---");
        String id = readString("Enter Donor ID: ");
        Donor d = findDonorById(id);
        if (d != null) {
            Main.donors.remove(d);
            Main.saveDonorsToFile();
            System.out.println("Donor " + d.getName() + " deleted successfully.");
        } else {
            System.out.println("Donor not found.");
        }
    }

    private void searchDonorFlow() {
        System.out.println("\n--- Search Donor ---");
        String query = readString("Enter Donor ID or Name to search: ");
        boolean found = false;
        for (Donor d : Main.donors) {
            if (d.getPersonId().equalsIgnoreCase(query) || d.getName().toLowerCase().contains(query.toLowerCase())) {
                d.displayDetails();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No matching donor found.");
        }
    }

    private void viewAllDonors() {
        System.out.println("\n--- Registered Donors List ---");
        if (Main.donors.isEmpty()) {
            System.out.println("No donors registered yet.");
            return;
        }
        for (Donor d : Main.donors) {
            System.out.println(d.getDetails());
        }
    }

    private void checkEligibilityFlow() {
        System.out.println("\n--- Check Donation Eligibility ---");
        String id = readString("Enter Donor ID: ");
        Donor d = findDonorById(id);
        if (d != null) {
            d.displayDetails();
        } else {
            System.out.println("Donor not found.");
        }
    }

    private Donor findDonorById(String id) {
        for (Donor d : Main.donors) {
            if (d.getPersonId().equalsIgnoreCase(id)) {
                return d;
            }
        }
        return null;
    }

    // --- Module 3: Recipient Management ---
    public void recipientMenu() {
        while (true) {
            System.out.println("\n--- Recipient Management ---");
            System.out.println("1. Register Recipient");
            System.out.println("2. Update Recipient Details");
            System.out.println("3. Delete Recipient");
            System.out.println("4. Search Recipient");
            System.out.println("5. View All Recipients");
            System.out.println("6. Track Request Status");
            System.out.println("7. Back to Dashboard");
            int choice = readInt("Enter choice (1-7): ");

            switch (choice) {
                case 1: registerRecipientFlow(); break;
                case 2: updateRecipientFlow(); break;
                case 3: deleteRecipientFlow(); break;
                case 4: searchRecipientFlow(); break;
                case 5: viewAllRecipients(); break;
                case 6: trackRequestStatusFlow(); break;
                case 7: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void registerRecipientFlow() {
        System.out.println("\n--- Register Recipient ---");
        String id = "REC_" + System.currentTimeMillis();
        String name = readString("Name: ");
        int age = readInt("Age: ");
        String gender = readString("Gender: ");
        String phone = readString("Phone Number: ");
        while (!val.validatePhoneNumber(phone)) {
            System.out.println("Invalid phone number format.");
            phone = readString("Phone Number: ");
        }
        String address = readString("Address: ");
        String bg = readString("Blood Group Required: ");
        while (!val.validateBloodGroup(bg)) {
            System.out.println("Invalid Blood Group.");
            bg = readString("Blood Group Required: ");
        }
        String disease = readString("Disease / Medical Condition: ");
        String doctor = readString("Attending Doctor: ");
        int units = readInt("Units Required: ");
        String hospId = readString("Hospital ID (if associated, else leave blank/NA): ");

        Recipient r = new Recipient(id, name, age, gender, phone, address, bg, disease, doctor, units, hospId);
        Main.recipients.add(r);
        Main.saveRecipientsToFile();
        System.out.println("\nRecipient Registered Successfully!");
        r.displayDetails();
    }

    private void updateRecipientFlow() {
        System.out.println("\n--- Update Recipient Details ---");
        String id = readString("Enter Recipient ID: ");
        Recipient r = findRecipientById(id);
        if (r == null) {
            System.out.println("Recipient not found.");
            return;
        }

        System.out.println("Current details: " + r.getDetails());
        String name = readString("New Name (leave blank to keep current): ");
        if (name.isEmpty()) name = r.getName();

        int age = readInt("New Age (enter 0 to keep current): ");
        if (age <= 0) age = r.getAge();

        String gender = readString("New Gender (leave blank to keep current): ");
        if (gender.isEmpty()) gender = r.getGender();

        String phone = readString("New Phone (leave blank to keep current): ");
        if (phone.isEmpty()) {
            phone = r.getPhoneNumber();
        } else {
            while (!val.validatePhoneNumber(phone)) {
                System.out.println("Invalid phone.");
                phone = readString("New Phone: ");
            }
        }

        String address = readString("New Address (leave blank to keep current): ");
        if (address.isEmpty()) address = r.getAddress();

        String disease = readString("New Disease (leave blank to keep current): ");
        if (disease.isEmpty()) disease = r.getDisease();

        String doctor = readString("New Doctor (leave blank to keep current): ");
        if (doctor.isEmpty()) doctor = r.getDoctorName();

        int units = readInt("New Units Required (enter 0 to keep current): ");
        if (units <= 0) units = r.getUnitsRequired();

        String hospId = readString("New Hospital ID (leave blank to keep current): ");
        if (hospId.isEmpty()) hospId = r.getHospitalId();

        r.updateDetails(name, age, gender, phone, address);
        r.setDisease(disease);
        r.setDoctorName(doctor);
        r.setUnitsRequired(units);
        r.setHospitalId(hospId);

        Main.saveRecipientsToFile();
        System.out.println("Recipient details updated successfully.");
    }

    private void deleteRecipientFlow() {
        System.out.println("\n--- Delete Recipient ---");
        String id = readString("Enter Recipient ID: ");
        Recipient r = findRecipientById(id);
        if (r != null) {
            Main.recipients.remove(r);
            Main.saveRecipientsToFile();
            System.out.println("Recipient " + r.getName() + " deleted successfully.");
        } else {
            System.out.println("Recipient not found.");
        }
    }

    private void searchRecipientFlow() {
        System.out.println("\n--- Search Recipient ---");
        String query = readString("Enter Recipient ID or Name: ");
        boolean found = false;
        for (Recipient r : Main.recipients) {
            if (r.getPersonId().equalsIgnoreCase(query) || r.getName().toLowerCase().contains(query.toLowerCase())) {
                r.displayDetails();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No matching recipient found.");
        }
    }

    private void viewAllRecipients() {
        System.out.println("\n--- Registered Recipients List ---");
        if (Main.recipients.isEmpty()) {
            System.out.println("No recipients registered yet.");
            return;
        }
        for (Recipient r : Main.recipients) {
            System.out.println(r.getDetails());
        }
    }

    private void trackRequestStatusFlow() {
        System.out.println("\n--- Track Request Status ---");
        String id = readString("Enter Recipient ID: ");
        boolean found = false;
        for (BloodRequest req : Main.requests) {
            if (req.getRecipientId().equalsIgnoreCase(id)) {
                req.displayTransaction();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No request history found for recipient " + id);
        }
    }

    private Recipient findRecipientById(String id) {
        for (Recipient r : Main.recipients) {
            if (r.getPersonId().equalsIgnoreCase(id)) {
                return r;
            }
        }
        return null;
    }

    // --- Module 4: Hospital Management ---
    public void hospitalMenu() {
        while (true) {
            System.out.println("\n--- Hospital Management ---");
            System.out.println("1. Register Hospital");
            System.out.println("2. Update Hospital Details");
            System.out.println("3. Delete Hospital");
            System.out.println("4. Search Hospital");
            System.out.println("5. View All Hospitals");
            System.out.println("6. Back to Dashboard");
            int choice = readInt("Enter choice (1-6): ");

            switch (choice) {
                case 1: registerHospitalFlow(); break;
                case 2: updateHospitalFlow(); break;
                case 3: deleteHospitalFlow(); break;
                case 4: searchHospitalFlow(); break;
                case 5: viewAllHospitals(); break;
                case 6: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void registerHospitalFlow() {
        System.out.println("\n--- Register Hospital ---");
        String id = "HOSP_" + System.currentTimeMillis();
        String name = readString("Hospital Name: ");
        String address = readString("Address: ");
        String phone = readString("Contact Number: ");
        while (!val.validatePhoneNumber(phone)) {
            System.out.println("Invalid phone number format.");
            phone = readString("Contact Number: ");
        }
        String type = readString("Hospital Type (e.g. Government, Private): ");
        String emergencyPhone = readString("Emergency Contact Number: ");
        while (!val.validatePhoneNumber(emergencyPhone)) {
            System.out.println("Invalid emergency contact number format.");
            emergencyPhone = readString("Emergency Contact Number: ");
        }

        Hospital h = new Hospital(id, name, address, phone, type, emergencyPhone);
        Main.hospitals.add(h);
        Main.saveHospitalsToFile();
        System.out.println("\nHospital Registered Successfully!");
        h.displayOrganization();
    }

    private void updateHospitalFlow() {
        System.out.println("\n--- Update Hospital Details ---");
        String id = readString("Enter Hospital ID: ");
        Hospital h = findHospitalById(id);
        if (h == null) {
            System.out.println("Hospital not found.");
            return;
        }

        h.displayOrganization();
        String name = readString("New Name (leave blank to keep current): ");
        if (name.isEmpty()) name = h.getOrganizationName();

        String address = readString("New Address (leave blank to keep current): ");
        if (address.isEmpty()) address = h.getAddress();

        String phone = readString("New Contact Number (leave blank to keep current): ");
        if (phone.isEmpty()) {
            phone = h.getContactNumber();
        } else {
            while (!val.validatePhoneNumber(phone)) {
                System.out.println("Invalid phone.");
                phone = readString("New Contact Number: ");
            }
        }

        String type = readString("New Hospital Type (leave blank to keep current): ");
        if (type.isEmpty()) type = h.getHospitalType();

        String emergencyContact = readString("New Emergency Contact (leave blank to keep current): ");
        if (emergencyContact.isEmpty()) {
            emergencyContact = h.getEmergencyContact();
        } else {
            while (!val.validatePhoneNumber(emergencyContact)) {
                System.out.println("Invalid phone.");
                emergencyContact = readString("New Emergency Contact: ");
            }
        }

        h.updateOrganization(name, address, phone);
        h.updateHospitalDetails(type, emergencyContact);
        Main.saveHospitalsToFile();
        System.out.println("Hospital details updated successfully.");
    }

    private void deleteHospitalFlow() {
        System.out.println("\n--- Delete Hospital ---");
        String id = readString("Enter Hospital ID: ");
        Hospital h = findHospitalById(id);
        if (h != null) {
            Main.hospitals.remove(h);
            Main.saveHospitalsToFile();
            System.out.println("Hospital " + h.getOrganizationName() + " deleted successfully.");
        } else {
            System.out.println("Hospital not found.");
        }
    }

    private void searchHospitalFlow() {
        System.out.println("\n--- Search Hospital ---");
        String query = readString("Enter Hospital ID or Name: ");
        boolean found = false;
        for (Hospital h : Main.hospitals) {
            if (h.getOrganizationId().equalsIgnoreCase(query) || h.getOrganizationName().toLowerCase().contains(query.toLowerCase())) {
                h.displayOrganization();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No matching hospital found.");
        }
    }

    private void viewAllHospitals() {
        System.out.println("\n--- Registered Hospitals List ---");
        if (Main.hospitals.isEmpty()) {
            System.out.println("No hospitals registered yet.");
            return;
        }
        for (Hospital h : Main.hospitals) {
            System.out.println("ID: " + h.getOrganizationId() + " | Name: " + h.getOrganizationName() + 
                               " | Phone: " + h.getContactNumber() + " | Type: " + h.getHospitalType());
        }
    }

    private Hospital findHospitalById(String id) {
        for (Hospital h : Main.hospitals) {
            if (h.getOrganizationId().equalsIgnoreCase(id)) {
                return h;
            }
        }
        return null;
    }

    // --- Module 5: Blood Donation ---
    public void donationMenu() {
        while (true) {
            System.out.println("\n--- Blood Donation Management ---");
            System.out.println("1. Record New Donation");
            System.out.println("2. View Donation History");
            System.out.println("3. Back to Dashboard");
            int choice = readInt("Enter choice (1-3): ");

            switch (choice) {
                case 1: recordDonationFlow(); break;
                case 2: viewDonationHistory(); break;
                case 3: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void recordDonationFlow() {
        System.out.println("\n--- Record New Blood Donation ---");
        String donorId = readString("Enter Donor ID: ");
        Donor d = findDonorById(donorId);
        if (d == null) {
            System.out.println("Error: Donor ID not registered. Register donor first.");
            return;
        }

        // Verify eligibility
        if (!d.isEligible()) {
            System.out.println("Error: Donor " + d.getName() + " is NOT eligible for donation. Details:");
            d.displayDetails();
            return;
        }

        // Perform check of last donation date (must be > 90 days ago ideally, but let's warn if it is within 90 days)
        if (!d.getLastDonationDate().isEmpty()) {
            try {
                LocalDate last = LocalDate.parse(d.getLastDonationDate());
                LocalDate now = LocalDate.now();
                long days = java.time.temporal.ChronoUnit.DAYS.between(last, now);
                if (days < 90) {
                    System.out.println("WARNING: Donor last donated " + days + " days ago. Minimum interval is 90 days.");
                    String proceed = readString("Do you want to override and proceed anyway? (yes/no): ");
                    if (!proceed.equalsIgnoreCase("yes")) {
                        System.out.println("Donation cancelled.");
                        return;
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors and proceed
            }
        }

        int quantity = readInt("Quantity (units, e.g. 1): ");
        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        String txnId = "DON_TXN_" + System.currentTimeMillis();
        String dateStr = LocalDate.now().toString();

        BloodDonation bd = new BloodDonation(txnId, dateStr, "Completed", donorId, d.getBloodGroup(), quantity);
        bd.recordDonation();
        
        // Add to inventory
        String unitId = "UNIT_" + System.currentTimeMillis();
        String collectionDate = dateStr;
        String expiryDate = LocalDate.now().plusDays(42).toString(); // Blood units expire in 42 days (standard)
        
        BloodUnit bu = new BloodUnit(unitId, d.getBloodGroup(), quantity, collectionDate, expiryDate);
        Main.inventory.addBloodUnit(bu);
        Main.donations.add(bd);

        // Update donor's last donation date
        d.setLastDonationDate(dateStr);
        d.setEligible(d.checkEligibility());

        // Save states to files
        Main.saveDonationsToFile();
        Main.saveBloodUnitsToFile();
        Main.saveDonorsToFile();

        System.out.println("Blood donation recorded and inventory updated.");
        bd.displayTransaction();
    }

    private void viewDonationHistory() {
        System.out.println("\n--- Blood Donations History ---");
        if (Main.donations.isEmpty()) {
            System.out.println("No donations recorded yet.");
            return;
        }
        for (BloodDonation bd : Main.donations) {
            System.out.println("Txn ID: " + bd.getTransactionId() + " | Donor ID: " + bd.getDonorId() + 
                               " | Group: " + bd.getBloodGroup() + " | Qty: " + bd.getQuantity() + " | Date: " + bd.getTransactionDate());
        }
    }

    // --- Module 7: Blood Requests ---
    public void requestMenu() {
        while (true) {
            System.out.println("\n--- Blood Request Management ---");
            System.out.println("1. Accept Recipient/Hospital Request");
            System.out.println("2. View All Requests");
            System.out.println("3. Process Request (Approve/Reject)");
            System.out.println("4. Back to Dashboard");
            int choice = readInt("Enter choice (1-4): ");

            switch (choice) {
                case 1: acceptRequestFlow(); break;
                case 2: viewAllRequests(); break;
                case 3: processRequestFlow(); break;
                case 4: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void acceptRequestFlow() {
        System.out.println("\n--- Accept New Blood Request ---");
        String recId = readString("Enter Recipient ID: ");
        Recipient r = findRecipientById(recId);
        if (r == null) {
            System.out.println("Error: Recipient not registered. Register recipient first.");
            return;
        }

        String hospId = readString("Enter Hospital ID (leave blank if direct recipient request): ");
        if (!hospId.isEmpty() && findHospitalById(hospId) == null) {
            System.out.println("Warning: Hospital ID not registered.");
        }

        int units = readInt("Units Required: ");
        if (units <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        String txnId = "REQ_TXN_" + System.currentTimeMillis();
        String dateStr = LocalDate.now().toString();

        BloodRequest req = new BloodRequest(txnId, dateStr, "Pending", recId, hospId, r.getBloodGroup(), units);
        Main.requests.add(req);
        Main.saveRequestsToFile();
        System.out.println("Blood request recorded with status: Pending.");
        req.displayTransaction();
    }

    private void viewAllRequests() {
        System.out.println("\n--- Blood Requests List ---");
        if (Main.requests.isEmpty()) {
            System.out.println("No requests recorded.");
            return;
        }
        for (BloodRequest req : Main.requests) {
            System.out.println("ID: " + req.getTransactionId() + " | Recipient ID: " + req.getRecipientId() + 
                               " | Group: " + req.getBloodGroup() + " | Units: " + req.getUnitsRequested() + 
                               " | Status: " + req.getStatus() + " | Date: " + req.getTransactionDate());
        }
    }

    private void processRequestFlow() {
        System.out.println("\n--- Process Pending Blood Request ---");
        String id = readString("Enter Request (Txn) ID: ");
        BloodRequest req = findRequestById(id);
        if (req == null) {
            System.out.println("Request not found.");
            return;
        }

        if (!"Pending".equalsIgnoreCase(req.getStatus())) {
            System.out.println("Request has already been processed (Status: " + req.getStatus() + ").");
            return;
        }

        req.displayTransaction();
        
        // Check availability
        int stockAvailable = Main.inventory.getStockForGroup(req.getBloodGroup());
        System.out.println("Current Stock for Group " + req.getBloodGroup() + ": " + stockAvailable + " units.");

        if (stockAvailable < req.getUnitsRequested()) {
            System.out.println("INSUFFICIENT STOCK. You can reject the request or hold for stock.");
            String action = readString("Do you want to Reject this request? (yes/no): ");
            if (action.equalsIgnoreCase("yes")) {
                req.rejectRequest();
                Main.saveRequestsToFile();
                System.out.println("Request Rejected.");
            }
            return;
        }

        String action = readString("Approve Request? (yes/no): ");
        if (action.equalsIgnoreCase("yes")) {
            // Deduct stock from oldest units first (FIFO order)
            int unitsToDeduct = req.getUnitsRequested();
            List<BloodUnit> activeUnits = Main.inventory.searchBloodGroup(req.getBloodGroup());
            
            // Sort active units by collection date to implement FIFO
            activeUnits.sort((u1, u2) -> u1.getCollectionDate().compareTo(u2.getCollectionDate()));

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

            req.approveRequest();
            Main.saveRequestsToFile();
            Main.saveBloodUnitsToFile();
            System.out.println("Request Approved and stock deducted.");
        } else {
            System.out.println("No action taken.");
        }
    }

    private BloodRequest findRequestById(String id) {
        for (BloodRequest req : Main.requests) {
            if (req.getTransactionId().equalsIgnoreCase(id)) {
                return req;
            }
        }
        return null;
    }

    // --- Module 8: Blood Transfer ---
    public void transferMenu() {
        while (true) {
            System.out.println("\n--- Blood Transfer Management ---");
            System.out.println("1. Record Blood Transfer");
            System.out.println("2. View Transfer History");
            System.out.println("3. Back to Dashboard");
            int choice = readInt("Enter choice (1-3): ");

            switch (choice) {
                case 1: recordTransferFlow(); break;
                case 2: viewTransferHistory(); break;
                case 3: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void recordTransferFlow() {
        System.out.println("\n--- Record Blood Transfer between Blood Banks ---");
        String srcBB = readString("Source Blood Bank ID (e.g. BB_01): ");
        String destBB = readString("Destination Blood Bank ID (e.g. BB_02): ");
        String bg = readString("Blood Group: ");
        while (!val.validateBloodGroup(bg)) {
            System.out.println("Invalid blood group.");
            bg = readString("Blood Group: ");
        }
        int units = readInt("Units to Transfer: ");
        if (units <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        // Deduct from our stock if we are source
        int stockAvailable = Main.inventory.getStockForGroup(bg);
        if (stockAvailable < units) {
            System.out.println("Error: Insufficient stock of " + bg + " in inventory (Available: " + stockAvailable + " units).");
            return;
        }

        int unitsToDeduct = units;
        List<BloodUnit> activeUnits = Main.inventory.searchBloodGroup(bg);
        activeUnits.sort((u1, u2) -> u1.getCollectionDate().compareTo(u2.getCollectionDate()));

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

        String txnId = "TRF_TXN_" + System.currentTimeMillis();
        String dateStr = LocalDate.now().toString();

        BloodTransfer bt = new BloodTransfer(txnId, dateStr, "Completed", srcBB, destBB, bg, units);
        bt.transferUnits();
        
        Main.transfers.add(bt);
        Main.saveTransfersToFile();
        Main.saveBloodUnitsToFile();
        
        System.out.println("Blood transfer recorded and stock updated.");
        bt.displayTransaction();
    }

    private void viewTransferHistory() {
        System.out.println("\n--- Blood Transfer History ---");
        if (Main.transfers.isEmpty()) {
            System.out.println("No transfer records found.");
            return;
        }
        for (BloodTransfer bt : Main.transfers) {
            System.out.println("Txn ID: " + bt.getTransactionId() + " | Source: " + bt.getSourceBloodBankId() + 
                               " | Dest: " + bt.getDestinationBloodBankId() + " | Group: " + bt.getBloodGroup() + 
                               " | Units: " + bt.getUnitsTransferred() + " | Date: " + bt.getTransactionDate());
        }
    }

    // --- Module 9: Alert System ---
    public void alertMenu() {
        while (true) {
            System.out.println("\n--- Alert & Expiry Monitor ---");
            System.out.println("1. Run Low Stock Check");
            System.out.println("2. Run Expiry Check");
            System.out.println("3. Configure Alert Thresholds");
            System.out.println("4. Back to Dashboard");
            int choice = readInt("Enter choice (1-4): ");

            switch (choice) {
                case 1: Main.alertManager.checkLowStock(Main.inventory); break;
                case 2: Main.alertManager.checkExpiry(Main.inventory); break;
                case 3:
                    int stockThreshold = readInt("Enter new Low Stock Threshold: ");
                    int expiryDays = readInt("Enter new Expiry Warning Threshold (days): ");
                    Main.alertManager.setLowStockThreshold(stockThreshold);
                    Main.alertManager.setExpiryAlertDays(expiryDays);
                    System.out.println("Alert thresholds updated successfully.");
                    break;
                case 4: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // --- Module 10: Reports Menu ---
    public void reportsMenu() {
        while (true) {
            System.out.println("\n--- Reports Dashboard ---");
            System.out.println("1. Current Inventory Stock Report");
            System.out.println("2. Blood Donation History Report");
            System.out.println("3. Recipient/Hospital Requests Report");
            System.out.println("4. Blood Transfer History Report");
            System.out.println("5. Back to Dashboard");
            int choice = readInt("Enter choice (1-5): ");

            switch (choice) {
                case 1: Main.reportGenerator.generateInventoryReport(Main.inventory); break;
                case 2: Main.reportGenerator.generateDonationReport(Main.donations); break;
                case 3: Main.reportGenerator.generateRequestReport(Main.requests); break;
                case 4: Main.reportGenerator.generateTransferReport(Main.transfers); break;
                case 5: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
}
