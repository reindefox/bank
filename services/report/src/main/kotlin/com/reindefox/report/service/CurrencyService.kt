package com.reindefox.report.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

data class MoneyDto(val amount: Double, val currency: String)

@Service
class CurrencyService(
    @Value("\${currency.service.url}") private val currencyServiceUrl: String
) {
    private val webClient = WebClient.builder()
        .baseUrl(currencyServiceUrl)
        .build()
    
    fun convertCurrency(amount: Double, from: String, to: String): Mono<MoneyDto> {
        return webClient.get()
            .uri("/api/currency/convert/view?amount={amount}&from={from}&to={to}", amount, from, to)
            .retrieve()
            .bodyToMono<MoneyDto>()
            .timeout(Duration.ofSeconds(5))
            .onErrorReturn(MoneyDto(amount, to)) // Fallback to original amount if service unavailable
    }
}

