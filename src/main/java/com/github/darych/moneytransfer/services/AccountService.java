package com.github.darych.moneytransfer.services;

import com.github.darych.moneytransfer.model.Account;
import com.github.darych.moneytransfer.model.Storage;
import com.github.darych.moneytransfer.model.StorageException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AccountService {
    private Storage storage;

    @Inject
    public AccountService(Storage storage) {
        this.storage = storage;
    }

    /**
     * Saves account in storage.
     * @param account with name and balance.
     * @return account with internal id, name and balance.
     */
    public Account save(Account account) {
        try {
            return storage.save(account);
        } catch (StorageException e) {
            throw new AccountServiceException("An error occured while creatin account object.", e);
        }
    }

    /**
     * Get account by its internal id.
     * @param id internal id.
     * @return account.
     */
    public Account getById(int id) {
        return storage.get(id);
    }
}
