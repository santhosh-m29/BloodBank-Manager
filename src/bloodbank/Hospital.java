package bloodbank;

public class Hospital extends Organization {
    private String hospitalType;
    private String emergencyContact;

    public Hospital(String organizationId, String organizationName, String address, String contactNumber,
                    String hospitalType, String emergencyContact) {
        super(organizationId, organizationName, address, contactNumber);
        this.hospitalType = hospitalType;
        this.emergencyContact = emergencyContact;
    }

    public String getHospitalType() { return hospitalType; }
    public void setHospitalType(String hospitalType) { this.hospitalType = hospitalType; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    @Override
    public void displayOrganization() {
        System.out.println("----------------------------------------");
        System.out.println("Hospital Details:");
        System.out.println("ID: " + organizationId);
        System.out.println("Name: " + organizationName);
        System.out.println("Address: " + address);
        System.out.println("Contact No: " + contactNumber);
        System.out.println("Type: " + hospitalType);
        System.out.println("Emergency Contact No: " + emergencyContact);
        System.out.println("----------------------------------------");
    }

    @Override
    public void updateOrganization(String organizationName, String address, String contactNumber) {
        this.organizationName = organizationName;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    public void updateHospitalDetails(String hospitalType, String emergencyContact) {
        this.hospitalType = hospitalType;
        this.emergencyContact = emergencyContact;
    }

    public void sendBloodRequest() {
        System.out.println("Hospital " + organizationName + " has sent a blood request.");
    }

    public void receiveBlood() {
        System.out.println("Hospital " + organizationName + " has received blood units.");
    }

    public void viewRequestHistory() {
        System.out.println("Displaying request history for hospital: " + organizationName);
    }
}
