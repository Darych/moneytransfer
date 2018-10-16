package com.github.darych.moneytransfer.model;

import com.google.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class StorageImpl implements Storage {
    private Map<Integer, Account> storage;
    private int maxId;

    public StorageImpl() {
        storage = new ConcurrentHashMap<>();
        maxId = 0;
    }

    /**
     * Save account in local storage.
     * @param account with name and balance
     * @return accound with filled id field.
     * @throws StorageException if account is null.
     */
    @Override
    public Account save(Account account) throws StorageException {
        if (account == null) {
            throw new StorageException("Account object is null.");
        }
        if (account.getId() == 0) {
            account.setId(++maxId);
        }
        storage.put(account.getId(), account);
        return account;
    }

    /**
     * Get account by internal id.
     * @param id internal id.
     * @return account.
     */
    @Override
    public Account get(int id) {
        return storage.get(id);
    }

    /**
     * Clear local storage.
     */
    @Override
    public void clear() {
        storage.clear();
        maxId = 0;
    }

    /**
     * Get saved accounts count.
     *
     * @return accounts count.
     */
    @Override
    public int size() {
        return storage.size();
    }
}
