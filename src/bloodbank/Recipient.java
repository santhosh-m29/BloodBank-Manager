package bloodbank;

public class Recipient extends Person {
    private String bloodGroup;
    private String disease;
    private String doctorName;
    private int unitsRequired;
    private String hospitalId;

    public Recipient(String personId, String name, int age, String gender, String phoneNumber, String address,
                     String bloodGroup, String disease, String doctorName, int unitsRequired, String hospitalId) {
        super(personId, name, age, gender, phoneNumber, address);
        this.bloodGroup = bloodGroup;
        this.disease = disease;
        this.doctorName = doctorName;
        this.unitsRequired = unitsRequired;
        this.hospitalId = hospitalId;
    }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getDisease() { return disease; }
    public void setDisease(String disease) { this.disease = disease; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public int getUnitsRequired() { return unitsRequired; }
    public void setUnitsRequired(int unitsRequired) { this.unitsRequired = unitsRequired; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    @Override
    public String getDetails() {
        return "Recipient [ID: " + personId + ", Name: " + name + ", Blood Group: " + bloodGroup + 
               ", Required Units: " + unitsRequired + ", Hospital ID: " + hospitalId + "]";
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
        System.out.println("Recipient Details:");
        System.out.println("ID: " + personId);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Blood Group Required: " + bloodGroup);
        System.out.println("Disease/Condition: " + disease);
        System.out.println("Doctor: " + doctorName);
        System.out.println("Units Required: " + unitsRequired);
        System.out.println("Hospital ID: " + hospitalId);
        System.out.println("----------------------------------------");
    }

    public void requestBlood() {
        System.out.println("Recipient " + name + " has requested " + unitsRequired + " units of " + bloodGroup + ".");
    }

    public void receiveBlood() {
        System.out.println("Recipient " + name + " has received blood.");
    }

    public void viewRequestStatus() {
        System.out.println("Viewing request status for recipient: " + name);
    }
}
