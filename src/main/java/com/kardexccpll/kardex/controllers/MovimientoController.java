package com.kardexccpll.kardex.controllers;

import com.kardexccpll.kardex.dto.MovimientoHistorialDTO;
import com.kardexccpll.kardex.model.Articulo;
import com.kardexccpll.kardex.model.MovimientoHistorial;
import com.kardexccpll.kardex.repository.ArticuloRepository;
import com.kardexccpll.kardex.repository.MovimientoHistorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// ✅ NUEVOS IMPORTS PARA LIBRERÍAS
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/movimientos")
public class MovimientoController {

    private static final Logger logger = LoggerFactory.getLogger(MovimientoController.class); // ✅ LOGBACK

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private MovimientoHistorialRepository movimientoHistorialRepository;

    /**
     * Muestra la página principal de movimientos
     */
    @GetMapping
    public String mostrarMovimientos(Model model) {
        try {
            logger.info("Cargando página principal de movimientos"); // ✅ LOGBACK

            // ✅ GUAVA - Validación de dependencias
            Preconditions.checkNotNull(articuloRepository, "ArticuloRepository no puede ser nulo");
            Preconditions.checkNotNull(movimientoHistorialRepository, "MovimientoHistorialRepository no puede ser nulo");

            // Solo artículos activos
            List<Articulo> articulos = articuloRepository.findByActivoTrue();

            // ✅ COMMONS - Validación de lista
            Validate.notNull(articulos, "La lista de artículos no puede ser nula");

            model.addAttribute("articulos", articulos);
            model.addAttribute("movimiento", new MovimientoDTO());

            // Historial reciente (últimos 10 movimientos)
            List<MovimientoHistorialDTO> historialReciente = movimientoHistorialRepository.findAllByOrderByFechaMovimientoDesc()
                    .stream()
                    .limit(10)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            model.addAttribute("historialReciente", historialReciente);

            logger.info("Página de movimientos cargada exitosamente. Artículos: {}, Movimientos recientes: {}",
                    articulos.size(), historialReciente.size()); // ✅ LOGBACK

        } catch (Exception e) {
            logger.error("Error al cargar los datos de movimientos: {}", e.getMessage(), e); // ✅ LOGBACK
            model.addAttribute("error", "Error al cargar los datos: " + e.getMessage());
        }
        return "movimientos";
    }

