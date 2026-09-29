package com.autorepair.shop;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class InvoicePdfService {

    public byte[] generateInvoicePdf(Vehicle vehicle, List<LineItem> lineItems) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // --- 1. Font Definitions ---
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.DEFAULTSIZE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.DEFAULTSIZE);
            Font normalBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.DEFAULTSIZE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.DEFAULTSIZE);

            // --- 2. Brand Header Block ---
            Paragraph shopHeader = new Paragraph("⚙️ AUTOMANAGE REPAIR WORKSTATION", titleFont);
            shopHeader.setAlignment(Element.ALIGN_LEFT);
            document.add(shopHeader);

            Paragraph shopDetails = new Paragraph("123 Workshop Lane, Repair City, ST 12345 | Phone: (555) 0199\n\n", normalFont);
            document.add(shopDetails);

            // --- 3. Customer & Vehicle Metadata Split Grid Tables ---
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(20);

            // Left Side: Client Data Profile
            Customer c = vehicle.getCustomer();
            String clientText = "CUSTOMER DETAILS:\n" +
                    (c != null ? c.getFirstName() + " " + c.getLastName() + "\n" + c.getPhone() + "\n" + c.getEmail() : "Walk-in Client");
            PdfPCell clientCell = new PdfPCell(new Paragraph(clientText, normalFont));
            clientCell.setBorder(Rectangle.NO_BORDER);
            metaTable.addCell(clientCell);

            // Right Side: Vehicle Spec Files
            String vehicleText = "VEHICLE FILE WORK ORDER:\n" +
                    "Year/Make/Model: " + vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel() + "\n" +
                    "License Plate: " + vehicle.getLicensePlate() + "\n" +
                    "VIN ID: " + vehicle.getVin();
            PdfPCell vehicleCell = new PdfPCell(new Paragraph(vehicleText, normalFont));
            vehicleCell.setBorder(Rectangle.NO_BORDER);
            metaTable.addCell(vehicleCell);

            document.add(metaTable);

            // --- 4. Main Billing Line Items Table Grid ---
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 4f, 2f, 2f});
            table.setSpacingAfter(20);

            // Table Headers
            String[] headers = {"Type", "Description / Job Action", "Qty / Hrs", "Line Total"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, headerFont));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                cell.setPadding(6);
                table.addCell(cell);
            }

            BigDecimal grandTotal = BigDecimal.ZERO;

            // Populate Row Loops
            for (LineItem item : lineItems) {
                table.addCell(new PdfPCell(new Paragraph(item.getItemType().toString(), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(item.getDescription(), normalFont)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(item.getQuantity()), normalFont)));

                BigDecimal itemTotal = item.getLineTotal();
                grandTotal = grandTotal.add(itemTotal);
                table.addCell(new PdfPCell(new Paragraph("$" + itemTotal.toString(), normalFont)));
            }
            document.add(table);

            // --- 5. Financial Invoice Summary Cards Equivalents ---
            Paragraph summaryBlock = new Paragraph();
            summaryBlock.setAlignment(Element.ALIGN_RIGHT);
            summaryBlock.add(new Chunk("GRAND TOTAL DUE: $" + grandTotal.toString() + "\n", titleFont));
            summaryBlock.add(new Chunk("Thank you for choosing AutoManage Engine! All work guaranteed for 12 mos / 12,000 mi.", normalFont));
            document.add(summaryBlock);

            document.close();
        } catch (Exception e) {
            System.err.println("Document Compiling Failure Exception: " + e.getMessage());
        }

        return out.toByteArray();
    }
}
