package com.github.darych.moneytransfer.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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