package com.versionontrol.service;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Service
public class ExcelToPdfService {

    

 
    public byte[] convertExcelToPdf(MultipartFile file, String outputDirectory) throws IOException, DocumentException {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
    
        // Set page size to A2 landscape with proper margins
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();
    
        int columnCount = getMaxColumnCount(sheet);
        PdfPTable pdfTable = new PdfPTable(columnCount);
        pdfTable.setWidthPercentage(100);
    
        // Dynamically calculate column widths based on sheet
        float[] columnWidths = getColumnWidths(sheet, columnCount);
        pdfTable.setWidths(columnWidths);
    
      //  Font font = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL);
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
    
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                float rowHeight = row.getHeightInPoints(); // Get row height from Excel
                for (int colIndex = 0; colIndex < columnCount; colIndex++) {
                    String cellValue = "";
                    int colspan = 1;
    
                    Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cellValue = getCellValueAsString(cell);
                    
                    // Check if the cell is part of a merged region
                    for (CellRangeAddress region : mergedRegions) {
                        if (region.isInRange(rowIndex, colIndex)) {
                            colspan = region.getLastColumn() - region.getFirstColumn() + 1;
                            colIndex = region.getLastColumn(); // Skip merged columns
                            break;
                        }
                    }
                    BaseColor backgroundColor = getCellBackgroundColor(cell, workbook);
                    BaseColor textColor = BaseColor.BLACK; // Default text color
                    int fontStyle = Font.NORMAL;

                    if (backgroundColor != null) {
                        float brightness = (0.299f * backgroundColor.getRed() + 0.587f * backgroundColor.getGreen() + 0.114f * backgroundColor.getBlue()) / 255;
                        if (brightness < 0.5) {
                            textColor = BaseColor.WHITE; // Set text to white if background is dark
                        }
                    }

                   
                    CellStyle cellStyle = cell.getCellStyle();
                    if (cellStyle != null) {
                        org.apache.poi.ss.usermodel.Font excelFont = workbook.getFontAt(cellStyle.getFontIndex());
                    
                        if (excelFont.getBold()) {
                            fontStyle = Font.BOLD; // Apply bold if the font is bold
                        }
                       
                    }
                    Font adjustedFont = new Font(Font.FontFamily.HELVETICA,  (fontStyle == Font.BOLD ? 8 : 6), fontStyle, textColor);
                    
                    PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue,adjustedFont));
                    pdfCell.setPadding(2);
                   // pdfCell.setMinimumHeight(20);
                    pdfCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    pdfCell.setColspan(colspan);
                    pdfCell.setMinimumHeight(rowHeight);

                        // Apply background color
                        if (backgroundColor != null) {
                            pdfCell.setBackgroundColor(backgroundColor);
                        }
                   // pdfCell.setFixedHeight(25);
                  pdfCell.setNoWrap(false);
                    pdfTable.addCell(pdfCell);
                }
            }
        }
    
        document.add(pdfTable);//
        extractImages(workbook, document); // Add images if any
        document.close();
        workbook.close();
    
        savePdfToDirectory(baos, outputDirectory, "output.pdf");
        return baos.toByteArray();
    }
    
    private int getMaxColumnCount(Sheet sheet) {
        int maxColumns = 0;
        for (Row row : sheet) {
            if (row != null) {
                maxColumns = Math.max(maxColumns, row.getLastCellNum());
            }
        }
        return maxColumns;
    }
    
    private float[] getColumnWidths(Sheet sheet, int columnCount) {
        float[] columnWidths = new float[columnCount];
        float totalWidth = PageSize.A4.getWidth() - 60; // Adjust for margins
        float baseWidth = totalWidth / columnCount;
        for (int i = 0; i < columnCount; i++) {
            columnWidths[i] = Math.max(sheet.getColumnWidth(i) / 256f * 2f, baseWidth);
        }
        return columnWidths;
    }

    private String getCellValueAsString(Cell cell) {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (cell.getCellStyle().getDataFormatString().contains("%")) {
                    return decimalFormat.format(cell.getNumericCellValue() * 100) + "%";
                }
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    return dateFormat.format(cell.getDateCellValue());
                } else {
                    double numericValue = cell.getNumericCellValue();
                    return numericValue % 1 == 0 ? String.valueOf((long) numericValue) : decimalFormat.format(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private void savePdfToDirectory(ByteArrayOutputStream baos, String outputDirectory, String fileName) throws IOException {
        Path dirPath = Paths.get(outputDirectory);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        try (FileOutputStream fos = new FileOutputStream(dirPath.resolve(fileName).toFile())) {
            baos.writeTo(fos);
        }
    }
    

    private BaseColor getCellBackgroundColor(Cell cell, Workbook workbook) {
        if (cell == null) return null;
        CellStyle cellStyle = cell.getCellStyle();
        
        if (workbook instanceof XSSFWorkbook) {
            XSSFColor xssfColor = ((XSSFCellStyle) cellStyle).getFillForegroundColorColor();
            if (xssfColor != null && xssfColor.getRGB() != null) {
                byte[] rgb = xssfColor.getRGB();
                return new BaseColor(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF); // Convert bytes to unsigned int
            }
        } else if (workbook instanceof HSSFWorkbook hSSFWorkbook) {
            HSSFPalette palette = hSSFWorkbook.getCustomPalette();
            org.apache.poi.hssf.util.HSSFColor hssfColor = palette.getColor(cellStyle.getFillForegroundColor());
            if (hssfColor != null) {
                short[] rgb = hssfColor.getTriplet();
                return new BaseColor(rgb[0], rgb[1], rgb[2]);
            }
        }
        return null;
    }
    

    private void extractImages(Workbook workbook, Document document) throws IOException, DocumentException {
        if (workbook instanceof XSSFWorkbook xSSFWorkbook) {
            for (XSSFPictureData pictureData : xSSFWorkbook.getAllPictures()) {
                Image image = Image.getInstance(pictureData.getData());
                document.add(image);
            }
        } else if (workbook instanceof HSSFWorkbook hSSFWorkbook) {
            List<HSSFShape> shapes = hSSFWorkbook.getSheetAt(0).getDrawingPatriarch().getChildren();
            for (HSSFShape shape : shapes) {
                if (shape instanceof HSSFPicture pic) {
                    Image image = Image.getInstance(pic.getPictureData().getData());
                    document.add(image);
                }
            }
        }
        
    }
    
}