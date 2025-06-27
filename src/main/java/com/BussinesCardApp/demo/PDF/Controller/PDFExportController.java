package com.BussinesCardApp.demo.PDF.Controller;

import com.BussinesCardApp.demo.PDF.Service.BusinessCardDTO;
import com.BussinesCardApp.demo.PDF.Service.PDFGeneratorService;
import com.BussinesCardApp.demo.user.appuser.AppUser;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PDFExportController {

    private final PDFGeneratorService pdfService;

    public PDFExportController(PDFGeneratorService pdfService) {
        this.pdfService = pdfService;
    }
    /**
     * 1) Clientul trimite antetul  Authorization: Bearer <JWT>
     * 2) În body (JSON) trimite datele pentru cartea de vizită
     * 3) Metoda generează PDF-ul și îl „pune” în HttpServletResponse
     */
    @PostMapping(value = "/pdf/generate",
            consumes = "application/json",
            produces = "application/pdf")
    public void generatePdf(@RequestBody BusinessCardDTO card,
                            @AuthenticationPrincipal AppUser user,   // vine din token
                            HttpServletResponse response) throws Exception {

        // delegi toată logica serviciului
        pdfService.exportCardsAsPdf(card, user, response);
        // serviciul setează Content-Type, Content-Disposition etc.
    }
}
