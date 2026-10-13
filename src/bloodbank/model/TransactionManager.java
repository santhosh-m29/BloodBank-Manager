package bloodbank.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransactionManager<T extends Transaction> implements Iterable<T> {
    private List<T> transactions;

    public TransactionManager() {
        transactions = new ArrayList<>();
    }

    public TransactionManager(List<T> transactions) {
        if (transactions == null) this.transactions = new ArrayList<>();
        else this.transactions = transactions;
    }

    public boolean add(T transaction) {
        Transaction parentReference = transaction;
        if (parentReference == null) return false;

        for (T savedTransaction : transactions) {
            if (savedTransaction.getTransactionId().equals(parentReference.getTransactionId())) {
                return false;
            }
        }
        transactions.add(transaction);
        return true;
    }

    public T findById(String transactionId) {
        for (T transaction : transactions) {
            if (transaction.getTransactionId().equals(transactionId)) return transaction;
        }
        return null;
    }

    // Remove by object reference
    public boolean remove(T transaction) {
        return transactions.remove(transaction);
    }

    // Method Overloading: Remove by transaction ID
    public boolean remove(String transactionId) {
        T found = findById(transactionId);
        return found != null && transactions.remove(found);
    }

    public List<T> getTransactions() {
        return transactions;
    }

    @Override
    public Iterator<T> iterator() {
        return transactions.iterator();
    }
}
