package com.github.darych.moneytransfer.services;

import com.github.darych.moneytransfer.model.Account;
import com.github.darych.moneytransfer.model.Storage;
import com.github.darych.moneytransfer.model.StorageException;
import com.github.darych.moneytransfer.model.Transaction;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TransactionService {
    private Storage storage;

    @Inject
    TransactionService(Storage storage) {
        this.storage = storage;
    }

    /**
     * Transfer money from one account to another.
     * @param tx transaction info.
     */
    public void processTx(Transaction tx) {
        double changeAmount = tx.getAmount();
        checkAmount(changeAmount);
        int fromId = tx.getFromAccountId();
        int toId = tx.getToAccountId();
        checkIds(fromId, toId);

        Account fromAcc = getAccount(fromId);
        Account toAcc = getAccount(toId);
        updateBalance(fromAcc, toAcc, changeAmount);
    }

    private void updateBalance(Account fromAcc, Account toAcc, double changeAmount) {
        Account first = fromAcc;
        Account second = toAcc;
        if (first.getId() > second.getId()) {
            first = toAcc;
            second = fromAcc;
        }
        synchronized (first) {
            synchronized (second) {
                checkSourceBalance(fromAcc.getBalance(), changeAmount);

                fromAcc.setBalance(fromAcc.getBalance() - changeAmount);
                toAcc.setBalance(toAcc.getBalance() + changeAmount);
                saveAccounts(fromAcc, toAcc);
            }
        }
    }

    private Account getAccount(int id) {
        Account acc = storage.get(id);
        if (acc == null) {
            throw new TransactionServiceException(String.format("Account with id %d does not exist", id));
        }
        return acc;
    }

    private void checkAmount(double amount) {
        if (amount <= 0) {
            throw new TransactionServiceException("Amount must be greater than zero.");
        }
    }

    private void checkSourceBalance(double srcBalance, double changeAmount) {
        if (srcBalance < changeAmount) {
            throw new TransactionServiceException("Source account does not have enough money.");
        }
    }

    private void checkIds(int fromId, int toId) {
        if (fromId == toId) {
            throw new TransactionServiceException("Money transfer between same accounts are prohibited.");
        }
    }

    private void saveAccounts(Account from, Account to) {
        try {
            storage.save(from);
            storage.save(to);
        } catch (StorageException e) {
            throw new TransactionServiceException("Failed to process transaction.", e);
        }
    }
}
