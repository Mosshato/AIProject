package com.BussinesCardApp.demo.PDF.Service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;

import com.itextpdf.layout.properties.AreaBreakType;

import java.awt.*;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import com.itextpdf.layout.element.Image;       // ← this one

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
public class PDFGeneratorService {

    private ImageData bufferedImageToImageData(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return ImageDataFactory.create(baos.toByteArray());
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

    public void exportCardsAsPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"business-cards.pdf\"");

        // 1) Build your two card images
        BufferedImage faceCard = createFaceImage();
        BufferedImage backCard = createBackImage();

        // 2) Convert to iText ImageData
        ImageData faceData = bufferedImageToImageData(faceCard);
        ImageData backData = bufferedImageToImageData(backCard);

        // 3) Create PDF (portrait A4)
        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf, PageSize.A4)) {

            tileImageOnPage(doc, faceData);              // page 1: faces
            doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            tileImageOnPage(doc, backData);              // page 2: backs
        }
    }
    private BufferedImage createBackImage() throws Exception {
        BufferedImage bg   = ImageIO.read(new ClassPathResource("Fundal.jpg").getInputStream());
        BufferedImage logo = ImageIO.read(new ClassPathResource("SiglaUVTSpate.png").getInputStream());
        int wPx = bg.getWidth(), hPx = bg.getHeight();

        double origDpi   = 96.0;
        double targetDpi = 300.0;
        double scale     = targetDpi / origDpi;
        int hiResW = (int)Math.round(wPx * scale);
        int hiResH = (int)Math.round(hPx * scale);

        BufferedImage card = new BufferedImage(hiResW, hiResH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = card.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);

        g.scale(scale, scale);
        g.drawImage(bg, 0, 0, null);
        int lx = (wPx - logo.getWidth()) / 2;
        int ly = (hPx - logo.getHeight()) / 2;
        g.drawImage(logo, lx, ly, null);
        g.dispose();

        // optional smooth + sharpen
        BufferedImage smooth = new BufferedImage(hiResW, hiResH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = smooth.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(card, 0, 0, null);
        g2.dispose();
        float[] kernel = {0f,-1f,0f, -1f,5f,-1f, 0f,-1f,0f};
        ConvolveOp op = new ConvolveOp(new Kernel(3,3,kernel), ConvolveOp.EDGE_NO_OP, null);
        return op.filter(smooth, null);
    }
    private BufferedImage createFaceImage() throws Exception {
        // 1) load your 96 dpi bitmaps
        BufferedImage bg   = ImageIO.read(new ClassPathResource("Fundal.jpg").getInputStream());
        BufferedImage logo = ImageIO.read(new ClassPathResource("SiglaUVTFata.png").getInputStream());
        int wPx = bg.getWidth(), hPx = bg.getHeight();

        // 2) bump up to 300 dpi
        double origDpi   = 96.0;
        double targetDpi = 300.0;
        double scale     = targetDpi / origDpi;
        int hiResW = (int) Math.round(wPx * scale);
        int hiResH = (int) Math.round(hPx * scale);

        // 3) create a high-res canvas + quality hints
        BufferedImage card = new BufferedImage(hiResW, hiResH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = card.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,    RenderingHints.VALUE_STROKE_PURE);

        // 4) scale your coordinate system so that your 96 dpi math still works
        g.scale(scale, scale);

        // 5) --- paste in your entire old draw logic here ---

        // background + logo
        g.drawImage(bg,  0,  0, null);
        g.drawImage(logo, 0,  0, null);

        // fixed textbox 3.69"x0.53" @96dpi
        double dpi = 96.0;
        int tbW     = (int)Math.round(3.69 * dpi);
        int tbH     = (int)Math.round(0.53 * dpi);
        int padding = 4;
        int tbX     = 0;
        int tbY     = logo.getHeight() + padding;
        g.setColor(Color.WHITE);
        g.fillRect(tbX, tbY, tbW, tbH);

        int rightMargin = 25;
        String text1 = "Assist. Prof. Ph.D. ";
        String text2 = "Sebastian-Aurelian ȘTEFĂNIGĂ";
        Font   f1    = new Font("Open Sans", Font.PLAIN, 10);
        Font   f2    = new Font("Open Sans", Font.BOLD, 12);
        FontMetrics fm1 = g.getFontMetrics(f1);
        FontMetrics fm2 = g.getFontMetrics(f2);

        int totalWidth1 = fm1.stringWidth(text1) + fm2.stringWidth(text2);
        int xLine1      = tbX + tbW - rightMargin - totalWidth1;
        int baseline1   = tbY + padding + fm1.getAscent();

        g.setFont(f1);
        g.setColor(Color.BLACK);
        g.drawString(text1, xLine1, baseline1);
        g.setFont(f2);
        g.drawString(text2, xLine1 + fm1.stringWidth(text1), baseline1);

        String text3 = "Vice Dean";
        Font   f3    = new Font("Open Sans", Font.BOLD, 9);
        FontMetrics fm3 = g.getFontMetrics(f3);
        int xLine2    = tbX + tbW - rightMargin - fm3.stringWidth(text3);
        int baseline2 = baseline1 + Math.max(fm1.getHeight(), fm2.getHeight()) + 2;
        g.setFont(f3);
        g.setColor(new Color(255, 140, 0));
        g.drawString(text3, xLine2, baseline2);

        // second textbox 1.83"x0.81"
        int tb2W          = (int)Math.round(1.83 * dpi);
        int tb2H          = (int)Math.round(0.81 * dpi);
        int paddingBetween= 2;
        int shiftUp       = 10;
        int shiftRight    = 1;
        int tb2X = tbX + tbW - rightMargin - tb2W + shiftRight;
        int tb2Y = tbY + tbH + paddingBetween - shiftUp;
        g.setColor(Color.WHITE);
        g.fillRect(tb2X, tb2Y, tb2W, tb2H);

        // contact info lines
        Font labelFont  = new Font("Calibri", Font.PLAIN, 10);
        Font valueFont  = new Font("Calibri", Font.PLAIN, 11);
        Font symbolFont = new Font("Adobe Ming Std L", Font.PLAIN, 18);
        FontMetrics fmLabel  = g.getFontMetrics(labelFont);
        FontMetrics fmValue  = g.getFontMetrics(valueFont);
        FontMetrics fmSymbol = g.getFontMetrics(symbolFont);

        int innerPad   = 4;
        int symX       = tb2X + innerPad;
        int textStartX = symX + fmSymbol.stringWidth("|") + 4;
        int currentY   = tb2Y + innerPad + Math.max(fmLabel.getAscent(), fmValue.getAscent());
        int lineH      = Math.max(fmLabel.getHeight(), fmValue.getHeight()) + 2;

        String[][] groups = {
                { "Office: +40 256-592.261", "Mobile: +40 762-696.901" },
                { "E-mail: sebastian.stefaniga@e-uvt.ro", "Web:   www.info.uvt.ro" }
        };
        for (String[] grp : groups) {
            int groupH      = lineH * grp.length - 2;
            int symBaseline = currentY + (groupH - fmSymbol.getAscent())/2;
            g.setFont(symbolFont);
            g.setColor(Color.GRAY);
            g.drawString("|", symX, symBaseline);
            for (String raw : grp) {
                String[] parts = raw.split(":",2);
                String label = parts[0] + ":";
                String value = parts.length>1 ? parts[1].trim() : "";
                g.setFont(labelFont);
                g.setColor(Color.BLACK);
                g.drawString(label, textStartX, currentY);
                int lw = fmLabel.stringWidth(label);
                g.setFont(valueFont);
                g.drawString(value, textStartX + lw + 4, currentY);
                currentY += lineH;
            }
        }

        g.dispose();

        // 6) optional: smooth + sharpen at 300 dpi
        BufferedImage smooth = new BufferedImage(hiResW, hiResH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = smooth.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(card, 0, 0, null);
        g2.dispose();
        float[] kernel = {0f,-1f,0f, -1f,5f,-1f, 0f,-1f,0f};
        ConvolveOp op = new ConvolveOp(new Kernel(3,3,kernel), ConvolveOp.EDGE_NO_OP, null);
        return op.filter(smooth, null);
    }

}
