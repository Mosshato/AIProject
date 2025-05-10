package com.BussinesCardApp.demo.PDF.Service;


import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PDFGeneratorService {
    public void export(HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();
        Font fontTitle = FontFactory.getFont(FontFactory.COURIER_BOLD);
        Paragraph paragraph = new Paragraph("This is a title", fontTitle);
        paragraph.setAlignment(Element.ALIGN_CENTER);

        Font fontParagraph = FontFactory.getFont(FontFactory.HELVETICA);
        paragraph.setFont(fontParagraph);

        Paragraph paragraph2 = new Paragraph("This is a paragraph", fontParagraph);
        paragraph2.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
        document.add(paragraph2);
        document.close();
    }
}
