package co.com.menor.commerce_core_bd.catalogo.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductoConCodigos {

    private Producto producto;
    private List<CodigoBarra> codigos;

}
