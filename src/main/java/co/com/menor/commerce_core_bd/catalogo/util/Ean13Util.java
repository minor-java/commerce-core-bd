package co.com.menor.commerce_core_bd.catalogo.util;

public final class Ean13Util {

    private static final int LONGITUD_SECUENCIA = 10;

    private Ean13Util() {}

    public static String generar(String prefijo, long secuencia) {
        String base = prefijo + String.format("%0" + LONGITUD_SECUENCIA + "d", secuencia);
        return base + digitoVerificador(base);
    }

    private static int digitoVerificador(String doceDigitos) {
        int suma = 0;
        for (int i = 0; i < doceDigitos.length(); i++) {
            int digito = Character.getNumericValue(doceDigitos.charAt(i));
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        int resto = suma % 10;
        return (resto == 0) ? 0 : 10 - resto;
    }
}
