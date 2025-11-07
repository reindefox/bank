package com.reindefox.report.controller

import com.reindefox.report.entity.Transaction
import com.reindefox.report.service.CurrencyService
import com.reindefox.report.service.ReportService
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream

data class TransactionDto(
    val id: String,
    val amount: Double,
    val currency: String,
    val description: String
)

data class AccountAnalyticsDto(
    val accountId: String,
    val totalInflow: Double,
    val totalOutflow: Double,
    val avgTransaction: Double
)

@RestController
@RequestMapping("/api/report")
class ReportController(
    private val reportService: ReportService,
    private val currencyService: CurrencyService
) {

    @GetMapping("/transactions")
    fun getTransactions(@RequestParam(required = false) accountId: String?): List<TransactionDto> {
        val transactions = reportService.getTransactions(accountId)
        return transactions.map { toDto(it) }
    }

    @GetMapping("/transactions/history")
    fun getTransactionHistory(@RequestParam(required = false) accountId: String?): List<TransactionDto> {
        val transactions = reportService.getTransactionHistory(accountId)
        return transactions.map { toDto(it) }
    }

    @GetMapping("/analytics/{accountId}")
    fun getAccountAnalytics(@PathVariable accountId: String): AccountAnalyticsDto {
        val analytics = reportService.getAccountAnalytics(accountId)
        return AccountAnalyticsDto(
            accountId = analytics.accountId,
            totalInflow = analytics.totalInflow,
            totalOutflow = analytics.totalOutflow,
            avgTransaction = analytics.avgTransaction
        )
    }

    @PostMapping("/account/{accountId}/statement")
    fun generateAccountStatement(
        @PathVariable accountId: String,
        @RequestParam(required = false, defaultValue = "USD") targetCurrency: String
    ): ResponseEntity<ByteArray> {
        val account = reportService.getAccount(accountId)
        val analytics = reportService.getAccountAnalytics(accountId)
        
        // Вызов вложенной функции - конвертация валюты через сервис валют
        val accountCurrency = account?.currency ?: "USD"
        val convertedBalance = if (accountCurrency != targetCurrency && account != null) {
            currencyService.convertCurrency(
                account.balance.toDouble(),
                accountCurrency,
                targetCurrency
            ).block()?.amount ?: account.balance.toDouble()
        } else {
            account?.balance?.toDouble() ?: 0.0
        }
        
        val lines = mutableListOf<String>().apply {
            add("Account Statement")
            add("Account ID: $accountId")
            add("Account Number: ${account?.accountNumber ?: "N/A"}")
            add("Balance: ${account?.balance ?: "0.00"} ${accountCurrency}")
            if (accountCurrency != targetCurrency) {
                add("Balance in $targetCurrency: $convertedBalance")
            }
            add("Total Inflow: ${analytics.totalInflow} ${accountCurrency}")
            add("Total Outflow: ${analytics.totalOutflow} ${accountCurrency}")
            add("Average Transaction: ${analytics.avgTransaction} ${accountCurrency}")
        }
        
        val bytes = generateSimplePdf(
            title = "Account Statement",
            lines = lines
        )
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=statement-$accountId.pdf")
            .body(bytes)
    }
    
    private fun toDto(transaction: Transaction): TransactionDto {
        return TransactionDto(
            id = transaction.id,
            amount = transaction.amount.toDouble(),
            currency = transaction.currency,
            description = transaction.description ?: ""
        )
    }

    private fun generateSimplePdf(title: String, lines: List<String>): ByteArray {
        PDDocument().use { doc ->
            val page = PDPage()
            doc.addPage(page)
            PDPageContentStream(doc, page).use { stream ->
                stream.beginText()
                stream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 18f)
                stream.newLineAtOffset(50f, 750f)
                stream.showText(title)
                stream.endText()

                var y = 720f
                for (line in lines) {
                    stream.beginText()
                    stream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
                    stream.newLineAtOffset(50f, y)
                    stream.showText(line)
                    stream.endText()
                    y -= 18f
                }
            }
            val out = ByteArrayOutputStream()
            doc.save(out)
            return out.toByteArray()
        }
    }
}

