package bloodbank.model;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private ArrayList<BloodUnit> bloodUnits;

    public Inventory() {
        this.bloodUnits = new ArrayList<>();
    }

    public ArrayList<BloodUnit> getBloodUnits() { return bloodUnits; }
    public int getTotalStock() {
        int stock = 0;
        for (BloodUnit bu : bloodUnits) {
            if ("AVAILABLE".equalsIgnoreCase(bu.getStatus()) && !bu.isExpired()) {
                stock += bu.getQuantity();
            }
        }
        return stock;
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
    }

    public boolean removeBloodUnit(String bloodUnitId) {
        for (int i = 0; i < bloodUnits.size(); i++) {
            if (bloodUnits.get(i).getBloodUnitId().equals(bloodUnitId)) {
                bloodUnits.remove(i);
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
        System.out.printf("%-15s %-16s %-18s\n", "Blood Group", "Available Units", "Donation Date");
        System.out.println("-----------------------------------------------------------------------------------------");

        ArrayList<BloodUnit> groupedUnits = new ArrayList<>();
        ArrayList<Integer> groupedQuantities = new ArrayList<>();
        for (BloodUnit bu : bloodUnits) {
            if (!"AVAILABLE".equalsIgnoreCase(bu.getStatus()) || bu.isExpired()) {
                continue;
            }

            int groupIndex = -1;
            for (int i = 0; i < groupedUnits.size(); i++) {
                BloodUnit group = groupedUnits.get(i);
                if (group.getBloodGroup().equalsIgnoreCase(bu.getBloodGroup())
                        && group.getCollectionDate().equals(bu.getCollectionDate())) {
                    groupIndex = i;
                    break;
                }
            }

            if (groupIndex == -1) {
                groupedUnits.add(bu);
                groupedQuantities.add(bu.getQuantity());
            } else {
                groupedQuantities.set(groupIndex, groupedQuantities.get(groupIndex) + bu.getQuantity());
            }
        }

        if (groupedUnits.isEmpty()) {
            System.out.println("No available blood stock.");
        }
        for (int i = 0; i < groupedUnits.size(); i++) {
            BloodUnit group = groupedUnits.get(i);
            System.out.printf("%-15s %-16d %-18s\n",
                    group.getBloodGroup(), groupedQuantities.get(i), group.getCollectionDate());
        }
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("Total Usable Stock: " + getTotalStock() + " units");
        System.out.println("-----------------------------------------------------------------------------------------");
    }

}
