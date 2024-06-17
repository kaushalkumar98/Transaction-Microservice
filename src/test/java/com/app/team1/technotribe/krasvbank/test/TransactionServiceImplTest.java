package com.app.team1.technotribe.krasvbank.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.app.team2.technotribe.krasvbank.dto.TransactionDto;
import com.app.team2.technotribe.krasvbank.entity.Transaction;
import com.app.team2.technotribe.krasvbank.repository.TransactionRepository;
import com.app.team2.technotribe.krasvbank.service.impl.TransactionServiceImpl;

public class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveTransaction_Success() {
        // Prepare test data
        TransactionDto transactionDto = new TransactionDto("TRANSFER", BigDecimal.valueOf(500.00), "1234567890", "SUCCESS");

        // Mock repository save method to return the saved entity
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            //savedTransaction.setId(1L); // Simulate ID assignment by persistence layer
            return savedTransaction;
        });

        // Call the method under test
        transactionService.saveTransaction(transactionDto);

        // Verify that save method was called with correct arguments
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }


    @Test
    void testSaveTransaction_NullAmount() {
        // Prepare test data with null amount
        TransactionDto transactionDto = new TransactionDto("TRANSFER", null, "1234567890", "SUCCESS");

        // Mock repository save method to return the saved entity
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            //savedTransaction.setId(1L); // Simulate ID assignment by persistence layer
            return savedTransaction;
        });

        // Call the method under test
        transactionService.saveTransaction(transactionDto);

        // Verify that save method was called with correct arguments and default amount
        verify(transactionRepository, times(1)).save(argThat(savedTransaction ->
                savedTransaction.getTransactionType().equals("TRANSFER") &&
                savedTransaction.getAmount() == null &&
                savedTransaction.getAccountNumber().equals("1234567890") &&
                savedTransaction.getStatus().equals("SUCCESS")));
    }
}
