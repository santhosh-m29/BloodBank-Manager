package bloodbank;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    // Shared state variables
    public static List<Admin> admins = new ArrayList<>();
    public static List<Donor> donors = new ArrayList<>();
    public static List<Recipient> recipients = new ArrayList<>();
    public static List<Hospital> hospitals = new ArrayList<>();
    public static List<BloodDonation> donations = new ArrayList<>();
    public static List<BloodRequest> requests = new ArrayList<>();
    public static List<BloodTransfer> transfers = new ArrayList<>();
    public static Inventory inventory = new Inventory();

    // Data File Paths
    public static final String DATA_DIR = "data/";
    public static final String ADMIN_FILE = DATA_DIR + "admin.txt";
    public static final String DONORS_FILE = DATA_DIR + "donors.txt";
    public static final String RECIPIENTS_FILE = DATA_DIR + "recipients.txt";
    public static final String HOSPITALS_FILE = DATA_DIR + "hospitals.txt";
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
        reportGenerator = new ReportGenerator();

        // Load Persistent Data
        admins = FileManager.loadAdmins(ADMIN_FILE);
        donors = FileManager.loadDonors(DONORS_FILE);
        recipients = FileManager.loadRecipients(RECIPIENTS_FILE);
        hospitals = FileManager.loadHospitals(HOSPITALS_FILE);
        donations = FileManager.loadDonations(DONATIONS_FILE);
        requests = FileManager.loadRequests(REQUESTS_FILE);
        transfers = FileManager.loadTransfers(TRANSFERS_FILE);
        
        List<BloodUnit> units = FileManager.loadBloodUnits(BLOODUNITS_FILE);
        inventory.setBloodUnits(new ArrayList<>(units));

        // Create default administrator if none exists
        if (admins.isEmpty()) {
            admins.add(new Admin("ADM001", "System Administrator", 35, "Male", "9999999999", "Blood Bank Headquarter", "admin", "admin123", "Administrator"));
            FileManager.saveAdmins(ADMIN_FILE, admins);
            System.out.println("Default admin credentials initialized: admin/admin123");
        }

        System.out.println("System Initialization Complete.");
    }

    public static void closeApplication() {
        System.out.println("Closing Blood Bank Management System...");
        System.out.println("Data saved successfully.");
    }

    // Static helper methods for components to trigger quick updates
    public static void saveDonorsToFile() {
        FileManager.saveDonors(DONORS_FILE, donors);
    }

    public static void saveRecipientsToFile() {
        FileManager.saveRecipients(RECIPIENTS_FILE, recipients);
    }

    public static void saveHospitalsToFile() {
        FileManager.saveHospitals(HOSPITALS_FILE, hospitals);
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
        FileManager.saveBloodUnits(BLOODUNITS_FILE, inventory.getBloodUnits());
    }
}
