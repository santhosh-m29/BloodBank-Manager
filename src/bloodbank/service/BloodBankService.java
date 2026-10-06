package bloodbank.service;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import bloodbank.model.*;
import bloodbank.utility.*;
/** The authorization and transaction boundary used by every interactive operation. */
public final class BloodBankService {
    private SystemState state;
    private final FileManager files;
    private final Clock clock;
    private final LoginManager login = new LoginManager();
    public BloodBankService(SystemState state, FileManager files, Clock clock) {
        state.validate();
        this.state = FileManager.copy(state);
        this.files = files;
        this.clock = clock;
    }
    public LocalDate today() {
        return LocalDate.now(clock);
    }
    public static String newId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString();
    }
    public boolean login(String username, String password) {
        return login.authenticateUser(state, username, password);
    }
    public void logout() {
        login.logoutUser();
    }
    public Person currentUser() {
        return login.current(FileManager.copy(state));
    }
    private Person actor(SystemState s) {
        return login.current(s);
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
        return found(s.requests.get(id), "Request");
    }
    private BloodRequest bankRequest(SystemState s, String id, BloodBankAdmin a) {
        BloodRequest r = request(s, id);
        Validation.require(a.getFacilityId().equals(r.getBankId()), "Request is not assigned to your blood bank.");
        patient(s, r.getPatientId(), r.getHospitalId());
        return r;
    }
    private BloodDonation donation(SystemState s, String id, BloodBankAdmin a) {
        BloodDonation d = found(s.donations.get(id), "Donation");
        Validation.require(d.getBankId().equals(a.getFacilityId()), "Donation belongs to another blood bank.");
        return d;
    }
    private <T> T commit(String action, Function<SystemState, T> operation) {
        actor(state);
        SystemState next = FileManager.copy(state);
        next.facilities.values().forEach(f -> f.getInventory().expire(today()));
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
    public void changePassword(String oldPassword, String newPassword) {
        commit("PASSWORD_CHANGED", s -> {
            login.changePassword(s, oldPassword, newPassword); return null;
        });
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
    public BloodDonation donate(String bankId, int quantity) {
        return commit("DONATION_COLLECTED", s -> {
            Validation.require(actor(s) instanceof Donor, "Only donors can initiate a donation."); Validation.require(s.facilities.get(bankId) instanceof BloodBank, "Blood bank not found."); Donor donor = (Donor) actor(s); Validation.positive(quantity); donor.recordCollection(today()); BloodDonation d = new BloodDonation(newId("DON"), today(), donor.getPersonId(), donor.getBloodGroup(), quantity, bankId); d.recordDonation(inventory(s, bankId)); s.donations.put(d.getTransactionId(), d); return d;
        });
    }
    public void labTest(String donationId, boolean passed, String note) {
        commit("LAB_TEST " + donationId, s -> {
            BloodBankAdmin a = admin(s, true); BloodDonation d = donation(s, donationId, a); Validation.require(d.getUnits().stream().noneMatch(u -> u.isExpired(today())), "Donation has expired."); d.test(passed, note, a.getPersonId()); return null;
        });
    }
    public void registerDonation(String donationId) {
        commit("DONATION_REGISTERED " + donationId, s -> {
            BloodBankAdmin a = admin(s, true); donation(s, donationId, a).updateInventory(inventory(s, a.getFacilityId()), today(), a.getPersonId()); return null;
        });
    }
    public void createPatient(Patient patient) {
        commit("PATIENT_CREATED " + patient.getPersonId(), s -> {
            HospitalStaff h = staff(s); Validation.require(h.getFacilityId().equals(patient.getHospitalId()), "Cannot create patients for another hospital."); s.addPerson(patient); return null;
        });
    }
    public void registerDonor(Donor donor) {
        commit("DONOR_CREATED " + donor.getPersonId(), s -> {
            admin(s, true); s.addPerson(donor); return null;
        });
    }
    public void registerFacility(Organization facility) {
        commit("FACILITY_CREATED " + facility.getOrganizationId(), s -> {
            Validation.require(admin(s, true).getAdminLevel().equals("SUPER"), "Only SUPER administrators can create facilities."); Validation.require(!s.facilities.containsKey(facility.getOrganizationId()) && facility.getInventory().getBloodUnits().isEmpty(), "Duplicate facility or nonempty initial inventory."); s.facilities.put(facility.getOrganizationId(), facility); return null;
        });
    }
    public void registerStaff(Staff person) {
        commit("STAFF_CREATED " + person.getPersonId(), s -> {
            Validation.require(admin(s, true).getAdminLevel().equals("SUPER"), "Only SUPER administrators can create staff."); Validation.require((person instanceof HospitalStaff && s.facilities.get(person.getFacilityId()) instanceof Hospital) || (person instanceof BloodBankAdmin && s.facilities.get(person.getFacilityId()) instanceof BloodBank), "Invalid staff facility."); s.addPerson(person); return null;
        });
    }
    public BloodRequest requestBlood(String patientId, int units, String urgency) {
        return commit("REQUEST_CREATED " + patientId, s -> {
            HospitalStaff h = staff(s); Patient p = patient(s, patientId, h.getFacilityId()); BloodRequest r = new BloodRequest(newId("REQ"), today(), patientId, h.getFacilityId(), p.getBloodGroup(), units, urgency); s.requests.put(r.getTransactionId(), r); reserveLocal(s, r); return r;
        });
    }
    private void reserveLocal(SystemState s, BloodRequest r) {
        Inventory local = inventory(s, r.getHospitalId());
        local.release(r.getTransactionId());
        List<BloodUnit> available = local.available(r.getBloodGroup(), r.getTransactionId(), today());
        available.stream().limit(r.getUnitsRequested()).forEach(u -> u.reserve(r.getTransactionId(), today()));
        if (shortage(s, r) == 0) r.ready();
    }
    private int shortage(SystemState s, BloodRequest r) {
        return Math.max(0, r.getUnitsRequested() - inventory(s, r.getHospitalId()).reserved(r.getTransactionId(), today()).size());
    }
    public int shortage(String requestId) {
        BloodRequest r = request(state, requestId);
        authorizeRequestRead(state, r);
        return shortage(state, r);
    }
    public void requestFromBank(String requestId, String bankId) {
        commit("REQUEST_ROUTED " + requestId, s -> {
            HospitalStaff h = staff(s); BloodRequest r = request(s, requestId); patient(s, r.getPatientId(), h.getFacilityId()); Validation.require(s.facilities.get(bankId) instanceof BloodBank, "Blood bank not found."); r.reopen(); reserveLocal(s, r); if (shortage(s, r) > 0) r.route(bankId); return null;
        });
    }
    public void approveRequest(String requestId) {
        commit("REQUEST_APPROVED " + requestId, s -> {
            BloodBankAdmin a = admin(s, true); BloodRequest r = bankRequest(s, requestId, a); Validation.require(r.getStatus().equals("PENDING"), "Only pending requests can be approved."); int shortage = shortage(s, r); List<BloodUnit> stock = inventory(s, a.getFacilityId()).available(r.getBloodGroup(), null, today()); Validation.require(stock.size() >= shortage, "Insufficient central stock for the shortage.");
            // Do not consume scarce stock ahead of an earlier, fulfillable request of higher priority.
            for (BloodRequest earlier : sortedRequests(s)) {
                if (earlier == r) break; if (earlier.getStatus().equals("PENDING") && a.getFacilityId().equals(earlier.getBankId()) && earlier.getBloodGroup().equals(r.getBloodGroup()) && shortage(s, earlier) > 0 && shortage(s, earlier) <= stock.size()) throw new IllegalArgumentException("Process higher-priority/earlier request first: " + earlier.getTransactionId());
            }
            if (shortage == 0) r.ready(); else {
                stock.stream().limit(shortage).forEach(u -> u.reserve(r.getTransactionId(), today())); r.approveRequest();
            }
            return null;
        });
    }
    public void rejectRequest(String requestId, String reason) {
        commit("REQUEST_REJECTED " + requestId, s -> {
            BloodBankAdmin a = admin(s, true); BloodRequest r = bankRequest(s, requestId, a); r.rejectRequest(reason); s.facilities.values().forEach(f -> f.getInventory().release(requestId)); return null;
        });
    }
    public BloodTransfer dispatchTransfer(String requestId) {
        return commit("TRANSFER_DISPATCHED " + requestId, s -> {
            BloodBankAdmin a = admin(s, true); BloodRequest r = bankRequest(s, requestId, a); Validation.require(r.getStatus().equals("APPROVED"), "Request must be approved before dispatch."); int missing = shortage(s, r); Inventory central = inventory(s, a.getFacilityId()); Validation.require(central.reserved(requestId, today()).size() == missing, "Reserved stock changed or expired; reject and recreate this request."); BloodTransfer t = new BloodTransfer(newId("TR"), today(), a.getFacilityId(), r.getHospitalId(), r.getBloodGroup(), missing, requestId); t.transferUnits(central, inventory(s, r.getHospitalId()), today()); s.transfers.put(t.getTransactionId(), t); r.ready(); return t;
        });
    }
    public void issueBlood(String requestId) {
        commit("BLOOD_ISSUED " + requestId, s -> {
            HospitalStaff h = staff(s); BloodRequest r = request(s, requestId); patient(s, r.getPatientId(), h.getFacilityId()); Validation.require(r.getStatus().equals("READY"), "Request is not ready for issue."); List<BloodUnit> units = inventory(s, h.getFacilityId()).reserved(requestId, today()); Validation.require(units.size() == r.getUnitsRequested(), "Local stock expired or is insufficient. Recheck/request blood from the bank."); units.forEach(u -> u.issue(requestId, r.getPatientId(), today())); r.fulfill(units); return null;
        });
    }
    private List<BloodRequest> sortedRequests(SystemState s) {
        return s.requests.values().stream().sorted(Comparator.comparingInt(BloodRequest::priorityDispatch).thenComparing(BloodRequest::getTransactionDate)).toList();
    }
    private boolean canReadRequest(Person p, BloodRequest r) {
        return p instanceof Patient && p.getPersonId().equals(r.getPatientId()) || p instanceof HospitalStaff h && h.getFacilityId().equals(r.getHospitalId()) || p instanceof BloodBankAdmin a && a.getFacilityId().equals(r.getBankId());
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
    public List<BloodTransfer> transfers() {
        SystemState copy = FileManager.copy(state);
        Person p = actor(copy);
        Validation.require(p instanceof Staff, "Transfer access denied.");
        String facility = ((Staff) p).getFacilityId();
        return copy.transfers.values().stream().filter(t -> t.getSourceFacilityId().equals(facility) || t.getDestinationFacilityId().equals(facility)).toList();
    }
    public Inventory ownInventory() {
        Person p = actor(state);
        Validation.require(p instanceof Staff, "Inventory access denied.");
        SystemState copy = FileManager.copy(state);
        Inventory inv = inventory(copy, ((Staff) p).getFacilityId());
        inv.expire(today());
        return inv;
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
