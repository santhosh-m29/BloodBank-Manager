package bloodbank;

public class Admin extends Person {
    private String username;
    private String password;
    private String role;

    public Admin(String personId, String name, int age, String gender, String phoneNumber, String address,
                 String username, String password, String role) {
        super(personId, name, age, gender, phoneNumber, address);
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String getDetails() {
        return "Admin [ID: " + personId + ", Name: " + name + ", Role: " + role + ", Username: " + username + "]";
    }

    @Override
    public void updateDetails(String name, int age, String gender, String phoneNumber, String address) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    @Override
    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Admin Details:");
        System.out.println("ID: " + personId);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Username: " + username);
        System.out.println("Role: " + role);
        System.out.println("----------------------------------------");
    }

    public boolean login() {
        // Handled in LoginManager, but provided here as model operation
        return true;
    }

    public void logout() {
        System.out.println("Admin " + username + " logged out successfully.");
    }

    public void manageInventory() {
        System.out.println("Managing inventory...");
    }

    public void approveBloodRequest() {
        System.out.println("Approving blood request...");
    }

    public void generateReports() {
        System.out.println("Generating reports...");
    }
}
