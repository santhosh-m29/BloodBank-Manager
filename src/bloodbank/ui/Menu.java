package bloodbank.ui;

import bloodbank.Main;
import bloodbank.model.*;

import java.util.Scanner;

public class Menu {
    private Scanner scanner;

    public Menu() {
        this.scanner = new Scanner(System.in);
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
            if (choiceVal == 1) {
                login();
            } else if (choiceVal == 2) {
                Main.loginManager.registerUser(scanner);
            } else if (choiceVal == 3) {
                Main.closeApplication();
                System.out.println("Goodbye!");
                System.exit(0);
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void login() {
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
            System.out.println("5. Donate Blood");
            System.out.println("6. Change Password");
            System.out.println("7. Logout");
            System.out.println("==================================================");
            int choiceVal = readInt("Enter choice (1-7): ");
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
                case 5: donor.donate(scanner); break;
                case 6:
                    String newPass = readString("Enter New Password: ");
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
            switch (choiceVal) {
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
            System.out.println("3. Check Hospital Stock");
            System.out.println("4. Issue Blood to Patient");
            System.out.println("5. Request Restock from Blood Bank");
            System.out.println("6. View Request History");
            System.out.println("7. View Hospital Inventory");
            System.out.println("8. Change Password");
            System.out.println("9. Logout");
            System.out.println("==================================================");
            int choiceVal = readInt("Enter choice (1-9): ");
            switch (choiceVal) {
                case 1:
                    staff.createPatient(scanner);
                    break;
                case 2:
                    staff.requestBlood(scanner);
                    break;
                case 3:
                    if (hospital != null) {
                        hospital.viewInventory();
                    } else {
                        System.out.println("Facility association missing.");
                    }
                    break;
                case 4:
                    staff.issueBlood(scanner);
                    break;
                case 5:
                    staff.requestRestock(scanner);
                    break;
                case 6:
                    if (hospital != null) hospital.viewRequestHistory();
                    break;
                case 7:
                    if (hospital != null) hospital.viewInventory();
                    break;
                case 8:
                    String newPass = readString("Enter New Password: ");
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
        BloodBankAdmin admin = (BloodBankAdmin) Main.loginManager.getLoggedInUser();
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("            BLOOD BANK ADMIN DASHBOARD            ");
            System.out.println("==================================================");
            System.out.println("Admin: " + admin.getName() + " | Facility: " + admin.getFacilityId());
            System.out.println("1. View Donations");
            System.out.println("2. Approve/Reject Requests & Dispatch Transfer");
            System.out.println("3. View Blood-Bank Inventory");
            System.out.println("4. Remove Blood Unit Manually");
            System.out.println("5. View Expiry & Low-Stock Alerts");
            System.out.println("6. Generate Inventory Report");
            System.out.println("7. Generate Donation Report");
            System.out.println("8. Generate Request Report");
            System.out.println("9. Manage Facilities / Users");
            System.out.println("10. Change Password");
            System.out.println("11. Logout");
            System.out.println("==================================================");
            int choiceVal = readInt("Enter choice (1-11): ");
            switch (choiceVal) {
                case 1:
                    System.out.println("\n--- Donations ---");
                    if (Main.donations.isEmpty()) System.out.println("No donations recorded.");
                    for (BloodDonation donation : Main.donations) donation.displayTransaction();
                    break;
                case 2: admin.reviewRequestsAndDispatch(scanner); break;
                case 3:
                    System.out.println("\nCentral Inventory Stock:");
                    Main.inventory.displayInventory();
                    break;
                case 4: admin.removeBloodUnit(scanner); break;
                case 5:
                    Main.alertManager.checkLowStock();
                    Main.alertManager.checkExpiry();
                    break;
                case 6: Main.reportGenerator.generateInventoryReport(); break;
                case 7: Main.reportGenerator.generateDonationReport(); break;
                case 8: Main.reportGenerator.generateRequestReport(); break;
                case 9: admin.manageFacilities(scanner); break;
                case 10:
                    String newPass = readString("Enter New Password: ");
                    Main.loginManager.changePassword(newPass);
                    break;
                case 11:
                    Main.loginManager.logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }


}
