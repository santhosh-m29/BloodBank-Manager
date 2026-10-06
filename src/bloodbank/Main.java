package bloodbank;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Clock;
import bloodbank.service.*;
import bloodbank.ui.Menu;
import bloodbank.utility.FileManager;
public final class Main {
    private Main() {
    }
    public static void main(String[] args) {
        if (args.length > 2) {
            System.err.println("Usage: bloodbank.Main [data-directory] [report-directory]");
            System.exit(2);
        }
        Path directory = Path.of(args.length > 0 ? args[0] : "data/v2");
        Path reports = Path.of(args.length > 1 ? args[1] : "reports/v2");
        try (FileManager files = new FileManager(directory)) {
            files.lock();
            BloodBankService service = initializeSystem(files);
            new Menu(service, new InputStreamReader(System.in, StandardCharsets.UTF_8), System.out, reports).displayMainMenu();
            closeApplication(service);
        } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
            System.err.println("Application could not continue: " + ex.getMessage());
            System.err.println("Existing saved data has not been replaced with demo data.");
            System.exit(1);
        }
    }
    public static BloodBankService initializeSystem(FileManager files) throws IOException {
        SystemState state = files.loadData();
        if (state == null) {
            state = DemoData.create();
            files.saveData(state);
            System.out.println("Created a new demonstration dataset with empty inventories.");
            System.out.println("Accounts: admin/admin123, hosp1/hosp1234, hosp2/hosp1234, donor/donor123, patient/patient123");
            System.out.println("Legacy flat files are preserved separately; see README for version-2 data details.");
        }
        System.out.println("Data: " + files.getFilePath().toAbsolutePath());
        return new BloodBankService(state, files, Clock.systemDefaultZone());
    }
    public static void closeApplication(BloodBankService service) {
        service.logout();
        System.out.println("All completed operations saved. Goodbye.");
    }
}
