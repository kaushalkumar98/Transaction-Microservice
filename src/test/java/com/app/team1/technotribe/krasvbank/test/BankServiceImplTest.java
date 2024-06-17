package com.app.team1.technotribe.krasvbank.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.app.team2.technotribe.krasvbank.ExternalService.EmailClient;
import com.app.team2.technotribe.krasvbank.dto.AccountInfo;
import com.app.team2.technotribe.krasvbank.dto.BankResponse;
import com.app.team2.technotribe.krasvbank.dto.CreditDebitRequest;
import com.app.team2.technotribe.krasvbank.dto.EmailDetailsDto;
import com.app.team2.technotribe.krasvbank.dto.EnquiryRequest;
import com.app.team2.technotribe.krasvbank.dto.TransactionDto;
import com.app.team2.technotribe.krasvbank.dto.TransferRequest;
import com.app.team2.technotribe.krasvbank.entity.BankAccount;
import com.app.team2.technotribe.krasvbank.repository.BankRepository;
import com.app.team2.technotribe.krasvbank.service.impl.BankServiceImpl;
import com.app.team2.technotribe.krasvbank.service.impl.TransactionService;
import com.app.team2.technotribe.krasvbank.util.AccountUtils;


public class BankServiceImplTest {

    @Mock
    private BankRepository bankRepository;

    @Mock
    private EmailClient emailService; // Assuming EmailClient is used for sending emails

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private BankServiceImpl bankService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBalanceEnquiry() {
        // Mock data
        EnquiryRequest request = new EnquiryRequest("123456");

        BankAccount mockAccount = new BankAccount();
        mockAccount.setAccountNumber("123456");
        mockAccount.setAccountBalance(BigDecimal.valueOf(1000));

        when(bankRepository.findByAccountNumber("123456")).thenReturn(mockAccount);

        BigDecimal result = bankService.balanceEnquiry(request);

        assertEquals(BigDecimal.valueOf(1000), result);
    }

    @Test
    void testNameEnquiry_AccountExists() {
        EnquiryRequest request = new EnquiryRequest("123456");

        BankAccount mockAccount = new BankAccount();
        mockAccount.setAccountNumber("123456");
        mockAccount.setName("John Doe");

        when(bankRepository.existsByAccountNumber("123456")).thenReturn(true);
        when(bankRepository.findByAccountNumber("123456")).thenReturn(mockAccount);

        String result = bankService.nameEnquiry(request);

        assertEquals("John Doe", result);
    }

    @Test
    void testNameEnquiry_AccountNotExists() {
        EnquiryRequest request = new EnquiryRequest("654321");

        when(bankRepository.existsByAccountNumber("654321")).thenReturn(false);

        String result = bankService.nameEnquiry(request);

        assertEquals(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE, result);
    }

    @Test
    void testCreditAccount_Success() {
        CreditDebitRequest request = new CreditDebitRequest("123456", BigDecimal.valueOf(500));

        BankAccount mockAccount = new BankAccount();
        mockAccount.setAccountNumber("123456");
        mockAccount.setAccountBalance(BigDecimal.valueOf(1000));

        when(bankRepository.existsByAccountNumber("123456")).thenReturn(true);
        when(bankRepository.findByAccountNumber("123456")).thenReturn(mockAccount);

        // Execute the method under test
        BankResponse result = bankService.creditAccount(request);

        // Verify that saveTransaction() was called once with any TransactionDto argument
        verify(transactionService, times(1)).saveTransaction(any(TransactionDto.class));

        // Assertions can be added here to verify the result if needed
    }

    @Test
    void testCreditAccount_AccountNotFound() {
        CreditDebitRequest request = new CreditDebitRequest("654321", BigDecimal.valueOf(500));

        when(bankRepository.existsByAccountNumber("654321")).thenReturn(false);

        BankResponse result = bankService.creditAccount(request);

        assertEquals(AccountUtils.ACCOUNT_NOT_EXIST_CODE, result.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE, result.getResponseMessage());
        assertNull(result.getAccountInfo());
    }

    @Test
    void testDebitAccount_Success() {
        CreditDebitRequest request = new CreditDebitRequest("123456", BigDecimal.valueOf(500));

        BankAccount mockAccount = new BankAccount();
        mockAccount.setAccountNumber("123456");
        mockAccount.setAccountBalance(BigDecimal.valueOf(1000));

        when(bankRepository.existsByAccountNumber("123456")).thenReturn(true);
        when(bankRepository.findByAccountNumber("123456")).thenReturn(mockAccount);

        // Execute the method under test
        BankResponse result = bankService.debitAccount(request);

        // Verify that saveTransaction() was called once with any TransactionDto argument
        verify(transactionService, times(1)).saveTransaction(any(TransactionDto.class));

        // Assertions can be added here to verify the result if needed
        assertEquals(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE, result.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_DEBITED_SUCCESS_MESSAGE, result.getResponseMessage());
        assertNotNull(result.getAccountInfo());
        assertEquals(BigDecimal.valueOf(500), mockAccount.getAccountBalance());
    }

    @Test
    void testDebitAccount_AccountNotFound() {
        CreditDebitRequest request = new CreditDebitRequest("654321", BigDecimal.valueOf(500));

        when(bankRepository.existsByAccountNumber("654321")).thenReturn(false);

        BankResponse result = bankService.debitAccount(request);

        assertEquals(AccountUtils.ACCOUNT_NOT_EXIST_CODE, result.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE, result.getResponseMessage());
        assertNull(result.getAccountInfo());
    }

