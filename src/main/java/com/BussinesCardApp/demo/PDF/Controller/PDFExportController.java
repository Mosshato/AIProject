package com.BussinesCardApp.demo.PDF.Controller;

import com.BussinesCardApp.demo.PDF.Service.PDFGeneratorService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PDFExportController {

    private final PDFGeneratorService pdfService;

    public PDFExportController(PDFGeneratorService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Endpoint simplu: invocă serviciul de generare PDF
     * și trimite fișierul rezultat.
     */
    @GetMapping(value = "/pdf/generate", produces = "application/pdf")
    public void generatePDF(HttpServletResponse response) throws Exception {
        // Tot header-ul, conversia și stream-ul sunt gestionate în service
        pdfService.exportCardsAsPdf(response);
    }
}
