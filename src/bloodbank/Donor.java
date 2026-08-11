package bloodbank;


public class Donor extends Person {
    private String bloodGroup;
    private double haemoglobin;
    private double weight;
    private String lastDonationDate; // Format: YYYY-MM-DD
    private boolean eligible;

    public Donor(String personId, String name, int age, String gender, String phoneNumber, String address,
                 String bloodGroup, double haemoglobin, double weight, String lastDonationDate) {
        super(personId, name, age, gender, phoneNumber, address);
        this.bloodGroup = bloodGroup;
        this.haemoglobin = haemoglobin;
        this.weight = weight;
        this.lastDonationDate = lastDonationDate;
        this.eligible = checkEligibility();
    }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public double getHaemoglobin() { return haemoglobin; }
    public void setHaemoglobin(double haemoglobin) { this.haemoglobin = haemoglobin; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(String lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public boolean isEligible() { 
        this.eligible = checkEligibility();
        return eligible; 
    }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    @Override
    public String getDetails() {
        return "Donor [ID: " + personId + ", Name: " + name + ", Blood Group: " + bloodGroup + 
               ", Age: " + age + ", Weight: " + weight + "kg, Hb: " + haemoglobin + 
               ", Eligible: " + (eligible ? "Yes" : "No") + "]";
    }

    @Override
    public void updateDetails(String name, int age, String gender, String phoneNumber, String address) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.eligible = checkEligibility();
    }

    public void updateMedicalDetails(double haemoglobin, double weight, String lastDonationDate) {
        this.haemoglobin = haemoglobin;
        this.weight = weight;
        this.lastDonationDate = lastDonationDate;
        this.eligible = checkEligibility();
    }

    @Override
    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Donor Details:");
        System.out.println("ID: " + personId);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Blood Group: " + bloodGroup);
        System.out.println("Hemoglobin: " + haemoglobin + " g/dL");
        System.out.println("Weight: " + weight + " kg");
        System.out.println("Last Donation: " + (lastDonationDate.isEmpty() ? "N/A" : lastDonationDate));
        System.out.println("Eligible: " + (isEligible() ? "YES" : "NO"));
        System.out.println("----------------------------------------");
    }

    public void registerDonor() {
        System.out.println("Donor " + name + " has been successfully registered.");
    }

    public final boolean checkEligibility() {
        // Validation Rules:
        // * Age >= 18 and Age <= 60
        // * Weight >= 50 kg
        // * Hemoglobin >= 12.5
        // * Valid Blood Group
        boolean isAgeValid = (age >= 18 && age <= 60);
        boolean isWeightValid = (weight >= 50.0);
        boolean isHbValid = (haemoglobin >= 12.5);
        boolean isBloodGroupValid = Validation.isValidBloodGroupStatic(bloodGroup);
        
        return isAgeValid && isWeightValid && isHbValid && isBloodGroupValid;
    }

    public void donateBlood() {
        if (isEligible()) {
            System.out.println("Donor " + name + " is donating blood.");
        } else {
            System.out.println("Donor " + name + " is NOT eligible to donate blood.");
        }
    }

    public void viewDonationHistory() {
        System.out.println("Donation history for donor: " + name);
    }
}
