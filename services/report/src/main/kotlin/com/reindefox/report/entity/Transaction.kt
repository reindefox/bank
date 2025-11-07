package com.reindefox.report.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    val id: String,
    
    @Column(name = "account_id", nullable = false)
    val accountId: String,
    
    @Column(nullable = false)
    val amount: java.math.BigDecimal,
    
    @Column(nullable = false)
    val currency: String = "USD",
    
    val description: String? = null,
    
    @Column(name = "transaction_type", nullable = false)
    val transactionType: String,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null
)

