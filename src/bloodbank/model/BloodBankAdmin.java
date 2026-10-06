package bloodbank.model;
import bloodbank.service.BloodBankService;
import bloodbank.utility.Validation;
public final class BloodBankAdmin extends Staff {
    private static final long serialVersionUID = 1L;
    private final String adminLevel;
    public BloodBankAdmin(String id, String name, int age, String gender, String phone, String address, String username, String password, String employee, String facility, String level) {
        super(id, name, age, gender, phone, address, username, password, employee, facility);
        Validation.require(java.util.Set.of("STANDARD", "SUPER", "READ_ONLY").contains(level), "Invalid admin level.");
        adminLevel = level;
    }
    public String getRole() {
        return "BLOOD_BANK";
    }
    public String getAdminLevel() {
        return adminLevel;
    }
    public void registerDonation(BloodBankService service, String donationId) {
        service.registerDonation(donationId);
    }
    public boolean initiateLabTest(BloodBankService service, String donationId, boolean passed, String note) {
        service.labTest(donationId, passed, note);
        return passed;
    }
    public void approveRequest(BloodBankService service, String requestId) {
        service.approveRequest(requestId);
    }
    public BloodTransfer dispatchTransfer(BloodBankService service, String requestId) {
        return service.dispatchTransfer(requestId);
    }
}
