package com.reindefox.report.service

import com.reindefox.report.entity.Account
import com.reindefox.report.entity.Transaction
import com.reindefox.report.repository.AccountRepository
import com.reindefox.report.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class ReportServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @InjectMocks
    private lateinit var reportService: ReportService

    private lateinit var testAccount: Account
    private lateinit var testTransactions: List<Transaction>

    @BeforeEach
    fun setUp() {
        testAccount = Account(
            id = "acc1",
            accountNumber = "ACC-001",
            balance = BigDecimal("1000.00"),
            currency = "USD",
            createdAt = LocalDateTime.now()
        )

        testTransactions = listOf(
            Transaction(
                id = "t1",
                accountId = "acc1",
                amount = BigDecimal("100.00"),
                currency = "USD",
                description = "Salary",
                transactionType = "INCOME",
                createdAt = LocalDateTime.now()
            ),
            Transaction(
                id = "t2",
                accountId = "acc1",
                amount = BigDecimal("-25.50"),
                currency = "USD",
                description = "Groceries",
                transactionType = "EXPENSE",
                createdAt = LocalDateTime.now()
            )
        )
    }

    @Test
    fun `test getAccount - returns account when exists`() {
        whenever(accountRepository.findById("acc1")).thenReturn(Optional.of(testAccount))

        val result = reportService.getAccount("acc1")

        assertEquals(testAccount, result)
    }

    @Test
    fun `test getAccount - returns null when not exists`() {
        whenever(accountRepository.findById("acc2")).thenReturn(Optional.empty())

        val result = reportService.getAccount("acc2")

        assertNull(result)
    }

    @Test
    fun `test getTransactions - returns transactions for specific account`() {
        whenever(transactionRepository.findByAccountId("acc1")).thenReturn(testTransactions)

        val result = reportService.getTransactions("acc1")

        assertEquals(2, result.size)
        assertEquals("t1", result[0].id)
        assertEquals("t2", result[1].id)
    }

    @Test
    fun `test getTransactions - returns all transactions when accountId is null`() {
        whenever(transactionRepository.findAll()).thenReturn(testTransactions)

        val result = reportService.getTransactions(null)

        assertEquals(2, result.size)
    }

    @Test
    fun `test getTransactionHistory - returns sorted transactions for account`() {
        val sortedTransactions = testTransactions.sortedByDescending { it.createdAt }
        whenever(transactionRepository.findByAccountIdOrderByCreatedAtDesc("acc1"))
            .thenReturn(sortedTransactions)

        val result = reportService.getTransactionHistory("acc1")

        assertEquals(2, result.size)
    }

    @Test
    fun `test getAccountAnalytics - calculates correct analytics`() {
        whenever(transactionRepository.sumInflowByAccountId("acc1"))
            .thenReturn(BigDecimal("100.00"))
        whenever(transactionRepository.sumOutflowByAccountId("acc1"))
            .thenReturn(BigDecimal("-25.50"))
        whenever(transactionRepository.avgTransactionAmountByAccountId("acc1"))
            .thenReturn(62.75)

        val result = reportService.getAccountAnalytics("acc1")

        assertEquals("acc1", result.accountId)
        assertEquals(100.0, result.totalInflow)
        assertEquals(-25.5, result.totalOutflow)
        assertEquals(62.75, result.avgTransaction)
    }

    @Test
    fun `test getAccountAnalytics - handles null values`() {
        whenever(transactionRepository.sumInflowByAccountId("acc2")).thenReturn(null)
        whenever(transactionRepository.sumOutflowByAccountId("acc2")).thenReturn(null)
        whenever(transactionRepository.avgTransactionAmountByAccountId("acc2")).thenReturn(null)

        val result = reportService.getAccountAnalytics("acc2")

        assertEquals("acc2", result.accountId)
        assertEquals(0.0, result.totalInflow)
        assertEquals(0.0, result.totalOutflow)
        assertEquals(0.0, result.avgTransaction)
    }
}

