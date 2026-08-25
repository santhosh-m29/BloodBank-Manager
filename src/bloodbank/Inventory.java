package bloodbank;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private ArrayList<BloodUnit> bloodUnits;
    private int totalStock;

    public Inventory() {
        this.bloodUnits = new ArrayList<>();
        this.totalStock = 0;
    }

    public ArrayList<BloodUnit> getBloodUnits() { return bloodUnits; }
    public void setBloodUnits(ArrayList<BloodUnit> bloodUnits) { 
        this.bloodUnits = bloodUnits;
        calculateTotalStock();
    }

    public int getTotalStock() { 
        calculateTotalStock();
        return totalStock; 
    }
    public void setTotalStock(int totalStock) { this.totalStock = totalStock; }

    public void calculateTotalStock() {
        int stock = 0;
        for (BloodUnit bu : bloodUnits) {
            if ("AVAILABLE".equalsIgnoreCase(bu.getStatus()) && !bu.isExpired()) {
                stock += bu.getQuantity();
            }
        }
        this.totalStock = stock;
    }

    public void addBloodUnit(BloodUnit unit) {
        // Avoid duplicate ID
        for (BloodUnit bu : bloodUnits) {
            if (bu.getBloodUnitId().equals(unit.getBloodUnitId())) {
                System.out.println("Warning: BloodUnit ID " + unit.getBloodUnitId() + " already exists in inventory. Skipping addition.");
                return;
            }
        }
        bloodUnits.add(unit);
        calculateTotalStock();
    }

    public boolean removeBloodUnit(String bloodUnitId) {
        for (int i = 0; i < bloodUnits.size(); i++) {
            if (bloodUnits.get(i).getBloodUnitId().equals(bloodUnitId)) {
                bloodUnits.remove(i);
                calculateTotalStock();
                return true;
            }
        }
        return false;
    }

    public BloodUnit[] searchBloodGroup(String bloodGroup) {
        List<BloodUnit> result = new ArrayList<>();
        for (BloodUnit bu : bloodUnits) {
            if (bu.getBloodGroup().equalsIgnoreCase(bloodGroup) && "AVAILABLE".equalsIgnoreCase(bu.getStatus()) && !bu.isExpired()) {
                result.add(bu);
            }
        }
        return result.toArray(new BloodUnit[0]);
    }

    public int getStockForGroup(String bloodGroup) {
        int qty = 0;
        for (BloodUnit bu : bloodUnits) {
            if (bu.getBloodGroup().equalsIgnoreCase(bloodGroup) && "AVAILABLE".equalsIgnoreCase(bu.getStatus()) && !bu.isExpired()) {
                qty += bu.getQuantity();
            }
        }
        return qty;
    }

    public void displayInventory() {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.printf("%-15s %-12s %-10s %-15s %-15s %-12s\n", 
            "Blood Unit ID", "Blood Group", "Quantity", "Collection Date", "Expiry Date", "Status");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (BloodUnit bu : bloodUnits) {
            System.out.printf("%-15s %-12s %-10d %-15s %-15s %-12s %s\n", 
                bu.getBloodUnitId(), 
                bu.getBloodGroup(), 
                bu.getQuantity(), 
                bu.getCollectionDate(), 
                bu.getExpiryDate(),
                bu.getStatus(),
                bu.isExpired() ? "(EXPIRED)" : "");
        }
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("Total Usable Stock: " + getTotalStock() + " units");
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    public void checkLowStock() {
        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        int threshold = 10;
        System.out.println("--- Low Stock Alert Check (Threshold: " + threshold + " units) ---");
        boolean anyLowStock = false;
        for (String g : groups) {
            int stock = getStockForGroup(g);
            if (stock < threshold) {
                System.out.println("ALERT: Blood group " + g + " is low on stock! Current: " + stock + " units.");
                anyLowStock = true;
            }
        }
        if (!anyLowStock) {
            System.out.println("All blood groups are above threshold.");
        }
    }
    
    // Parameterless compliance methods from class diagram
    public void addBloodUnit() {
        System.out.println("Use parameterized addBloodUnit(BloodUnit) to add a unit to the collection.");
    }
    
    public void removeBloodUnit() {
        System.out.println("Use removeBloodUnit(String id) to remove a unit.");
    }
}
