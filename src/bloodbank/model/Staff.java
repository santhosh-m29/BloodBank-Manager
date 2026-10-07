package bloodbank.model;
import bloodbank.utility.Validation;
public abstract class Staff extends Person {
    private static final long serialVersionUID = 1L;
    private final String employeeId;
    private final String facilityId;
    protected Staff(String id, String name, int age, String gender, String phone, String address, String employeeId, String facilityId) {
        super(id, name, age, gender, phone, address);
        this.employeeId = Validation.id(employeeId);
        this.facilityId = Validation.id(facilityId);
    }
    public String getEmployeeId() {
        return employeeId;
    }
    public String getFacilityId() {
        return facilityId;
    }
    public void displayStaffDetails() {
        System.out.println(getDetails() + " | Employee: " + employeeId + " | Facility: " + facilityId);
    }
}
