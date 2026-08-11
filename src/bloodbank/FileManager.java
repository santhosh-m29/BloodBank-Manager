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

    // Standard signatures from class diagram (dummy implementations for exact structural compliance)
    public void saveData() {
        System.out.println("Data saved.");
    }
    public void loadData() {
        System.out.println("Data loaded.");
    }
    public void appendData() {
        System.out.println("Data appended.");
    }
    public void deleteRecord() {
        System.out.println("Record deleted.");
    }

    // Helper to ensure data directory and files exist
    public static void ensureDirectoryAndFile(String path) {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating file " + path + ": " + e.getMessage());
            }
        }
    }

    // --- Admin Loading / Saving ---
    public static List<Admin> loadAdmins(String path) {
        ensureDirectoryAndFile(path);
        List<Admin> admins = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    admins.add(new Admin(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]));
                } else if (parts.length >= 3) {
                    // For short format (username, password, role)
                    admins.add(new Admin("ADM" + (admins.size() + 1), "Admin User", 30, "Male", "0000000000", "Blood Bank", parts[0], parts[1], parts[2]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading admins: " + e.getMessage());
        }
        return admins;
    }

    public static void saveAdmins(String path, List<Admin> admins) {
        ensureDirectoryAndFile(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (Admin admin : admins) {
                bw.write(String.join(",",
                    admin.getPersonId(),
                    admin.getName(),
                    String.valueOf(admin.getAge()),
                    admin.getGender(),
                    admin.getPhoneNumber(),
                    admin.getAddress(),
                    admin.getUsername(),
                    admin.getPassword(),
                    admin.getRole()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving admins: " + e.getMessage());
        }
    }

    // --- Donor Loading / Saving ---
    public static List<Donor> loadDonors(String path) {
        ensureDirectoryAndFile(path);
        List<Donor> donors = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 10) {
                    donors.add(new Donor(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4], parts[5], 
                        parts[6], Double.parseDouble(parts[7]), Double.parseDouble(parts[8]), parts[9]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading donors: " + e.getMessage());
        }
        return donors;
    }

    public static void saveDonors(String path, List<Donor> donors) {
        ensureDirectoryAndFile(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (Donor donor : donors) {
                bw.write(String.join(",",
                    donor.getPersonId(),
                    donor.getName(),
                    String.valueOf(donor.getAge()),
                    donor.getGender(),
                    donor.getPhoneNumber(),
                    donor.getAddress(),
                    donor.getBloodGroup(),
                    String.valueOf(donor.getHaemoglobin()),
                    String.valueOf(donor.getWeight()),
                    donor.getLastDonationDate()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving donors: " + e.getMessage());
        }
    }

    // --- Recipient Loading / Saving ---
    public static List<Recipient> loadRecipients(String path) {
        ensureDirectoryAndFile(path);
        List<Recipient> recipients = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 11) {
                    recipients.add(new Recipient(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4], parts[5],
                        parts[6], parts[7], parts[8], Integer.parseInt(parts[9]), parts[10]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading recipients: " + e.getMessage());
        }
        return recipients;
    }

    public static void saveRecipients(String path, List<Recipient> recipients) {
        ensureDirectoryAndFile(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (Recipient r : recipients) {
                bw.write(String.join(",",
                    r.getPersonId(),
                    r.getName(),
                    String.valueOf(r.getAge()),
                    r.getGender(),
                    r.getPhoneNumber(),
                    r.getAddress(),
                    r.getBloodGroup(),
                    r.getDisease(),
                    r.getDoctorName(),
                    String.valueOf(r.getUnitsRequired()),
                    r.getHospitalId()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving recipients: " + e.getMessage());
        }
    }

    // --- Hospital Loading / Saving ---
    public static List<Hospital> loadHospitals(String path) {
        ensureDirectoryAndFile(path);
        List<Hospital> hospitals = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    hospitals.add(new Hospital(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading hospitals: " + e.getMessage());
        }
        return hospitals;
    }

    public static void saveHospitals(String path, List<Hospital> hospitals) {
        ensureDirectoryAndFile(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (Hospital h : hospitals) {
                bw.write(String.join(",",
                    h.getOrganizationId(),
                    h.getOrganizationName(),
                    h.getAddress(),
                    h.getContactNumber(),
                    h.getHospitalType(),
                    h.getEmergencyContact()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving hospitals: " + e.getMessage());
        }
    }

    // --- BloodUnit Loading / Saving ---
    public static List<BloodUnit> loadBloodUnits(String path) {
        ensureDirectoryAndFile(path);
        List<BloodUnit> units = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 5) {
                    units.add(new BloodUnit(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading blood units: " + e.getMessage());
        }
        return units;
    }

    public static void saveBloodUnits(String path, List<BloodUnit> units) {
        ensureDirectoryAndFile(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (BloodUnit bu : units) {
                bw.write(String.join(",",
                    bu.getBloodUnitId(),
                    bu.getBloodGroup(),
                    String.valueOf(bu.getQuantity()),
                    bu.getCollectionDate(),
                    bu.getExpiryDate()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving blood units: " + e.getMessage());
        }
    }

    // --- BloodDonation Loading / Saving ---
    public static List<BloodDonation> loadDonations(String path) {
        ensureDirectoryAndFile(path);
        List<BloodDonation> donations = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    donations.add(new BloodDonation(parts[0], parts[1], parts[2], parts[3], parts[4], Integer.parseInt(parts[5])));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading donations: " + e.getMessage());
        }
        return donations;
    }

    public static void saveDonations(String path, List<BloodDonation> donations) {
        ensureDirectoryAndFile(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (BloodDonation bd : donations) {
                bw.write(String.join(",",
                    bd.getTransactionId(),
                    bd.getTransactionDate(),
                    bd.getStatus(),
                    bd.getDonorId(),
                    bd.getBloodGroup(),
                    String.valueOf(bd.getQuantity())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving donations: " + e.getMessage());
        }
    }

    // --- BloodRequest Loading / Saving ---
    public static List<BloodRequest> loadRequests(String path) {
        ensureDirectoryAndFile(path);
        List<BloodRequest> requests = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 7) {
                    requests.add(new BloodRequest(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Integer.parseInt(parts[6])));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading requests: " + e.getMessage());
        }
        return requests;
    }

    public static void saveRequests(String path, List<BloodRequest> requests) {
        ensureDirectoryAndFile(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (BloodRequest brq : requests) {
                bw.write(String.join(",",
                    brq.getTransactionId(),
                    brq.getTransactionDate(),
                    brq.getStatus(),
                    brq.getRecipientId(),
                    brq.getHospitalId(),
                    brq.getBloodGroup(),
                    String.valueOf(brq.getUnitsRequested())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving requests: " + e.getMessage());
        }
    }

    // --- BloodTransfer Loading / Saving ---
    public static List<BloodTransfer> loadTransfers(String path) {
        ensureDirectoryAndFile(path);
        List<BloodTransfer> transfers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 7) {
                    transfers.add(new BloodTransfer(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Integer.parseInt(parts[6])));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading transfers: " + e.getMessage());
        }
        return transfers;
    }

    public static void saveTransfers(String path, List<BloodTransfer> transfers) {
        ensureDirectoryAndFile(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            for (BloodTransfer bt : transfers) {
                bw.write(String.join(",",
                    bt.getTransactionId(),
                    bt.getTransactionDate(),
                    bt.getStatus(),
                    bt.getSourceBloodBankId(),
                    bt.getDestinationBloodBankId(),
                    bt.getBloodGroup(),
                    String.valueOf(bt.getUnitsTransferred())
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving transfers: " + e.getMessage());
        }
    }
}
