package bloodbank.service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import bloodbank.model.*;
import bloodbank.utility.Validation;
public final class ReportGenerator {
    private final String reportTitle;
    private final LocalDate generatedDate;
    public ReportGenerator(String title, LocalDate date) {
        reportTitle = Validation.text(title, "Report title");
        generatedDate = date;
    }
    private String header(String kind) {
        return reportTitle + " - " + kind + "\nGenerated: " + generatedDate + "\n\n";
    }
    public String generateInventoryReport(String facility, Inventory inventory) {
        StringBuilder result = new StringBuilder(header("Inventory")).append("Facility: ").append(facility).append("\nGroup | Usable | Unreserved | Low stock (<10)\n");
        for (String group : Validation.BLOOD_GROUPS) {
            int free = inventory.getStockForGroup(group, generatedDate);
            result.append(group).append(" | ").append(inventory.searchBloodGroup(group, generatedDate).size()).append(" | ").append(free).append(" | ").append(free < 10).append('\n');
        }
        result.append("\nUnit | Group | Quantity | Collected | Expires | Status | Lab | Reservation | Patient\n");
        inventory.getBloodUnits().forEach(u -> result.append(u).append('\n'));
        return result.toString();
    }
    public String generateDonationReport(List<BloodDonation> donations) {
        StringBuilder result = new StringBuilder(header("Donations"));
        donations.forEach(d -> {
            result.append(d).append('\n'); d.getUnits().forEach(u -> result.append("  ").append(u).append('\n'));
        });
        return result.toString();
    }
    public String generateRequestReport(List<BloodRequest> requests) {
        StringBuilder result = new StringBuilder(header("Requests"));
        requests.forEach(r -> result.append(r).append('\n'));
        return result.toString();
    }
    public String generateTransferReport(List<BloodTransfer> transfers) {
        StringBuilder result = new StringBuilder(header("Transfers"));
        transfers.forEach(t -> result.append(t).append('\n'));
        return result.toString();
    }
    public Path exportReport(String report, Path filePath) throws IOException {
        Files.createDirectories(filePath.toAbsolutePath().getParent());
        Files.writeString(filePath, report, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        return filePath;
    }
}
