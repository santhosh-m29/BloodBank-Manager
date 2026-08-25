package bloodbank;

public class LoginManager {
    private String username;
    private String password;
    private Person loggedInUser;

    public LoginManager() {
        this.username = "";
        this.password = "";
        this.loggedInUser = null;
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

    // Parameterless compliance method
    public boolean authenticateUser() {
        return loggedInUser != null;
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

    // Parameterless compliance method
    public void changePassword() {
        System.out.println("Use changePassword(String newPassword) to update your password.");
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
