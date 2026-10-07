package bloodbank.service;

import java.io.Serializable;
import java.util.*;
import bloodbank.model.*;
import bloodbank.utility.Validation;

public final class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;
    boolean hospitalStarterStockAdded;
    public final LinkedHashMap<String, Person> people = new LinkedHashMap<>();
    public final LinkedHashMap<String, Organization> facilities = new LinkedHashMap<>();
    public final LinkedHashMap<String, BloodDonation> donations = new LinkedHashMap<>();
    public final LinkedHashMap<String, BloodRequest> requests = new LinkedHashMap<>();
    public final LinkedHashMap<String, BloodTransfer> transfers = new LinkedHashMap<>();
    public final ArrayList<String> audit = new ArrayList<>();

    public String nextId(String prefix) {
        int number = 1;
        String id;
        do {
            id = prefix + String.format(java.util.Locale.ROOT, "%03d", number++);
        } while (donations.containsKey(id) || requests.containsKey(id) || transfers.containsKey(id));
        return id;
    }

    public Organization getFacility(String id) {
        return facilities.get(id);
    }

    public void addFacility(Organization facility) {
        facilities.put(facility.getOrganizationId(), facility);
    }

    public void addPerson(Person person) {
        Validation.require(person != null, "Person cannot be null.");
        people.put(person.getPersonId(), person);
        if (person instanceof HospitalStaff staff) {
            Organization org = facilities.get(staff.getFacilityId());
            if (org instanceof Hospital hospital) {
                hospital.addStaff(staff);
            }
        }
    }

    public void addDonation(BloodDonation donation) {
        donations.put(donation.getTransactionId(), donation);
    }

    public void addRequest(BloodRequest request) {
        requests.put(request.getTransactionId(), request);
    }

    public void addTransfer(BloodTransfer transfer) {
        transfers.put(transfer.getTransactionId(), transfer);
    }

    public void restorePatientCompletion() {
        for (BloodRequest request : requests.values()) {
            if ("COMPLETED".equalsIgnoreCase(request.getStatus()) || "FULFILLED".equalsIgnoreCase(request.getStatus())) {
                Person person = people.get(request.getPatientId());
                if (person instanceof Patient patient) patient.markCompleted();
            }
        }
    }

    public void validate() {
        Validation.require(!facilities.isEmpty(), "At least one facility must exist.");
        Validation.require(!people.isEmpty(), "At least one person must exist.");
    }
}
