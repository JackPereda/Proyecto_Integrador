package com.kardexccpll.kardex.service;

import com.kardexccpll.kardex.model.Articulo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils; 
import org.apache.commons.lang3.Validate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReporteExcelService {

    public byte[] generarReporteArticulos(List<Articulo> articulos) throws IOException {
        
        Validate.notNull(articulos, "La lista de artículos no puede ser nula");

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Inventario Artículos");

            // Crear headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Código", "Descripción", "Unidad", "Precio", "Inv. Final", "Estado"};

            for (int i = 0; i < headers.length; i++) {
               
                String header = StringUtils.defaultString(headers[i], "Columna " + (i + 1));
                headerRow.createCell(i).setCellValue(header);
            }

            // Llenar datos
            int rowNum = 1;
            for (Articulo articulo : articulos) {
                Row row = sheet.createRow(rowNum++);

               
                row.createCell(0).setCellValue(StringUtils.defaultString(articulo.getCodigo(), "N/A"));
                row.createCell(1).setCellValue(StringUtils.defaultString(articulo.getDescripcion(), "Sin descripción"));
                row.createCell(2).setCellValue(StringUtils.defaultString(articulo.getUnidad(), "N/A"));

                if (articulo.getPrecio() != null) {
                    row.createCell(3).setCellValue(articulo.getPrecio().doubleValue());
                } else {
                    row.createCell(3).setCellValue(0.0);
                }

                row.createCell(4).setCellValue(articulo.getInventarioFinal());

                String estado = articulo.getInventarioFinal() == 0 ? "SIN STOCK" :
                        articulo.getInventarioFinal() <= 10 ? "STOCK BAJO" : "EN STOCK";
                row.createCell(5).setCellValue(estado);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}