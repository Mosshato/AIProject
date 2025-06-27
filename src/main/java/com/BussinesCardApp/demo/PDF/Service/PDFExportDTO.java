package com.BussinesCardApp.demo.PDF.Service;

import java.time.Instant;

public class PDFExportDTO {
    private final String id;
    private final Instant generatedAt;
    private final String filePath;

    public PDFExportDTO(String id, Instant generatedAt, String filePath) {
        this.id          = id;
        this.generatedAt = generatedAt;
        this.filePath    = filePath;
    }

    public String getId()         { return id;          }
    public Instant getGeneratedAt(){ return generatedAt; }
    public String getFilePath()   { return filePath;    }
}
