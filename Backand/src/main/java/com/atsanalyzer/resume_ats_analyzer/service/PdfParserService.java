package com.atsanalyzer.resume_ats_analyzer.service;
 import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;  
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;

@Service
public class PdfParserService {
    
    public String extractText(MultipartFile file) throws IOException, TikaException, SAXException {
        BodyContentHandler handler = new BodyContentHandler(-1); // No limit
        Metadata metadata = new Metadata();
        PDFParser pdfParser = new PDFParser();
        ParseContext parseContext = new ParseContext();
        
        try (InputStream inputStream = file.getInputStream()) {
            pdfParser.parse(inputStream, handler, metadata, parseContext);
            
            // Clean and normalize text
            String text = handler.toString();
            text = text.replaceAll("\\s+", " ").trim();
            text = text.replaceAll("[^\\x20-\\x7E\\n\\r\\t]", " "); // Remove non-printable chars
            
            return text;
        }
    }
    
    public String getFileInfo(MultipartFile file) {
        return String.format("File: %s, Size: %d bytes, Type: %s", 
            file.getOriginalFilename(), file.getSize(), file.getContentType());
    }
}