    /**
     * Registra un nuevo movimiento
     */
    @PostMapping("/registrar")
    public String registrarMovimiento(@ModelAttribute MovimientoDTO movimientoDTO,
                                      RedirectAttributes redirectAttributes) {
        try {
            logger.info("Intentando registrar movimiento - Artículo: {}, Tipo: {}, Cantidad: {}",
                    movimientoDTO.getArticuloId(), movimientoDTO.getTipoMovimiento(), movimientoDTO.getCantidad()); // ✅ LOGBACK

            // ✅ MEJORA DE SEGURIDAD - Validación robusta con GUAVA y COMMONS
            Preconditions.checkNotNull(movimientoDTO, "El DTO de movimiento no puede ser nulo");

            if (movimientoDTO.getArticuloId() == null) {
                logger.warn("Intento de movimiento sin artículo ID"); // ✅ LOGBACK
                redirectAttributes.addFlashAttribute("error", "❌ Debe seleccionar un artículo");
                return "redirect:/movimientos";
            }

            // ✅ COMMONS - Validación de tipo de movimiento
            if (StringUtils.isBlank(movimientoDTO.getTipoMovimiento()) ||
                    (!movimientoDTO.getTipoMovimiento().equals("ENTRADA") &&
                            !movimientoDTO.getTipoMovimiento().equals("SALIDA"))) {
                logger.warn("Intento de movimiento con tipo inválido: {}", movimientoDTO.getTipoMovimiento()); // ✅ LOGBACK
                redirectAttributes.addFlashAttribute("error", "❌ Tipo de movimiento inválido");
                return "redirect:/movimientos";
            }

            // ✅ GUAVA - Validación de cantidad
            if (movimientoDTO.getCantidad() == null || movimientoDTO.getCantidad() <= 0) {
                logger.warn("Intento de movimiento con cantidad inválida: {}", movimientoDTO.getCantidad()); // ✅ LOGBACK
                redirectAttributes.addFlashAttribute("error", "❌ La cantidad debe ser mayor a 0");
                return "redirect:/movimientos";
            }

            Optional<Articulo> articuloOpt = articuloRepository.findById(movimientoDTO.getArticuloId());

            if (articuloOpt.isPresent()) {
                Articulo articulo = articuloOpt.get();
                int stockAnterior = articulo.getInventarioFinal();

                logger.debug("Artículo encontrado: {} - {}, Stock actual: {}",
                        articulo.getCodigo(), articulo.getDescripcion(), stockAnterior); // ✅ LOGBACK

                // Validar stock para salidas
                if ("SALIDA".equals(movimientoDTO.getTipoMovimiento())) {
                    if (articulo.getInventarioFinal() < movimientoDTO.getCantidad()) {
                        logger.warn("Stock insuficiente para salida. Artículo: {}, Stock actual: {}, Cantidad solicitada: {}",
                                articulo.getCodigo(), articulo.getInventarioFinal(), movimientoDTO.getCantidad()); // ✅ LOGBACK
                        redirectAttributes.addFlashAttribute("error",
                                "❌ Stock insuficiente. Stock actual: " + articulo.getInventarioFinal());
                        return "redirect:/movimientos";
                    }
                }

                // Actualizar artículo
                if ("ENTRADA".equals(movimientoDTO.getTipoMovimiento())) {
                    articulo.setEntradas(articulo.getEntradas() + movimientoDTO.getCantidad());
                    logger.debug("Registrando entrada de {} unidades para artículo {}",
                            movimientoDTO.getCantidad(), articulo.getCodigo()); // ✅ LOGBACK
                } else if ("SALIDA".equals(movimientoDTO.getTipoMovimiento())) {
                    articulo.setSalidas(articulo.getSalidas() + movimientoDTO.getCantidad());
                    logger.debug("Registrando salida de {} unidades para artículo {}",
                            movimientoDTO.getCantidad(), articulo.getCodigo()); // ✅ LOGBACK
                }

                // Recalcular inventario final
                articulo.calcularInventarioFinal();
                articuloRepository.save(articulo);

                // ✅ COMMONS - Sanitización del motivo
                String motivoSanitizado = StringUtils.defaultString(movimientoDTO.getMotivo(), "Movimiento registrado");

                // Registrar en historial
                MovimientoHistorial historial = new MovimientoHistorial(
                        articulo,
                        movimientoDTO.getTipoMovimiento(),
                        movimientoDTO.getCantidad(),
                        motivoSanitizado,
                        stockAnterior,
                        articulo.getInventarioFinal()
                );
                movimientoHistorialRepository.save(historial);

                logger.info("Movimiento registrado exitosamente. Artículo: {}, Tipo: {}, Cantidad: {}, Stock nuevo: {}",
                        articulo.getCodigo(), movimientoDTO.getTipoMovimiento(),
                        movimientoDTO.getCantidad(), articulo.getInventarioFinal()); // ✅ LOGBACK

                redirectAttributes.addFlashAttribute("success",
                        "✅ Movimiento registrado correctamente. Stock actual: " + articulo.getInventarioFinal());

            } else {
                logger.error("Artículo no encontrado con ID: {}", movimientoDTO.getArticuloId()); // ✅ LOGBACK
                redirectAttributes.addFlashAttribute("error", "❌ Artículo no encontrado");
            }

        } catch (Exception e) {
            logger.error("Error al registrar movimiento: {}", e.getMessage(), e); // ✅ LOGBACK
            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al registrar movimiento: " + e.getMessage());
        }

        return "redirect:/movimientos";
    }

    /**
     * Muestra el historial completo de movimientos
     */
    @GetMapping("/historial")
    public String mostrarHistorialCompleto(Model model) {
        try {
            logger.info("Cargando historial completo de movimientos"); // ✅ LOGBACK

            List<MovimientoHistorialDTO> historial = movimientoHistorialRepository.findAllByOrderByFechaMovimientoDesc()
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // ✅ COMMONS - Validación
            Validate.notNull(historial, "El historial no puede ser nulo");

            model.addAttribute("historial", historial);

            logger.info("Historial completo cargado: {} movimientos", historial.size()); // ✅ LOGBACK

        } catch (Exception e) {
            logger.error("Error al cargar el historial completo: {}", e.getMessage(), e); // ✅ LOGBACK
            model.addAttribute("error", "❌ Error al cargar el historial: " + e.getMessage());
        }
        return "historialMovimientos";
    }

