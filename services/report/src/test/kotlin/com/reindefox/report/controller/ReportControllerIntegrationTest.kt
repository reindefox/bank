package com.reindefox.report.controller

import com.reindefox.report.entity.Account
import com.reindefox.report.entity.Transaction
import com.reindefox.report.repository.AccountRepository
import com.reindefox.report.repository.TransactionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ReportControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("currency.service.url") { "http://localhost:8082" }
        }
    }

    @BeforeEach
    fun setUp() {
        transactionRepository.deleteAll()
        accountRepository.deleteAll()

        val account = Account(
            id = "test-acc-1",
            accountNumber = "TEST-001",
            balance = BigDecimal("1000.00"),
            currency = "USD",
            createdAt = LocalDateTime.now()
        )
        accountRepository.save(account)

        val transactions = listOf(
            Transaction(
                id = "test-t1",
                accountId = "test-acc-1",
                amount = BigDecimal("500.00"),
                currency = "USD",
                description = "Test Income",
                transactionType = "INCOME",
                createdAt = LocalDateTime.now().minusDays(1)
            ),
            Transaction(
                id = "test-t2",
                accountId = "test-acc-1",
                amount = BigDecimal("-100.00"),
                currency = "USD",
                description = "Test Expense",
                transactionType = "EXPENSE",
                createdAt = LocalDateTime.now()
            )
        )
        transactionRepository.saveAll(transactions)
    }

    @Test
    fun `test getTransactions - returns transactions from database`() {
        mockMvc.perform(get("/api/report/transactions"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].amount").exists())
    }

    @Test
    fun `test getTransactions with accountId - returns filtered transactions`() {
        mockMvc.perform(get("/api/report/transactions").param("accountId", "test-acc-1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value("test-t1"))
            .andExpect(jsonPath("$[1].id").value("test-t2"))
    }

    @Test
    fun `test getTransactionHistory - returns sorted transactions`() {
        mockMvc.perform(get("/api/report/transactions/history").param("accountId", "test-acc-1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").value("test-t2")) // Most recent first
    }

    @Test
    fun `test getAccountAnalytics - calculates analytics from database`() {
        mockMvc.perform(get("/api/report/analytics/test-acc-1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accountId").value("test-acc-1"))
            .andExpect(jsonPath("$.totalInflow").value(500.0))
            .andExpect(jsonPath("$.totalOutflow").value(-100.0))
            .andExpect(jsonPath("$.avgTransaction").exists())
    }

    @Test
    fun `test getHelpPdf - returns PDF file`() {
        mockMvc.perform(get("/api/report/help/pdf"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().string("Content-Disposition", "inline; filename=help.pdf"))
    }
}

