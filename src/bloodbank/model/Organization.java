package bloodbank.model;
import java.io.Serializable;
import bloodbank.utility.Validation;
public abstract class Organization implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String organizationId;
    private String organizationName, address, contactNumber;
    private final Inventory inventory = new Inventory();
    protected Organization(String id, String name, String address, String phone) {
        organizationId = Validation.id(id);
        updateOrganization(name, address, phone);
    }
    public final void updateOrganization(String name, String address, String phone) {
        Validation.text(name, "Organization name");
        Validation.text(address, "Address");
        Validation.require(Validation.validatePhoneNumber(phone), "Contact number must contain 10 digits.");
        organizationName = name;
        this.address = address;
        contactNumber = phone;
    }
    public String getOrganizationId() {
        return organizationId;
    }
    public String getOrganizationName() {
        return organizationName;
    }
    public String getAddress() {
        return address;
    }
    public String getContactNumber() {
        return contactNumber;
    }
    public String getPhoneNumber() {
        return contactNumber;
    }
    public Inventory getInventory() {
        return inventory;
    }
    public void displayOrganization() {
        System.out.println(organizationId + " | " + organizationName + " | " + address + " | " + contactNumber);
    }
    public void viewInventory() {
        inventory.displayInventory();
    }
}
