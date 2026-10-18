package Parqueadero;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Vehiculo {

    private String placa;
    private String tipo;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    private double pago;

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public Vehiculo(String placa, String tipo,
                    LocalDateTime entrada) {
        this.placa = placa.trim().toUpperCase();
        this.tipo = tipo;
        this.entrada = entrada;
        this.salida = null;
        this.pago = 0.0;
    }

    public String getPlaca() {
        return placa;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    public LocalDateTime getSalida() {
        return salida;
    }

    public double getPago() {
        return pago;
    }

    public boolean estaDentro() {
        return salida == null;
    }

    public void registrarSalida(LocalDateTime salida,
                                double pago) {
        this.salida = salida;
        this.pago = pago;
    }

    public String getEntradaTexto() {
        return entrada.format(FORMATO);
    }

    public String getSalidaTexto() {
        return salida == null
                ? "Pendiente"
                : salida.format(FORMATO);
    }

    public String toArchivo() {
        return placa + ";" + tipo + ";" + entrada + ";"
                + (salida == null ? "" : salida) + ";"
                + pago;
    }

    public static Vehiculo desdeArchivo(String linea) {
        String[] datos = linea.split(";", -1);

        if (datos.length != 5) {
            throw new IllegalArgumentException(
                    "Registro de vehículo inválido.");
        }

        Vehiculo v = new Vehiculo(
                datos[0],
                datos[1],
                LocalDateTime.parse(datos[2])
        );

        if (!datos[3].isEmpty()) {
            v.registrarSalida(
                    LocalDateTime.parse(datos[3]),
                    Double.parseDouble(datos[4])
            );
        }

        return v;
    }
}
