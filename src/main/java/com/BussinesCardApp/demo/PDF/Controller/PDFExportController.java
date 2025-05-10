package com.BussinesCardApp.demo.PDF.Controller;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import com.BussinesCardApp.demo.PDF.Service.PDFGeneratorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class PDFExportController {

    private final PDFGeneratorService pdfGeneratorService;


    public PDFExportController(PDFGeneratorService pdfGeneratorService) {
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @GetMapping(value = "/pdf/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    public void generatePDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());

        String HeaderKey = "Content-Disposition";
        String HeaderValue = "attachament; filename=\"" + currentDateTime + ".pdf\"";
        response.setHeader(HeaderKey, HeaderValue);

        this.pdfGeneratorService.export(response);
        
    }
}
