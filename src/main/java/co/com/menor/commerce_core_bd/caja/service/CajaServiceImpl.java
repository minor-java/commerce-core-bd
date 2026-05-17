package co.com.menor.commerce_core_bd.caja.service;

import co.com.menor.commerce_core_bd.caja.mapper.CajaMapper;
import co.com.menor.commerce_core_bd.caja.model.Caja;
import co.com.menor.commerce_core_bd.caja.repository.CajaRepository;
import co.com.menor.commerce_core_bd.usuario.service.UsuarioService;
import co.com.menor.comun_dto.caja.request.AbrirCajaRequest;
import co.com.menor.comun_dto.caja.request.CerrarCajaRequest;
import co.com.menor.comun_dto.caja.request.FiltroCajaRequest;
import co.com.menor.comun_dto.caja.response.CajaResponse;
import co.com.menor.comun_dto.utils.CajaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;
    private final CajaMapper cajaMapper;
    private final UsuarioService usuarioService;

    @Override
    @Transactional
    public CajaResponse abrirCaja(AbrirCajaRequest req) {        
        
        Caja caja = cajaMapper.toEntityApertura(req);
        Caja guardada = cajaRepository.save(caja);
        
        log.info(
            "abrirCaja: caja id={} usuario={}", 
            guardada.getId(), 
            req.getUsuarioId()
        );

        return cajaMapper.toResponse(guardada);
    }

    @Override
    @Transactional
    public CajaResponse cerrarCaja(CerrarCajaRequest req) {

        Caja caja = cajaRepository.findById(req.getCajaId()).get();
            
        caja.setEstado(req.getEstado());
        caja.setFechaCierre(req.getFechaCierre());
        caja.setMontoCierreReal(req.getMontoCierreReal());
        caja.setSaldoEsperado(req.getSaldoEsperado());
        caja.setDiferencia(req.getDiferencia());
        caja.setTotalIngresos(req.getTotalIngresos());
        caja.setTotalEgresos(req.getTotalEgresos());

        Caja guardada = cajaRepository.save(caja);

        log.info(
            "cerrarCaja: caja id={} saldoEsperado={} diferencia={}",
            guardada.getId(), 
            guardada.getSaldoEsperado(), 
            guardada.getDiferencia()
        );

        return cajaMapper.toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponse obtenerPorCajaId(Long id) {
        
        Caja caja = cajaRepository.findById(id)
        .orElse(null);

        return cajaMapper.toResponse(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existeCaja(Long usuarioId) {

        return cajaRepository.existsByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponse obtenerPorUsuarioIdPorEstado(Long usuarioId) {

        return cajaRepository.findByUsuarioIdAndEstado(
            usuarioId,
            CajaConstants.ESTADO_ABIERTA
        ).map(cajaMapper::toResponse).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean tieneCajaActiva(Long usuarioId) {
        return cajaRepository.findByUsuarioIdAndEstado(usuarioId, CajaConstants.ESTADO_ABIERTA).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CajaResponse> buscarPaginado(FiltroCajaRequest filtro) {
        PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
        Specification<Caja> spec = buildSpec(filtro);
        Page<Caja> page = cajaRepository.findAll(spec, pageable);
        Map<Long, String> usuarioCache = new HashMap<>();
        return page.map(caja -> {
            String usuario = resolverNombreUsuario(caja.getUsuarioId(), usuarioCache);
            return cajaMapper.toPaginadoResponse(caja, usuario);
        });
    }

    private String resolverNombreUsuario(Long usuarioId, Map<Long, String> cache) {
        if (usuarioId == null) return null;
        if (cache.containsKey(usuarioId)) return cache.get(usuarioId);
        String nombre = null;
        try {
            nombre = usuarioService.findById(usuarioId)
                .map(u -> u.getUsuario())
                .orElse(null);
        } catch (Exception e) {
            log.warn("No se pudo obtener usuario id={}", usuarioId);
        }
        cache.put(usuarioId, nombre);
        return nombre;
    }

    private Specification<Caja> buildSpec(FiltroCajaRequest filtro) {
        return (root, query, cb) -> {

            java.util.List<javax.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (filtro.getEstado() != null && !filtro.getEstado().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("estado"), filtro.getEstado().trim().toUpperCase()));
            }

            if (filtro.getUsuarioId() != null) {
                predicates.add(cb.equal(root.get("usuarioId"), filtro.getUsuarioId()));
            }

            if (filtro.getFechaAperturaDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaApertura"), filtro.getFechaAperturaDesde().atStartOfDay()));
            }

            if (filtro.getFechaAperturaHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaApertura"), filtro.getFechaAperturaHasta().atTime(23, 59, 59)));
            }

            if (filtro.getFechaCierreDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCierre"), filtro.getFechaCierreDesde().atStartOfDay()));
            }

            if (filtro.getFechaCierreHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCierre"), filtro.getFechaCierreHasta().atTime(23, 59, 59)));
            }

            if (filtro.getConDiferencia() != null) {
                if (filtro.getConDiferencia()) {
                    predicates.add(cb.notEqual(root.get("diferencia"), java.math.BigDecimal.ZERO));
                } else {
                    predicates.add(cb.equal(root.get("diferencia"), java.math.BigDecimal.ZERO));
                }
            }

            return cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }
}
