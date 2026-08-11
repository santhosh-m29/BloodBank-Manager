package bloodbank;

import java.util.List;

public class LoginManager {
    private String username;
    private String password;
    private Admin loggedInAdmin;

    public LoginManager() {
        this.username = "";
        this.password = "";
        this.loggedInAdmin = null;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Admin getLoggedInAdmin() { return loggedInAdmin; }

    public boolean authenticateUser(String username, String password, String adminFilePath) {
        List<Admin> admins = FileManager.loadAdmins(adminFilePath);
        for (Admin admin : admins) {
            if (admin.getUsername().equals(username) && admin.getPassword().equals(password)) {
                this.username = username;
                this.password = password;
                this.loggedInAdmin = admin;
                return true;
            }
        }
        return false;
    }

    public boolean authenticateUser() {
        return false;
    }

    public void changePassword(String newPassword, String adminFilePath) {
        if (loggedInAdmin == null) {
            System.out.println("No administrator currently logged in.");
            return;
        }
        
        List<Admin> admins = FileManager.loadAdmins(adminFilePath);
        boolean updated = false;
        for (Admin admin : admins) {
            if (admin.getUsername().equals(loggedInAdmin.getUsername())) {
                admin.setPassword(newPassword);
                loggedInAdmin.setPassword(newPassword);
                this.password = newPassword;
                updated = true;
                break;
            }
        }
        
        if (updated) {
            FileManager.saveAdmins(adminFilePath, admins);
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Error: Current admin not found in file.");
        }
    }

    public void changePassword() {
        System.out.println("Password changed.");
    }

    public void logoutUser() {
        if (loggedInAdmin != null) {
            System.out.println("Admin '" + loggedInAdmin.getUsername() + "' logged out.");
            loggedInAdmin = null;
            this.username = "";
            this.password = "";
        } else {
            System.out.println("No user is currently logged in.");
        }
    }
}
