import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Restores the demo data files to their starting state.
 * Run this program from the BloodBank-Manager project folder.
 */
public class ResetData {
    public static void main(String[] args) throws IOException {
        if (!new File("src/bloodbank/Main.java").exists()) {
            System.out.println("Run ResetData from the BloodBank-Manager project folder.");
            return;
        }

        File dataFolder = new File("data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        String[] admins = {
            "ADM001,System Administrator,35,Male,9999999999,Blood Bank HQ,admin,admin123,BLOOD_BANK_ADMIN,EMP001,BB001"
        };
        String[] bloodBanks = {
            "BB001,City Blood Center,100 Central Ave,555-0100,Dr. Adams"
        };
        String[] hospitals = {
            "HOSP001,St. Jude Hospital,456 Medical Dr,555-0111,Private,911",
            "HOSP002,Grace Clinic,789 Care Blvd,555-0222,Public,100",
            "HOSP003,Mercy General Hospital,12 Mercy Way,555-0333,Public,911"
        };
        String[] hospitalStaff = {
            "STF001,Dr. Clara,30,Female,8888888888,St. Jude,hosp1,hosp123,HOSPITAL_STAFF,EMP002,HOSP001,Emergency",
            "STF002,Dr. Robert,42,Male,7777777777,Grace Clinic,hosp2,hosp123,HOSPITAL_STAFF,EMP003,HOSP002,Pediatrics",
            "STF003,Nurse Sarah,28,Female,9991112222,St. Jude,hosp3,hosp123,HOSPITAL_STAFF,EMP004,HOSP001,ICU"
        };
        String[] donors = {
            "DON001,Alice Smith,28,Female,1234567890,456 Oak Street,donor,donor123,DONOR,A+,13.5,55.0,",
            "DON002,Michael Green,34,Male,2223334444,789 Pine Rd,mike_donor,pass123,DONOR,O-,15.0,82.5,2026-06-20",
            "DON003,Emily White,22,Female,3334445555,101 Elm Dr,emily_d,pass123,DONOR,B+,12.8,61.0,2026-04-10",
            "DON004,David Black,45,Male,4445556666,202 Maple Ave,david_b,pass123,DONOR,AB+,14.5,75.0,2026-07-01",
            "DON005,Sushil,28,Male,9090909090,Church St,Don001,don001,DONOR,O+,13.5,50.0,"
        };
        String[] patients = {
            "PAT001,Bob Brown,45,Male,5556667777,101 Maple Ave,patient,patient123,PATIENT,A+,Anemia,Dr. Green,2,HOSP001",
            "PAT002,Jane Davis,60,Female,7778889999,202 Birch Ln,jane_p,pass123,PATIENT,O+,Leukemia,Dr. Watson,4,HOSP002",
            "PAT003,Charles Miller,33,Male,8889990000,303 Cedar Dr,charles_m,pass123,PATIENT,B+,Trauma,Dr. Grey,3,HOSP001",
            "PAT004,Santhosh,18,Male,8778565698,Chennai,santh29osh,ssn,PATIENT,A+,Accident,Sushil,10,HOSP001"
        };

        writeLines("data/admin.txt", admins);
        writeLines("data/bloodbanks.txt", bloodBanks);
        writeLines("data/hospitals.txt", hospitals);
        writeLines("data/hospitalstaff.txt", hospitalStaff);
        writeLines("data/donors.txt", donors);
        writeLines("data/patients.txt", patients);

        // Clear saved activity so the demo starts without previous transactions.
        writeLines("data/donations.txt", new String[0]);
        writeLines("data/requests.txt", new String[0]);
        writeLines("data/transfers.txt", new String[0]);
        writeLines("data/recipients.txt", new String[0]);

        // Central stock starts empty. Each hospital starts with ten A+, B+, and O+ units.
        String today = LocalDate.now().toString();
        String expiryDate = LocalDate.now().plusDays(42).toString();
        String[] bloodUnits = new String[90];
        int unitNumber = 1;
        for (int hospitalNumber = 1; hospitalNumber <= 3; hospitalNumber++) {
            String hospitalId = String.format("HOSP%03d", hospitalNumber);
            for (String bloodGroup : new String[]{"A+", "B+", "O+"}) {
                for (int i = 1; i <= 10; i++) {
                    String unitId = String.format("UNIT%03d", unitNumber);
                    bloodUnits[unitNumber - 1] = unitId + "," + bloodGroup + ",1," + today + ","
                            + expiryDate + ",AVAILABLE," + hospitalId;
                    unitNumber++;
                }
            }
        }
        writeLines("data/bloodunits.txt", bloodUnits);

        System.out.println("Demo data reset complete.");
        System.out.println("Transaction history cleared.");
        System.out.println("Central blood-bank stock: 0 units.");
        System.out.println("Each hospital has 10 A+, 10 B+, and 10 O+ units.");
        System.out.println("Data folder: " + dataFolder.getAbsolutePath());
    }

    private static void writeLines(String fileName, String[] lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
