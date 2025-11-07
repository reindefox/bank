package com.reindefox.report.service

import com.reindefox.report.entity.Account
import com.reindefox.report.entity.Transaction
import com.reindefox.report.repository.AccountRepository
import com.reindefox.report.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ReportService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    fun getAccount(accountId: String): Account? {
        return accountRepository.findById(accountId).orElse(null)
    }
    
    fun getTransactions(accountId: String?): List<Transaction> {
        return if (accountId != null) {
            transactionRepository.findByAccountId(accountId)
        } else {
            transactionRepository.findAll()
        }
    }
    
    fun getTransactionHistory(accountId: String?): List<Transaction> {
        return if (accountId != null) {
            transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
        } else {
            transactionRepository.findAll().sortedByDescending { it.createdAt }
        }
    }
    
    fun getAccountAnalytics(accountId: String): AccountAnalytics {
        val inflow = transactionRepository.sumInflowByAccountId(accountId) ?: BigDecimal.ZERO
        val outflow = transactionRepository.sumOutflowByAccountId(accountId) ?: BigDecimal.ZERO
        val avgTransaction = transactionRepository.avgTransactionAmountByAccountId(accountId) ?: 0.0
        
        return AccountAnalytics(
            accountId = accountId,
            totalInflow = inflow.toDouble(),
            totalOutflow = outflow.toDouble(),
            avgTransaction = avgTransaction
        )
    }
}

data class AccountAnalytics(
    val accountId: String,
    val totalInflow: Double,
    val totalOutflow: Double,
    val avgTransaction: Double
)

