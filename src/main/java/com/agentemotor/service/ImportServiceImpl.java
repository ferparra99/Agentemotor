package com.agentemotor.service;

import com.agentemotor.dto.ImportResultDTO;
import com.agentemotor.dto.PolicyRequestDTO;
import com.agentemotor.model.PolicyType;
import com.agentemotor.utils.AppConstants;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private final PolicyService policyService;

    @Override
    public ImportResultDTO importClients(MultipartFile file, Long advisorId) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            log.warn("Intento de importación con archivo sin nombre");
            return ImportResultDTO.builder()
                    .totalProcesados(0).totalErrores(1).totalCreados(0)
                    .errores(List.of(AppConstants.ERROR_IMPORT_EMPTY))
                    .build();
        }

        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        log.info("Iniciando importación desde archivo: {} (formato: {})", filename, ext);

        try {
            List<ImportRow> rows;
            if (AppConstants.IMPORT_FORMAT_XLSX.equals(ext)) {
                rows = parseExcel(file.getInputStream());
            } else if (AppConstants.IMPORT_FORMAT_XML.equals(ext)) {
                rows = parseXml(file.getInputStream());
            } else {
                log.warn("Formato de archivo no soportado: {}", ext);
                return ImportResultDTO.builder()
                        .totalProcesados(0).totalErrores(1).totalCreados(0)
                        .errores(List.of(AppConstants.ERROR_IMPORT_UNSUPPORTED))
                        .build();
            }

            if (rows.isEmpty()) {
                log.warn("El archivo {} no contiene datos válidos", filename);
                return ImportResultDTO.builder()
                        .totalProcesados(0).totalErrores(1).totalCreados(0)
                        .errores(List.of(AppConstants.ERROR_IMPORT_EMPTY))
                        .build();
            }

            log.info("Archivo {} parseado correctamente: {} registros encontrados", filename, rows.size());

            int procesados = 0, creados = 0;
            List<String> errores = new ArrayList<>();

            for (int i = 0; i < rows.size(); i++) {
                ImportRow row = rows.get(i);
                try {
                    validateRow(row, i + 1);
                    PolicyRequestDTO request = PolicyRequestDTO.builder()
                            .clientName(row.nombre)
                            .clientPhone(row.telefono != null ? row.telefono : AppConstants.DEFAULT_PHONE)
                            .clientEmail(row.email)
                            .clientNotes(row.notas)
                            .policyNumber(row.numeroPoliza)
                            .type(row.tipo)
                            .insurer(row.aseguradora)
                            .startDate(row.fechaInicio)
                            .expirationDate(row.fechaVencimiento)
                            .build();
                    policyService.createPolicy(request);
                    procesados++;
                    creados++;
                    log.debug("Fila {} importada: póliza {} para cliente {}", i + 1, row.numeroPoliza, row.nombre);
                } catch (Exception e) {
                    errores.add(String.format(AppConstants.ERROR_IMPORT_INVALID_ROW, i + 1, e.getMessage()));
                    procesados++;
                    log.warn("Error en fila {}: {} - {}", i + 1, row.nombre, e.getMessage());
                }
            }

            ImportResultDTO result = ImportResultDTO.builder()
                    .totalProcesados(procesados)
                    .totalCreados(creados)
                    .totalErrores(errores.size())
                    .errores(errores)
                    .build();

            log.info("Importación completada: {} procesados, {} creados, {} errores (archivo: {})",
                    result.getTotalProcesados(), result.getTotalCreados(), result.getTotalErrores(), filename);

            return result;

        } catch (Exception e) {
            log.error("Error al leer el archivo {}: {}", filename, e.getMessage(), e);
            return ImportResultDTO.builder()
                    .totalProcesados(0).totalErrores(1).totalCreados(0)
                    .errores(List.of(AppConstants.ERROR_IMPORT_FILE_READ + e.getMessage()))
                    .build();
        }
    }

    private void validateRow(ImportRow row, int rowNum) {
        List<String> missing = new ArrayList<>();
        if (row.nombre == null || row.nombre.isBlank()) missing.add(AppConstants.FIELD_NOMBRE);
        if (row.numeroPoliza == null || row.numeroPoliza.isBlank()) missing.add(AppConstants.FIELD_NUMERO_POLIZA);
        if (row.tipo == null || row.tipo.isBlank()) missing.add(AppConstants.FIELD_TIPO);
        if (row.aseguradora == null || row.aseguradora.isBlank()) missing.add(AppConstants.FIELD_ASEGURADORA);
        if (row.fechaInicio == null) missing.add(AppConstants.FIELD_FECHA_INICIO);
        if (row.fechaVencimiento == null) missing.add(AppConstants.FIELD_FECHA_VENCIMIENTO);
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(AppConstants.ERROR_IMPORT_MISSING_FIELDS + String.join(", ", missing));
        }
        try {
            PolicyType.valueOf(row.tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(AppConstants.ERROR_IMPORT_INVALID_TYPE_PREFIX + row.tipo);
        }
    }

    private List<ImportRow> parseExcel(InputStream is) throws Exception {
        List<ImportRow> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) return rows;

            Map<String, Integer> colMap = buildExcelColumnMap(sheet.getRow(0));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row excelRow = sheet.getRow(i);
                if (excelRow == null) continue;

                ImportRow row = new ImportRow();
                row.nombre = getCellStringValueSafe(excelRow, colMap, "nombre");
                row.telefono = getCellStringValueSafe(excelRow, colMap, "telefono", "teléfono", "tel", "phone");
                row.email = getCellStringValueSafe(excelRow, colMap, "email", "e-mail", "correo");
                row.notas = getCellStringValueSafe(excelRow, colMap, "notas", "nota", "observaciones", "notes");
                row.numeroPoliza = getCellStringValueSafe(excelRow, colMap, "numero", "número",
                        "numero poliza", "número póliza", "nro poliza", "num poliza",
                        "poliza", "póliza", "policy number", "nro_poliza");
                row.tipo = getCellStringValueSafe(excelRow, colMap, "tipo", "type");
                row.aseguradora = getCellStringValueSafe(excelRow, colMap, "aseguradora", "aseguradora", "insurer", "company");
                row.fechaInicio = getCellDateValueSafe(excelRow, colMap, "fecha inicio", "fecha de inicio", "fecha_inicio",
                        "start date", "inicio", "startdate", "fecha inicial");
                row.fechaVencimiento = getCellDateValueSafe(excelRow, colMap, "fecha vencimiento", "fecha de vencimiento",
                        "fecha_vencimiento", "vencimiento", "expiration date", "expirationdate",
                        "end date", "fecha fin");

                if (row.nombre != null && !row.nombre.isBlank()) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private Map<String, Integer> buildExcelColumnMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String val = getCellStringValue(headerRow.getCell(i));
            if (val != null && !val.isBlank()) {
                String normalized = normalizeColumnName(val);
                map.put(normalized, i);
            }
        }
        return map;
    }

    private String normalizeColumnName(String name) {
        String result = name.trim();
        result = result.replaceAll("([a-z])([A-Z])", "$1 $2");
        result = result.replaceAll("([A-Z])([A-Z][a-z])", "$1 $2");
        result = java.text.Normalizer.normalize(result, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[\\s_-]+", " ")
                .trim();
        return result;
    }

    private String getCellStringValueSafe(Row excelRow, Map<String, Integer> colMap, String... possibleNames) {
        Integer colIdx = findColumnIndex(colMap, possibleNames);
        if (colIdx == null) return null;
        Cell cell = excelRow.getCell(colIdx);
        return getCellStringValue(cell);
    }

    private LocalDate getCellDateValueSafe(Row excelRow, Map<String, Integer> colMap, String... possibleNames) {
        Integer colIdx = findColumnIndex(colMap, possibleNames);
        if (colIdx == null) return null;
        Cell cell = excelRow.getCell(colIdx);
        if (cell == null) return null;

        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        return parseDate(getCellStringValue(cell));
    }

    private Integer findColumnIndex(Map<String, Integer> colMap, String... possibleNames) {
        for (String name : possibleNames) {
            String normalized = normalizeColumnName(name);
            Integer idx = colMap.get(normalized);
            if (idx != null) return idx;
        }
        return null;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf((int) cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        yield cell.getStringCellValue();
                    } catch (Exception e2) {
                        yield cell.toString();
                    }
                }
            }
            default -> null;
        };
    }

    private List<ImportRow> parseXml(InputStream is) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        ImportXmlWrapper wrapper = xmlMapper.readValue(is, ImportXmlWrapper.class);
        if (wrapper == null || wrapper.clientes == null) return List.of();

        List<ImportRow> rows = new ArrayList<>();
        for (ImportXmlCliente xmlCliente : wrapper.clientes) {
            if (xmlCliente.polizas == null || xmlCliente.polizas.isEmpty()) {
                ImportRow row = new ImportRow();
                row.nombre = xmlCliente.nombre;
                row.telefono = xmlCliente.telefono;
                row.email = xmlCliente.email;
                row.notas = xmlCliente.notas;
                rows.add(row);
            } else {
                for (ImportXmlPoliza poliza : xmlCliente.polizas) {
                    ImportRow row = new ImportRow();
                    row.nombre = xmlCliente.nombre;
                    row.telefono = xmlCliente.telefono;
                    row.email = xmlCliente.email;
                    row.notas = xmlCliente.notas;
                    row.numeroPoliza = poliza.numero;
                    row.tipo = poliza.tipo;
                    row.aseguradora = poliza.aseguradora;
                    row.fechaInicio = poliza.fechaInicio;
                    row.fechaVencimiento = poliza.fechaVencimiento;
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        DateTimeFormatter[] formats = {
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyyMMdd")
        };
        for (DateTimeFormatter fmt : formats) {
            try {
                return LocalDate.parse(value.trim(), fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static class ImportRow {
        String nombre;
        String telefono;
        String email;
        String notas;
        String numeroPoliza;
        String tipo;
        String aseguradora;
        LocalDate fechaInicio;
        LocalDate fechaVencimiento;
    }

    @lombok.Getter @lombok.Setter
    private static class ImportXmlWrapper {
        private List<ImportXmlCliente> clientes;
    }

    @lombok.Getter @lombok.Setter
    private static class ImportXmlCliente {
        private String nombre;
        private String telefono;
        private String email;
        private String notas;
        private List<ImportXmlPoliza> polizas;
    }

    @lombok.Getter @lombok.Setter
    private static class ImportXmlPoliza {
        private String numero;
        private String tipo;
        private String aseguradora;
        private LocalDate fechaInicio;
        private LocalDate fechaVencimiento;
    }
}
