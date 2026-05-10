package co.com.menor.commerce_core_bd.caja.service;

import co.com.menor.commerce_core_bd.caja.mapper.CajaMapper;
import co.com.menor.commerce_core_bd.caja.model.Caja;
import co.com.menor.commerce_core_bd.caja.repository.CajaRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;
    private final CajaMapper cajaMapper;

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
    public CajaResponse obtenerPorUsuarioId(Long usuarioId) {

        Caja caja = cajaRepository.findByUsuarioIdAndEstado(
            usuarioId,
            "ABIERTA"
        ).orElse(null);

        return cajaMapper.toResponse(caja);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CajaResponse> buscarPaginado(FiltroCajaRequest filtro) {
        PageRequest pageable = PageRequest.of(filtro.getPage(), filtro.getSize());
        Specification<Caja> spec = buildSpec(filtro);
        return cajaRepository.findAll(spec, pageable).map(cajaMapper::toResponse);
    }

    // friltro paginado
    private Specification<Caja> buildSpec(FiltroCajaRequest filtro) {
        return (root, query, cb) -> {

            java.util.List<javax.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
           
            if (filtro.getEstado() != null && !filtro.getEstado().trim().isEmpty()) {
                predicates.add(
                    cb.equal(
                        root.get("estado"), 
                        filtro.getEstado().trim().toUpperCase()
                    )
                );
            }

            if (filtro.getCreadoPor() != null) {
                predicates.add(
                    cb.equal(
                        root.get("usuarioId"),
                        filtro.getCreadoPor()
                    )
                );
            }

            if (filtro.getFechaDesde() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("fechaApertura"), 
                        filtro.getFechaDesde()
                    )
                );
            }

            if (filtro.getFechaHasta() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(
                        root.get("fechaApertura"), 
                        filtro.getFechaHasta()
                    )
                );
            }

            return cb.and(
                predicates.toArray(new javax.persistence.criteria.Predicate[0])
            );
        };
    }
}
