package com.BussinesCardApp.demo.PDF.Controller;

import com.BussinesCardApp.demo.PDF.Service.PDFExport;
import com.BussinesCardApp.demo.PDF.Service.PDFExportDTO;
import com.BussinesCardApp.demo.PDF.Service.PDFRepository;
import com.BussinesCardApp.demo.user.appuser.AppUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pdf/history")
public class PDFHistoryController {

    private final PDFRepository pdfRepo;

    public PDFHistoryController(PDFRepository pdfRepo) {
        this.pdfRepo = pdfRepo;
    }

    /**
     * Returnează lista tuturor PDF-urilor generate de utilizatorul logat.
     * Necesită JWT -> @AuthenticationPrincipal va fi populat automat.
     */
    @GetMapping
    public List<PDFExportDTO> getHistory(@AuthenticationPrincipal AppUser user) {
        if (user == null) {
            // ar trebui să fie oricum 401 din Spring Security, dar de siguranță:
            throw new RuntimeException("Unauthorized");
        }

        List<PDFExport> exports = pdfRepo.findByUserIdOrderByGeneratedAtDesc(user.getId());

        return exports.stream()
                .map(e -> new PDFExportDTO(
                        e.getId(),
                        e.getGeneratedAt(),
                        e.getFilePath()))
                .collect(Collectors.toList());
    }
}
