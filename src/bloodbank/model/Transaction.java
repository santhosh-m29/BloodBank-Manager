package bloodbank.model;
import java.time.LocalDate;
import bloodbank.utility.Validation;
public abstract class Transaction {
    private final String transactionId;
    private final LocalDate transactionDate;
    protected String status;
    protected Transaction(String id, LocalDate date, String status) {
        transactionId = Validation.id(id);
        Validation.require(date != null, "Transaction date is required.");
        transactionDate = date;
        this.status = status;
    }
    public String getTransactionId() {
        return transactionId;
    }
    public LocalDate getTransactionDate() {
        return transactionDate;
    }
    public String getStatus() {
        return status;
    }
    public void displayTransaction() {
        System.out.println(this);
    }
    @Override public String toString() {
        return transactionId + " | " + transactionDate + " | " + status;
    }
}
