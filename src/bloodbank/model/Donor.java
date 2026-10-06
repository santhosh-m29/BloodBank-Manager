package bloodbank.model;
import java.time.LocalDate;
import java.util.List;
import bloodbank.service.BloodBankService;
import bloodbank.utility.Validation;
public final class Donor extends Person {
    private static final long serialVersionUID = 1L;
    private final String bloodGroup;
    private double haemoglobin;
    private double weight;
    private LocalDate lastDonationDate;
    public Donor(String id, String name, int age, String gender, String phone, String address, String username, String password, String group, double hb, double weight) {
        super(id, name, age, gender, phone, address, username, password);
        bloodGroup = Validation.bloodGroup(group);
        updateMeasurements(hb, weight);
    }
    public String getRole() {
        return "DONOR";
    }
    public String getBloodGroup() {
        return bloodGroup;
    }
    public double getHaemoglobin() {
        return haemoglobin;
    }
    public double getWeight() {
        return weight;
    }
    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }
    public void updateMeasurements(double hb, double weight) {
        Validation.positive(hb);
        Validation.positive(weight);
        this.haemoglobin = hb;
        this.weight = weight;
    }
    public boolean isEligible(LocalDate today) {
        return getAge() >= 18 && getAge() <= 65 && weight >= 50 && haemoglobin >= 12.5 && (lastDonationDate == null || !lastDonationDate.plusDays(90).isAfter(today));
    }
    public void recordCollection(LocalDate date) {
        Validation.require(isEligible(date), "Donor does not meet the configured eligibility rules.");
        lastDonationDate = date;
    }
    public BloodDonation donate(BloodBankService service, String bankId, int quantity) {
        return service.donate(bankId, quantity);
    }
    public String viewProfile(List<BloodDonation> history, LocalDate today) {
        String donations = history.isEmpty() ? "No donations yet." : history.stream()
                .map(BloodDonation::toString).collect(java.util.stream.Collectors.joining("\n"));
        return getDetails() + "\nBlood group: " + bloodGroup + " | Weight: " + weight + " | Haemoglobin: " + haemoglobin
                + "\nLast donation: " + (lastDonationDate == null ? "None" : lastDonationDate)
                + " | Eligible: " + (isEligible(today) ? "Yes" : "No") + "\nDonation history:\n" + donations;
    }
}
