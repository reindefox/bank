package com.reindefox.report.controller

import com.reindefox.report.entity.Account
import com.reindefox.report.entity.Transaction
import com.reindefox.report.repository.AccountRepository
import com.reindefox.report.repository.TransactionRepository
import com.reindefox.report.service.CurrencyService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ReportControllerServiceIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var currencyService: CurrencyService

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:17")
            .withDatabaseName("testdb2")
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
            id = "test-acc-2",
            accountNumber = "TEST-002",
            balance = BigDecimal("1000.00"),
            currency = "USD",
            createdAt = LocalDateTime.now()
        )
        accountRepository.save(account)
    }

    @Test
    fun `test generateAccountStatement - calls currency service for conversion`() {
        // Mock currency service response
        whenever(currencyService.convertCurrency(1000.0, "USD", "EUR"))
            .thenReturn(Mono.just(com.reindefox.report.service.MoneyDto(900.0, "EUR")))

        mockMvc.perform(
            post("/api/report/account/test-acc-2/statement")
                .param("targetCurrency", "EUR")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().string("Content-Disposition", "inline; filename=statement-test-acc-2.pdf"))
    }

    @Test
    fun `test generateAccountStatement - uses same currency when no conversion needed`() {
        mockMvc.perform(
            post("/api/report/account/test-acc-2/statement")
                .param("targetCurrency", "USD")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
    }

    @Test
    fun `test generateAccountStatement - handles currency service error gracefully`() {
        // Mock currency service error
        whenever(currencyService.convertCurrency(1000.0, "USD", "EUR"))
            .thenReturn(Mono.error(RuntimeException("Service unavailable")))

        // Should still return PDF with fallback
        mockMvc.perform(
            post("/api/report/account/test-acc-2/statement")
                .param("targetCurrency", "EUR")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
    }
}

