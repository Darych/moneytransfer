package com.github.darych.moneytransfer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StorageImplTest {
    private Storage storage = new StorageImpl();
    private Account account1 = new Account("user1", 1.2);

    @BeforeEach
    void setupTest() {
        storage.clear();
    }

    @Nested
    @DisplayName("Save tests")
    class CreateTest {
        @Test
        @DisplayName("Save account with id 1 in empty storage")
        void createAccountWithId1InEmptyStorage() throws StorageException {
            Account accountWithId = storage.save(account1);

            assertEquals(1, accountWithId.getId());
        }

        @Test
        @DisplayName("Save account with id 2 in storage with size 1")
        void createAccountId2InStorageSize1() throws StorageException {
            storage.save(account1);
            Account account2 = new Account("user2", 2.3);

            Account account2WithId = storage.save(account2);

            assertEquals(2, account2WithId.getId());
        }

        @Test
        @DisplayName("Save existing account with updated balance")
        void saveExistingAccountWithUpdatedBalance() throws StorageException {
            storage.save(account1);
            account1.setBalance(10);

            storage.save(account1);

            assertEquals(1, storage.size());
            assertEquals(10, storage.get(1).getBalance());
        }

        @Test
        @DisplayName("Save many users in parallel")
        void saveManyUsersInParallel() {
            final int COUNT = 1000;
            ArrayList<Thread> threads = new ArrayList<>(COUNT);
            for (int i = 0; i < COUNT; ++i) {
                threads.add(new Thread(new RunnableSave(storage, i)));
            }

            for (int i = 0; i < COUNT; ++i) {
                threads.get(i).start();
            }

            for (int i = 0; i < COUNT; ++i) {
                try {
                    threads.get(i).join(1000);
                } catch (InterruptedException e) {
                    fail(e);
                }
            }

            assertEquals(COUNT, storage.size());
            for (int i = 1; i < COUNT; ++i) {
                assertNotNull(storage.get(i+1));
            }
        }

        class RunnableSave implements Runnable {
            private int i;
            private Storage storage;

            RunnableSave(Storage storage, int i) {
                this.storage = storage;
                this.i = i;
            }

            @Override
            public void run() {
                try {
                    storage.save(new Account("user" + i, i * 10));
                } catch (StorageException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Nested
        class Negative {
            @Test
            @DisplayName("Throw StorageException if account is null")
            void throwStorageExceptionIfAccountNull() {
                assertThrows(StorageException.class, () -> storage.save(null));
            }
        }
    }

    @Nested
    @DisplayName("Get tests")
    class GetTest {
        @Test
        @DisplayName("Get existing account")
        void getExistingAccount() throws StorageException{
            storage.save(account1);
            assertEquals(account1, storage.get(account1.getId()));
        }

        @Test
        @DisplayName("Return null if account is absent")
        void returnNullIfAccountIsAbsent() {
            assertNull(storage.get(1));
        }
    }
}