package com.reindefox.report.controller

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
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
class ReportController {

    @GetMapping("/help/pdf")
    fun getHelpPdf(): ResponseEntity<ByteArray> {
        val bytes = generateSimplePdf("Bank Report Service Help", listOf(
            "Endpoints:",
            "/api/report/transactions",
            "/api/report/transactions/history",
            "/api/report/analytics/{accountId}",
            "/api/report/account/{accountId}/statement"
        ))
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=help.pdf")
            .body(bytes)
    }

    @GetMapping("/transactions")
    fun getTransactions(@RequestParam(required = false) accountId: String?): List<TransactionDto> =
        listOf(
            TransactionDto("t1", 100.0, "USD", "Salary"),
            TransactionDto("t2", -25.5, "USD", "Groceries")
        )

    @GetMapping("/transactions/history")
    fun getTransactionHistory(@RequestParam(required = false) accountId: String?): List<TransactionDto> =
        listOf(
            TransactionDto("t0", -12.0, "USD", "Coffee"),
            TransactionDto("t-1", -8.4, "USD", "Snacks")
        )

    @GetMapping("/analytics/{accountId}")
    fun getAccountAnalytics(@PathVariable accountId: String): AccountAnalyticsDto =
        AccountAnalyticsDto(accountId, totalInflow = 1200.0, totalOutflow = 340.0, avgTransaction = 120.0)

    @PostMapping("/account/{accountId}/statement")
    fun generateAccountStatement(@PathVariable accountId: String): ResponseEntity<ByteArray> {
        val bytes = generateSimplePdf(
            title = "Account Statement",
            lines = listOf("Account: $accountId", "Period: demo", "Total: 860.0 USD")
        )
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=statement-$accountId.pdf")
            .body(bytes)
    }

    private fun generateSimplePdf(title: String, lines: List<String>): ByteArray {
        PDDocument().use { doc ->
            val page = PDPage()
            doc.addPage(page)
            PDPageContentStream(doc, page).use { stream ->
                stream.beginText()
//                stream.setFont(PDType1Font.HELVETICA_BOLD, 18f)
                stream.newLineAtOffset(50f, 750f)
                stream.showText(title)
                stream.endText()

                var y = 720f
                for (line in lines) {
                    stream.beginText()
//                    stream.setFont(PDType1Font.HELVETICA, 12f)
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

