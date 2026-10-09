package bloodbank.service;

import bloodbank.Main;
import bloodbank.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class ReportGenerator {
    private String reportTitle;
    private String generatedDate;

    public ReportGenerator(String reportTitle) {
        this.reportTitle = reportTitle;
        this.generatedDate = LocalDate.now().toString();
    }

    public ReportGenerator() {
        this.reportTitle = "Blood Bank Management System Summary Report";
        this.generatedDate = LocalDate.now().toString();
    }

    public void generateInventoryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================================================\n");
        sb.append("                               INVENTORY STOCK REPORT                                    \n");
        sb.append("Title: ").append(reportTitle).append("\n");
        sb.append("Generated Date: ").append(generatedDate).append("\n");
        sb.append("=========================================================================================\n");
        
        // 1. Central Blood Bank Inventory
        sb.append("\n>>> CENTRAL BLOOD BANK INVENTORY <<<\n");
        sb.append(String.format("%-15s %-12s %-10s %-15s %-15s %-12s\n", 
            "Unit ID", "Blood Group", "Quantity", "Collection Date", "Expiry Date", "Status"));
        sb.append("-----------------------------------------------------------------------------------------\n");
        for (BloodUnit bu : Main.inventory.getBloodUnits()) {
            sb.append(String.format("%-15s %-12s %-10d %-15s %-15s %-12s %s\n",
                bu.getBloodUnitId(), bu.getBloodGroup(), bu.getQuantity(),
                bu.getCollectionDate(), bu.getExpiryDate(), bu.getStatus(),
                bu.isExpired() ? "(EXPIRED)" : ""));
        }
        sb.append("-----------------------------------------------------------------------------------------\n");
        sb.append("Central BB Total Usable Stock: ").append(Main.inventory.getTotalStock()).append(" units\n");
        sb.append("=========================================================================================\n");

        // 2. Hospital Inventories
        for (Hospital h : Main.hospitals) {
            sb.append("\n>>> HOSPITAL INVENTORY: ").append(h.getOrganizationName()).append(" (ID: ").append(h.getOrganizationId()).append(") <<<\n");
            sb.append(String.format("%-15s %-12s %-10s %-15s %-15s %-12s\n", 
                "Unit ID", "Blood Group", "Quantity", "Collection Date", "Expiry Date", "Status"));
            sb.append("-----------------------------------------------------------------------------------------\n");
            for (BloodUnit bu : h.getInventory().getBloodUnits()) {
                sb.append(String.format("%-15s %-12s %-10d %-15s %-15s %-12s %s\n",
                    bu.getBloodUnitId(), bu.getBloodGroup(), bu.getQuantity(),
                    bu.getCollectionDate(), bu.getExpiryDate(), bu.getStatus(),
                    bu.isExpired() ? "(EXPIRED)" : ""));
            }
            sb.append("-----------------------------------------------------------------------------------------\n");
            sb.append("Hospital Total Usable Stock: ").append(h.getInventory().getTotalStock()).append(" units\n");
            sb.append("=========================================================================================\n");
        }

        String reportContent = sb.toString();
        System.out.print(reportContent);
        writeReportToFile("InventoryReport_" + generatedDate + ".txt", reportContent);
    }

    public void generateDonationReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================================================\n");
        sb.append("                                DONATION HISTORY REPORT                                  \n");
        sb.append("Title: ").append(reportTitle).append("\n");
        sb.append("Generated Date: ").append(generatedDate).append("\n");
        sb.append("=========================================================================================\n");
        sb.append(String.format("%-15s %-15s %-15s %-12s %-10s %-12s\n", 
            "Transaction ID", "Date", "Donor ID", "Blood Group", "Quantity", "Status"));
        sb.append("-----------------------------------------------------------------------------------------\n");
        
        int totalUnits = 0;
        int completedCount = 0;
        for (BloodDonation bd : Main.donations) {
            sb.append(String.format("%-15s %-15s %-15s %-12s %-10d %-12s\n",
                bd.getTransactionId(),
                bd.getTransactionDate(),
                bd.getDonorId(),
                bd.getBloodGroup(),
                bd.getQuantity(),
                bd.getStatus()));
            if ("Completed".equalsIgnoreCase(bd.getStatus())) {
                totalUnits += bd.getQuantity();
                completedCount++;
            }
        }
        sb.append("-----------------------------------------------------------------------------------------\n");
        sb.append("Total Donation Txns: ").append(Main.donations.size())
          .append(" | Completed: ").append(completedCount)
          .append(" | Total Units Donated: ").append(totalUnits).append(" units\n");
        sb.append("=========================================================================================\n");

        String reportContent = sb.toString();
        System.out.print(reportContent);
        writeReportToFile("DonationReport_" + generatedDate + ".txt", reportContent);
    }

    public void generateRequestReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================================================\n");
        sb.append("                                 REQUEST FULFILLMENT REPORT                              \n");
        sb.append("Title: ").append(reportTitle).append("\n");
        sb.append("Generated Date: ").append(generatedDate).append("\n");
        sb.append("=========================================================================================\n");
        sb.append(String.format("%-15s %-12s %-15s %-15s %-12s %-8s %-12s %-10s\n", 
            "Txn ID", "Date", "Patient ID", "Hospital ID", "Blood Group", "Units", "Urgency", "Status"));
        sb.append("-----------------------------------------------------------------------------------------\n");

        int totalUnits = 0;
        int approvedCount = 0;
        for (BloodRequest br : Main.requests) {
            sb.append(String.format("%-15s %-12s %-15s %-15s %-12s %-8d %-12s %-10s\n",
                br.getTransactionId(), br.getTransactionDate(), br.getPatientId(), br.getHospitalId(),
                br.getBloodGroup(), br.getUnitsRequested(), br.getUrgency(), br.getStatus()));
            totalUnits += br.getUnitsRequested();
            if ("APPROVED".equalsIgnoreCase(br.getStatus()) || "COMPLETED".equalsIgnoreCase(br.getStatus())) {
                approvedCount++;
            }
        }
        sb.append("-----------------------------------------------------------------------------------------\n");
        sb.append("Total Requests: ").append(Main.requests.size())
          .append(" | Approved/Fulfilled: ").append(approvedCount)
          .append(" | Total Units Requested: ").append(totalUnits).append(" units\n");
        sb.append("=========================================================================================\n");

        String reportContent = sb.toString();
        System.out.print(reportContent);
        writeReportToFile("RequestReport_" + generatedDate + ".txt", reportContent);
    }

    public void exportReport() {
        // Consolidated Export Summary
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================================\n");
        sb.append("                       SYSTEM SUMMARY REPORT                      \n");
        sb.append("Generated: ").append(generatedDate).append("\n");
        sb.append("==================================================================\n");
        sb.append("Total Registered Donors:        ").append(Main.donors.size()).append("\n");
        sb.append("Total Registered Patients:      ").append(Main.patients.size()).append("\n");
        sb.append("Total Hospitals Registered:     ").append(Main.hospitals.size()).append("\n");
        sb.append("Total Blood Banks:              ").append(Main.bloodBanks.size()).append("\n");
        sb.append("Central Inventory Total Stock:  ").append(Main.inventory.getTotalStock()).append(" units\n");
        sb.append("Total Donations Recorded:       ").append(Main.donations.size()).append("\n");
        sb.append("Total Requests Logged:          ").append(Main.requests.size()).append("\n");
        sb.append("Total Transfers Executed:       ").append(Main.transfers.size()).append("\n");
        sb.append("==================================================================\n");

        String reportContent = sb.toString();
        System.out.print(reportContent);
        writeReportToFile("SystemSummary_" + generatedDate + ".txt", reportContent);
    }

    private void writeReportToFile(String fileName, String content) {
        File dir = new File("reports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(content);
            System.out.println("[REPORT EXPORT] Report exported successfully to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[REPORT EXPORT ERROR] Failed to write report file: " + e.getMessage());
        }
    }
}
