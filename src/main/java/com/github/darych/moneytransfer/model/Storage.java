package com.github.darych.moneytransfer.model;

/**
 * Accounts storage.
 */
public interface Storage {
    Account save(Account account) throws StorageException;
    Account get(int id);
    void clear();
    int size();
}
