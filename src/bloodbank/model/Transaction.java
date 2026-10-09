package bloodbank.model;

public abstract class Transaction {
    protected String transactionId;
    protected String transactionDate; // Format: YYYY-MM-DD
    protected String status;          // "Pending", "Approved", "Rejected", "Completed", etc.

    public Transaction(String transactionId, String transactionDate, String status) {
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.status = status;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public abstract void displayTransaction();
}
