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
        if (args.length > 1) {
            System.err.println("Usage: bloodbank.Main [data-directory]");
            System.exit(2);
        }
        Path directory = Path.of(args.length > 0 ? args[0] : "data");
        try (FileManager files = new FileManager(directory)) {
            BloodBankService service = initializeSystem(files);
            new Menu(service, new InputStreamReader(System.in, StandardCharsets.UTF_8), System.out).displayMainMenu();
            closeApplication(service);
        } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
            System.err.println("Application could not continue: " + ex.getMessage());
            System.exit(1);
        }
    }

    public static BloodBankService initializeSystem(FileManager files) throws IOException {
        SystemState state = files.loadData();
        if (state == null) {
            state = DemoData.create();
            files.saveData(state);
            System.out.println("Created default dataset in " + files.getDataDirectory());
        }
        System.out.println("Data directory: " + files.getDataDirectory().toAbsolutePath());
        return new BloodBankService(state, files, Clock.systemDefaultZone());
    }

    public static void closeApplication(BloodBankService service) {
        service.clearRole();
        System.out.println("All completed operations saved. Goodbye.");
    }
}
