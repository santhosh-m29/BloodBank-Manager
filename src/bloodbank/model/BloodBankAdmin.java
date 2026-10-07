package bloodbank.model;
import bloodbank.service.BloodBankService;
import bloodbank.utility.Validation;
public final class BloodBankAdmin extends Staff {
    private static final long serialVersionUID = 1L;
    private final String adminLevel;
    public BloodBankAdmin(String id, String name, int age, String gender, String phone, String address, String employee, String facility, String level) {
        super(id, name, age, gender, phone, address, employee, facility);
        String normalized = "SuperAdmin".equalsIgnoreCase(level) || "SUPER".equalsIgnoreCase(level) ? "SUPER"
                : "READ_ONLY".equalsIgnoreCase(level) ? "READ_ONLY" : "STANDARD";
        adminLevel = normalized;
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

    public void approveRequest(BloodBankService service, String requestId) {
        service.approveRequest(requestId);
    }
    public BloodTransfer dispatchTransfer(BloodBankService service, String requestId) {
        return service.dispatchTransfer(requestId);
    }
}
