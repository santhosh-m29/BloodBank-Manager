package bloodbank.service;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import bloodbank.model.*;
import bloodbank.utility.*;
/** The role workflow and transaction boundary used by every interactive operation. */
public final class BloodBankService {
    private SystemState state;
    private final FileManager files;
    private final Clock clock;
    private String selectedRole;
    private String selectedPatientId;
    public BloodBankService(SystemState state, FileManager files, Clock clock) {
        state.restorePatientCompletion();
        state.validate();
        this.state = FileManager.copy(state);
        this.files = files;
        this.clock = clock;
        if (DemoData.addHospitalStarterStock(this.state, today())) {
            try { files.saveData(this.state); }
            catch (IOException ex) { throw new IllegalStateException("Could not save hospital starter stock.", ex); }
        }
    }
    public LocalDate today() {
        return LocalDate.now(clock);
    }
    public void selectRole(String role) {
        selectedRole = null;
        selectedPatientId = null;
        Validation.require(Set.of("DONOR", "PATIENT", "HOSPITAL", "BLOOD_BANK").contains(role), "Invalid role choice.");
        selectedRole = role;
    }

    public void clearRole() { selectedRole = null; selectedPatientId = null; }

    public Person currentUser() {
        return actor(FileManager.copy(state));
    }
    private Person actor(SystemState s) {
        Validation.require(selectedRole != null, "Choose a role first.");
        if ("PATIENT".equals(selectedRole) && selectedPatientId != null) return found(s.people.get(selectedPatientId), "Patient");
        return s.people.values().stream().filter(p -> p.getRole().equals(selectedRole)).findFirst().orElseThrow(() -> new IllegalStateException("Selected role is missing."));
    }
    private HospitalStaff staff(SystemState s) {
        Person p = actor(s);
        Validation.require(p instanceof HospitalStaff, "Only hospital staff can perform this operation.");
        return (HospitalStaff) p;
    }
    private BloodBankAdmin admin(SystemState s, boolean write) {
        Person p = actor(s);
        Validation.require(p instanceof BloodBankAdmin, "Only blood-bank administrators can perform this operation.");
        BloodBankAdmin a = (BloodBankAdmin) p;
        Validation.require(!write || !a.getAdminLevel().equals("READ_ONLY"), "This administrator has read-only access.");
        return a;
    }
    private static <T> T found(T value, String label) {
        Validation.require(value != null, label + " not found.");
        return value;
    }
    private Inventory inventory(SystemState s, String facility) {
        return found(s.facilities.get(facility), "Facility").getInventory();
    }
    private Patient patient(SystemState s, String id, String hospital) {
        Person p = s.people.get(id);
        Validation.require(p instanceof Patient && ((Patient) p).getHospitalId().equals(hospital), "Patient does not belong to your hospital.");
        return (Patient) p;
    }
    private BloodRequest request(SystemState s, String id) {
        String searchId = id == null ? "" : id.trim();
        BloodRequest r = s.requests.get(searchId);
        if (r == null) {
            r = s.requests.values().stream()
                    .filter(item -> item.getTransactionId().equalsIgnoreCase(searchId))
                    .findFirst()
                    .orElse(null);
        }
        return found(r, "Request");
    }
    private BloodRequest bankRequest(SystemState s, String id, BloodBankAdmin a) {
        BloodRequest r = request(s, id);
        Validation.require(a.getFacilityId().equals(r.getBankId()), "Request is not assigned to your blood bank.");
        patient(s, r.getPatientId(), r.getHospitalId());
        return r;
    }
    private BloodDonation donation(SystemState s, String id, BloodBankAdmin a) {
        String searchId = id == null ? "" : id.trim();
        BloodDonation d = s.donations.get(searchId);
        if (d == null) {
            d = s.donations.values().stream()
                    .filter(item -> item.getTransactionId().equalsIgnoreCase(searchId))
                    .findFirst()
                    .orElse(null);
        }
        Validation.require(d != null, "Donation not found with ID: " + id);
        Validation.require(d.getBankId().equals(a.getFacilityId()), "Donation belongs to another blood bank.");
        return d;
    }
    private <T> T commit(String action, Function<SystemState, T> operation) {
        actor(state);
        SystemState next = FileManager.copy(state);
        T result = operation.apply(next);
        next.audit.add(Instant.now(clock) + " | " + actor(next).getPersonId() + " | " + action);
        next.validate();
        try {
            files.saveData(next);
        } catch (IOException ex) {
            throw new IllegalStateException("Save failed; no changes were committed: " + ex.getMessage(), ex);
        }
        // Detach returned objects from authoritative state so callers cannot bypass the service.
        state = FileManager.copy(next);
        return result;
    }

