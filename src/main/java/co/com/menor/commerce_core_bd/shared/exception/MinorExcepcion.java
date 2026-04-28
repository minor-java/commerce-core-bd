package co.com.menor.commerce_core_bd.shared.exception;

public class MinorExcepcion extends RuntimeException {

    private final String codigo;

    public MinorExcepcion(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
