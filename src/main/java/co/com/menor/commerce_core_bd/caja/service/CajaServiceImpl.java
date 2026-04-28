package co.com.menor.commerce_core_bd.caja.service;

import co.com.menor.commerce_core_bd.caja.mapper.CajaMapper;
import co.com.menor.commerce_core_bd.caja.model.Caja;
import co.com.menor.commerce_core_bd.caja.repository.CajaRepository;
import co.com.menor.commerce_core_bd.caja.repository.MovimientoCajaRepository;
import co.com.menor.commerce_core_bd.shared.exception.MinorExcepcion;
import co.com.menor.comun_dto.caja.request.AbrirCajaRequest;
import co.com.menor.comun_dto.caja.request.CerrarCajaRequest;
import co.com.menor.comun_dto.caja.request.FiltroCajaRequest;
import co.com.menor.comun_dto.caja.response.CajaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final CajaMapper cajaMapper;

    @Override
    @Transactional
    public CajaResponse abrirCaja(AbrirCajaRequest req) {
        if (req == null) {
            throw new MinorExcepcion("REQUEST_NULO", "El cuerpo de la solicitud es obligatorio");
        }
        if (req.getMontoInicial() == null || req.getCreadoPor() == null) {
            throw new MinorExcepcion("CAMPOS_OBLIGATORIOS_FALTANTES",
                    "Los campos montoInicial y creadoPor son obligatorios");
        }
        if (req.getMontoInicial().compareTo(BigDecimal.ZERO) < 0) {
            throw new MinorExcepcion("MONTO_INVALIDO", "El monto inicial no puede ser negativo");
        }

        cajaRepository.findByCreadoPorAndEstado(req.getCreadoPor(), "ABIERTA")
                .ifPresent(c -> {
                    throw new MinorExcepcion("CAJA_YA_ABIERTA",
                            "El usuario " + req.getCreadoPor() + " ya tiene una caja abierta (id=" + c.getId() + ")");
                });

        Caja caja = cajaMapper.toEntityApertura(req);
        Caja guardada = cajaRepository.save(caja);
        log.info("abrirCaja: caja id={} usuario={}", guardada.getId(), req.getCreadoPor());
        return cajaMapper.toResponse(guardada);
    }

    @Override
    @Transactional
    public CajaResponse cerrarCaja(CerrarCajaRequest req) {
        if (req == null) {
            throw new MinorExcepcion("REQUEST_NULO", "El cuerpo de la solicitud es obligatorio");
        }
        if (req.getCajaId() == null || req.getMontoCierreReal() == null || req.getCreadoPor() == null) {
            throw new MinorExcepcion("CAMPOS_OBLIGATORIOS_FALTANTES",
                    "Los campos cajaId, montoCierreReal y creadoPor son obligatorios");
        }

        Caja caja = cajaRepository.findById(req.getCajaId())
                .orElseThrow(() -> new MinorExcepcion("CAJA_NO_ENCONTRADA",
                        "No existe una caja con id: " + req.getCajaId()));

        if (!"ABIERTA".equals(caja.getEstado())) {
            throw new MinorExcepcion("CAJA_YA_CERRADA", "La caja id=" + req.getCajaId() + " ya está cerrada");
        }
        if (!caja.getCreadoPor().equals(req.getCreadoPor())) {
            throw new MinorExcepcion("CAJA_NO_PERTENECE",
                    "La caja no pertenece al usuario " + req.getCreadoPor());
        }

        BigDecimal ingresoEfectivo = movimientoCajaRepository
                .sumMontoByCajaIdAndTipoAndMetodoPago(caja.getId(), "INGRESO", "EFECTIVO");
        BigDecimal egresoEfectivo = movimientoCajaRepository
                .sumMontoByCajaIdAndTipoAndMetodoPago(caja.getId(), "EGRESO", "EFECTIVO");
        BigDecimal totalIngresos = movimientoCajaRepository
                .sumMontoByCajaIdAndTipo(caja.getId(), "INGRESO");
        BigDecimal totalEgresos = movimientoCajaRepository
                .sumMontoByCajaIdAndTipo(caja.getId(), "EGRESO");

        BigDecimal saldoEsperado = caja.getMontoInicial().add(ingresoEfectivo).subtract(egresoEfectivo);
        BigDecimal diferencia = req.getMontoCierreReal().subtract(saldoEsperado);

        caja.setEstado("CERRADA");
        caja.setFechaCierre(LocalDateTime.now());
        caja.setMontoCierreReal(req.getMontoCierreReal());
        caja.setSaldoEsperado(saldoEsperado);
        caja.setDiferencia(diferencia);
        caja.setTotalIngresos(totalIngresos);
        caja.setTotalEgresos(totalEgresos);

        Caja guardada = cajaRepository.save(caja);
        log.info("cerrarCaja: caja id={} saldoEsperado={} diferencia={}", guardada.getId(), saldoEsperado, diferencia);
        return cajaMapper.toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponse obtenerPorId(Long id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new MinorExcepcion("CAJA_NO_ENCONTRADA",
                        "No existe una caja con id: " + id));
        return cajaMapper.toResponse(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponse obtenerCajaAbierta(Long usuarioId) {
        Caja caja = cajaRepository.findByCreadoPorAndEstado(usuarioId, "ABIERTA")
                .orElseThrow(() -> new MinorExcepcion("CAJA_NO_ABIERTA",
                        "El usuario " + usuarioId + " no tiene una caja abierta"));
        return cajaMapper.toResponse(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CajaResponse> buscarPaginado(FiltroCajaRequest filtro) {
        PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
        Specification<Caja> spec = buildSpec(filtro);
        return cajaRepository.findAll(spec, pageable).map(cajaMapper::toResponse);
    }

    private Specification<Caja> buildSpec(FiltroCajaRequest filtro) {
        return (root, query, cb) -> {
            java.util.List<javax.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (filtro.getEstado() != null && !filtro.getEstado().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado().trim().toUpperCase()));
            }
            if (filtro.getCreadoPor() != null) {
                predicates.add(cb.equal(root.get("creadoPor"), filtro.getCreadoPor()));
            }
            if (filtro.getFechaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaApertura"), filtro.getFechaDesde()));
            }
            if (filtro.getFechaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaApertura"), filtro.getFechaHasta()));
            }
            return cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }
}
