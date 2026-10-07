package bloodbank.model;
import bloodbank.service.BloodBankService;
import bloodbank.utility.Validation;
public final class HospitalStaff extends Staff {
    private final String department;
    public HospitalStaff(String id, String name, int age, String gender, String phone, String address, String employee, String facility, String department) {
        super(id, name, age, gender, phone, address, employee, facility);
        this.department = Validation.text(department, "Department");
    }
    public String getRole() {
        return "HOSPITAL";
    }
    public String getDepartment() {
        return department;
    }

    public BloodRequest requestBlood(BloodBankService service, String patientId, String urgency) {
        return service.requestBlood(patientId, urgency);
    }
    public void issueBlood(BloodBankService service, String requestId) {
        service.issueBlood(requestId);
    }
}
