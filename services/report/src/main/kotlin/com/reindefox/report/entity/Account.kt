package com.reindefox.report.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "accounts")
data class Account(
    @Id
    val id: String,
    
    @Column(name = "account_number", unique = true, nullable = false)
    val accountNumber: String,
    
    @Column(nullable = false)
    val balance: java.math.BigDecimal,
    
    @Column(nullable = false)
    val currency: String = "USD",
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime? = null
)