    public void updateProfile(String name, int age, String gender, String phone, String address) {
        commit("PROFILE_UPDATED", s -> {
            actor(s).updateDetails(name, age, gender, phone, address); return null;
        });
    }
    public void updateMeasurements(double hb, double weight) {
        commit("MEASUREMENTS_UPDATED", s -> {
            Validation.require(actor(s) instanceof Donor, "Only donors can update donor measurements."); ((Donor) actor(s)).updateMeasurements(hb, weight); return null;
        });
    }
    public BloodDonation donate(int quantity) {
        return commit("DONATION_AUTOMATICALLY_TESTED", s -> {
            Validation.require(actor(s) instanceof Donor, "Choose Donor to donate blood.");
            Donor donor = (Donor) actor(s);
            String bankId = s.facilities.values().stream().filter(f -> f instanceof BloodBank).findFirst().orElseThrow().getOrganizationId();
            BloodDonation donation = new BloodDonation(s.nextId("D"), today(), donor.getPersonId(), donor.getBloodGroup(), quantity, bankId);
            donation.recordDonation(inventory(s, bankId));
            donation.test(donor, today());
            if (donation.getStatus().equals("TEST_PASSED")) donor.recordCollection(today());
            s.donations.put(donation.getTransactionId(), donation);
            return donation;
        });
    }


    public void registerDonation(String donationId) {
        commit("DONATION_REGISTERED " + donationId, s -> {
            BloodBankAdmin a = admin(s, true); donation(s, donationId, a).updateInventory(inventory(s, a.getFacilityId()), today(), a.getPersonId()); return null;
        });
    }




    public BloodRequest requestBlood(String patientId, String urgency) {
        return commit("REQUEST_CREATED " + patientId, s -> {
            HospitalStaff h = staff(s);
            Patient p = patient(s, patientId, h.getFacilityId());
            Validation.require(!p.getStatus().equals("COMPLETED"), "This patient has already received blood and is COMPLETED.");
            Validation.require(Set.of("CRITICAL", "HIGH", "MEDIUM", "LOW").contains(urgency), "Invalid urgency.");
            BloodRequest r = new BloodRequest(s.nextId("R"), today(), patientId, h.getFacilityId(), p.getBloodGroup(), p.getUnitsRequired(), urgency);
            s.requests.put(r.getTransactionId(), r);
            return r;
        });
    }
    public Patient createPatient(String name, int age, String gender, String phone, String address, String group, String reason, String doctor, int units) {
        return commit("PATIENT_CREATED", s -> {
            HospitalStaff h = staff(s);
            int n = 1;
            while (s.people.containsKey(String.format(Locale.ROOT, "PAT%03d", n))) n++;
            Patient p = new Patient(String.format(Locale.ROOT, "PAT%03d", n), name, age, gender, phone, address, group, reason, doctor, units, h.getFacilityId());
            s.addPerson(p);
            return p;
        });
    }
    public List<Patient> patientChoices() {
        Validation.require("PATIENT".equals(selectedRole), "Choose the Patient role first.");
        return FileManager.copy(state).people.values().stream().filter(p -> p instanceof Patient).map(p -> (Patient)p).toList();
    }
    public void selectPatient(String id) {
        Validation.require("PATIENT".equals(selectedRole) && state.people.get(id) instanceof Patient, "Invalid patient selection.");
        selectedPatientId = id;
    }

