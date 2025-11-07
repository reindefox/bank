package com.reindefox.report.repository

import com.reindefox.report.entity.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : JpaRepository<Transaction, String> {
    fun findByAccountId(accountId: String): List<Transaction>
    
    fun findByAccountIdOrderByCreatedAtDesc(accountId: String): List<Transaction>
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.transactionType = 'INCOME'")
    fun sumInflowByAccountId(@Param("accountId") accountId: String): java.math.BigDecimal?
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.transactionType = 'EXPENSE'")
    fun sumOutflowByAccountId(@Param("accountId") accountId: String): java.math.BigDecimal?
    
    @Query("SELECT AVG(ABS(t.amount)) FROM Transaction t WHERE t.accountId = :accountId")
    fun avgTransactionAmountByAccountId(@Param("accountId") accountId: String): Double?
}

