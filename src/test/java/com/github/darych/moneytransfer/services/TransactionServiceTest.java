package com.github.darych.moneytransfer.services;

import com.github.darych.moneytransfer.model.Account;
import com.github.darych.moneytransfer.model.Storage;
import com.github.darych.moneytransfer.model.StorageException;
import com.github.darych.moneytransfer.model.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction service tests")
class TransactionServiceTest {
    @Mock
    private Storage storage;

    @InjectMocks
    private TransactionService service = new TransactionService(storage);

    private Transaction tx = new Transaction(1, 2, 50);
    private Account account1Before = new Account(1, "user1", 200);
    private Account account2Before = new Account(2, "user2", 50);

    private Account account1After = new Account(1, "user1", 150);
    private Account account2After = new Account(2, "user2", 100);

    @Test
    @DisplayName("Process correct transaction")
    void processCorrectTx() throws StorageException {
        when(storage.get(anyInt())).thenReturn(account1Before, account2Before);

        service.processTx(tx);

        verify(storage).get(account1Before.getId());
        verify(storage).get(account2Before.getId());
        verify(storage).save(account1After);
        verify(storage).save(account2After);
    }

    @Nested
    class Negative {
        @Nested
        class Storage {
            @Test
            @DisplayName("Throw TransactionServiceException if save failed")
            void throwExceptionIfSaveFailed() throws StorageException {
                when(storage.get(anyInt())).thenReturn(account1Before, account2Before);
                when(storage.save(any())).thenThrow(new StorageException(""));

                assertThrows(TransactionServiceException.class, () -> {
                    service.processTx(tx);
                });
            }
        }

        @Nested
        class Ids {
            @Test
            @DisplayName("Throw TransactionServiceException if transfer between same accounts")
            void throwExceptionIfTransferBetweenSameAccounts() {
                Transaction txSameAccs = new Transaction(1, 1, 10);

                assertThrows(TransactionServiceException.class, () -> {
                    service.processTx(txSameAccs);
                });

                verifyNoMoreInteractions(storage);
            }

            @Test
            @DisplayName("Throw TransactionServiceException if source account does not exist")
            void throwExceptionIfSourceAccountNotExist() {
                when(storage.get(anyInt())).thenReturn(null);

                assertThrows(TransactionServiceException.class, () -> { service.processTx(tx); });

                verifyNoMoreInteractions(storage);
            }

            @Test
            @DisplayName("Throw TransactionServiceException if target account does not exist")
            void throwExceptionIfTargetAccountNotExist() {
                when(storage.get(anyInt())).thenReturn(account1Before, null);

                assertThrows(TransactionServiceException.class, () -> { service.processTx(tx); });

                verifyNoMoreInteractions(storage);
            }
        }

        @Nested
        class Amount {
            @Test
            @DisplayName("Throw TransactionServiceException if amount is negative")
            void throwExceptionIfAmountIsNegative() {
                Transaction txNegAmount = new Transaction(1, 2, -10);

                assertThrows(TransactionServiceException.class, () -> {
                    service.processTx(txNegAmount);
                });

                verifyNoMoreInteractions(storage);
            }

            @Test
            @DisplayName("Throw TransactionServiceException if amount is zero")
            void throwExceptionIfAmountIsZero() {
                Transaction txNegAmount = new Transaction(1, 2, 0);

                assertThrows(TransactionServiceException.class, () -> {
                    service.processTx(txNegAmount);
                });

                verifyNoMoreInteractions(storage);
            }

            @Test
            @DisplayName("Throw TransactionServiceException if amount greater than source balance")
            void throwExceptionIfAmountGreaterThanSourceBalance() {
                when(storage.get(anyInt())).thenReturn(account1Before, account2Before);
                Transaction txHugeAmount = new Transaction(1, 2, 100500);

                assertThrows(TransactionServiceException.class, () -> {
                    service.processTx(txHugeAmount);
                });

                verify(storage).get(account1Before.getId());
                verify(storage).get(account2Before.getId());
                verifyNoMoreInteractions(storage);
            }
        }
    }
}