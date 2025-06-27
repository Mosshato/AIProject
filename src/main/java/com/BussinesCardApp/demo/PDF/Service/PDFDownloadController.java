package com.BussinesCardApp.demo.PDF.Service;

import com.BussinesCardApp.demo.PDF.Service.PDFExport;
import com.BussinesCardApp.demo.PDF.Service.PDFRepository;
import com.BussinesCardApp.demo.user.appuser.AppUser;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/pdf")
public class PDFDownloadController {

    private final PDFRepository pdfRepo;

    public PDFDownloadController(PDFRepository pdfRepo) {
        this.pdfRepo = pdfRepo;
    }

    /** Download-ul fișierului salvat, doar dacă aparține user-ului logat */
    @GetMapping("/download/{id}")
    public void download(@PathVariable String id,
                         @AuthenticationPrincipal AppUser user,
                         HttpServletResponse response) throws Exception {

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // 1) găsim documentul în Mongo
        PDFExport exp = pdfRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 2) verificăm proprietatea
        if (!user.getId().equals(exp.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        // 3) fișierul pe disc
        Path file = Paths.get(exp.getFilePath());
        if (!Files.exists(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File missing on server");
        }

        // 4) trimitem binarul
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + file.getFileName() + '"');
        Files.copy(file, response.getOutputStream());
        response.flushBuffer();
    }
}
