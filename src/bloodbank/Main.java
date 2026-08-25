package bloodbank;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {
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
    public static ReportGenerator reportGenerator;

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
        alertManager = new AlertManager(10, 7); // low stock threshold = 10 units, near expiry = 7 days
        reportGenerator = new ReportGenerator("Main System Report");

        // Load Persistent Data
        FileManager fm = new FileManager();
        admins = FileManager.loadAdmins(ADMIN_FILE);
        hospitalStaffs = FileManager.loadHospitalStaffs(HOSPITALSTAFF_FILE);
        donors = FileManager.loadDonors(DONORS_FILE);
        patients = FileManager.loadPatients(PATIENTS_FILE);
        bloodBanks = FileManager.loadBloodBanks(BLOODBANKS_FILE);
        hospitals = FileManager.loadHospitals(HOSPITALS_FILE);
        donations = FileManager.loadDonations(DONATIONS_FILE);
        requests = FileManager.loadRequests(REQUESTS_FILE);
        transfers = FileManager.loadTransfers(TRANSFERS_FILE);
        
        // Reconstruct hospital structures and route blood units
        fm.loadAllBloodUnits(BLOODUNITS_FILE);

        // Seed default demo data if lists are empty or insufficient
        if (bloodBanks.isEmpty()) {
            bloodBanks.add(new BloodBank("BB001", "City Blood Center", "100 Central Ave", "555-0100", "Dr. Adams", inventory));
            saveBloodBanksToFile();
        }
        
        if (hospitals.size() < 2) {
            hospitals.clear();
            hospitals.add(new Hospital("HOSP001", "St. Jude Hospital", "456 Medical Dr", "555-0111", "Private", "911", new Inventory()));
            hospitals.add(new Hospital("HOSP002", "Grace Clinic", "789 Care Blvd", "555-0222", "Public", "100", new Inventory()));
            saveHospitalsToFile();
            
            // Clear staff to re-seed so they match the hospitals correctly
            hospitalStaffs.clear();
        }

        boolean hasDefaultAdmin = false;
        for (BloodBankAdmin a : admins) {
            if ("admin".equalsIgnoreCase(a.getUsername())) {
                hasDefaultAdmin = true;
                break;
            }
        }
        if (!hasDefaultAdmin) {
            admins.add(new BloodBankAdmin("ADM001", "System Administrator", 35, "Male", "9999999999", "Blood Bank HQ", "admin", "admin123", "BLOOD_BANK_ADMIN", "EMP001", "BB001", "SuperAdmin"));
            saveAdminsToFile();
            System.out.println("Default admin credentials initialized: admin/admin123");
        }

        if (hospitalStaffs.size() < 2) {
            hospitalStaffs.clear();
            HospitalStaff staff1 = new HospitalStaff("STF001", "Dr. Clara", 30, "Female", "8888888888", "St. Jude", "hosp1", "hosp123", "HOSPITAL_STAFF", "STF_JUDE_001", "HOSP001", "Emergency");
            HospitalStaff staff2 = new HospitalStaff("STF002", "Dr. Robert", 42, "Male", "7777777777", "Grace Clinic", "hosp2", "hosp123", "HOSPITAL_STAFF", "STF_GRACE_001", "HOSP002", "Pediatrics");
            hospitalStaffs.add(staff1);
            hospitalStaffs.add(staff2);
            saveHospitalStaffsToFile();
            
            // Map staff back to hospitals
            hospitals.get(0).setStaffs(new HospitalStaff[]{staff1});
            hospitals.get(1).setStaffs(new HospitalStaff[]{staff2});
            saveHospitalsToFile();
        }

        if (donors.isEmpty()) {
            donors.add(new Donor("DON001", "Alice Smith", 28, "Female", "1234567890", "456 Oak St", "donor", "donor123", "DONOR", "A+", 13.5, 55.0, ""));
            saveDonorsToFile();
        }

        if (patients.isEmpty()) {
            patients.add(new Patient("PAT001", "Bob Brown", 45, "Male", "5556667777", "101 Maple Ave", "patient", "patient123", "PATIENT", "A+", "Anemia", "Dr. Green", 2, "HOSP001"));
            savePatientsToFile();
        }

        // Seed starting blood stock if all inventories are empty
        boolean anyStock = !inventory.getBloodUnits().isEmpty();
        for (Hospital h : hospitals) {
            if (!h.getInventory().getBloodUnits().isEmpty()) {
                anyStock = true;
                break;
            }
        }
        if (!anyStock) {
            // Central Bank Stock
            inventory.addBloodUnit(new BloodUnit("UNIT001", "A+", 5, LocalDate.now().toString(), LocalDate.now().plusDays(42).toString(), "AVAILABLE"));
            inventory.addBloodUnit(new BloodUnit("UNIT002", "O-", 3, LocalDate.now().toString(), LocalDate.now().plusDays(42).toString(), "AVAILABLE"));
            inventory.addBloodUnit(new BloodUnit("UNIT003", "B+", 4, LocalDate.now().toString(), LocalDate.now().minusDays(2).toString(), "AVAILABLE")); // Expired unit for testing
            
            // Hospital Local Stock
            hospitals.get(0).getInventory().addBloodUnit(new BloodUnit("UNIT004", "A+", 2, LocalDate.now().toString(), LocalDate.now().plusDays(42).toString(), "AVAILABLE"));
            
            fm.saveAllBloodUnits(BLOODUNITS_FILE);
        }

        // Reconstruct staffs lists on loaded Hospital objects
        for (Hospital h : hospitals) {
            List<HospitalStaff> matchedStaff = new ArrayList<>();
            for (HospitalStaff hs : hospitalStaffs) {
                if (hs.getFacilityId().equals(h.getOrganizationId())) {
                    matchedStaff.add(hs);
                }
            }
            h.setStaffs(matchedStaff.toArray(new HospitalStaff[0]));
        }

        System.out.println("System Initialization Complete.");
        
        // Run checks
        alertManager.checkLowStock();
        alertManager.checkExpiry();
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
}
