package com.app.team1.technotribe.krasvbank.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.app.team2.technotribe.krasvbank.bankStatementPdfGenerator.BankStatement;
import com.app.team2.technotribe.krasvbank.controller.BankController;
import com.app.team2.technotribe.krasvbank.dto.BankResponse;
import com.app.team2.technotribe.krasvbank.dto.CreditDebitRequest;
import com.app.team2.technotribe.krasvbank.dto.EnquiryRequest;
import com.app.team2.technotribe.krasvbank.dto.TransferRequest;
import com.app.team2.technotribe.krasvbank.entity.BankAccount;
import com.app.team2.technotribe.krasvbank.entity.Transaction;
import com.app.team2.technotribe.krasvbank.service.impl.BankService;
import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper here

public class BankControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BankService bankService;

    @Mock
    private BankStatement bankStatement;

    @InjectMocks
    private BankController bankController;

    private ObjectMapper objectMapper; // Declare ObjectMapper

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bankController).build();
        objectMapper = new ObjectMapper(); // Initialize ObjectMapper
    }

    @Test
    void testCreateBankAccount() throws Exception {
        BankAccount newAccount = new BankAccount();
        newAccount.setName("John Doe");
        newAccount.setEmail("john.doe@example.com");
        newAccount.setPassword("password123");

        when(bankService.createAccount(any(BankAccount.class))).thenReturn("SUCCESS");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/createaccount")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAccount)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("SUCCESS"));

        verify(bankService, times(1)).createAccount(any(BankAccount.class));
    }

    @Test
    void testGenerateBankStatement() throws Exception {
        List<Transaction> mockTransactions = new ArrayList<>();
        mockTransactions.add(new Transaction());
        mockTransactions.add(new Transaction());

        when(bankStatement.generateStatement(anyString(), anyString(), anyString())).thenReturn(mockTransactions);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/bankStatement")
                .param("accountNumber", "1234567890")
                .param("startDate", "2023-01-01")
                .param("endDate", "2023-06-01"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(mockTransactions.size()));

        verify(bankStatement, times(1)).generateStatement("1234567890", "2023-01-01", "2023-06-01");
    }

    @Test
    void testBalanceEnquiry() throws Exception {
        BigDecimal mockBalance = BigDecimal.valueOf(1000.00);

        when(bankService.balanceEnquiry(any(EnquiryRequest.class))).thenReturn(mockBalance);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/balanceEnquiry/{accountNumber}", "1234567890"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().string(mockBalance.toString()));

        verify(bankService, times(1)).balanceEnquiry(any(EnquiryRequest.class));
    }

    @Test
    void testCreditAccount() throws Exception {
        CreditDebitRequest request = new CreditDebitRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(BigDecimal.valueOf(500.00));

        BankResponse mockResponse = new BankResponse();
        mockResponse.setResponseCode("200");
        mockResponse.setResponseMessage("Credit successful");

        when(bankService.creditAccount(any(CreditDebitRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("200"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.responseMessage").value("Credit successful"));

        verify(bankService, times(1)).creditAccount(any(CreditDebitRequest.class));
    }

    @Test
    void testDebitAccount() throws Exception {
        CreditDebitRequest request = new CreditDebitRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(BigDecimal.valueOf(200.00));

        BankResponse mockResponse = new BankResponse();
        mockResponse.setResponseCode("200");
        mockResponse.setResponseMessage("Debit successful");

        when(bankService.debitAccount(any(CreditDebitRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("200"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.responseMessage").value("Debit successful"));

        verify(bankService, times(1)).debitAccount(any(CreditDebitRequest.class));
    }

    @Test
    void testTransfer() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("1234567890");
        request.setDestinationAccountNumber("9876543210");
        request.setAmount(BigDecimal.valueOf(300.00));

        BankResponse mockResponse = new BankResponse();
        mockResponse.setResponseCode("200");
        mockResponse.setResponseMessage("Transfer successful");

        when(bankService.transfer(any(TransferRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.responseCode").value("200"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.responseMessage").value("Transfer successful"));

        verify(bankService, times(1)).transfer(any(TransferRequest.class));
    }


}
