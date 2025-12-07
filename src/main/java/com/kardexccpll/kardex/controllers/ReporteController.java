package com.kardexccpll.kardex.controllers;

import com.kardexccpll.kardex.model.Articulo;
import com.kardexccpll.kardex.repository.ArticuloRepository;
import com.kardexccpll.kardex.service.ReporteExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
public class ReporteController {

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private ReporteExcelService reporteExcelService;

    @GetMapping("/reportes/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel() {
        try {
            List<Articulo> articulos = articuloRepository.findByActivoTrue();
            byte[] excelBytes = reporteExcelService.generarReporteArticulos(articulos);

            String filename = "reporte_inventario_" + new Date().getTime() + ".xlsx";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(excelBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}