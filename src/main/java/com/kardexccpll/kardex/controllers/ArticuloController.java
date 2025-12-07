package com.kardexccpll.kardex.controllers;

import com.kardexccpll.kardex.dto.ArticuloDTO;
import com.kardexccpll.kardex.model.Articulo;
import com.kardexccpll.kardex.repository.ArticuloRepository;
import com.kardexccpll.kardex.service.CodigoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 
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
@RequestMapping("/articulos")
public class ArticuloController {

    private static final Logger logger = LoggerFactory.getLogger(ArticuloController.class);

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private CodigoService codigoService;

    /**
     * Muestra la lista de artículos activos (SOLO LECTURA)
     */
    @GetMapping
    public String listaArticulos(Model model) {
        try {
            logger.info("Cargando lista de artículos activos");

         
            Preconditions.checkNotNull(articuloRepository, "ArticuloRepository no puede ser nulo");

            List<Articulo> articulosActivos = articuloRepository.findByActivoTrue();
            List<ArticuloDTO> articulos = articulosActivos.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

         
            Validate.notNull(articulos, "La lista de artículos no puede ser nula");

            model.addAttribute("articulos", articulos);
            logger.info("Se cargaron {} artículos", articulos.size()); //
        } catch (Exception e) {
            logger.error("Error al cargar los artículos: {}", e.getMessage(), e); // 
            model.addAttribute("error", "Error al cargar los artículos: " + e.getMessage());
        }
        return "list";
    }

    /**
     * Muestra formulario para registrar nuevo artículo
     */
    @GetMapping("/registrarArticulo")
    public String mostrarFormularioRegistro(Model model) {
        try {
            logger.debug("Mostrando formulario de registro de artículo"); 

            
            Preconditions.checkNotNull(codigoService, "CodigoService no puede ser nulo");

            ArticuloDTO articuloDTO = new ArticuloDTO();
            String codigoAutomatico = codigoService.generarCodigoArticulo();

           
            if (Strings.isNullOrEmpty(codigoAutomatico)) {
                throw new IllegalStateException("No se pudo generar el código automático");
            }

            articuloDTO.setCodigo(codigoAutomatico);
            model.addAttribute("articulo", articuloDTO);
            logger.info("Código generado automáticamente: {}", codigoAutomatico);
        } catch (Exception e) {
            logger.error("Error al generar código: {}", e.getMessage(), e); 
            model.addAttribute("error", "Error al generar código: " + e.getMessage());
        }
        return "registrarArticulo";
    }

