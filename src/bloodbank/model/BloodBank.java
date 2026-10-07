package bloodbank.model;
import bloodbank.utility.Validation;
public final class BloodBank extends Organization {
    private final String managerName;
    public BloodBank(String id, String name, String address, String phone, String manager) {
        super(id, name, address, phone);
        managerName = Validation.text(manager, "Manager");
    }
    public String getManagerName() {
        return managerName;
    }
    public void addBloodUnit(BloodUnit unit) {
        getInventory().addBloodUnit(unit);
    }
    public boolean removeBloodUnit(String id) {
        return getInventory().removeBloodUnit(id);
    }
    public void transferBlood(BloodTransfer transfer, Hospital destination, java.time.LocalDate today) {
        throw new UnsupportedOperationException("Blood transfer is planned for Phase 2 and is not part of the current 50% implementation.");
    }
}
