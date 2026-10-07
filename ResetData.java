import bloodbank.service.*;
import bloodbank.utility.*;
import bloodbank.model.*;
import java.nio.file.*;
import java.time.*;

/**
 * ResetData — run this from the project root to wipe all transaction history
 * and restore the data directory to a clean demo state.
 *
 * Compile & run (from project root):
 *   javac -cp bin -d bin ResetData.java
 *   java  -cp bin ResetData
 *
 * Or with source-launcher (Java 11+):
 *   java -cp bin ResetData.java
 */
public class ResetData {
    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of("data");

        // 1. Build a fresh system state from the built-in demo data
        SystemState state = DemoData.create();

        // 2. Seed the hospital with starter blood stock dated today
        DemoData.addHospitalStarterStock(state, LocalDate.now());

        // 3. Persist the clean state (overwrites donations, requests, transfers, etc.)
        FileManager files = new FileManager(dataDir);
        files.saveData(state);

        // 4. Clear the recipients / request-history file explicitly
        Path recipientsFile = dataDir.resolve("recipients.txt");
        if (Files.exists(recipientsFile)) {
            Files.writeString(recipientsFile, "");
        }

        // 5. Verify the reset looks correct
        SystemState loaded = files.loadData();
        if (!loaded.donations.isEmpty())  throw new AssertionError("Expected empty donations after reset");
        if (!loaded.requests.isEmpty())   throw new AssertionError("Expected empty requests after reset");
        if (!loaded.transfers.isEmpty())  throw new AssertionError("Expected empty transfers after reset");

        Inventory inv = loaded.getFacility("HOSP001").getInventory();
        for (String group : new String[]{"A+", "B+", "O+"}) {
            int stock = inv.getStockForGroup(group, LocalDate.now());
            if (stock != 5) throw new AssertionError("Expected 5 units of " + group + ", got " + stock);
        }

        Donor donor = (Donor) loaded.people.get("DON001");
        if (donor.getLastDonationDate() != null) {
            throw new AssertionError("Expected donor DON001 to be eligible (no prior donation date)");
        }

        System.out.println("✓ Data reset complete.");
        System.out.println("  • All transaction history cleared (donations, requests, transfers).");
        System.out.println("  • Hospital HOSP001 stocked: A+/B+/O+ = 5 units each.");
        System.out.println("  • Donor DON001 is eligible to donate.");
        System.out.println("  • Data directory: " + dataDir.toAbsolutePath());
    }
}