    /**
     * Procesa el registro de nuevo artículo
     */
    @PostMapping("/registrarArticulo")
    public String crearArticulo(@ModelAttribute ArticuloDTO articuloDTO,
                                RedirectAttributes redirectAttributes) {
        try {
       
            Preconditions.checkNotNull(articuloDTO, "El DTO de artículo no puede ser nulo");

            
            String descripcion = StringUtils.defaultString(articuloDTO.getDescripcion(), "Sin descripción");
            logger.info("Intentando registrar nuevo artículo: {}", descripcion); 

            // Validaciones básicas con GUAVA
            if (Strings.isNullOrEmpty(articuloDTO.getDescripcion())) {
                logger.warn("Intento de registro sin descripción"); 
                redirectAttributes.addFlashAttribute("error", "La descripción es obligatoria");
                return "redirect:/articulos/registrarArticulo";
            }

            if (Strings.isNullOrEmpty(articuloDTO.getUnidad())) {
                logger.warn("Intento de registro sin unidad: {}", articuloDTO.getDescripcion());
                redirectAttributes.addFlashAttribute("error", "La unidad es obligatoria");
                return "redirect:/articulos/registrarArticulo";
            }

            // Convertir DTO a Entity
            Articulo articulo = convertToEntity(articuloDTO);
            articulo.calcularInventarioFinal();

            // Guardar en base de datos
            Articulo articuloGuardado = articuloRepository.save(articulo);

            logger.info("Artículo registrado exitosamente: {} - {}",
                    articuloGuardado.getCodigo(), articuloGuardado.getDescripcion()); 

            redirectAttributes.addFlashAttribute("success",
                    " Artículo " + articuloGuardado.getCodigo() + " - " +
                            articuloGuardado.getDescripcion() + " registrado correctamente");

        } catch (Exception e) {
            String descripcion = articuloDTO != null ? StringUtils.defaultString(articuloDTO.getDescripcion(), "N/A") : "N/A";
            logger.error("Error al registrar el artículo {}: {}", descripcion, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al registrar el artículo: " + e.getMessage());
        }

        return "redirect:/articulos";
    }

    /**
     * Muestra página para gestionar artículos (editar/eliminar)
     */
    @GetMapping("/gestionar")
    public String gestionarArticulos(Model model) {
        try {
            logger.info("Cargando página de gestión de artículos");

            List<Articulo> articulosActivos = articuloRepository.findByActivoTrue();
            List<ArticuloDTO> articulos = articulosActivos.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            model.addAttribute("articulos", articulos);
            logger.info("Cargados {} artículos para gestión", articulos.size()); 
        } catch (Exception e) {
            logger.error("Error al cargar los artículos para gestión: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los artículos: " + e.getMessage());
        }
        return "gestionarArticulos";
    }

    /**
     * Muestra formulario para editar artículo
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        try {
            logger.info("Cargando formulario de edición para artículo ID: {}", id); 

           
            Preconditions.checkArgument(id != null && id > 0, "El ID del artículo debe ser válido");

            Optional<Articulo> articuloOpt = articuloRepository.findById(id);
            if (articuloOpt.isPresent() && articuloOpt.get().getActivo()) {
                ArticuloDTO articuloDTO = convertToDTO(articuloOpt.get());
                model.addAttribute("articulo", articuloDTO);
                logger.info("Artículo cargado para edición: {} - {}",
                        articuloDTO.getCodigo(), articuloDTO.getDescripcion()); 
            } else {
                logger.warn("Artículo no encontrado o inactivo con ID: {}", id);
                model.addAttribute("error", "Artículo no encontrado");
                return "redirect:/articulos/gestionar";
            }
        } catch (Exception e) {
            logger.error("Error al cargar el artículo con ID {}: {}", id, e.getMessage(), e); 
            model.addAttribute("error", "Error al cargar el artículo: " + e.getMessage());
            return "redirect:/articulos/gestionar";
        }
        return "editarArticulo";
    }

    /**
     * Procesa la actualización de artículo
     */
    @PostMapping("/editar/{id}")
    public String actualizarArticulo(@PathVariable Integer id,
                                     @ModelAttribute ArticuloDTO articuloDTO,
                                     RedirectAttributes redirectAttributes) {
        try {
            logger.info("Intentando actualizar artículo con ID: {}", id); 

            // ✅ GUAVA - Validaciones
            Preconditions.checkArgument(id != null && id > 0, "El ID del artículo debe ser válido");
            Preconditions.checkNotNull(articuloDTO, "El DTO de artículo no puede ser nulo");

            Optional<Articulo> articuloExistente = articuloRepository.findById(id);
            if (!articuloExistente.isPresent() || !articuloExistente.get().getActivo()) {
                logger.warn("Intento de actualizar artículo no encontrado o inactivo con ID: {}", id);
                redirectAttributes.addFlashAttribute("error", "Artículo no encontrado");
                return "redirect:/articulos/gestionar";
            }

            // Convertir y actualizar
            Articulo articulo = convertToEntity(articuloDTO);
            articulo.setId(id);
            articulo.setActivo(true); // Mantener activo
            articulo.calcularInventarioFinal();

            Articulo articuloActualizado = articuloRepository.save(articulo);

            logger.info("Artículo actualizado exitosamente: {} - {}",
                    articuloActualizado.getCodigo(), articuloActualizado.getDescripcion());

            redirectAttributes.addFlashAttribute("success",
                    "✅ Artículo " + articuloActualizado.getCodigo() + " actualizado correctamente");

        } catch (Exception e) {
            logger.error("Error al actualizar el artículo con ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al actualizar el artículo: " + e.getMessage());
        }

        return "redirect:/articulos/gestionar";
    }

    /**
     * Elimina un artículo (eliminación lógica)
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarArticulo(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Intentando eliminar artículo con ID: {}", id); 

           
            Preconditions.checkArgument(id != null && id > 0, "El ID del artículo debe ser válido");

            Optional<Articulo> articuloOpt = articuloRepository.findById(id);
            if (articuloOpt.isPresent()) {
                Articulo articulo = articuloOpt.get();
                String codigoArticulo = articulo.getCodigo();
                articulo.setActivo(false); // Eliminación lógica
                articuloRepository.save(articulo);

                logger.info("Artículo eliminado exitosamente: {}", codigoArticulo);

                redirectAttributes.addFlashAttribute("success",
                        "✅ Artículo " + codigoArticulo + " eliminado correctamente");
            } else {
                logger.warn("Intento de eliminar artículo no encontrado con ID: {}", id);
                redirectAttributes.addFlashAttribute("error", "Artículo no encontrado");
            }
        } catch (Exception e) {
            logger.error("Error al eliminar artículo con ID {}: {}", id, e.getMessage(), e); 
            redirectAttributes.addFlashAttribute("error",
                    "❌ Error al eliminar el artículo: " + e.getMessage());
        }
        return "redirect:/articulos/gestionar";
    }

    // Métodos de conversión
    private ArticuloDTO convertToDTO(Articulo articulo) {
      
        Preconditions.checkNotNull(articulo, "El artículo no puede ser nulo");

        ArticuloDTO dto = new ArticuloDTO();
        dto.setId(articulo.getId());

        dto.setCodigo(StringUtils.defaultString(articulo.getCodigo(), "N/A"));
        dto.setDescripcion(StringUtils.defaultString(articulo.getDescripcion(), "Sin descripción"));
        dto.setUnidad(StringUtils.defaultString(articulo.getUnidad(), "N/A"));

        dto.setPrecio(articulo.getPrecio());
        dto.setInventarioInicial(articulo.getInventarioInicial());
        dto.setEntradas(articulo.getEntradas());
        dto.setSalidas(articulo.getSalidas());
        dto.setAjuste(articulo.getAjuste());
        dto.setInventarioFinal(articulo.getInventarioFinal());
        dto.setCreatedAt(articulo.getCreatedAt());
        return dto;
    }

    private Articulo convertToEntity(ArticuloDTO dto) {
   
        Preconditions.checkNotNull(dto, "El DTO no puede ser nulo");

        Articulo articulo = new Articulo();
        articulo.setId(dto.getId());


        articulo.setCodigo(StringUtils.defaultString(dto.getCodigo(), "TEMP"));
        articulo.setDescripcion(StringUtils.defaultString(dto.getDescripcion(), "Sin descripción"));
        articulo.setUnidad(StringUtils.defaultString(dto.getUnidad(), "UNIDAD"));

        articulo.setPrecio(dto.getPrecio());
        articulo.setInventarioInicial(dto.getInventarioInicial() != null ? dto.getInventarioInicial() : 0);
        articulo.setEntradas(dto.getEntradas() != null ? dto.getEntradas() : 0);
        articulo.setSalidas(dto.getSalidas() != null ? dto.getSalidas() : 0);
        articulo.setAjuste(dto.getAjuste() != null ? dto.getAjuste() : 0);
        articulo.setActivo(true);
        return articulo;
    }
}