package bloodbank.utility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import bloodbank.model.*;
import bloodbank.service.SystemState;

/**
 * Handles reading from and writing to plain text (.txt) files in the data directory.
 */
public final class FileManager implements AutoCloseable {
    private final Path dataDirectory;

    public FileManager() {
        this(Path.of("data"));
    }

    public FileManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Path getFilePath() {
        return dataDirectory;
    }

    public void lock() {
        // Flat file mode - no locking mechanism needed
    }

    @Override
    public void close() {
        // AutoCloseable resource cleanup
    }

    public static SystemState copy(SystemState data) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bytes)) { out.writeObject(data); }
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { return (SystemState) in.readObject(); }
        } catch (IOException | ClassNotFoundException ex) { throw new IllegalStateException("Could not copy application state.", ex); }
    }

    public SystemState loadData() throws IOException {
        if (!Files.exists(dataDirectory)) {
            Files.createDirectories(dataDirectory);
            return null;
        }

        Path bloodBanksFile = dataDirectory.resolve("bloodbanks.txt");
        Path hospitalsFile = dataDirectory.resolve("hospitals.txt");
        Path adminFile = dataDirectory.resolve("admin.txt");
        Path staffFile = dataDirectory.resolve("hospitalstaff.txt");
        Path donorsFile = dataDirectory.resolve("donors.txt");
        Path patientsFile = dataDirectory.resolve("patients.txt");
        Path bloodUnitsFile = dataDirectory.resolve("bloodunits.txt");
        Path donationsFile = dataDirectory.resolve("donations.txt");
        Path requestsFile = dataDirectory.resolve("requests.txt");
        Path transfersFile = dataDirectory.resolve("transfers.txt");

        if (!Files.exists(bloodBanksFile) && !Files.exists(hospitalsFile) && !Files.exists(adminFile)) {
            return null;
        }

        SystemState state = new SystemState();

        // 1. BloodBanks
        if (Files.exists(bloodBanksFile)) {
            for (String line : Files.readAllLines(bloodBanksFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 5) {
                    state.addFacility(new BloodBank(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim()));
                }
            }
        }

        // 2. Hospitals
        if (Files.exists(hospitalsFile)) {
            for (String line : Files.readAllLines(hospitalsFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    state.addFacility(new Hospital(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim()));
                }
            }
        }

        // 3. Admins
        if (Files.exists(adminFile)) {
            for (String line : Files.readAllLines(adminFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 11) {
                    String level = parts.length >= 12 ? parts[11].trim() : "STANDARD";
                    state.addPerson(new BloodBankAdmin(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[9].trim(), parts[10].trim(), level));
                } else if (parts.length >= 9) {
                    state.addPerson(new BloodBankAdmin(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[6].trim(), parts[7].trim(), parts[8].trim()));
                }
            }
        }

        // 4. Hospital Staff
        if (Files.exists(staffFile)) {
            for (String line : Files.readAllLines(staffFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 11) {
                    String dept = parts.length >= 12 ? parts[11].trim() : "General";
                    state.addPerson(new HospitalStaff(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[9].trim(), parts[10].trim(), dept));
                } else if (parts.length >= 9) {
                    state.addPerson(new HospitalStaff(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[6].trim(), parts[7].trim(), parts[8].trim()));
                }
            }
        }

        // 5. Donors
        if (Files.exists(donorsFile)) {
            for (String line : Files.readAllLines(donorsFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 12) {
                    Donor donor = new Donor(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[9].trim(), Double.parseDouble(parts[10].trim()), Double.parseDouble(parts[11].trim()));
                    if (parts.length >= 13 && !parts[12].trim().isEmpty()) {
                        try {
                            donor.setLastDonationDate(LocalDate.parse(parts[12].trim()));
                        } catch (Exception ignored) {}
                    }
                    state.addPerson(donor);
                } else if (parts.length >= 9) {
                    Donor donor = new Donor(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[6].trim(), Double.parseDouble(parts[7].trim()), Double.parseDouble(parts[8].trim()));
                    state.addPerson(donor);
                }
            }
        }

        // 6. Patients
        if (Files.exists(patientsFile)) {
            for (String line : Files.readAllLines(patientsFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 14) {
                    state.addPerson(new Patient(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[9].trim(), parts[10].trim(), parts[11].trim(), Integer.parseInt(parts[12].trim()), parts[13].trim()));
                } else if (parts.length >= 11) {
                    state.addPerson(new Patient(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[6].trim(), parts[7].trim(), parts[8].trim(), Integer.parseInt(parts[9].trim()), parts[10].trim()));
                }
            }
        }

        // 7. Blood Units
        if (Files.exists(bloodUnitsFile)) {
            for (String line : Files.readAllLines(bloodUnitsFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    String unitId = parts[0].trim();
                    String group = parts[1].trim();
                    int qty = Integer.parseInt(parts[2].trim());
                    LocalDate collected = LocalDate.parse(parts[3].trim());
                    LocalDate expires = LocalDate.parse(parts[4].trim());
                    String status = parts[5].trim();
                    String facilityId = parts.length >= 7 ? parts[6].trim() : "BB001";
                    BloodUnit unit = new BloodUnit(unitId, group, qty, collected, expires, status, parts.length >= 8 ? parts[7].trim() : unitId);
                    if (parts.length >= 9) unit.restoreIssuedTo(parts[8].trim());
                    Organization org = state.getFacility(facilityId);
                    if (org != null) {
                        org.getInventory().addBloodUnit(unit);
                    } else if (!state.facilities.isEmpty()) {
                        state.facilities.values().iterator().next().getInventory().addBloodUnit(unit);
                    }
                }
            }
        }

        // 8. Donations
        if (Files.exists(donationsFile)) {
            for (String line : Files.readAllLines(donationsFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    String id = parts[0].trim();
                    LocalDate date = LocalDate.parse(parts[1].trim());
                    String status = parts[2].trim();
                    String donorId = parts[3].trim();
                    String group = parts[4].trim();
                    int qty = Integer.parseInt(parts[5].trim());
                    String bankId = parts.length >= 7 ? parts[6].trim() : "BB001";
                    state.addDonation(new BloodDonation(id, date, status, donorId, group, qty, bankId));
                }
            }
        }

        // 9. Requests
        if (Files.exists(requestsFile)) {
            for (String line : Files.readAllLines(requestsFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 8) {
                    String id = parts[0].trim();
                    LocalDate date = LocalDate.parse(parts[1].trim());
                    String status = parts[2].trim();
                    String patientId = parts[3].trim();
                    String hospitalId = parts[4].trim();
                    String group = parts[5].trim();
                    int qty = Integer.parseInt(parts[6].trim());
                    String urgency = parts[7].trim();
                    BloodRequest request = new BloodRequest(id, date, status, patientId, hospitalId, group, qty, urgency);
                    if (parts.length >= 11) request.restoreIssuedUnitIds(parts[10].trim());
                    state.addRequest(request);
                }
            }
        }

        // 10. Transfers
        if (Files.exists(transfersFile)) {
            for (String line : Files.readAllLines(transfersFile, StandardCharsets.UTF_8)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 7) {
                    String id = parts[0].trim();
                    LocalDate date = LocalDate.parse(parts[1].trim());
                    String status = parts[2].trim();
                    String src = parts[3].trim();
                    String dst = parts[4].trim();
                    String group = parts[5].trim();
                    int qty = Integer.parseInt(parts[6].trim());
                    state.addTransfer(new BloodTransfer(id, date, status, src, dst, group, qty, ""));
                }
            }
        }

        state.restorePatientCompletion();
        return state;
    }

    public void saveData(SystemState state) throws IOException {
        Files.createDirectories(dataDirectory);

        // 1. BloodBanks
        Path bbPath = dataDirectory.resolve("bloodbanks.txt");
        List<String> bbLines = new ArrayList<>();
        for (Organization org : state.facilities.values()) {
            if (org instanceof BloodBank b) {
                bbLines.add(String.join(",", b.getOrganizationId(), b.getOrganizationName(), b.getAddress(), b.getPhoneNumber(), b.getManagerName()));
            }
        }
        Files.write(bbPath, bbLines, StandardCharsets.UTF_8);

        // 2. Hospitals
        Path hospPath = dataDirectory.resolve("hospitals.txt");
        List<String> hospLines = new ArrayList<>();
        for (Organization org : state.facilities.values()) {
            if (org instanceof Hospital h) {
                hospLines.add(String.join(",", h.getOrganizationId(), h.getOrganizationName(), h.getAddress(), h.getPhoneNumber(), h.getHospitalType(), h.getEmergencyContact()));
            }
        }
        Files.write(hospPath, hospLines, StandardCharsets.UTF_8);

        // 3. Admins
        Path adminPath = dataDirectory.resolve("admin.txt");
        List<String> adminLines = new ArrayList<>();
        for (Person p : state.people.values()) {
            if (p instanceof BloodBankAdmin a) {
                adminLines.add(String.join(",", a.getPersonId(), a.getName(), String.valueOf(a.getAge()), a.getGender(), a.getPhoneNumber(), a.getAddress(), "admin", "admin123", "BLOOD_BANK_ADMIN", a.getEmployeeId(), a.getFacilityId(), a.getAdminLevel()));
            }
        }
        Files.write(adminPath, adminLines, StandardCharsets.UTF_8);

        // 4. Hospital Staff
        Path staffPath = dataDirectory.resolve("hospitalstaff.txt");
        List<String> staffLines = new ArrayList<>();
        for (Person p : state.people.values()) {
            if (p instanceof HospitalStaff s) {
                staffLines.add(String.join(",", s.getPersonId(), s.getName(), String.valueOf(s.getAge()), s.getGender(), s.getPhoneNumber(), s.getAddress(), "staff", "staff123", "HOSPITAL_STAFF", s.getEmployeeId(), s.getFacilityId(), s.getDepartment()));
            }
        }
        Files.write(staffPath, staffLines, StandardCharsets.UTF_8);

        // 5. Donors
        Path donorPath = dataDirectory.resolve("donors.txt");
        List<String> donorLines = new ArrayList<>();
        for (Person p : state.people.values()) {
            if (p instanceof Donor d) {
                String lastDate = d.getLastDonationDate() != null ? d.getLastDonationDate().toString() : "";
                donorLines.add(String.join(",", d.getPersonId(), d.getName(), String.valueOf(d.getAge()), d.getGender(), d.getPhoneNumber(), d.getAddress(), "donor", "donor123", "DONOR", d.getBloodGroup(), String.valueOf(d.getHaemoglobin()), String.valueOf(d.getWeight()), lastDate));
            }
        }
        Files.write(donorPath, donorLines, StandardCharsets.UTF_8);

        // 6. Patients
        Path patientPath = dataDirectory.resolve("patients.txt");
        List<String> patientLines = new ArrayList<>();
        for (Person p : state.people.values()) {
            if (p instanceof Patient pat) {
                patientLines.add(String.join(",", pat.getPersonId(), pat.getName(), String.valueOf(pat.getAge()), pat.getGender(), pat.getPhoneNumber(), pat.getAddress(), "patient", "patient123", "PATIENT", pat.getBloodGroup(), pat.getDisease(), pat.getDoctorName(), String.valueOf(pat.getUnitsRequired()), pat.getHospitalId()));
            }
        }
        Files.write(patientPath, patientLines, StandardCharsets.UTF_8);

        // 7. Blood Units
        Path unitsPath = dataDirectory.resolve("bloodunits.txt");
        List<String> unitLines = new ArrayList<>();
        for (Organization org : state.facilities.values()) {
            for (BloodUnit u : org.getInventory().getBloodUnits()) {
                unitLines.add(String.join(",", u.getBloodUnitId(), u.getBloodGroup(), String.valueOf(u.getQuantity()), u.getCollectionDate().toString(), u.getExpiryDate().toString(), u.getStatus(), org.getOrganizationId(), u.getDonationId(), u.getIssuedTo() == null ? "" : u.getIssuedTo()));
            }
        }
        Files.write(unitsPath, unitLines, StandardCharsets.UTF_8);

        // 8. Donations
        Path donPath = dataDirectory.resolve("donations.txt");
        List<String> donLines = new ArrayList<>();
        for (BloodDonation d : state.donations.values()) {
            donLines.add(String.join(",", d.getTransactionId(), d.getTransactionDate().toString(), d.getStatus(), d.getDonorId(), d.getBloodGroup(), String.valueOf(d.getQuantity()), d.getBankId()));
        }
        Files.write(donPath, donLines, StandardCharsets.UTF_8);

        // 9. Requests
        Path reqPath = dataDirectory.resolve("requests.txt");
        List<String> reqLines = new ArrayList<>();
        for (BloodRequest r : state.requests.values()) {
            reqLines.add(String.join(",", r.getTransactionId(), r.getTransactionDate().toString(), r.getStatus(), r.getPatientId(), r.getHospitalId(), r.getBloodGroup(), String.valueOf(r.getUnitsRequested()), r.getUrgency(), r.getUrgency(), "false", String.join(";", r.getIssuedUnitIds())));
        }
        Files.write(reqPath, reqLines, StandardCharsets.UTF_8);

        // 10. Transfers
        Path trPath = dataDirectory.resolve("transfers.txt");
        List<String> trLines = new ArrayList<>();
        for (BloodTransfer t : state.transfers.values()) {
            trLines.add(String.join(",", t.getTransactionId(), t.getTransactionDate().toString(), t.getStatus(), t.getSourceFacilityId(), t.getDestinationFacilityId(), t.getBloodGroup(), String.valueOf(t.getUnitsTransferred())));
        }
        Files.write(trPath, trLines, StandardCharsets.UTF_8);
    }
}
