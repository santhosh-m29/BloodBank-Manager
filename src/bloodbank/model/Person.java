package bloodbank.model;
import java.io.Serializable;
import bloodbank.utility.*;
public abstract class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String personId;
    private String name;
    private int age;
    private String gender;
    private String phoneNumber;
    private String address;
    private final String username;
    private String passwordHash;
    protected Person(String id, String name, int age, String gender, String phone, String address, String username, String password) {
        this.personId = Validation.id(id);
        this.username = Validation.id(username);
        updateDetails(name, age, gender, phone, address);
        passwordHash = Passwords.hash(password);
    }
    public final void updateDetails(String name, int age, String gender, String phone, String address) {
        Validation.text(name, "Name");
        Validation.text(gender, "Gender");
        Validation.text(address, "Address");
        Validation.require(Validation.validateAge(age), "Age must be between 0 and 120.");
        Validation.require(Validation.validatePhoneNumber(phone), "Phone number must contain 10 digits.");
        this.name = name.trim();
        this.age = age;
        this.gender = gender.trim();
        this.phoneNumber = phone;
        this.address = address.trim();
    }
    public final boolean authenticate(String password) {
        return Passwords.matches(password, passwordHash);
    }
    public final void changePassword(String oldPassword, String newPassword) {
        Validation.require(authenticate(oldPassword), "Current password is incorrect.");
        passwordHash = Passwords.hash(newPassword);
    }
    public String getPersonId() {
        return personId;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
    public String getGender() {
        return gender;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public String getAddress() {
        return address;
    }
    public String getUsername() {
        return username;
    }
    public abstract String getRole();
    public String getDetails() {
        return personId + " | " + name + " | Age: " + age + " | " + gender + " | Phone: " + phoneNumber + " | " + address;
    }
    public void displayDetails() {
        System.out.println(getDetails());
    }
}
