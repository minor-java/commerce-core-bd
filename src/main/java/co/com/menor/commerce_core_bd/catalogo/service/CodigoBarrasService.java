package co.com.menor.commerce_core_bd.catalogo.service;

import java.util.List;
import java.util.Optional;

import co.com.menor.commerce_core_bd.catalogo.model.CodigoBarra;
import co.com.menor.comun_dto.codigo_barras.request.CreateCondigoBarrasRequest;
import co.com.menor.comun_dto.codigo_barras.request.EliminarCodigosBarrasRequest;

public interface CodigoBarrasService {

    CodigoBarra saveCodigoBarras(CreateCondigoBarrasRequest req);
    boolean existsCodigoBarras(String codigoBarras);
    Optional<CodigoBarra> findByCodigo(String codigoBarras);
    List<CodigoBarra> findByProductoId(Long productoId);
    List<CodigoBarra> findByComboId(Long comboId);
    void deleteByIds(EliminarCodigosBarrasRequest codigoBarras);
    void deleteByComboId(Long comboId);
}
