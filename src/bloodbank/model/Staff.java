package bloodbank.model;

public abstract class Staff extends Person {
    protected String employeeId;
    protected String facilityId; // Valid Hospital or Blood Bank ID

    public Staff(String personId, String name, int age, String gender, String phoneNumber, String address,
                 String username, String password, String role, String employeeId, String facilityId) {
        super(personId, name, age, gender, phoneNumber, address, username, password, role);
        this.employeeId = employeeId;
        this.facilityId = facilityId;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }

}
