package bloodbank.ui;

import bloodbank.Main;
import bloodbank.model.*;

public class Menu {
    public Menu() {}

    public void displayMainMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     BLOOD BANK MANAGEMENT SYSTEM - MAIN MENU     ");
            System.out.println("==================================================");
            System.out.println("1. Login");
            System.out.println("2. Register New User");
            System.out.println("3. Exit System");
            System.out.println("==================================================");
            System.out.print("Enter choice: ");
            int choiceVal;
            try {
                choiceVal = Integer.parseInt(Main.sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                continue;
            }
            if (choiceVal == 1) {
                login();
            } else if (choiceVal == 2) {
                Main.loginManager.registerUser();
            } else if (choiceVal == 3) {
                Main.closeApplication();
                System.out.println("Goodbye");
                System.exit(0);
            } else {
                System.out.println("Invalid choice");
            }
        }
    }

    private void login() {
        System.out.println("\n--- User Login ---");
        System.out.print("Enter Username: ");
        String username = Main.sc.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = Main.sc.nextLine().trim();

        boolean authenticated = Main.loginManager.authenticateUser(username, password);
        if (authenticated) {
            String role = Main.loginManager.getUserRole();
            System.out.println("\nLogin Successful! Welcome, " + Main.loginManager.getLoggedInName() + " [" + role + "]");
            
            if ("BLOOD_BANK_ADMIN".equalsIgnoreCase(role)) {
                adminMenu();
            } else if ("HOSPITAL_STAFF".equalsIgnoreCase(role)) {
                staffMenu();
            } else if ("DONOR".equalsIgnoreCase(role)) {
                donorMenu();
            } else if ("PATIENT".equalsIgnoreCase(role)) {
                patientMenu();
            }
        } else {
            System.out.println("\nInvalid Username or Password. Login failed.");
        }
    }
    // --- Donor Dashboard Menu ---
    public void donorMenu() {
        Donor donor = Main.loginManager.getLoggedInDonor();
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("                 DONOR DASHBOARD                  ");
            System.out.println("==================================================");
            System.out.println("Logged in as: " + donor.getName() + " | Blood Group: " + donor.getBloodGroup());
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. Check Eligibility");
            System.out.println("4. View Donation History");
            System.out.println("5. Donate Blood");
            System.out.println("6. Change Password");
            System.out.println("7. Logout");
            System.out.println("==================================================");
            System.out.print("Enter choice: ");
            int choiceVal;
            try {
                choiceVal = Integer.parseInt(Main.sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                continue;
            }
            switch (choiceVal) {
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
                case 5: donor.donate(); break;
                case 6:
                    System.out.print("Enter New Password: ");
                    String newPass = Main.sc.nextLine().trim();
                    Main.loginManager.changePassword(newPass);
                    break;
                case 7:
                    Main.loginManager.logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // --- Patient Dashboard Menu ---
    public void patientMenu() {
        Patient patient = Main.loginManager.getLoggedInPatient();
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
            System.out.print("Enter choice: ");
            int choiceVal;
            try {
                choiceVal = Integer.parseInt(Main.sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                continue;
            }
            switch (choiceVal) {
                case 1: patient.displayDetails(); break;
                case 2: patient.updateDetails(); break;
                case 3: patient.viewRequestStatus(); break;
                case 4:
                    System.out.print("Enter New Password: ");
                    String newPass = Main.sc.nextLine().trim();
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
        HospitalStaff staff = Main.loginManager.getLoggedInStaff();
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
            System.out.println("3. View Hospital Inventory");
            System.out.println("4. Issue Blood to Patient");
            System.out.println("5. Request Restock from Blood Bank");
            System.out.println("6. View Patient Request History");
            System.out.println("7. View Hospital Stock Request History");
            System.out.println("8. Change Password");
            System.out.println("9. Logout");
            System.out.println("==================================================");
            System.out.print("Enter choice: ");
            int choiceVal;
            try {
                choiceVal = Integer.parseInt(Main.sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                continue;
            }
            switch (choiceVal) {
                case 1:
                    staff.createPatient();
                    break;
                case 2:
                    staff.requestBlood();
                    break;
                case 3:
                    if (hospital != null) {
                        hospital.viewInventory();
                    } else {
                        System.out.println("Facility association missing.");
                    }
                    break;
                case 4:
                    staff.issueBlood();
                    break;
                case 5:
                    staff.requestRestock();
                    break;
                case 6:
                    if (hospital != null) hospital.viewRequestHistory();
                    break;
                case 7:
                    if (hospital != null) hospital.viewRestockHistory();
                    break;
                case 8:
                    System.out.print("Enter New Password: ");
                    String newPass = Main.sc.nextLine().trim();
                    Main.loginManager.changePassword(newPass);
                    break;
                case 9:
                    Main.loginManager.logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }


    // --- Blood Bank Admin Dashboard Menu ---
    public void adminMenu() {
        BloodBankAdmin admin = Main.loginManager.getLoggedInAdmin();
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("            BLOOD BANK ADMIN DASHBOARD            ");
            System.out.println("==================================================");
            System.out.println("Admin: " + admin.getName() + " | Facility: " + admin.getFacilityId());
            System.out.println("1. View Donations");
            System.out.println("2. Approve Donations");
            System.out.println("3. Approve Hospital Stock Requests");
            System.out.println("4. View Blood-Bank Inventory");
            System.out.println("5. View Hospital Stocks");
            System.out.println("6. View Hospital Stock Request History");
            System.out.println("7. Manage Hospitals");
            System.out.println("8. Change Password");
            System.out.println("9. Logout");
            System.out.println("==================================================");
            System.out.print("Enter choice: ");
            int choiceVal;
            try {
                choiceVal = Integer.parseInt(Main.sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                continue;
            }
            switch (choiceVal) {
                case 1:
                    System.out.println("\n--- Donations ---");
                    if (Main.donations.isEmpty()) System.out.println("No donations recorded.");
                    for (BloodDonation donation : Main.donations) donation.displayTransaction();
                    break;
                case 2: admin.approvePassedDonations(); break;
                case 3: admin.reviewHospitalRestockRequests(); break;
                case 4:
                    System.out.println("\n--- Central Blood Bank Facility & Inventory ---");
                    if (Main.bloodBank != null) {
                        // Dynamic polymorphism via Parent reference
                        Main.bloodBank.displayOrganization();
                        // Explicit typecast from parent reference to child class BloodBank
                        BloodBank centralBank = (BloodBank) Main.bloodBank;
                        centralBank.viewInventory();
                    } else {
                        Main.inventory.displayInventory();
                    }
                    break;
                case 5:
                    Main.alertManager.displayHospitalStock();
                    Main.alertManager.checkExpiry();
                    break;
                case 6: admin.viewHospitalRestockHistory(); break;
                case 7: admin.manageFacilities(); break;
                case 8:
                    System.out.print("Enter New Password: ");
                    String newPass = Main.sc.nextLine().trim();
                    Main.loginManager.changePassword(newPass);
                    break;
                case 9:
                    Main.loginManager.logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }


}
