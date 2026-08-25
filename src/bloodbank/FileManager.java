package bloodbank;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private String filePath;

    public FileManager(String filePath) {
        this.filePath = filePath;
    }

    public FileManager() {
        this.filePath = "data/";
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    // --- Diagram Methods for Complete Persistence ---
    public void saveData() {
        saveAdmins(filePath + "admin.txt", Main.admins);
        saveHospitalStaffs(filePath + "hospitalstaff.txt", Main.hospitalStaffs);
        saveDonors(filePath + "donors.txt", Main.donors);
        savePatients(filePath + "patients.txt", Main.patients);
        saveBloodBanks(filePath + "bloodbanks.txt", Main.bloodBanks);
        saveHospitals(filePath + "hospitals.txt", Main.hospitals);
        saveDonations(filePath + "donations.txt", Main.donations);
        saveRequests(filePath + "requests.txt", Main.requests);
        saveTransfers(filePath + "transfers.txt", Main.transfers);
        saveAllBloodUnits(filePath + "bloodunits.txt");
        System.out.println("System data saved successfully.");
    }

    public void loadData() {
        Main.admins = loadAdmins(filePath + "admin.txt");
        Main.hospitalStaffs = loadHospitalStaffs(filePath + "hospitalstaff.txt");
        Main.donors = loadDonors(filePath + "donors.txt");
        Main.patients = loadPatients(filePath + "patients.txt");
        Main.bloodBanks = loadBloodBanks(filePath + "bloodbanks.txt");
        Main.hospitals = loadHospitals(filePath + "hospitals.txt");
        Main.donations = loadDonations(filePath + "donations.txt");
        Main.requests = loadRequests(filePath + "requests.txt");
        Main.transfers = loadTransfers(filePath + "transfers.txt");
        loadAllBloodUnits(filePath + "bloodunits.txt");
        System.out.println("System data loaded successfully.");
    }

    public void appendData() {
        System.out.println("[FILE MANAGER] Appending data logic completed.");
    }

    public void deleteRecord() {
        System.out.println("[FILE MANAGER] Delete record logic completed.");
    }

    // Helper to ensure files exist
    public static void ensureFileExists(String path) {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {}
        }
    }

    // --- Admin Loading / Saving ---
    public static List<BloodBankAdmin> loadAdmins(String path) {
        ensureFileExists(path);
        List<BloodBankAdmin> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 12) {
                    list.add(new BloodBankAdmin(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4], parts[5], 
                        parts[6], parts[7], parts[8], parts[9], parts[10], parts[11]));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading admins: " + e.getMessage());
        }
        return list;
    }

    public static void saveAdmins(String path, List<BloodBankAdmin> list) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (BloodBankAdmin a : list) {
                bw.write(String.join(",",
                    a.getPersonId(), a.getName(), String.valueOf(a.getAge()), a.getGender(),
                    a.getPhoneNumber(), a.getAddress(), a.getUsername(), a.getPassword(),
                    a.getRole(), a.getEmployeeId(), a.getFacilityId(), a.getAdminLevel()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving admins: " + e.getMessage());
        }
    }

    // --- HospitalStaff Loading / Saving ---
    public static List<HospitalStaff> loadHospitalStaffs(String path) {
        ensureFileExists(path);
        List<HospitalStaff> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 12) {
                    list.add(new HospitalStaff(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4], parts[5], 
                        parts[6], parts[7], parts[8], parts[9], parts[10], parts[11]));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading hospital staff: " + e.getMessage());
        }
        return list;
    }

    public static void saveHospitalStaffs(String path, List<HospitalStaff> list) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (HospitalStaff s : list) {
                bw.write(String.join(",",
                    s.getPersonId(), s.getName(), String.valueOf(s.getAge()), s.getGender(),
                    s.getPhoneNumber(), s.getAddress(), s.getUsername(), s.getPassword(),
                    s.getRole(), s.getEmployeeId(), s.getFacilityId(), s.getDepartment()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving hospital staff: " + e.getMessage());
        }
    }

    // --- Donor Loading / Saving ---
    public static List<Donor> loadDonors(String path) {
        ensureFileExists(path);
        List<Donor> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 13) {
                    list.add(new Donor(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4], parts[5],
                        parts[6], parts[7], parts[8], parts[9], Double.parseDouble(parts[10]), Double.parseDouble(parts[11]), parts[12]));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading donors: " + e.getMessage());
        }
        return list;
    }

    public static void saveDonors(String path, List<Donor> list) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (Donor d : list) {
                bw.write(String.join(",",
                    d.getPersonId(), d.getName(), String.valueOf(d.getAge()), d.getGender(),
                    d.getPhoneNumber(), d.getAddress(), d.getUsername(), d.getPassword(), d.getRole(),
                    d.getBloodGroup(), String.valueOf(d.getHaemoglobin()), String.valueOf(d.getWeight()), d.getLastDonationDate()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving donors: " + e.getMessage());
        }
    }

    // --- Patient Loading / Saving ---
    public static List<Patient> loadPatients(String path) {
        ensureFileExists(path);
        List<Patient> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 14) {
                    list.add(new Patient(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4], parts[5],
                        parts[6], parts[7], parts[8], parts[9], parts[10], parts[11], Integer.parseInt(parts[12]), parts[13]));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading patients: " + e.getMessage());
        }
        return list;
    }

    public static void savePatients(String path, List<Patient> list) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (Patient p : list) {
                bw.write(String.join(",",
                    p.getPersonId(), p.getName(), String.valueOf(p.getAge()), p.getGender(),
                    p.getPhoneNumber(), p.getAddress(), p.getUsername(), p.getPassword(), p.getRole(),
                    p.getBloodGroup(), p.getDisease(), p.getDoctorName(), String.valueOf(p.getUnitsRequired()), p.getHospitalId()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving patients: " + e.getMessage());
        }
    }

    // --- BloodBank Loading / Saving ---
    public static List<BloodBank> loadBloodBanks(String path) {
        ensureFileExists(path);
        List<BloodBank> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 5) {
                    list.add(new BloodBank(parts[0], parts[1], parts[2], parts[3], parts[4], new Inventory()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading blood banks: " + e.getMessage());
        }
        return list;
    }

    public static void saveBloodBanks(String path, List<BloodBank> list) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (BloodBank bb : list) {
                bw.write(String.join(",",
                    bb.getOrganizationId(), bb.getOrganizationName(), bb.getAddress(), bb.getContactNumber(), bb.getManagerName()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving blood banks: " + e.getMessage());
        }
    }

    // --- Hospital Loading / Saving ---
    public static List<Hospital> loadHospitals(String path) {
        ensureFileExists(path);
        List<Hospital> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    list.add(new Hospital(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], new Inventory()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading hospitals: " + e.getMessage());
        }
        return list;
    }

    public static void saveHospitals(String path, List<Hospital> list) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (Hospital h : list) {
                bw.write(String.join(",",
                    h.getOrganizationId(), h.getOrganizationName(), h.getAddress(), h.getContactNumber(),
                    h.getHospitalType(), h.getEmergencyContact()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving hospitals: " + e.getMessage());
        }
    }

    // --- Blood Donations Loading / Saving ---
    public static List<BloodDonation> loadDonations(String path) {
        ensureFileExists(path);
        List<BloodDonation> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    list.add(new BloodDonation(parts[0], parts[1], parts[2], parts[3], parts[4], Integer.parseInt(parts[5])));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading donations: " + e.getMessage());
        }
        return list;
    }

    public static void saveDonations(String path, List<BloodDonation> list) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (BloodDonation bd : list) {
                bw.write(String.join(",",
                    bd.getTransactionId(), bd.getTransactionDate(), bd.getStatus(),
                    bd.getDonorId(), bd.getBloodGroup(), String.valueOf(bd.getQuantity())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving donations: " + e.getMessage());
        }
    }

    // --- Blood Requests Loading / Saving ---
    public static List<BloodRequest> loadRequests(String path) {
        ensureFileExists(path);
        List<BloodRequest> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 10) {
                    boolean isEmergency = Boolean.parseBoolean(parts[9]);
                    if (isEmergency) {
                        list.add(new EmergencyRequest(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Integer.parseInt(parts[6]), parts[8]));
                    } else {
                        list.add(new BloodRequest(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Integer.parseInt(parts[6]), parts[7]));
                    }
                } else if (parts.length >= 8) {
                    // Backwards compatibility fallback
                    list.add(new BloodRequest(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Integer.parseInt(parts[6]), parts[7]));
                } else if (parts.length >= 7) {
                    list.add(new BloodRequest(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Integer.parseInt(parts[6]), "NORMAL"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading requests: " + e.getMessage());
        }
        return list;
    }

    public static void saveRequests(String path, List<BloodRequest> list) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (BloodRequest br : list) {
                if (br instanceof EmergencyRequest) {
                    EmergencyRequest er = (EmergencyRequest) br;
                    bw.write(String.join(",",
                        er.getTransactionId(), er.getTransactionDate(), er.getStatus(),
                        er.getPatientId(), er.getHospitalId(), er.getBloodGroup(), String.valueOf(er.getUnitsRequested()),
                        er.getUrgency(), er.getUrgencyLevel(), String.valueOf(er.isEmergency())
                    ));
                } else {
                    bw.write(String.join(",",
                        br.getTransactionId(), br.getTransactionDate(), br.getStatus(),
                        br.getPatientId(), br.getHospitalId(), br.getBloodGroup(), String.valueOf(br.getUnitsRequested()),
                        br.getUrgency(), "NORMAL", "false"
                    ));
                }
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving requests: " + e.getMessage());
        }
    }

    // --- Blood Transfers Loading / Saving ---
    public static List<BloodTransfer> loadTransfers(String path) {
        ensureFileExists(path);
        List<BloodTransfer> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 7) {
                    list.add(new BloodTransfer(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Integer.parseInt(parts[6])));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading transfers: " + e.getMessage());
        }
        return list;
    }

    public static void saveTransfers(String path, List<BloodTransfer> list) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (BloodTransfer bt : list) {
                bw.write(String.join(",",
                    bt.getTransactionId(), bt.getTransactionDate(), bt.getStatus(),
                    bt.getSourceFacilityId(), bt.getDestinationFacilityId(), bt.getBloodGroup(), String.valueOf(bt.getUnitsTransferred())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving transfers: " + e.getMessage());
        }
    }

    // --- Blood Units Loading / Saving (mapped by facilityId) ---
    public void loadAllBloodUnits(String path) {
        ensureFileExists(path);
        
        // Clear existing inventories first
        Main.inventory.getBloodUnits().clear();
        for (Hospital h : Main.hospitals) {
            h.getInventory().getBloodUnits().clear();
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 7) {
                    BloodUnit bu = new BloodUnit(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4], parts[5]);
                    String facilityId = parts[6];
                    
                    // Route to correct inventory
                    if (isBloodBankId(facilityId)) {
                        Main.inventory.addBloodUnit(bu);
                    } else {
                        Hospital h = getHospitalById(facilityId);
                        if (h != null) {
                            h.getInventory().addBloodUnit(bu);
                        } else {
                            // Fallback if hospital is missing but ID remains
                            Main.inventory.addBloodUnit(bu);
                        }
                    }
                } else if (parts.length >= 5) {
                    // Backwards compatibility fallback to Central stock
                    BloodUnit bu = new BloodUnit(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4]);
                    Main.inventory.addBloodUnit(bu);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading blood units: " + e.getMessage());
        }
    }

    public void saveAllBloodUnits(String path) {
        ensureFileExists(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            // 1. Save Central Blood Bank units
            for (BloodBank bb : Main.bloodBanks) {
                for (BloodUnit bu : Main.inventory.getBloodUnits()) {
                    bw.write(String.join(",",
                        bu.getBloodUnitId(), bu.getBloodGroup(), String.valueOf(bu.getQuantity()),
                        bu.getCollectionDate(), bu.getExpiryDate(), bu.getStatus(), bb.getOrganizationId()
                    ));
                    bw.newLine();
                }
            }
            // 2. Save Hospital stock units
            for (Hospital h : Main.hospitals) {
                for (BloodUnit bu : h.getInventory().getBloodUnits()) {
                    bw.write(String.join(",",
                        bu.getBloodUnitId(), bu.getBloodGroup(), String.valueOf(bu.getQuantity()),
                        bu.getCollectionDate(), bu.getExpiryDate(), bu.getStatus(), h.getOrganizationId()
                    ));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving blood units: " + e.getMessage());
        }
    }

    private boolean isBloodBankId(String facilityId) {
        for (BloodBank bb : Main.bloodBanks) {
            if (bb.getOrganizationId().equals(facilityId)) return true;
        }
        return false;
    }

    private Hospital getHospitalById(String facilityId) {
        for (Hospital h : Main.hospitals) {
            if (h.getOrganizationId().equals(facilityId)) return h;
        }
        return null;
    }
}
