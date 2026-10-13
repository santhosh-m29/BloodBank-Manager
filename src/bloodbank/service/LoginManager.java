package bloodbank.service;

import bloodbank.Main;
import bloodbank.model.*;
import bloodbank.utility.Validation;

public class LoginManager {
    private String username;
    private String password;
    private String userRole;
    private BloodBankAdmin loggedInAdmin;
    private HospitalStaff loggedInStaff;
    private Donor loggedInDonor;
    private Patient loggedInPatient;

    public LoginManager() {
        this.username = "";
        this.password = "";
        this.userRole = "";
        this.loggedInAdmin = null;
        this.loggedInStaff = null;
        this.loggedInDonor = null;
        this.loggedInPatient = null;
    }

    public boolean isUsernameTaken(String username) {
        for (BloodBankAdmin admin : Main.admins) {
            if (admin.getUsername().equalsIgnoreCase(username)) return true;
        }
        for (HospitalStaff staff : Main.hospitalStaffs) {
            if (staff.getUsername().equalsIgnoreCase(username)) return true;
        }
        for (Donor donor : Main.donors) {
            if (donor.getUsername().equalsIgnoreCase(username)) return true;
        }
        for (Patient patient : Main.patients) {
            if (patient.getUsername().equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    public void registerUser() {
        System.out.println("\n--- User Registration ---");
        System.out.println("Select Registration Role:");
        System.out.println("1. Donor\n2. Hospital Staff");
        System.out.print("Enter choice: ");
        int roleChoice;
        try {
            roleChoice = Integer.parseInt(Main.sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice. Registration cancelled.");
            return;
        }
        System.out.print("Enter Full Name: ");
        String name = Main.sc.nextLine().trim();
        System.out.print("Enter Age: ");
        int age;
        try {
            age = Integer.parseInt(Main.sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Registration cancelled.");
            return;
        }
        System.out.print("Enter Gender: ");
        String gender = Main.sc.nextLine().trim();
        System.out.print("Enter Phone Number: ");
        String phone = Main.sc.nextLine().trim();
        while (!Validation.validatePhoneNumber(phone)) {
            System.out.println("Invalid phone");
            System.out.print("Enter Phone Number: ");
            phone = Main.sc.nextLine().trim();
        }
        System.out.print("Enter Address: ");
        String address = Main.sc.nextLine().trim();
        System.out.print("Choose Username: ");
        String username = Main.sc.nextLine().trim();
        while (isUsernameTaken(username)) {
            System.out.println("Username is already taken. Try another.");
            System.out.print("Choose Username: ");
            username = Main.sc.nextLine().trim();
        }
        System.out.print("Choose Password: ");
        String password = Main.sc.nextLine().trim();

        if (roleChoice == 1) {
            String group = readBloodGroup("Blood Group (e.g. A+, O-): ");
            System.out.print("Hemoglobin level (g/dL): ");
            double haemoglobin;
            try {
                haemoglobin = Double.parseDouble(Main.sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid hemoglobin level. Registration cancelled.");
                return;
            }
            System.out.print("Weight (kg): ");
            double weight;
            try {
                weight = Double.parseDouble(Main.sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid weight. Registration cancelled.");
                return;
            }
            String id = Main.nextId("DON");
            Main.donors.add(new Donor(id, name, age, gender, phone, address,
                    username, password, "DONOR", group, haemoglobin, weight, ""));
            Main.saveDonorsToFile();
            System.out.println("Donor registered successfully! Your ID is: " + id);
        } else if (roleChoice == 2) {
            String hospitalId = chooseHospital();
            System.out.print("Enter Department: ");
            String department = Main.sc.nextLine().trim();
            String id = Main.nextId("STF");
            HospitalStaff staff = new HospitalStaff(id, name, age, gender, phone, address,
                    username, password, "HOSPITAL_STAFF", Main.nextId("EMP"), hospitalId, department);
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

    private String chooseHospital() {
        System.out.println("Available Hospitals:");
        Main.hospitals.forEach(hospital -> System.out.println(
                " - " + hospital.getOrganizationId() + ": " + hospital.getOrganizationName()));
        System.out.print("Enter Hospital ID: ");
        return Main.sc.nextLine().trim();
    }

    private String readBloodGroup(String prompt) {
        System.out.print(prompt);
        String group = Main.sc.nextLine().trim();
        while (!Validation.validateBloodGroup(group)) {
            System.out.println("Invalid blood group.");
            System.out.print(prompt);
            group = Main.sc.nextLine().trim();
        }
        return group;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserRole() { return userRole; }
    public BloodBankAdmin getLoggedInAdmin() { return loggedInAdmin; }
    public HospitalStaff getLoggedInStaff() { return loggedInStaff; }
    public Donor getLoggedInDonor() { return loggedInDonor; }
    public Patient getLoggedInPatient() { return loggedInPatient; }

    public String getLoggedInName() {
        if (loggedInAdmin != null) return loggedInAdmin.getName();
        if (loggedInStaff != null) return loggedInStaff.getName();
        if (loggedInDonor != null) return loggedInDonor.getName();
        if (loggedInPatient != null) return loggedInPatient.getName();
        return "";
    }

    public boolean authenticateUser(String username, String password) {
        // Clear previous state
        this.loggedInAdmin = null;
        this.loggedInStaff = null;
        this.loggedInDonor = null;
        this.loggedInPatient = null;
        this.userRole = "";

        for (BloodBankAdmin admin : Main.admins) {
            if (admin.getUsername().equalsIgnoreCase(username) && admin.getPassword().equals(password)) {
                this.username = username;
                this.password = password;
                this.userRole = "BLOOD_BANK_ADMIN";
                this.loggedInAdmin = admin;
                return true;
            }
        }
        for (HospitalStaff staff : Main.hospitalStaffs) {
            if (staff.getUsername().equalsIgnoreCase(username) && staff.getPassword().equals(password)) {
                this.username = username;
                this.password = password;
                this.userRole = "HOSPITAL_STAFF";
                this.loggedInStaff = staff;
                return true;
            }
        }
        for (Donor donor : Main.donors) {
            if (donor.getUsername().equalsIgnoreCase(username) && donor.getPassword().equals(password)) {
                this.username = username;
                this.password = password;
                this.userRole = "DONOR";
                this.loggedInDonor = donor;
                return true;
            }
        }
        for (Patient patient : Main.patients) {
            if (patient.getUsername().equalsIgnoreCase(username) && patient.getPassword().equals(password)) {
                this.username = username;
                this.password = password;
                this.userRole = "PATIENT";
                this.loggedInPatient = patient;
                return true;
            }
        }
        return false;
    }

    public void changePassword(String newPassword) {
        if (loggedInAdmin == null && loggedInStaff == null && loggedInDonor == null && loggedInPatient == null) {
            System.out.println("No user is currently logged in.");
            return;
        }

        this.password = newPassword;

        if (loggedInAdmin != null) {
            loggedInAdmin.setPassword(newPassword);
            Main.saveAdminsToFile();
        } else if (loggedInStaff != null) {
            loggedInStaff.setPassword(newPassword);
            Main.saveHospitalStaffsToFile();
        } else if (loggedInDonor != null) {
            loggedInDonor.setPassword(newPassword);
            Main.saveDonorsToFile();
        } else if (loggedInPatient != null) {
            loggedInPatient.setPassword(newPassword);
            Main.savePatientsToFile();
        }
        System.out.println("Password changed successfully.");
    }

    public void logoutUser() {
        if (loggedInAdmin != null || loggedInStaff != null || loggedInDonor != null || loggedInPatient != null) {
            System.out.println("User '" + username + "' logged out successfully.");
            this.loggedInAdmin = null;
            this.loggedInStaff = null;
            this.loggedInDonor = null;
            this.loggedInPatient = null;
            this.userRole = "";
            this.username = "";
            this.password = "";
        } else {
            System.out.println("No user is currently logged in.");
        }
    }
}
