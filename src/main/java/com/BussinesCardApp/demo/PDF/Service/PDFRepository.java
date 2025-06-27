package com.BussinesCardApp.demo.PDF.Service;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PDFRepository extends MongoRepository<PDFExport, String> {
    // Dacă mai vrei metode custom, le poți declara aici
}
