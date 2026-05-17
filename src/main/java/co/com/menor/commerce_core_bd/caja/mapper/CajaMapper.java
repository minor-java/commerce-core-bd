package co.com.menor.commerce_core_bd.caja.mapper;

import co.com.menor.commerce_core_bd.caja.model.Caja;
import co.com.menor.commerce_core_bd.caja.model.MovimientoCaja;
import co.com.menor.comun_dto.caja.request.AbrirCajaRequest;
import co.com.menor.comun_dto.caja.response.CajaResponse;
import co.com.menor.comun_dto.caja.response.MovimientoCajaResponse;
import co.com.menor.comun_dto.utils.CajaConstants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CajaMapper {

    public Caja toEntityApertura(AbrirCajaRequest req) {
        
        if (req == null) return null;
        
        Caja caja = new Caja();
        
        caja.setMontoInicial(req.getMontoInicial());
        caja.setTotalIngresos(BigDecimal.ZERO);
        caja.setTotalEgresos(BigDecimal.ZERO);
        caja.setEstado(CajaConstants.ESTADO_ABIERTA);
        caja.setFechaApertura(LocalDateTime.now());
        caja.setUsuarioId(req.getUsuarioId());

        return caja;
    }

    public CajaResponse toResponse(Caja caja) {
        if (caja == null) return null;
        return CajaResponse.builder()
            .id(caja.getId())
            .montoInicial(caja.getMontoInicial())
            .totalIngresos(caja.getTotalIngresos())
            .totalEgresos(caja.getTotalEgresos())
            .saldoEsperado(caja.getSaldoEsperado())
            .montoCierreReal(caja.getMontoCierreReal())
            .diferencia(caja.getDiferencia())
            .estado(caja.getEstado())
            .fechaApertura(caja.getFechaApertura())
            .fechaCierre(caja.getFechaCierre())
            .usuarioId(caja.getUsuarioId())
        .build();
    }

    public CajaResponse toPaginadoResponse(Caja caja, String usuario) {
        if (caja == null) return null;
        boolean conDiferencia = caja.getDiferencia() != null
            && caja.getDiferencia().compareTo(BigDecimal.ZERO) != 0;
        return CajaResponse.builder()
            .id(caja.getId())
            .montoInicial(caja.getMontoInicial())
            .totalIngresos(caja.getTotalIngresos())
            .totalEgresos(caja.getTotalEgresos())
            .saldoEsperado(caja.getSaldoEsperado())
            .montoCierreReal(caja.getMontoCierreReal())
            .diferencia(caja.getDiferencia())
            .estado(caja.getEstado())
            .fechaApertura(caja.getFechaApertura())
            .fechaCierre(caja.getFechaCierre())
            .usuarioId(caja.getUsuarioId())
            .usuario(usuario)
            .conDiferencia(conDiferencia)
        .build();
    }

    public MovimientoCajaResponse toMovimientoResponse(MovimientoCaja movimiento) {
        if (movimiento == null) return null;
        return MovimientoCajaResponse.builder()
            .id(movimiento.getId())
            .cajaId(movimiento.getCajaId())
            .tipo(movimiento.getTipo())
            .metodoPago(movimiento.getMetodoPago())
            .monto(movimiento.getMonto())
            .referenciaTipo(movimiento.getReferenciaTipo())
            .referenciaId(movimiento.getReferenciaId())
            .observacion(movimiento.getObservacion())
            .fechaCreacion(movimiento.getFechaCreacion())
            .usuarioId(movimiento.getUsuarioId())
        .build();
    }
}
