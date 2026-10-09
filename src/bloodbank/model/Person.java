package bloodbank.model;

public abstract class Person {
    protected String personId;
    protected String name;
    protected int age;
    protected String gender;
    protected String phoneNumber;
    protected String address;
    
    // Credentials for role-based authentication
    protected String username;
    protected String password;
    protected String role; // "DONOR", "PATIENT", "HOSPITAL_STAFF", "BLOOD_BANK_ADMIN"

    public Person(String personId, String name, int age, String gender, String phoneNumber, String address,
                  String username, String password, String role) {
        this.personId = personId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public abstract void updateDetails();
    public abstract void displayDetails();
}
