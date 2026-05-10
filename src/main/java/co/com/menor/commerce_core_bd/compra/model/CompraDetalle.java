package co.com.menor.commerce_core_bd.compra.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "COMPRA_DETALLE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compra_id", nullable = false)
    private Long compraId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "subtotal", precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
}
