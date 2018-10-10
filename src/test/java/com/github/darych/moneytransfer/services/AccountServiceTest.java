package com.github.darych.moneytransfer.services;

import com.github.darych.moneytransfer.model.Account;
import com.github.darych.moneytransfer.model.Storage;
import com.github.darych.moneytransfer.model.StorageException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account service tests")
class AccountServiceTest {
    @Mock
    private Storage storage;

    @InjectMocks
    private AccountService service = new AccountService(storage);

    private String userName = "user1";
    private double balance = 100.1;
    private Account account = new Account(userName, balance);
    private Account accountWithId = new Account(1, userName, balance);

    @BeforeEach
    void setupTest() {
        MockitoAnnotations.initMocks(this);

        reset(storage);
    }

    @AfterEach
    void cleanupTest() {
        verifyNoMoreInteractions(storage);
    }

    @Nested
    @DisplayName("Create account tests")
    class CreateTest {
        @Test
        @DisplayName("Create account and return it with id")
        void createAccountAndReturnWithId() throws StorageException {
            when(storage.save(account)).thenReturn(accountWithId);

            assertEquals(accountWithId, service.save(account));

            verify(storage).save(account);
        }

        @Nested
        class Negative {
            @Test
            @DisplayName("Throw AccountServiceException on exception in Storage")
            void throwAccountServiceExceptionOnStorageException() throws StorageException {
                when(storage.save(account)).thenThrow(new StorageException(""));

                assertThrows(AccountServiceException.class, () -> {
                    service.save(account);
                });

                verify(storage).save(account);
            }
        }
    }

    @Nested
    @DisplayName("Get account tests")
    class GetTest {
        private Account account = new Account(1, "user1", 1.2);
        @Test
        @DisplayName("Get existing account")
        void getExistingAccount() {
            when(storage.get(account.getId())).thenReturn(account);

            assertEquals(account, service.getById(account.getId()));

            verify(storage).get(account.getId());
        }

        @Test
        @DisplayName("Return null if account does not exist")
        void returnNullIfAccountDoesntExist() {
            when(storage.get(anyInt())).thenReturn(null);

            assertNull(service.getById(account.getId()));

            verify(storage).get(account.getId());
        }
    }
}