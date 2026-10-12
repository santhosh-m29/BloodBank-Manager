package bloodbank;

import bloodbank.model.*;
import bloodbank.service.*;
import bloodbank.ui.Menu;
import bloodbank.utility.FileManager;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final Scanner sc = new Scanner(System.in);
    // Shared state variables
    public static List<BloodBankAdmin> admins = new ArrayList<>();
    public static List<HospitalStaff> hospitalStaffs = new ArrayList<>();
    public static List<Donor> donors = new ArrayList<>();
    public static List<Patient> patients = new ArrayList<>();
    public static List<Hospital> hospitals = new ArrayList<>();
    public static List<BloodBank> bloodBanks = new ArrayList<>();
    public static List<BloodDonation> donations = new ArrayList<>();
    public static List<BloodRequest> requests = new ArrayList<>();
    public static List<BloodTransfer> transfers = new ArrayList<>();
    
    // Central Blood Bank Inventory
    public static Inventory inventory = new Inventory();

    // Data File Paths
    public static final String DATA_DIR = "data/";
    public static final String ADMIN_FILE = DATA_DIR + "admin.txt";
    public static final String HOSPITALSTAFF_FILE = DATA_DIR + "hospitalstaff.txt";
    public static final String DONORS_FILE = DATA_DIR + "donors.txt";
    public static final String PATIENTS_FILE = DATA_DIR + "patients.txt";
    public static final String HOSPITALS_FILE = DATA_DIR + "hospitals.txt";
    public static final String BLOODBANKS_FILE = DATA_DIR + "bloodbanks.txt";
    public static final String BLOODUNITS_FILE = DATA_DIR + "bloodunits.txt";
    public static final String DONATIONS_FILE = DATA_DIR + "donations.txt";
    public static final String REQUESTS_FILE = DATA_DIR + "requests.txt";
    public static final String TRANSFERS_FILE = DATA_DIR + "transfers.txt";

    // Services
    public static LoginManager loginManager;
    public static AlertManager alertManager;

    public static void main(String[] args) {
        initializeSystem();
        
        Menu menu = new Menu();
        menu.displayMainMenu();
    }

    public static void initializeSystem() {
        System.out.println("Initializing Blood Bank Management System...");
        
        // Ensure data directory exists
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Initialize Services
        loginManager = new LoginManager();
        alertManager = new AlertManager(7); // alert window for expiry = 7 days

        // Load Persistent Data
        FileManager fm = new FileManager();
        admins = FileManager.loadAdmins(ADMIN_FILE);
        hospitalStaffs = FileManager.loadHospitalStaffs(HOSPITALSTAFF_FILE);
        donors = FileManager.loadDonors(DONORS_FILE);
        patients = FileManager.loadPatients(PATIENTS_FILE);
        bloodBanks = FileManager.loadBloodBanks(BLOODBANKS_FILE);
        boolean bloodBanksChanged = false;
        BloodBank centralBank = null;
        for (BloodBank bank : bloodBanks) {
            if ("BB001".equalsIgnoreCase(bank.getOrganizationId())) centralBank = bank;
        }
        if (centralBank == null) {
            centralBank = new BloodBank("BB001", "City Blood Center", "100 Central Ave", "555-0100", "Dr. Adams", inventory);
            bloodBanks.add(centralBank);
            bloodBanksChanged = true;
        }
        if (bloodBanks.size() != 1 || bloodBanks.get(0) != centralBank) {
            bloodBanks.clear();
            bloodBanks.add(centralBank);
            bloodBanksChanged = true;
        }
        for (BloodBank bloodBank : bloodBanks) {
            bloodBank.setInventory(inventory);
        }
        if (bloodBanksChanged) {
            saveBloodBanksToFile();
        }
        hospitals = FileManager.loadHospitals(HOSPITALS_FILE);
        donations = FileManager.loadDonations(DONATIONS_FILE);
        requests = FileManager.loadRequests(REQUESTS_FILE);
        transfers = FileManager.loadTransfers(TRANSFERS_FILE);
        
        // Reconstruct hospital structures and route blood units
        fm.loadAllBloodUnits(BLOODUNITS_FILE);

        // Keep three hospitals in the demo system.
        if (!hasHospital("HOSP001")) {
            hospitals.add(new Hospital("HOSP001", "St. Jude Hospital", "456 Medical Dr", "555-0111", "Private", "911", new Inventory()));
        }
        if (!hasHospital("HOSP002")) {
            hospitals.add(new Hospital("HOSP002", "Grace Clinic", "789 Care Blvd", "555-0222", "Public", "100", new Inventory()));
        }
        if (!hasHospital("HOSP003")) {
            hospitals.add(new Hospital("HOSP003", "Mercy General Hospital", "12 Mercy Way", "555-0333", "Public", "911", new Inventory()));
        }
        if (hospitals.size() > 3) {
            while (hospitals.size() > 3) hospitals.remove(hospitals.size() - 1);
        }
        if (hospitals.size() == 3) {
            saveHospitalsToFile();
        }

        boolean adminsChanged = false;
        for (int i = admins.size() - 1; i >= 0; i--) {
            if (!"BB001".equalsIgnoreCase(admins.get(i).getFacilityId())) {
                admins.remove(i);
                adminsChanged = true;
            }
        }
        while (admins.size() > 1) {
            admins.remove(admins.size() - 1);
            adminsChanged = true;
        }
        if (admins.isEmpty()) {
            admins.add(new BloodBankAdmin("ADM001", "City Blood Bank Admin", 35, "Male", "9999999999", "City Blood Center", "admin", "admin123", "BLOOD_BANK_ADMIN", "EMP001", "BB001"));
            adminsChanged = true;
        }
        if (adminsChanged) {
            saveAdminsToFile();
            System.out.println("Blood bank admin account initialized.");
        }

        if (hospitalStaffs.size() < 2) {
            hospitalStaffs.clear();
            HospitalStaff staff1 = new HospitalStaff("STF001", "Dr. Clara", 30, "Female", "8888888888", "St. Jude", "hosp1", "hosp123", "HOSPITAL_STAFF", "STF_JUDE_001", "HOSP001", "Emergency");
            HospitalStaff staff2 = new HospitalStaff("STF002", "Dr. Robert", 42, "Male", "7777777777", "Grace Clinic", "hosp2", "hosp123", "HOSPITAL_STAFF", "STF_GRACE_001", "HOSP002", "Pediatrics");
            hospitalStaffs.add(staff1);
            hospitalStaffs.add(staff2);
            saveHospitalStaffsToFile();
            
            // Map staff back to hospitals
            hospitals.get(0).getStaffs().add(staff1);
            hospitals.get(1).getStaffs().add(staff2);
            saveHospitalsToFile();
        }

        if (donors.isEmpty()) {
            donors.add(new Donor("DON001", "Alice Smith", 28, "Female", "1234567890", "456 Oak St", "donor", "donor123", "DONOR", "A+", 13.5, 55.0, LocalDate.now().minusDays(120).toString()));
            saveDonorsToFile();
        }

        if (patients.isEmpty()) {
            patients.add(new Patient("PAT001", "Bob Brown", 45, "Male", "5556667777", "101 Maple Ave", "patient", "patient123", "PATIENT", "A+", "Anemia", "Dr. Green", 2, "HOSP001"));
            savePatientsToFile();
        }

        // Start with empty central stock and basic local hospital stock.
        for (Hospital h : hospitals) {
            if (h.getInventory().getBloodUnits().isEmpty()) {
                for (int i = 1; i <= 5; i++) {
                    String date = LocalDate.now().toString();
                    String expiry = LocalDate.now().plusDays(42).toString();
                    h.getInventory().addBloodUnit(new BloodUnit(nextBloodUnitId(), "A+", 1, date, expiry, "AVAILABLE"));
                    h.getInventory().addBloodUnit(new BloodUnit(nextBloodUnitId(), "B+", 1, date, expiry, "AVAILABLE"));
                }
                fm.saveAllBloodUnits(BLOODUNITS_FILE);
            }
        }

        // Reconstruct staffs lists on loaded Hospital objects
        for (Hospital h : hospitals) {
            List<HospitalStaff> matchedStaff = new ArrayList<>();
            for (HospitalStaff hs : hospitalStaffs) {
                if (hs.getFacilityId().equals(h.getOrganizationId())) {
                    matchedStaff.add(hs);
                }
            }
            h.getStaffs().clear();
            h.getStaffs().addAll(matchedStaff);
        }

        System.out.println("System Initialization Complete.");
    }

    public static void closeApplication() {
        System.out.println("Closing Blood Bank Management System...");
        // Save everything cleanly
        new FileManager().saveData();
        System.out.println("Data saved successfully.");
    }

    // Static helper methods for components to trigger updates
    public static void saveAdminsToFile() {
        FileManager.saveAdmins(ADMIN_FILE, admins);
    }

    public static void saveHospitalStaffsToFile() {
        FileManager.saveHospitalStaffs(HOSPITALSTAFF_FILE, hospitalStaffs);
    }

    public static void saveDonorsToFile() {
        FileManager.saveDonors(DONORS_FILE, donors);
    }

    public static void savePatientsToFile() {
        FileManager.savePatients(PATIENTS_FILE, patients);
    }

    public static void saveHospitalsToFile() {
        FileManager.saveHospitals(HOSPITALS_FILE, hospitals);
    }

    public static void saveBloodBanksToFile() {
        FileManager.saveBloodBanks(BLOODBANKS_FILE, bloodBanks);
    }

    public static void saveDonationsToFile() {
        FileManager.saveDonations(DONATIONS_FILE, donations);
    }

    public static void saveRequestsToFile() {
        FileManager.saveRequests(REQUESTS_FILE, requests);
    }

    public static void saveTransfersToFile() {
        FileManager.saveTransfers(TRANSFERS_FILE, transfers);
    }

    public static void saveBloodUnitsToFile() {
        new FileManager().saveAllBloodUnits(BLOODUNITS_FILE);
    }

    private static boolean hasHospital(String hospitalId) {
        for (Hospital hospital : hospitals) {
            if (hospital.getOrganizationId().equalsIgnoreCase(hospitalId)) return true;
        }
        return false;
    }

    public static String nextId(String prefix) {
        int largest = 0;
        for (BloodBankAdmin admin : admins) {
            largest = Math.max(largest, idNumber(admin.getPersonId(), prefix));
            largest = Math.max(largest, idNumber(admin.getEmployeeId(), prefix));
        }
        for (HospitalStaff staff : hospitalStaffs) {
            largest = Math.max(largest, idNumber(staff.getPersonId(), prefix));
            largest = Math.max(largest, idNumber(staff.getEmployeeId(), prefix));
        }
        for (Person person : donors) largest = Math.max(largest, idNumber(person.getPersonId(), prefix));
        for (Person person : patients) largest = Math.max(largest, idNumber(person.getPersonId(), prefix));
        for (BloodDonation donation : donations) largest = Math.max(largest, idNumber(donation.getTransactionId(), prefix));
        for (BloodRequest request : requests) largest = Math.max(largest, idNumber(request.getTransactionId(), prefix));
        for (BloodTransfer transfer : transfers) largest = Math.max(largest, idNumber(transfer.getTransactionId(), prefix));
        for (Hospital hospital : hospitals) largest = Math.max(largest, idNumber(hospital.getOrganizationId(), prefix));
        for (BloodBank bank : bloodBanks) largest = Math.max(largest, idNumber(bank.getOrganizationId(), prefix));
        return prefix + String.format("%03d", largest + 1);
    }

    private static int idNumber(String id, String prefix) {
        if (id == null || !id.startsWith(prefix)) return 0;
        try { return Integer.parseInt(id.substring(prefix.length())); }
        catch (NumberFormatException e) { return 0; }
    }

    public static String nextBloodUnitId() {
        int largest = 0;
        for (BloodUnit unit : inventory.getBloodUnits()) largest = Math.max(largest, idNumber(unit.getBloodUnitId(), "UNIT"));
        for (BloodDonation donation : donations) largest = Math.max(largest, idNumber(donation.getBloodUnitId(), "UNIT"));
        for (Hospital hospital : hospitals) {
            for (BloodUnit unit : hospital.getInventory().getBloodUnits()) largest = Math.max(largest, idNumber(unit.getBloodUnitId(), "UNIT"));
        }
        return "UNIT" + String.format("%03d", largest + 1);
    }

}
