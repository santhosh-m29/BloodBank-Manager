package bloodbank.service;

import bloodbank.Main;
import bloodbank.model.*;
import bloodbank.utility.Validation;

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
        for (Hospital hospital : Main.hospitals) {
            System.out.println(" - " + hospital.getOrganizationId() + ": " + hospital.getOrganizationName());
        }
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
