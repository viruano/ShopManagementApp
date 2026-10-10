package com.autorepair.shop;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    // =========================================================
    // ⚙️ ENCAPSULATED REPAIR SHOP VARIABLE VALUES INJECTIONS
    // =========================================================
    @Value("${shop.config.demo-mode:false}")
    private boolean isDemoMode;

    @Value("${shop.config.live.name:Main Street Auto Repair}")
    private String liveName;

    @Value("${shop.config.live.address:123 Main St, Annapolis, MD}")
    private String liveAddress;

    @Value("${shop.config.live.phone:(555) 123-4567}")
    private String livePhone;

    @Value("${shop.config.live.tax-rate:0.06}")
    private double liveTaxRate;

    @Value("${shop.config.demo.name:Demo Shop Management}")
    private String demoName;

    @Value("${shop.config.demo.address:456 Workshop Way, Cloud City}")
    private String demoAddress;

    @Value("${shop.config.demo.phone:(555) 987-6543}")
    private String demoPhone;

    @Value("${shop.config.demo.tax-rate:0.06}")
    private double demoTaxRate;

    // ⚡ ENVIRONMENT SWITCH VALUE SOLVER ENGINE METHOD
    private String getShopDetail(String propertyKey) {
        if (isDemoMode) {
            switch (propertyKey) {
                case "name": return demoName;
                case "address": return demoAddress;
                case "phone": return demoPhone;
                default: return "";
            }
        } else {
            switch (propertyKey) {
                case "name": return liveName;
                case "address": return liveAddress;
                case "phone": return livePhone;
                default: return "";
            }
        }
    }

    private BigDecimal getActiveTaxRate() {
        return BigDecimal.valueOf(isDemoMode ? demoTaxRate : liveTaxRate);
    }

    public ByteArrayInputStream generateInvoicePdf(WorkOrder order) {
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font Layout Design Definitions
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            Font fontMono = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.BLACK);

            // 1. TOP HEADER BRANDING BLOCK (DYNAMIC PROPERTY EXTRACTIONS)
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidths(new float[]{60, 40});
            headerTable.setWidthPercentage(100);

            PdfPCell shopCell = new PdfPCell();
            shopCell.setBorder(Rectangle.NO_BORDER);
            // ⚡ LOOKS UP AND PLACES ENVIRONMENT SPECIFIC DETAILS AUTOMATICALLY
            shopCell.addElement(new Paragraph(getShopDetail("name"), titleFont));
            shopCell.addElement(new Paragraph(getShopDetail("address"), regularFont));
            shopCell.addElement(new Paragraph(getShopDetail("phone"), regularFont));
            headerTable.addCell(shopCell);

            PdfPCell invMetaCell = new PdfPCell();
            invMetaCell.setBorder(Rectangle.NO_BORDER);
            invMetaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph invNum = new Paragraph("INVOICE / REPAIR ORDER", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.GRAY));
            invNum.setAlignment(Element.ALIGN_RIGHT);
            invMetaCell.addElement(invNum);

            Paragraph invDetails = new Paragraph("Order: " + order.getInvoiceNumber() + "\nDate: " +
                    order.getDateOpened().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), boldFont);
            invDetails.setAlignment(Element.ALIGN_RIGHT);
            invMetaCell.addElement(invDetails);
            headerTable.addCell(invMetaCell);

            document.add(headerTable);
            document.add(new Paragraph("\n"));

            // 2. CLIENT & VEHICLE CHASSIS CONFIGURATION CARDS
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);

            Customer client = order.getVehicle().getCustomer();
            PdfPCell clientCell = new PdfPCell();
            clientCell.setBorder(Rectangle.BOX);
            clientCell.setPadding(8);
            clientCell.setBackgroundColor(new BaseColor(245, 247, 250));
            clientCell.addElement(new Paragraph("CUSTOMER PROFILE:", boldFont));
            clientCell.addElement(new Paragraph(client.getFirstName() + " " + client.getLastName(), regularFont));
            clientCell.addElement(new Paragraph(client.getPhone(), fontMono));
            clientCell.addElement(new Paragraph(client.getStreet() + ", " + client.getCity() + " " + client.getState(), regularFont));
            infoTable.addCell(clientCell);

            Vehicle vh = order.getVehicle();
            PdfPCell vehicleCell = new PdfPCell();
            vehicleCell.setBorder(Rectangle.BOX);
            vehicleCell.setPadding(8);
            vehicleCell.setBackgroundColor(new BaseColor(245, 247, 250));
            vehicleCell.addElement(new Paragraph("VEHICLE REPAIR TICKET:", boldFont));
            vehicleCell.addElement(new Paragraph(vh.getYear() + " " + vh.getMake() + " " + vh.getModel(), regularFont));
            vehicleCell.addElement(new Paragraph("Plate: " + vh.getLicensePlate() + " | VIN: " + vh.getVin(), fontMono));
            vehicleCell.addElement(new Paragraph("Odometer In: " + order.getOdometerIn() + " mi | Out: " + order.getOdometerOut() + " mi", fontMono));
            infoTable.addCell(vehicleCell);

            document.add(infoTable);

            // 3. ITEMIZED PARTS & LABOR MATRIX TABLE
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{15, 45, 20, 20});

            String[] headers = {"Type", "Description / Job Action", "Qty/Hrs × Price", "Total due"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, headerFont));
                cell.setBackgroundColor(new BaseColor(79, 70, 229)); // Slate Indigo Blue Header
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(cell);
            }

            for (LineItem item : order.getLineItems()) {
                table.addCell(new PdfPCell(new Paragraph(item.getItemType().toString(), fontMono)));
                table.addCell(new PdfPCell(new Paragraph(item.getDescription(), regularFont)));
                table.addCell(new PdfPCell(new Paragraph(item.getQuantity() + " × $" + item.getRetailPrice(), fontMono)));

                PdfPCell lineTot = new PdfPCell(new Paragraph("$" + item.getLineTotal(), fontMono));
                lineTot.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(lineTot);
            }
            document.add(table);

            // 4. BALANCES SUMMARY ACCUMULATOR FOOTER
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{60, 40});
            footerTable.setSpacingBefore(15);

            PdfPCell disclaimerCell = new PdfPCell(new Paragraph("Thank you for choosing us for your automotive maintenance needs. All repairs carry a 12-Month / 12,000-Mile warranty on parts and flat-rate labor actions.", regularFont));
            disclaimerCell.setBorder(Rectangle.NO_BORDER);
            footerTable.addCell(disclaimerCell);

            // Dynamic calculation blocks using the injected environment values
            BigDecimal partsSub = order.getPartsSubtotal();
            BigDecimal laborSub = order.getLaborSubtotal();
            BigDecimal subTotal = partsSub.add(laborSub);
            BigDecimal taxRateValue = getActiveTaxRate();
            BigDecimal taxBilled = subTotal.multiply(taxRateValue).setScale(2, RoundingMode.HALF_UP);
            BigDecimal grandTotal = subTotal.add(taxBilled);
            BigDecimal remainingBal = grandTotal.subtract(order.getAmountPaid());

            PdfPCell calcCell = new PdfPCell();
            calcCell.setBorder(Rectangle.NO_BORDER);

            PdfPTable calcInner = new PdfPTable(2);
            calcInner.setWidthPercentage(100);

            BigDecimal taxDisplayPercent = taxRateValue.multiply(BigDecimal.valueOf(100)).setScale(2);

            addCalcRow(calcInner, "Parts Subtotal:", "$" + partsSub, fontMono, Element.ALIGN_RIGHT);
            addCalcRow(calcInner, "Labor Subtotal:", "$" + laborSub, fontMono, Element.ALIGN_RIGHT);
            addCalcRow(calcInner, "Sales Tax (" + taxDisplayPercent + "%):", "$" + taxBilled, fontMono, Element.ALIGN_RIGHT);
            addCalcRow(calcInner, "Grand Total Billed:", "$" + grandTotal, boldFont, Element.ALIGN_RIGHT);
            addCalcRow(calcInner, "Payments Posted:", "-$" + order.getAmountPaid(), fontMono, Element.ALIGN_RIGHT);
            addCalcRow(calcInner, "Balance Outstanding:", "$" + remainingBal, boldFont, Element.ALIGN_RIGHT);

            calcCell.addElement(calcInner);
            footerTable.addCell(calcCell);

            footerTable.addCell(calcCell);
            document.add(footerTable);
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addCalcRow(PdfPTable table, String label, String value, Font font, int align) {
        PdfPCell lCell = new PdfPCell(new Paragraph(label, font));
        lCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Paragraph(value, font));
        vCell.setBorder(Rectangle.NO_BORDER);
        vCell.setHorizontalAlignment(align);
        table.addCell(vCell);
    }
}
