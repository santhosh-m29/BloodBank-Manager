package bloodbank.service;
import java.io.Serializable;
import java.util.*;
import bloodbank.model.*;
import bloodbank.utility.Validation;
/** A single aggregate is persisted atomically, including all inventories and history. */
public final class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;
    final int version = 2;
    final LinkedHashMap<String, Person> people = new LinkedHashMap<>();
    final LinkedHashMap<String, Organization> facilities = new LinkedHashMap<>();
    final LinkedHashMap<String, BloodDonation> donations = new LinkedHashMap<>();
    final LinkedHashMap<String, BloodRequest> requests = new LinkedHashMap<>();
    final LinkedHashMap<String, BloodTransfer> transfers = new LinkedHashMap<>();
    final ArrayList<String> audit = new ArrayList<>();
    public void addPerson(Person person) {
        Validation.require(!people.containsKey(person.getPersonId()) && people.values().stream().noneMatch(p -> p.getUsername().equalsIgnoreCase(person.getUsername())), "Duplicate person ID or username.");
        if (person instanceof Staff staff) Validation.require(people.values().stream().noneMatch(p -> p instanceof Staff other && other.getEmployeeId().equals(staff.getEmployeeId())), "Duplicate employee ID.");
        people.put(person.getPersonId(), person);
        if (person instanceof HospitalStaff staff) ((Hospital) facilities.get(staff.getFacilityId())).addStaff(staff);
    }
    public void validate() {
        Validation.require(version == 2, "Unsupported data version.");
        Set<String> usernames = new HashSet<>(), unitIds = new HashSet<>(), transactionIds = new HashSet<>();
        for (Person person : people.values()) {
            Validation.require(usernames.add(person.getUsername().toLowerCase(Locale.ROOT)), "Duplicate username in saved data.");
            if (person instanceof HospitalStaff staff) Validation.require(facilities.get(staff.getFacilityId()) instanceof Hospital, "Missing staff hospital.");
            if (person instanceof BloodBankAdmin admin) Validation.require(facilities.get(admin.getFacilityId()) instanceof BloodBank, "Missing admin blood bank.");
            if (person instanceof Patient patient) Validation.require(facilities.get(patient.getHospitalId()) instanceof Hospital, "Missing patient hospital.");
        }
        for (Organization facility : facilities.values()) for (BloodUnit unit : facility.getInventory().getBloodUnits()) {
            Validation.require(unitIds.add(unit.getBloodUnitId()), "Blood unit exists in multiple inventories.");
            Validation.require(donations.containsKey(unit.getDonationId()), "Blood unit has no donation record.");
            if (unit.getReservedFor() != null) Validation.require(requests.containsKey(unit.getReservedFor()), "Missing reservation request.");
        }
        for (BloodDonation donation : donations.values()) {
            Validation.require(transactionIds.add(donation.getTransactionId()) && people.get(donation.getDonorId()) instanceof Donor && facilities.get(donation.getBankId()) instanceof BloodBank, "Invalid donation references.");
            Validation.require(donation.getUnits().size() == donation.getQuantity() && donation.getUnits().stream().allMatch(u -> unitIds.contains(u.getBloodUnitId())), "Donation unit history is incomplete.");
        }
        for (BloodRequest request : requests.values()) {
            Validation.require(transactionIds.add(request.getTransactionId()), "Duplicate transaction ID.");
            Validation.require(people.get(request.getPatientId()) instanceof Patient, "Missing request patient.");
            Patient patient = (Patient) people.get(request.getPatientId());
            Validation.require(patient.getHospitalId().equals(request.getHospitalId()) && patient.getBloodGroup().equals(request.getBloodGroup()), "Request patient/hospital/group mismatch.");
            if (request.getBankId() != null) Validation.require(facilities.get(request.getBankId()) instanceof BloodBank, "Missing request blood bank.");
        }
        for (BloodTransfer transfer : transfers.values()) Validation.require(transactionIds.add(transfer.getTransactionId()) && requests.containsKey(transfer.getRequestId()) && facilities.get(transfer.getSourceFacilityId()) instanceof BloodBank && facilities.get(transfer.getDestinationFacilityId()) instanceof Hospital && transfer.getUnitIds().size() == transfer.getUnitsTransferred(), "Invalid transfer history.");
    }
}
