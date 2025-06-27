package com.BussinesCardApp.demo.PDF.Service;

import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

@Service
public class BusinessCardMaker {

    BufferedImage createFaceImage(BusinessCardDTO cardDetails) throws Exception {
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
        String text1 = cardDetails.getScientificTitle() + " ";
        String text2 = cardDetails.getFirstName() + " " + cardDetails.getLastName();
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

        String text3 = cardDetails.getAcademicPosition();
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
                {
                        "Office: " + cardDetails.getOfficePhone(),
                        "Mobile: " + cardDetails.getMobilePhone()
                },
                {
                        "E-mail: " + cardDetails.getEmail(),
                        "Web: " + cardDetails.getWebsite()
                }
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

    BufferedImage createBackImage() throws Exception {
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
}
