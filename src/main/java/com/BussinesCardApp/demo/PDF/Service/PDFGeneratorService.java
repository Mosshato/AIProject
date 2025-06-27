package com.BussinesCardApp.demo.PDF.Service;

import com.BussinesCardApp.demo.user.appuser.AppUser;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.AreaBreakType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
 
@Service
public class PDFGeneratorService {

    private final BusinessCardMaker cardMaker;
    private final PDFRepository exportRepository;

    /**
     * Locația de bază unde se salvează fișierele PDF.
     * Dacă nu există în application.properties, se folosește "pdf-storage" implicit.
     */
    private final Path storageRoot;

    public PDFGeneratorService(
            BusinessCardMaker cardMaker,
            PDFRepository exportRepository,
            @Value("${pdf.storage.location:pdf-storage}") String storageLocation
    ) {
        this.cardMaker   = cardMaker;
        this.exportRepository  = exportRepository;
        this.storageRoot = Paths.get(storageLocation);
    }

    /**
     * Convertește un BufferedImage într-un ImageData (folosit de iText).
     */
    private ImageData bufferedImageToImageData(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return ImageDataFactory.create(baos.toByteArray());
    }

    /**
     * Această metodă:
     *  1) generează PDF-ul în memorie (ca byte[]),
     *  2) îl trimite clientului prin HttpServletResponse,
     *  3) salvează același PDF pe disc, la {storageRoot}/{userId}/{timestamp}.pdf,
     *  4) inserează în MongoDB metadatele exportului.
     */
/*
    public void generateAndSavePdf(HttpServletResponse response, AppUser user) throws Exception {
        // --------------- 1) Generează PDF-ul în memorie ---------------
        byte[] pdfBytes = generatePdfBytes();

        // --------------- 2) Trimite PDF-ul către client ---------------
        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"business-cards-" + user.getId() + ".pdf\""
        );
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();

        // --------------- 3) Scrie PDF-ul pe disc ---------------
        // Creăm directorul root + subfolderul userId
        Path userDir = storageRoot.resolve(user.getId());
        Files.createDirectories(userDir);

        String fileName = Instant.now().toEpochMilli() + ".pdf";
        Path filePath = userDir.resolve(fileName);

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(pdfBytes);
        }

        // --------------- 4) Salvează metadatele în MongoDB ---------------
        PDFExport exportEvent = new PDFExport(
                user.getId(),
                user.getEmail(),
                Instant.now(),
                filePath.toString()
        );
        pdfRepo.save(exportEvent);
    }
*/

    /**
     * Generează PDF-ul ca un array de byte[], fără a-l trimite direct în response.
     * Este folosită intern de generateAndSavePdf().
     */
/*
    private byte[] generatePdfBytes() throws Exception {
        BufferedImage faceCard = cardMaker.createFaceImage();
        BufferedImage backCard = cardMaker.createBackImage();

        ImageData faceData = bufferedImageToImageData(faceCard);
        ImageData backData = bufferedImageToImageData(backCard);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer   = new PdfWriter(baos);
             PdfDocument pdf    = new PdfDocument(writer);
             Document doc       = new Document(pdf, PageSize.A4)) {

            // Adaugă imaginea de față pe prima pagină
            tileImageOnPage(doc, faceData);
            // Salt la pagina următoare
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            // Adaugă imaginea de spate pe a doua pagină
            tileImageOnPage(doc, backData);
            
            doc.close();
            return baos.toByteArray();
        }
    }
*/

    /**
     * Desenează (tilează) imaginea pe pagină, în grid de 2×4.
     */
    private void tileImageOnPage(Document doc, ImageData data) {
        var ps  = doc.getPdfDocument().getDefaultPageSize();
        float W = ps.getWidth();
        float H = ps.getHeight();

        float mmToPt = 72f / 25.4f;
        float cardW  = 90f * mmToPt;
        float cardH  = 50f * mmToPt;

        int cols = 2, rows = 4;
        float hGap = (W - cols * cardW) / (cols + 1);
        float vGap = (H - rows * cardH) / (rows + 1);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float x = hGap + c * (cardW + hGap);
                float y = vGap + (rows - 1 - r) * (cardH + vGap);
                Image img = new Image(data)
                        .scaleAbsolute(cardW, cardH)
                        .setFixedPosition(x, y);
                doc.add(img);
            }
        }
    }

    /**
     * Metoda veche, pe care o poți păstra doar dacă mai ai vreo rută care apelează exportCardsAsPdf().
     *
     */
    public void exportCardsAsPdf(BusinessCardDTO card,AppUser user,HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"business-cards.pdf\"");

        BufferedImage faceCard = cardMaker.createFaceImage(card);
        BufferedImage backCard = cardMaker.createBackImage();

        ImageData faceData = bufferedImageToImageData(faceCard);
        ImageData backData = bufferedImageToImageData(backCard);

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf   = new PdfDocument(writer);
             Document doc      = new Document(pdf, PageSize.A4)) {

            tileImageOnPage(doc, faceData);
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            tileImageOnPage(doc, backData);
        }
    }

}
