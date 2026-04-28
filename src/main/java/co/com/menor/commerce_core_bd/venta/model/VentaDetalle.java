package co.com.menor.commerce_core_bd.venta.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "VENTA_DETALLE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", precision = 14, scale = 2)
    private BigDecimal subtotal;
}
