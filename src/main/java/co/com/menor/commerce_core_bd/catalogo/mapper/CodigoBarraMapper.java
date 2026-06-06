package co.com.menor.commerce_core_bd.catalogo.mapper;

import org.springframework.stereotype.Component;

import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.comun_dto.codigo_barras.request.CreateCondigoBarrasRequest;

@Component
public class CodigoBarraMapper {

    public CodigoBarra toEntity(CreateCondigoBarrasRequest req) {
        
        if (req == null) {
            return null;
        }

        CodigoBarra codigoBarra = new CodigoBarra();
        codigoBarra.setId(null);
        codigoBarra.setCodigo(req.getCodigo());
        codigoBarra.setTipo(req.getTipo());
        codigoBarra.setFechaCreacion(req.getFechaCreacion());
        codigoBarra.setUsuarioId(req.getUsuarioId());
        codigoBarra.setProductoId(req.getProductoId());
        codigoBarra.setComboId(req.getComboId());

        return codigoBarra;
    }
    
}
