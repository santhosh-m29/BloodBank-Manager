package bloodbank;

import java.util.Scanner;

public class HospitalStaff extends Staff {
    private String department;

    public HospitalStaff(String personId, String name, int age, String gender, String phoneNumber, String address,
                         String username, String password, String role, String employeeId, String facilityId,
                         String department) {
        super(personId, name, age, gender, phoneNumber, address, username, password, role, employeeId, facilityId);
        this.department = department;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String getDetails() {
        return "HospitalStaff [ID: " + personId + ", Name: " + name + ", Department: " + department + ", Hospital: " + facilityId + "]";
    }

    @Override
    public void updateDetails() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Updating details for Hospital Staff: " + name);
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

        System.out.print("Enter New Department (Current: " + department + "): ");
        String newDept = sc.nextLine().trim();
        if (!newDept.isEmpty()) this.department = newDept;
        
        System.out.println("Hospital staff details updated successfully.");
    }

    @Override
    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Hospital Staff Details:");
        System.out.println("ID: " + personId);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Hospital ID (Facility): " + facilityId);
        System.out.println("Department: " + department);
        System.out.println("----------------------------------------");
    }

    @Override
    public void displayStaffDetails() {
        System.out.println("Staff: " + name + " | Employee ID: " + employeeId + " | Facility: " + facilityId + " | Department: " + department);
    }

    public void createPatient() {
        System.out.println("Registering patient...");
    }

    public void requestBlood() {
        System.out.println("Requesting blood for patient...");
    }

    public void issueBlood() {
        System.out.println("Issuing blood unit to patient...");
    }

    public void checkLocalStock() {
        System.out.println("Checking local inventory stock levels...");
    }

    public void restock() {
        System.out.println("Requesting restock from blood bank...");
    }
}
