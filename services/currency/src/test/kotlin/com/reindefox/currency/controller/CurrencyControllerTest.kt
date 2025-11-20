package com.reindefox.currency.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CurrencyController::class)
class CurrencyControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `test viewInCurrency - USD to EUR conversion`() {
        mockMvc.perform(
            get("/api/currency/convert/view")
                .param("amount", "100.0")
                .param("from", "USD")
                .param("to", "EUR")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.amount").value(90.0))
            .andExpect(jsonPath("$.currency").value("EUR"))
    }

    @Test
    fun `test viewInCurrency - same currency returns same amount`() {
        mockMvc.perform(
            get("/api/currency/convert/view")
                .param("amount", "100.0")
                .param("from", "USD")
                .param("to", "USD")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.amount").value(100.0))
            .andExpect(jsonPath("$.currency").value("USD"))
    }

    @Test
    fun `test selectCurrency - returns uppercase currency`() {
        mockMvc.perform(
            post("/api/currency/select")
                .param("desired", "eur")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.selectedCurrency").value("EUR"))
    }

    @Test
    fun `test convert - USD to RUB conversion`() {
        mockMvc.perform(
            post("/api/currency/convert")
                .param("amount", "1.0")
                .param("from", "USD")
                .param("to", "RUB")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.amount").value(90.0))
            .andExpect(jsonPath("$.currency").value("RUB"))
    }

    @Test
    fun `test convert - RUB to USD conversion`() {
        mockMvc.perform(
            post("/api/currency/convert")
                .param("amount", "90.0")
                .param("from", "RUB")
                .param("to", "USD")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.amount").value(0.99))
            .andExpect(jsonPath("$.currency").value("USD"))
    }
}

