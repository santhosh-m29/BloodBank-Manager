package bloodbank.service;
import bloodbank.model.*;
public final class DemoData {
    private DemoData() {
    }
    public static SystemState create() {
        SystemState state = new SystemState();
        state.facilities.put("BB001", new BloodBank("BB001", "City Blood Center", "100 Central Avenue", "9999999999", "Dr. Adams"));
        state.facilities.put("HOSP001", new Hospital("HOSP001", "St. Jude Hospital", "456 Medical Drive", "8888888888", "Private", "112"));
        state.facilities.put("HOSP002", new Hospital("HOSP002", "Grace Clinic", "789 Care Boulevard", "7777777777", "Public", "112"));
        state.addPerson(new BloodBankAdmin("ADM001", "System Administrator", 35, "Male", "9999999999", "Blood Bank HQ", "admin", "admin123", "EMP001", "BB001", "SUPER"));
        state.addPerson(new HospitalStaff("STF001", "Dr. Clara", 30, "Female", "8888888888", "St. Jude", "hosp1", "hosp1234", "EMP002", "HOSP001", "Emergency"));
        state.addPerson(new HospitalStaff("STF002", "Dr. Robert", 42, "Male", "7777777777", "Grace Clinic", "hosp2", "hosp1234", "EMP003", "HOSP002", "General"));
        state.addPerson(new Donor("DON001", "Alice Smith", 28, "Female", "1234567890", "456 Oak Street", "donor", "donor123", "A+", 13.5, 55));
        state.addPerson(new Patient("PAT001", "Bob Brown", 45, "Male", "5556667777", "101 Maple Avenue", "patient", "patient123", "A+", "Anemia", "Dr. Green", 2, "HOSP001"));
        state.validate();
        return state;
    }
}
