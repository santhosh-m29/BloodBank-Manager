package bloodbank.model;
import java.util.List;
import bloodbank.utility.Validation;
public final class Patient extends Person {
    private static final long serialVersionUID = 1L;
    private final String bloodGroup, disease, doctorName, hospitalId;
    private final int unitsRequired;
    public Patient(String id, String name, int age, String gender, String phone, String address, String username, String password, String group, String disease, String doctor, int units, String hospitalId) {
        super(id, name, age, gender, phone, address, username, password);
        bloodGroup = Validation.bloodGroup(group);
        this.disease = Validation.text(disease, "Disease/reason");
        doctorName = Validation.text(doctor, "Doctor");
        unitsRequired = Validation.positive(units);
        this.hospitalId = Validation.id(hospitalId);
    }
    public String getRole() {
        return "PATIENT";
    }
    public String getBloodGroup() {
        return bloodGroup;
    }
    public String getDisease() {
        return disease;
    }
    public String getDoctorName() {
        return doctorName;
    }
    public String getHospitalId() {
        return hospitalId;
    }
    public int getUnitsRequired() {
        return unitsRequired;
    }
    public String viewRequestStatus(String id, List<BloodRequest> requests) {
        return requests.stream().filter(r -> r.getPatientId().equals(getPersonId()) && r.getTransactionId().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Request not found.")).getStatus();
    }
    @Override public String getDetails() {
        return super.getDetails() + " | " + bloodGroup + " | " + disease + " | Doctor: " + doctorName + " | Required: " + unitsRequired + " | Hospital: " + hospitalId;
    }
}
