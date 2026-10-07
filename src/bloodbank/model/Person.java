package bloodbank.model;
import bloodbank.utility.*;
public abstract class Person {
    private final String personId;
    private String name;
    private int age;
    private String gender;
    private String phoneNumber;
    private String address;
    protected Person(String id, String name, int age, String gender, String phone, String address) {
        this.personId = Validation.id(id);
        updateDetails(name, age, gender, phone, address);
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

    public abstract String getRole();
    public String getDetails() {
        return personId + " | " + name + " | Age: " + age + " | " + gender + " | Phone: " + phoneNumber + " | " + address;
    }
    public void displayDetails() {
        System.out.println(getDetails());
    }
}