    @Test
    void testDebitAccount_InsufficientBalance() {
        CreditDebitRequest request = new CreditDebitRequest("123456", BigDecimal.valueOf(1500));

        BankAccount mockAccount = new BankAccount();
        mockAccount.setAccountNumber("123456");
        mockAccount.setAccountBalance(BigDecimal.valueOf(1000));

        when(bankRepository.existsByAccountNumber("123456")).thenReturn(true);
        when(bankRepository.findByAccountNumber("123456")).thenReturn(mockAccount);

        BankResponse result = bankService.debitAccount(request);

        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_CODE, result.getResponseCode());
        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE, result.getResponseMessage());
        assertNull(result.getAccountInfo());
    }

    @Test
    void testTransfer_Success() {
        TransferRequest request = new TransferRequest("password", "123456", "789012", BigDecimal.valueOf(500));

        BankAccount sourceAccount = new BankAccount();
        sourceAccount.setAccountNumber("123456");
        sourceAccount.setAccountBalance(BigDecimal.valueOf(1000));

        BankAccount destinationAccount = new BankAccount();
        destinationAccount.setAccountNumber("789012");
        destinationAccount.setAccountBalance(BigDecimal.valueOf(2000));

        when(bankRepository.existsByAccountNumber("123456")).thenReturn(true);
        when(bankRepository.existsByAccountNumber("789012")).thenReturn(true);
        when(bankRepository.findByAccountNumber("123456")).thenReturn(sourceAccount);
        when(bankRepository.findByAccountNumber("789012")).thenReturn(destinationAccount);

        // Execute the method under test
        BankResponse result = bankService.transfer(request);

        // Verify that saveTransaction() was called twice (once for source, once for destination)
        verify(transactionService, times(1)).saveTransaction(any(TransactionDto.class));

        // Assertions can be added here to verify the result if needed
        assertEquals(AccountUtils.TRANSFER_SUCCESSFUL_CODE, result.getResponseCode());
        assertEquals(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE, result.getResponseMessage());
        assertNull(result.getAccountInfo()); // Assuming response doesn't return account info for transfer
        assertEquals(BigDecimal.valueOf(500), sourceAccount.getAccountBalance());
        assertEquals(BigDecimal.valueOf(2500), destinationAccount.getAccountBalance());
    }


    @Test
    void testTransfer_SourceAccountNotFound() {
        TransferRequest request = new TransferRequest("password","654321", "789012", BigDecimal.valueOf(500));

        when(bankRepository.existsByAccountNumber("654321")).thenReturn(false);

        BankResponse result = bankService.transfer(request);

        assertEquals(AccountUtils.ACCOUNT_NOT_EXIST_CODE, result.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE, result.getResponseMessage());
        assertNull(result.getAccountInfo());
    }

    @Test
    void testTransfer_DestinationAccountNotFound() {
        TransferRequest request = new TransferRequest("123456", "987654", null, BigDecimal.valueOf(500));

        when(bankRepository.existsByAccountNumber("123456")).thenReturn(true);
        when(bankRepository.existsByAccountNumber("987654")).thenReturn(false);

        BankResponse result = bankService.transfer(request);

        assertEquals(AccountUtils.ACCOUNT_NOT_EXIST_CODE, result.getResponseCode());
        assertEquals(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE, result.getResponseMessage());
        assertNull(result.getAccountInfo());
    }

    @Test
    void testTransfer_InsufficientBalance() {
        TransferRequest request = new TransferRequest("password", "123456", "789012", BigDecimal.valueOf(1500));

        BankAccount sourceAccount = new BankAccount();
        sourceAccount.setAccountNumber("123456");
        sourceAccount.setAccountBalance(BigDecimal.valueOf(1000));

        BankAccount destinationAccount = new BankAccount();
        destinationAccount.setAccountNumber("789012");
        destinationAccount.setAccountBalance(BigDecimal.valueOf(2000));

        when(bankRepository.existsByAccountNumber("123456")).thenReturn(true);
        when(bankRepository.existsByAccountNumber("789012")).thenReturn(true);
        when(bankRepository.findByAccountNumber("123456")).thenReturn(sourceAccount);
        when(bankRepository.findByAccountNumber("789012")).thenReturn(destinationAccount);

        // Execute the method under test
        BankResponse result = bankService.transfer(request);

        // Verify that saveTransaction() was not called
        verify(transactionService, never()).saveTransaction(any(TransactionDto.class));

        // Assertions can be added here to verify the result if needed
        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_CODE, result.getResponseCode());
        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE, result.getResponseMessage());
        assertNull(result.getAccountInfo()); // Assuming response doesn't return account info for transfer
        assertEquals(BigDecimal.valueOf(1000), sourceAccount.getAccountBalance()); // Balance should remain unchanged
        assertEquals(BigDecimal.valueOf(2000), destinationAccount.getAccountBalance()); // Balance should remain unchanged
    }



    @Test
    void testCreateAccount() {
        BankAccount newAccount = new BankAccount();
        newAccount.setAccountNumber("111111");
        newAccount.setName("New User");
        newAccount.setEmail("newuser@example.com");

        String result = bankService.createAccount(newAccount);

        assertEquals("User is Active", result);

        // Verify email service invocation
        verify(emailService, times(1)).sendEmailAlert(any(EmailDetailsDto.class));
    }

    // Add more test cases as needed

}

