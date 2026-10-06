package bloodbank.model;
import bloodbank.service.BloodBankService;
import bloodbank.utility.Validation;
public final class HospitalStaff extends Staff {
    private static final long serialVersionUID = 1L;
    private final String department;
    public HospitalStaff(String id, String name, int age, String gender, String phone, String address, String username, String password, String employee, String facility, String department) {
        super(id, name, age, gender, phone, address, username, password, employee, facility);
        this.department = Validation.text(department, "Department");
    }
    public String getRole() {
        return "HOSPITAL";
    }
    public String getDepartment() {
        return department;
    }
    public void createPatient(BloodBankService service, Patient patient) {
        service.createPatient(patient);
    }
    public BloodRequest requestBlood(BloodBankService service, String patientId, int units, String urgency) {
        return service.requestBlood(patientId, units, urgency);
    }
    public void issueBlood(BloodBankService service, String requestId) {
        service.issueBlood(requestId);
    }
}
