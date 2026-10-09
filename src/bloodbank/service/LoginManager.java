package bloodbank.service;

import bloodbank.Main;
import bloodbank.model.*;
import bloodbank.utility.Validation;
import java.util.Scanner;

public class LoginManager {
    private String username;
    private String password;
    private Person loggedInUser;

    public LoginManager() {
        this.username = "";
        this.password = "";
        this.loggedInUser = null;
    }

    public boolean isUsernameTaken(String username) {
        for (BloodBankAdmin admin : Main.admins) if (admin.getUsername().equalsIgnoreCase(username)) return true;
        for (HospitalStaff staff : Main.hospitalStaffs) if (staff.getUsername().equalsIgnoreCase(username)) return true;
        for (Donor donor : Main.donors) if (donor.getUsername().equalsIgnoreCase(username)) return true;
        for (Patient patient : Main.patients) if (patient.getUsername().equalsIgnoreCase(username)) return true;
        return false;
    }

    public void registerUser(Scanner scanner) {
        System.out.println("\n--- User Registration ---");
        System.out.println("Select Registration Role:");
        System.out.println("1. Donor\n2. Patient\n3. Hospital Staff");
        int roleChoice = readInt(scanner, "Enter choice (1-3): ");
        String name = ask(scanner, "Enter Full Name: ");
        int age = readInt(scanner, "Enter Age: ");
        String gender = ask(scanner, "Enter Gender: ");
        String phone = ask(scanner, "Enter Phone Number: ");
        while (!Validation.validatePhoneNumber(phone)) {
            System.out.println("Invalid phone (must be 10 digits).");
            phone = ask(scanner, "Enter Phone Number: ");
        }
        String address = ask(scanner, "Enter Address: ");
        String username = ask(scanner, "Choose Username: ");
        while (isUsernameTaken(username)) {
            System.out.println("Username is already taken. Try another.");
            username = ask(scanner, "Choose Username: ");
        }
        String password = ask(scanner, "Choose Password: ");

        if (roleChoice == 1) {
            String group = readBloodGroup(scanner, "Blood Group (e.g. A+, O-): ");
            double haemoglobin = readDouble(scanner, "Hemoglobin level (g/dL): ");
            double weight = readDouble(scanner, "Weight (kg): ");
            String id = "DON_" + System.currentTimeMillis();
            Main.donors.add(new Donor(id, name, age, gender, phone, address,
                    username, password, "DONOR", group, haemoglobin, weight, ""));
            Main.saveDonorsToFile();
            System.out.println("Donor registered successfully! Your ID is: " + id);
        } else if (roleChoice == 2) {
            String group = readBloodGroup(scanner, "Blood Group Required (e.g. B+, AB-): ");
            String disease = ask(scanner, "Disease/Reason for Request: ");
            String doctor = ask(scanner, "Referring Doctor: ");
            int units = readInt(scanner, "Units Required: ");
            String hospitalId = chooseHospital(scanner);
            String id = "PAT_" + System.currentTimeMillis();
            Main.patients.add(new Patient(id, name, age, gender, phone, address,
                    username, password, "PATIENT", group, disease, doctor, units, hospitalId));
            Main.savePatientsToFile();
            System.out.println("Patient registered successfully! Your ID is: " + id);
        } else if (roleChoice == 3) {
            String hospitalId = chooseHospital(scanner);
            String department = ask(scanner, "Enter Department: ");
            String id = "STF_" + System.currentTimeMillis();
            HospitalStaff staff = new HospitalStaff(id, name, age, gender, phone, address,
                    username, password, "HOSPITAL_STAFF", "EMP_" + System.currentTimeMillis(), hospitalId, department);
            Main.hospitalStaffs.add(staff);
            Hospital hospital = Hospital.findById(hospitalId);
            if (hospital != null) {
                hospital.getStaffs().add(staff);
                Main.saveHospitalsToFile();
            }
            Main.saveHospitalStaffsToFile();
            System.out.println("Hospital Staff registered successfully! Your ID is: " + id);
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private String chooseHospital(Scanner scanner) {
        System.out.println("Available Hospitals:");
        for (Hospital hospital : Main.hospitals) {
            System.out.println(" - " + hospital.getOrganizationId() + ": " + hospital.getOrganizationName());
        }
        return ask(scanner, "Enter Hospital ID: ");
    }

    private String readBloodGroup(Scanner scanner, String prompt) {
        String group = ask(scanner, prompt);
        while (!Validation.validateBloodGroup(group)) {
            System.out.println("Invalid blood group.");
            group = ask(scanner, prompt);
        }
        return group;
    }

    private String ask(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            try { return Integer.parseInt(ask(scanner, prompt)); }
            catch (NumberFormatException e) { System.out.println("Invalid input. Please enter an integer."); }
        }
    }

    private double readDouble(Scanner scanner, String prompt) {
        while (true) {
            try { return Double.parseDouble(ask(scanner, prompt)); }
            catch (NumberFormatException e) { System.out.println("Invalid input. Please enter a decimal number."); }
        }
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Person getLoggedInUser() { return loggedInUser; }

    public boolean authenticateUser(String username, String password) {
        // 1. Check Blood Bank Admins
        for (BloodBankAdmin admin : Main.admins) {
            if (admin.getUsername().equalsIgnoreCase(username) && admin.getPassword().equals(password)) {
                this.username = username;
                this.password = password;
                this.loggedInUser = admin;
                return true;
            }
        }
        
        // 2. Check Hospital Staff
        for (HospitalStaff staff : Main.hospitalStaffs) {
            if (staff.getUsername().equalsIgnoreCase(username) && staff.getPassword().equals(password)) {
                this.username = username;
                this.password = password;
                this.loggedInUser = staff;
                return true;
            }
        }

        // 3. Check Donors
        for (Donor donor : Main.donors) {
            if (donor.getUsername().equalsIgnoreCase(username) && donor.getPassword().equals(password)) {
                this.username = username;
                this.password = password;
                this.loggedInUser = donor;
                return true;
            }
        }

        // 4. Check Patients
        for (Patient patient : Main.patients) {
            if (patient.getUsername().equalsIgnoreCase(username) && patient.getPassword().equals(password)) {
                this.username = username;
                this.password = password;
                this.loggedInUser = patient;
                return true;
            }
        }

        return false;
    }

    public void changePassword(String newPassword) {
        if (loggedInUser == null) {
            System.out.println("No user is currently logged in.");
            return;
        }
        
        loggedInUser.setPassword(newPassword);
        this.password = newPassword;

        // Save updated details to corresponding files
        if (loggedInUser instanceof BloodBankAdmin) {
            Main.saveAdminsToFile();
        } else if (loggedInUser instanceof HospitalStaff) {
            Main.saveHospitalStaffsToFile();
        } else if (loggedInUser instanceof Donor) {
            Main.saveDonorsToFile();
        } else if (loggedInUser instanceof Patient) {
            Main.savePatientsToFile();
        }
        System.out.println("Password changed successfully.");
    }

    public void logoutUser() {
        if (loggedInUser != null) {
            System.out.println("User '" + loggedInUser.getUsername() + "' logged out successfully.");
            loggedInUser = null;
            this.username = "";
            this.password = "";
        } else {
            System.out.println("No user is currently logged in.");
        }
    }
}
