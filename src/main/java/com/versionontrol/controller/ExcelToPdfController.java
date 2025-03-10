package com.versionontrol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.versionontrol.service.ExcelToPdfService;

@RestController
public class ExcelToPdfController {

    @Autowired
    private ExcelToPdfService excelToPdfService;

    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertExcelToPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "outputDirectory", defaultValue = "C:\\Users\\hp\\OneDrive\\Desktop\\MCT\\File_version_control") String outputDirectory) throws IOException {
        try {
            byte[] pdfBytes = excelToPdfService.convertExcelToPdf(file, outputDirectory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "converted.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (DocumentException | IOException e) {
            throw new IOException("Failed to convert Excel to PDF: " + e.getMessage());
        }
    }
}
