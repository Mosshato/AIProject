package com.BussinesCardApp.demo.PDF.Service;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PDFRepository extends MongoRepository<PDFExport, String> {
    List<PDFExport> findByUserIdOrderByGeneratedAtDesc(String userId);
    // Dacă mai vrei metode custom, le poți declara aici
}