    /**
     * Muestra el historial de un artículo específico
     */
    @GetMapping("/historial/articulo/{id}")
    public String mostrarHistorialArticulo(@PathVariable Integer id, Model model) {
        try {
            logger.info("Cargando historial para artículo ID: {}", id); // ✅ LOGBACK

            // ✅ GUAVA - Validación de ID
            Preconditions.checkArgument(id != null && id > 0, "El ID del artículo debe ser válido");

            Optional<Articulo> articuloOpt = articuloRepository.findById(id);
            if (articuloOpt.isPresent()) {
                Articulo articulo = articuloOpt.get();
                List<MovimientoHistorialDTO> historial = movimientoHistorialRepository.findByArticuloIdOrderByFechaMovimientoDesc(id)
                        .stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());

                model.addAttribute("articulo", articulo);
                model.addAttribute("historial", historial);

                logger.info("Historial cargado para artículo {}: {} movimientos",
                        articulo.getCodigo(), historial.size()); // ✅ LOGBACK

                return "historialArticulo";
            } else {
                logger.warn("Artículo no encontrado con ID: {}", id); // ✅ LOGBACK
                model.addAttribute("error", "❌ Artículo no encontrado");
                return "redirect:/movimientos";
            }
        } catch (Exception e) {
            logger.error("Error al cargar el historial del artículo {}: {}", id, e.getMessage(), e); // ✅ LOGBACK
            model.addAttribute("error", "❌ Error al cargar el historial: " + e.getMessage());
            return "redirect:/movimientos";
        }
    }

    /**
     * Convierte MovimientoHistorial a MovimientoHistorialDTO
     */
    private MovimientoHistorialDTO convertToDTO(MovimientoHistorial historial) {
        // ✅ GUAVA - Validación de entrada
        Preconditions.checkNotNull(historial, "El historial no puede ser nulo");
        Preconditions.checkNotNull(historial.getArticulo(), "El artículo del historial no puede ser nulo");

        MovimientoHistorialDTO dto = new MovimientoHistorialDTO();
        dto.setId(historial.getId());

        // ✅ COMMONS - Manejo seguro de nulos
        dto.setCodigoArticulo(StringUtils.defaultString(historial.getArticulo().getCodigo(), "N/A"));
        dto.setDescripcionArticulo(StringUtils.defaultString(historial.getArticulo().getDescripcion(), "Sin descripción"));
        dto.setTipoMovimiento(StringUtils.defaultString(historial.getTipoMovimiento(), "DESCONOCIDO"));

        dto.setCantidad(historial.getCantidad());

        // ✅ COMMONS - Sanitización del motivo
        dto.setMotivo(StringUtils.defaultString(historial.getMotivo(), "Sin motivo especificado"));

        dto.setStockAnterior(historial.getStockAnterior());
        dto.setStockNuevo(historial.getStockNuevo());
        dto.setFechaMovimiento(historial.getFechaMovimiento());

        // Agregar el ID del artículo para los enlaces
        dto.setArticuloId(historial.getArticulo().getId());

        return dto;
    }

    /**
     * Clase DTO interna para movimientos
     */
    public static class MovimientoDTO {
        private Integer articuloId;
        private String tipoMovimiento;
        private Integer cantidad;
        private String motivo;

        // Getters y Setters
        public Integer getArticuloId() {
            return articuloId;
        }
        public void setArticuloId(Integer articuloId) {
            this.articuloId = articuloId;
        }

        public String getTipoMovimiento() {
            return tipoMovimiento;
        }
        public void setTipoMovimiento(String tipoMovimiento) {
            this.tipoMovimiento = tipoMovimiento;
        }

        public Integer getCantidad() {
            return cantidad;
        }
        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public String getMotivo() {
            return motivo;
        }
        public void setMotivo(String motivo) {
            this.motivo = motivo;
        }
    }
}