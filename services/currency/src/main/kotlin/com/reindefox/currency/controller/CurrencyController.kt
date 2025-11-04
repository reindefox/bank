package com.reindefox.currency.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class MoneyDto(val amount: Double, val currency: String)

@RestController
@RequestMapping("/api/currency")
class CurrencyController {

    @GetMapping("/convert/view")
    fun viewInCurrency(
        @RequestParam amount: Double,
        @RequestParam from: String,
        @RequestParam to: String
    ): MoneyDto {
        val rate = demoRate(from, to)
        return MoneyDto(amount * rate, to.uppercase())
    }

    @PostMapping("/select")
    fun selectCurrency(@RequestParam desired: String): Map<String, String> =
        mapOf("selectedCurrency" to desired.uppercase())

    @PostMapping("/convert")
    fun convert(
        @RequestParam amount: Double,
        @RequestParam from: String,
        @RequestParam to: String
    ): MoneyDto {
        val rate = demoRate(from, to)
        return MoneyDto(amount * rate, to.uppercase())
    }

    private fun demoRate(from: String, to: String): Double {
        if (from.equals(to, ignoreCase = true)) return 1.0
        return when (from.uppercase() to to.uppercase()) {
            "USD" to "EUR" -> 0.9
            "EUR" to "USD" -> 1.1
            "USD" to "RUB" -> 90.0
            "RUB" to "USD" -> 0.011
            else -> 1.0
        }
    }
}

