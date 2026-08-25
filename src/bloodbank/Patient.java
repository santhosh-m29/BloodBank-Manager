package bloodbank;

import java.util.Scanner;

public class Patient extends Person {
    private String bloodGroup;
    private String disease;
    private String doctorName;
    private int unitsRequired;
    private String hospitalId;

    public Patient(String personId, String name, int age, String gender, String phoneNumber, String address,
                   String username, String password, String role, String bloodGroup, String disease,
                   String doctorName, int unitsRequired, String hospitalId) {
        super(personId, name, age, gender, phoneNumber, address, username, password, role);
        this.bloodGroup = bloodGroup;
        this.disease = disease;
        this.doctorName = doctorName;
        this.unitsRequired = unitsRequired;
        this.hospitalId = hospitalId;
    }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getDisease() { return disease; }
    public void setDisease(String disease) { this.disease = disease; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public int getUnitsRequired() { return unitsRequired; }
    public void setUnitsRequired(int unitsRequired) { this.unitsRequired = unitsRequired; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    @Override
    public String getDetails() {
        return "Patient [ID: " + personId + ", Name: " + name + ", Blood Group: " + bloodGroup + 
               ", Required Units: " + unitsRequired + ", Hospital: " + hospitalId + "]";
    }

    @Override
    public void updateDetails() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Updating details for Patient: " + name);
        System.out.print("Enter New Name (Current: " + name + "): ");
        String newName = sc.nextLine().trim();
        if (!newName.isEmpty()) this.name = newName;

        System.out.print("Enter New Age (Current: " + age + "): ");
        String ageStr = sc.nextLine().trim();
        if (!ageStr.isEmpty()) {
            try {
                this.age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Enter New Gender (Current: " + gender + "): ");
        String newGender = sc.nextLine().trim();
        if (!newGender.isEmpty()) this.gender = newGender;

        System.out.print("Enter New Phone (Current: " + phoneNumber + "): ");
        String phone = sc.nextLine().trim();
        if (!phone.isEmpty() && Validation.validatePhoneNumberStatic(phone)) {
            this.phoneNumber = phone;
        }

        System.out.print("Enter New Address (Current: " + address + "): ");
        String newAddr = sc.nextLine().trim();
        if (!newAddr.isEmpty()) this.address = newAddr;

        System.out.print("Enter New Disease (Current: " + disease + "): ");
        String newDisease = sc.nextLine().trim();
        if (!newDisease.isEmpty()) this.disease = newDisease;

        System.out.print("Enter New Doctor Name (Current: " + doctorName + "): ");
        String newDoc = sc.nextLine().trim();
        if (!newDoc.isEmpty()) this.doctorName = newDoc;

        System.out.print("Enter New Units Required (Current: " + unitsRequired + "): ");
        String unitsStr = sc.nextLine().trim();
        if (!unitsStr.isEmpty()) {
            try {
                this.unitsRequired = Integer.parseInt(unitsStr);
            } catch (NumberFormatException ignored) {}
        }
        
        System.out.println("Patient details updated successfully.");
    }

    @Override
    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Patient Details:");
        System.out.println("ID: " + personId);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Blood Group Required: " + bloodGroup);
        System.out.println("Disease: " + disease);
        System.out.println("Doctor: " + doctorName);
        System.out.println("Units Required: " + unitsRequired);
        System.out.println("Hospital ID: " + hospitalId);
        System.out.println("----------------------------------------");
    }

    public void viewRequestStatus() {
        System.out.println("Viewing request history and status for patient: " + name);
        boolean found = false;
        for (BloodRequest req : Main.requests) {
            if (req.getPatientId().equals(this.personId)) {
                req.displayTransaction();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No blood requests found for this patient.");
        }
    }
}