    public int shortage(String requestId) {
        throw new UnsupportedOperationException("Shortage calculation and reservations are planned for Phase 2 and are not part of the current 50% implementation.");
    }
    public void requestFromBank(String requestId, String bankId) {
        throw new UnsupportedOperationException("Request routing and shortage transfer are planned for Phase 2 and are not part of the current 50% implementation.");
    }
    public void approveRequest(String requestId) {
        throw new UnsupportedOperationException("Request approval and reservations are planned for Phase 2 and are not part of the current 50% implementation.");
    }
    public void rejectRequest(String requestId, String reason) {
        throw new UnsupportedOperationException("Request rejection is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public BloodTransfer dispatchTransfer(String requestId) {
        throw new UnsupportedOperationException("Blood transfer is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void issueBlood(String requestId) {
        commit("BLOOD_ISSUED " + requestId, s -> {
            HospitalStaff h = staff(s);
            BloodRequest r = request(s, requestId);
            Patient recipient = patient(s, r.getPatientId(), h.getFacilityId());
            Validation.require(!recipient.getStatus().equals("COMPLETED"), "This patient has already received blood and is COMPLETED.");
            Validation.require(r.getStatus().equals("PENDING"), "Only pending requests can be issued; this request is already processed.");
            Inventory local = inventory(s, h.getFacilityId());
            Validation.require(local.getStockForGroup(r.getBloodGroup(), today()) >= r.getUnitsRequested(), "Insufficient hospital stock. Request remains PENDING; blood-bank transfers are not implemented yet.");
            List<BloodUnit> issued = new ArrayList<>();
            int remaining = r.getUnitsRequested();
            for (BloodUnit unit : local.available(r.getBloodGroup(), null, today())) {
                if (remaining == 0) break;
                int quantity = Math.min(remaining, unit.getQuantity());
                BloodUnit portion = unit.takeForIssue(quantity, r.getTransactionId(), r.getPatientId(), today());
                if (portion != unit) local.addBloodUnit(portion);
                issued.add(portion);
                remaining -= quantity;
            }
            r.fulfill(issued);
            recipient.markCompleted();
            return null;
        });
    }

    private List<BloodRequest> sortedRequests(SystemState s) {
        return s.requests.values().stream().sorted(Comparator.comparingInt(BloodRequest::priorityDispatch).thenComparing(BloodRequest::getTransactionDate)).toList();
    }
    private boolean canReadRequest(Person p, BloodRequest r) {
        return p instanceof Patient && p.getPersonId().equals(r.getPatientId()) || p instanceof HospitalStaff h && h.getFacilityId().equals(r.getHospitalId()) || p instanceof BloodBankAdmin;
    }
    private void authorizeRequestRead(SystemState s, BloodRequest r) {
        Validation.require(canReadRequest(actor(s), r), "Request access denied.");
    }
    public List<BloodRequest> requests() {
        SystemState copy = FileManager.copy(state);
        Person p = actor(copy);
        return sortedRequests(copy).stream().filter(r -> canReadRequest(p, r)).toList();
    }
    public List<BloodDonation> donations() {
        SystemState copy = FileManager.copy(state);
        Person p = actor(copy);
        return copy.donations.values().stream().filter(d -> p instanceof Donor && p.getPersonId().equals(d.getDonorId()) || p instanceof BloodBankAdmin a && a.getFacilityId().equals(d.getBankId())).toList();
    }
    public List<Patient> patients() {
        SystemState copy = FileManager.copy(state);
        HospitalStaff h = staff(copy);
        return copy.people.values().stream().filter(p -> p instanceof Patient patient && patient.getHospitalId().equals(h.getFacilityId())).map(p -> (Patient) p).toList();
    }
    public List<Patient> patientsAwaitingBlood() {
        return patients().stream().filter(p -> !p.getStatus().equals("COMPLETED")).toList();
    }
    public List<BloodTransfer> transfers() {
        throw new UnsupportedOperationException("Blood transfer history is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public Inventory ownInventory() {
        Person p = actor(state);
        Validation.require(p instanceof Staff, "Inventory access denied.");
        SystemState copy = FileManager.copy(state);
        return inventory(copy, ((Staff) p).getFacilityId());
    }
    public List<String> inventoryRows() {
        return ownInventory().summaryRows(id -> {
            BloodDonation donation = state.donations.get(id);
            return donation == null ? "Opening stock" : donation.getDonorId();
        }, today());
    }
    public Map<String, String> facilities() {
        actor(state);
        Map<String, String> result = new LinkedHashMap<>();
        state.facilities.forEach((id, f) -> result.put(id, f.getOrganizationName() + (f instanceof BloodBank ? " [Blood Bank]" : " [Hospital]")));
        return Collections.unmodifiableMap(result);
    }
    public List<String> audit() {
        admin(state, false);
        String id = actor(state).getPersonId();
        return state.audit.stream().filter(line -> line.contains(" | " + id + " | ")).toList();
    }
}
