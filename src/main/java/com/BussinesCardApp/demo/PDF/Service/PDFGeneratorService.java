package com.BussinesCardApp.demo.PDF.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import jakarta.servlet.ServletOutputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.util.Units;

import java.awt.*;
import java.math.BigInteger;

import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabs;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTabStop;
import jakarta.servlet.http.HttpServletResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PDFGeneratorService {

    /**
     * Converts an uploaded Word (.docx) file directly to PDF.
     */
    public byte[] convertWordToPdf(MultipartFile file) throws IOException {
        try (XWPFDocument wordDoc = new XWPFDocument(file.getInputStream())) {
            return convertWordToPdf(wordDoc);
        }
    }

    /**
     * Converts an in-memory XWPFDocument to PDF.
     */
    public byte[] convertWordToPdf(XWPFDocument wordDoc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer    = new PdfWriter(out);
        PdfDocument pdfDoc  = new PdfDocument(writer);
        Document layoutDoc  = new Document(pdfDoc);

        for (XWPFParagraph para : wordDoc.getParagraphs()) {
            layoutDoc.add(new Paragraph(para.getText()));
        }

        layoutDoc.close();
        return out.toByteArray();
    }

    /**
     * Builds a Word document with a background + logo, converts it to PDF,
     * and streams it back as an HTTP download.
     */
    public void exportBack(HttpServletResponse response) {
        // 1) Prepare response for .docx download
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"business-cards-back.docx\""
        );

        try (XWPFDocument doc = new XWPFDocument()) {
            // 2) SECTION: A4 + zero margins
            CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
            CTPageSz sz     = sectPr.addNewPgSz();
            sz.setW(BigInteger.valueOf(11906));   // ≈210 mm
            sz.setH(BigInteger.valueOf(16838));   // ≈297 mm
            CTPageMar pgMar = sectPr.addNewPgMar();
            pgMar.setTop(BigInteger.ZERO);
            pgMar.setBottom(BigInteger.ZERO);
            pgMar.setLeft(BigInteger.ZERO);
            pgMar.setRight(BigInteger.ZERO);

            // 3) LOAD & COMPOSE the “card” image
            BufferedImage bg   = ImageIO.read(new ClassPathResource("Fundal.jpg").getInputStream());
            BufferedImage logo = ImageIO.read(new ClassPathResource("SiglaUVTSpate.png").getInputStream());
            int wPx = bg.getWidth(), hPx = bg.getHeight();
            BufferedImage card = new BufferedImage(wPx, hPx, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = card.createGraphics();
            g.drawImage(bg, 0, 0, null);
            int lx = (wPx - logo.getWidth())/2, ly = (hPx - logo.getHeight())/2;
            g.drawImage(logo, lx, ly, null);
            g.dispose();

            // 4) Serialize to byte[]
            ByteArrayOutputStream cardOut = new ByteArrayOutputStream();
            ImageIO.write(card, "PNG", cardOut);
            byte[] picBytes = cardOut.toByteArray();

            // 5) Compute TWIPS values
            int cardWidthTwips   = wPx * 15;     // (1440/96)
            int cardHeightTwips  = hPx * 15;
            int pageWidthTwips   = 11906;        // A4 width
            int pageHeightTwips  = 16838;        // A4 height

            // horizontal spacing (left margin, middle gap, right margin)
            int spacingTwips     = (pageWidthTwips - 2 * cardWidthTwips) / 3;
            // vertical spacing (top margin + between rows + bottom margin)
            // we need 5 gaps for 4 rows: (pageHeight - 4*card)/5 each
            int verticalGapTwips = (pageHeightTwips - 4 * cardHeightTwips) / 5;

            // 6) Create 4 rows of two cards each, with equal verticalGapTwips before each row
            for (int row = 0; row < 4; row++) {
                XWPFParagraph p = doc.createParagraph();
                p.setAlignment(ParagraphAlignment.LEFT);
                p.setIndentationLeft(spacingTwips);
                p.setSpacingBefore(verticalGapTwips);     // same gap above every row

                // add tab stop at (spacing + cardWidth)
                CTPPr ppr = p.getCTP().isSetPPr() ? p.getCTP().getPPr() : p.getCTP().addNewPPr();
                CTTabs tabs = ppr.isSetTabs() ? ppr.getTabs() : ppr.addNewTabs();
                CTTabStop ts = tabs.addNewTab();
                ts.setVal(STTabJc.LEFT);
                ts.setPos(BigInteger.valueOf((long)(spacingTwips + cardWidthTwips)));

                // first image (left)
                XWPFRun r1 = p.createRun();
                r1.addPicture(
                        new ByteArrayInputStream(picBytes),
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "card-L" + row + ".png",
                        Units.pixelToEMU(wPx),
                        Units.pixelToEMU(hPx)
                );

                // tab to jump to right slot
                p.createRun().getCTR().addNewTab();

                // second image (right)
                XWPFRun r2 = p.createRun();
                r2.addPicture(
                        new ByteArrayInputStream(picBytes),
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "card-R" + row + ".png",
                        Units.pixelToEMU(wPx),
                        Units.pixelToEMU(hPx)
                );
            }

            // 7) Stream the DOCX back
            try (ServletOutputStream out = response.getOutputStream()) {
                doc.write(out);
                out.flush();
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la generarea Word-ului", e);
        }
    }
    public void exportFace(HttpServletResponse response) {
        // 1) Prepare response for .docx download
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"business-cards-face.docx\""
        );

        try (XWPFDocument doc = new XWPFDocument()) {
            // 2) A4 + zero margins
            CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
            CTPageSz sz     = sectPr.addNewPgSz();
            sz.setW(BigInteger.valueOf(11906));   // ≈210 mm
            sz.setH(BigInteger.valueOf(16838));   // ≈297 mm
            CTPageMar pgMar = sectPr.addNewPgMar();
            pgMar.setTop(BigInteger.ZERO);
            pgMar.setBottom(BigInteger.ZERO);
            pgMar.setLeft(BigInteger.ZERO);
            pgMar.setRight(BigInteger.ZERO);

            // 3) LOAD & COMPOSE the “card” image with logo + text-box
            BufferedImage bg   = ImageIO.read(
                    new ClassPathResource("Fundal.jpg").getInputStream()
            );
            BufferedImage logo = ImageIO.read(
                    new ClassPathResource("SiglaUVTFata.png").getInputStream()
            );
            int wPx = bg.getWidth(), hPx = bg.getHeight();
            BufferedImage card = new BufferedImage(wPx, hPx, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = card.createGraphics();

            // --- background + logo în colțul stânga-sus ---
            g.drawImage(bg, 0, 0, null);
            g.drawImage(logo, 0, 0, null);

            // --- text-box sub siglă (fără margini vizibile) ---
            String textboxText = "text1";
            Font font = new Font("Arial", Font.PLAIN, 14);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
            int padding = 6;
            int tbX = 0;
            int tbY = logo.getHeight() + padding;
            int tbW = fm.stringWidth(textboxText) + padding * 2;
            int tbH = fm.getHeight() + padding * 2;

            // fundal alb (fără chenar)
            g.setColor(Color.WHITE);
            g.fillRect(tbX, tbY, tbW, tbH);

            // scriem textul în interior
            g.setColor(Color.BLACK);
            int textX = tbX + padding;
            int textY = tbY + padding + fm.getAscent();
            g.drawString(textboxText, textX, textY);

            g.dispose();

            // 4) Serialize to byte[]
            ByteArrayOutputStream cardOut = new ByteArrayOutputStream();
            ImageIO.write(card, "PNG", cardOut);
            byte[] picBytes = cardOut.toByteArray();

            // 5) Compute TWIPS values
            int cardWidthTwips   = wPx * 15;
            int cardHeightTwips  = hPx * 15;
            int pageWidthTwips   = 11906;
            int pageHeightTwips  = 16838;

            int spacingTwips     = (pageWidthTwips - 2 * cardWidthTwips) / 3;
            int verticalGapTwips = (pageHeightTwips - 4 * cardHeightTwips) / 5;

            // 6) Create 4 rows of two cards each
            for (int row = 0; row < 4; row++) {
                XWPFParagraph p = doc.createParagraph();
                p.setAlignment(ParagraphAlignment.LEFT);
                p.setIndentationLeft(spacingTwips);
                p.setSpacingBefore(verticalGapTwips);

                // tab stop for 2nd image
                CTPPr ppr = p.getCTP().isSetPPr()
                        ? p.getCTP().getPPr()
                        : p.getCTP().addNewPPr();
                CTTabs tabs = ppr.isSetTabs() ? ppr.getTabs() : ppr.addNewTabs();
                CTTabStop ts = tabs.addNewTab();
                ts.setVal(STTabJc.LEFT);
                ts.setPos(BigInteger.valueOf((long)(spacingTwips + cardWidthTwips)));

                // first image (left)
                XWPFRun r1 = p.createRun();
                r1.addPicture(
                        new ByteArrayInputStream(picBytes),
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "card-L" + row + ".png",
                        Units.pixelToEMU(wPx),
                        Units.pixelToEMU(hPx)
                );

                // tab → right image
                p.createRun().getCTR().addNewTab();
                XWPFRun r2 = p.createRun();
                r2.addPicture(
                        new ByteArrayInputStream(picBytes),
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "card-R" + row + ".png",
                        Units.pixelToEMU(wPx),
                        Units.pixelToEMU(hPx)
                );
            }

            // 7) Stream .docx back
            try (ServletOutputStream out = response.getOutputStream()) {
                doc.write(out);
                out.flush();
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la generarea Word-ului", e);
        }
    }
}
