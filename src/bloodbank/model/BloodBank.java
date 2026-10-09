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
        int localStock = destination.getInventory().getStockForGroup(request.getBloodGroup());
        int unitsToMove = Math.max(0, request.getUnitsRequested() - localStock);
        int available = Main.inventory.getStockForGroup(request.getBloodGroup());
        if (available < unitsToMove) {
            System.out.println("Insufficient central stock for this transfer. Request remains approved.");
            return false;
        }
        if (unitsToMove == 0) {
            request.setStatus("READY");
            System.out.println("Hospital already has enough stock. Request is ready to issue.");
            return true;
        }

        int transferQuantity = unitsToMove;
        String transactionId = "TRANS_" + System.currentTimeMillis();
        BloodTransfer transfer = new BloodTransfer(transactionId, LocalDate.now().toString(), "Pending",
                organizationId, destination.getOrganizationId(), request.getBloodGroup(), transferQuantity);
        transfer.transferUnits();

        BloodUnit[] units = Main.inventory.searchBloodGroup(request.getBloodGroup());
        int transferCounter = 0;
        for (BloodUnit unit : units) {
            if (unitsToMove <= 0) break;
            int movedQuantity = Math.min(unit.getQuantity(), unitsToMove);
            if (unit.getQuantity() <= movedQuantity) {
                Main.inventory.removeBloodUnit(unit.getBloodUnitId());
            } else {
                unit.updateQuantity(unit.getQuantity() - movedQuantity);
            }
            String newUnitId = "HOSP_UNIT_" + System.currentTimeMillis() + "_" + transferCounter++;
            BloodUnit movedUnit = new BloodUnit(newUnitId, request.getBloodGroup(), movedQuantity,
                    unit.getCollectionDate(), unit.getExpiryDate(), "AVAILABLE");
            destination.getInventory().addBloodUnit(movedUnit);
            unitsToMove -= movedQuantity;
        }

        transfer.recordTransfer();
        Main.transfers.add(transfer);
        request.setStatus("READY");
        System.out.println("Transfer dispatched successfully. " + transferQuantity
                + " units were transferred; the request is ready to issue.");
        return true;
    }
}
