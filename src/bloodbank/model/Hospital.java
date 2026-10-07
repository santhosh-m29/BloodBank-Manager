package bloodbank.model;
import java.util.*;
import bloodbank.service.BloodBankService;
import bloodbank.utility.Validation;
public final class Hospital extends Organization {
    private static final long serialVersionUID = 1L;
    private final String hospitalType, emergencyContact;
    private final ArrayList<HospitalStaff> staffs = new ArrayList<>();
    public Hospital(String id, String name, String address, String phone, String type, String emergency) {
        super(id, name, address, phone);
        hospitalType = Validation.text(type, "Hospital type");
        emergencyContact = Validation.text(emergency, "Emergency contact");
    }
    public String getHospitalType() {
        return hospitalType;
    }
    public String getEmergencyContact() {
        return emergencyContact;
    }
    public List<HospitalStaff> getStaffs() {
        return List.copyOf(staffs);
    }
    public void addStaff(HospitalStaff staff) {
        Validation.require(staff.getFacilityId().equals(getOrganizationId()), "Staff belongs to another hospital.");
        Validation.require(staffs.stream().noneMatch(s -> s.getPersonId().equals(staff.getPersonId())), "Duplicate staff.");
        staffs.add(staff);
    }
    public BloodRequest sendBloodRequest(BloodBankService service, String patient, String urgency) {
        return service.requestBlood(patient, urgency);
    }
    public List<BloodRequest> viewRequestHistory(List<BloodRequest> requests) {
        return requests.stream().filter(r -> r.getHospitalId().equals(getOrganizationId())).toList();
    }
}
