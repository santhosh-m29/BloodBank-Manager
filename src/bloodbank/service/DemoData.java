package bloodbank.service;
import bloodbank.model.*;
public final class DemoData {
    private DemoData() {
    }
    public static boolean addHospitalStarterStock(SystemState state, java.time.LocalDate date) {
        if (state.hospitalStarterStockAdded) return false;
        Hospital hospital = (Hospital) state.facilities.values().stream().filter(f -> f instanceof Hospital).findFirst().orElse(null);
        if (hospital == null || !hospital.getInventory().getBloodUnits().isEmpty()) {
            state.hospitalStarterStockAdded = true;
            return false;
        }
        for (String group : java.util.List.of("A+", "B+", "O+")) {
            for (int i = 1; i <= 5; i++) {
                String id = "OPEN_" + group.charAt(0) + "_" + i;
                hospital.getInventory().addBloodUnit(BloodUnit.openingStock(id, group, date));
            }
        }
        state.hospitalStarterStockAdded = true;
        state.audit.add(date + " | SYSTEM | Added hospital opening stock: A+ 5, B+ 5, O+ 5");
        return true;
    }
    public static SystemState create() {
        SystemState state = new SystemState();
        state.facilities.put("BB001", new BloodBank("BB001", "City Blood Center", "100 Central Avenue", "9999999999", "Dr. Adams"));
        state.facilities.put("HOSP001", new Hospital("HOSP001", "St. Jude Hospital", "456 Medical Drive", "8888888888", "Private", "112"));
        state.addPerson(new BloodBankAdmin("ADM001", "Admin", 35, "Male", "9999999999", "Blood Bank HQ", "EMP001", "BB001", "SUPER"));
        state.addPerson(new HospitalStaff("STF001", "Dr. Clara", 30, "Female", "8888888888", "St. Jude", "EMP002", "HOSP001", "Emergency"));
        state.addPerson(new Donor("DON001", "Alice Smith", 28, "Female", "1234567890", "456 Oak Street", "A+", 13.5, 55));
        state.addPerson(new Patient("PAT001", "Bob Brown", 45, "Male", "5556667777", "101 Maple Avenue", "A+", "Anemia", "Dr. Green", 2, "HOSP001"));
        state.validate();
        return state;
    }
}
