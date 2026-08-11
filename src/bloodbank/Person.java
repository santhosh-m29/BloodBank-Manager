package bloodbank;

public abstract class Person {
    protected String personId;
    protected String name;
    protected int age;
    protected String gender;
    protected String phoneNumber;
    protected String address;

    public Person(String personId, String name, int age, String gender, String phoneNumber, String address) {
        this.personId = personId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
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

    public abstract String getDetails();
    public abstract void updateDetails(String name, int age, String gender, String phoneNumber, String address);
    public abstract void displayDetails();
}
