package com.BussinesCardApp.demo.PDF.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;

import com.itextpdf.layout.properties.AreaBreakType;

import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import com.itextpdf.layout.element.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
public class PDFGeneratorService {

    private final BusinessCardMaker cardMaker;

    public PDFGeneratorService(BusinessCardMaker cardMaker) {
        this.cardMaker = cardMaker;
    }

    private ImageData bufferedImageToImageData(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return ImageDataFactory.create(baos.toByteArray());
    }

    public void exportCardsAsPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition","attachment; filename=\"business-cards.pdf\"");

        // 1) ask BusinessCardMaker for your two images
        BufferedImage faceCard = cardMaker.createFaceImage();
        BufferedImage backCard = cardMaker.createBackImage();

        // 2) convert
        ImageData faceData = bufferedImageToImageData(faceCard);
        ImageData backData = bufferedImageToImageData(backCard);

        // 3) build PDF
        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf   = new PdfDocument(writer);
             Document doc      = new Document(pdf, PageSize.A4)) {

            tileImageOnPage(doc, faceData);
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            tileImageOnPage(doc, backData);
        }
    }

    private void tileImageOnPage(Document doc, ImageData data) {
        // A4 page size
        var ps    = doc.getPdfDocument().getDefaultPageSize();
        float W   = ps.getWidth();
        float H   = ps.getHeight();

        // desired card size
        float mmToPt = 72f / 25.4f;
        float cardW  = 90f * mmToPt;   // 85 mm
        float cardH  = 50f * mmToPt;   // 55 mm

        // how many across/down? adjust as you like
        int cols = 2, rows = 4;

        // optional: compute cell spacing (centering)
        float hGap = (W  - cols*cardW) / (cols+1);
        float vGap = (H  - rows*cardH) / (rows+1);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float x = hGap + c * (cardW + hGap);
                // PDF origin is bottom‐left
                float y = vGap + (rows-1-r) * (cardH + vGap);

                Image img = new Image(data)
                        .scaleAbsolute(cardW, cardH)
                        .setFixedPosition(x, y);

                doc.add(img);
            }
        }
    }

}
