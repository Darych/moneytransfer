package com.github.darych.moneytransfer.services;

import com.github.darych.moneytransfer.model.*;
import com.google.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Transaction Service integration tests")
class TransactionServiceIT {
    private Storage storage = new StorageImpl();

    @Inject
    private TransactionService service = new TransactionService(storage);

    @BeforeEach
    void setupTest() throws StorageException {
        storage.save(new Account(1, "user1", 200));
        storage.save(new Account(2, "user2", 50));
    }

    @Test
    @DisplayName("Parallel transfer in both directions")
    void parallelTransferInBothDirections() {
        Thread thread1 = new Thread() {
            public void run() {
                Transaction tx = new Transaction(1, 2, 50);
                service.processTx(tx);
            }
        };

        Thread thread2 = new Thread() {
            public void run() {
                Transaction tx = new Transaction(2, 1, 25);
                service.processTx(tx);
            }
        };

        thread1.start();
        thread2.start();

        try {
            thread1.join(1000);
        } catch (InterruptedException e) {
            fail(e);
        }

        assertEquals(175, storage.get(1).getBalance());
        assertEquals(75, storage.get(2).getBalance());
    }
}
