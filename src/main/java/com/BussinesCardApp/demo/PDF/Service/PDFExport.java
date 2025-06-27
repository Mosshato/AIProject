// src/main/java/com/BussinesCardApp/demo/pdf/PdfExportEvent.java
package com.BussinesCardApp.demo.PDF.Service;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "pdfExports")
public class PDFExport {
    @Id
    private String id;

    private String userId;
    private String email;
    private Instant generatedAt;
    private String filePath;   // calea completă pe disc

    public PDFExport(String userId, String email, Instant generatedAt, String filePath) {
        this.userId      = userId;
        this.email       = email;
        this.generatedAt = generatedAt;
        this.filePath    = filePath;
    }

    // getters (no setters necesare, e write-once)
    public String getId()            { return id;          }
    public String getUserId()        { return userId;      }
    public String getEmail()         { return email;       }
    public Instant getGeneratedAt()  { return generatedAt; }
    public String getFilePath()      { return filePath;    }
}
