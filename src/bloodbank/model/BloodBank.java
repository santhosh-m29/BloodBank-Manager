package bloodbank.model;

import bloodbank.Main;
import java.time.LocalDate;

public class BloodBank extends Organization {
    private String managerName;
    private Inventory inventory;

    public BloodBank(String organizationId, String organizationName, String address, String contactNumber,
                     String managerName, Inventory inventory) {
        super(organizationId, organizationName, address, contactNumber);
        this.managerName = managerName;
        this.inventory = inventory;
    }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    @Override
    public void displayOrganization() {
        System.out.println("----------------------------------------");
        System.out.println("Blood Bank Facility Details:");
        System.out.println("ID: " + organizationId);
        System.out.println("Name: " + organizationName);
        System.out.println("Address: " + address);
        System.out.println("Contact Number: " + contactNumber);
        System.out.println("Manager Name: " + managerName);
        System.out.println("Inventory Total Stock: " + inventory.getTotalStock() + " units");
        System.out.println("----------------------------------------");
    }

    public void viewInventory() {
        System.out.println("\n--- Inventory of Blood Bank: " + organizationName + " ---");
        inventory.displayInventory();
    }

    public boolean transferBloodUnits(BloodRequest request, Hospital destination) {
        if (!request.isHospitalRestockRequest()) {
            System.out.println("Only hospital stock requests are transferred from the central bank.");
            return false;
        }
        int unitsToMove = request.getUnitsRequested();
        int available = Main.inventory.getStockForGroup(request.getBloodGroup());
        if (available < unitsToMove) {
            System.out.println("Insufficient central stock for this transfer.");
            return false;
        }

        int transferQuantity = unitsToMove;
        String transactionId = Main.nextId("TRANS");
        BloodTransfer transfer = new BloodTransfer(transactionId, LocalDate.now().toString(), "Pending",
                organizationId, destination.getOrganizationId(), request.getBloodGroup(), transferQuantity);
        transfer.transferUnits();

        BloodUnit[] units = Main.inventory.searchBloodGroup(request.getBloodGroup());
        for (BloodUnit unit : units) {
            if (unitsToMove <= 0) break;
            int movedQuantity = Math.min(unit.getQuantity(), unitsToMove);
            if (unit.getQuantity() <= movedQuantity) {
                Main.inventory.removeBloodUnit(unit.getBloodUnitId());
            } else {
                unit.updateQuantity(unit.getQuantity() - movedQuantity);
            }
            String newUnitId = Main.nextBloodUnitId();
            BloodUnit movedUnit = new BloodUnit(newUnitId, request.getBloodGroup(), movedQuantity,
                    unit.getCollectionDate(), unit.getExpiryDate(), "AVAILABLE");
            destination.getInventory().addBloodUnit(movedUnit);
            unitsToMove -= movedQuantity;
        }

        transfer.recordTransfer();
        Main.transfers.add(transfer);
        request.setStatus("COMPLETED");
        System.out.println("Hospital stock request approved and transferred. "
                + transferQuantity + " units were added to local stock.");
        for (BloodRequest patientRequest : Main.requests) {
            if (!patientRequest.isHospitalRestockRequest()
                    && patientRequest.getHospitalId().equals(destination.getOrganizationId())
                    && patientRequest.getBloodGroup().equalsIgnoreCase(request.getBloodGroup())
                    && "PENDING".equalsIgnoreCase(patientRequest.getStatus())
                    && destination.getInventory().getStockForGroup(patientRequest.getBloodGroup())
                    >= patientRequest.getUnitsRequested()) {
                patientRequest.setStatus("READY");
            }
        }
        return true;
    }
}
