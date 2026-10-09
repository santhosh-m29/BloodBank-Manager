package bloodbank.model;

public abstract class Organization {
    protected String organizationId;
    protected String organizationName;
    protected String address;
    protected String contactNumber;

    public Organization(String organizationId, String organizationName, String address, String contactNumber) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public abstract void displayOrganization();
}
