package Parqueadero;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class Parqueadero {

    private final ArrayList<Vehiculo> vehiculos;
    private final String archivoVehiculos = "vehiculos.txt";
    private final String archivoCaja = "caja.txt";
    private final double tarifa = 0.50;

    private static final DateTimeFormatter FORMATO_CAJA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public Parqueadero() {
        vehiculos = new ArrayList<>();
        cargarVehiculos();
    }

    public ArrayList<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public void registrarEntrada(String placa, String tipo)
            throws IOException {

        placa = placa.trim().toUpperCase();

        if (placa.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingrese la placa del vehículo.");
        }

        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Seleccione el tipo de vehículo.");
        }

        if (buscarActivo(placa) != null) {
            throw new IllegalArgumentException(
                    "Este vehículo ya está dentro del parqueadero.");
        }

        Vehiculo v = new Vehiculo(
                placa, tipo, LocalDateTime.now());

        vehiculos.add(v);

        try {
            guardarVehiculos();
        } catch (IOException e) {
            vehiculos.remove(v);
            throw e;
        }
    }

    public Vehiculo registrarSalida(String placa)
            throws IOException {

        placa = placa.trim().toUpperCase();

        if (placa.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingrese la placa de salida.");
        }

        Vehiculo v = buscarActivo(placa);

        if (v == null) {
            throw new IllegalArgumentException(
                    "No existe un vehículo activo con esa placa.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        long minutos = Duration.between(
                v.getEntrada(), ahora).toMinutes();

        long horas = Math.max(1, (minutos + 59) / 60);
        double pago = horas * tarifa;

        guardarPago(v, ahora, minutos, horas, pago);

        v.registrarSalida(ahora, pago);

        try {
            guardarVehiculos();
        } catch (IOException e) {
            
            v.registrarSalida(null, 0.0);
            throw new IOException(
                    "El pago se guardó, pero no se pudo actualizar "
                    + "vehiculos.txt. No repita el cobro sin revisar "
                    + "caja.txt.", e);
        }

        return v;
    }

    private Vehiculo buscarActivo(String placa) {
        for (int i = vehiculos.size() - 1; i >= 0; i--) {
            Vehiculo v = vehiculos.get(i);

            if (v.getPlaca().equalsIgnoreCase(placa)
                    && v.estaDentro()) {
                return v;
            }
        }

        return null;
    }

    private void cargarVehiculos() {
        File archivo = new File(archivoVehiculos);

        if (!archivo.exists()) {
            return;
        }

        try (BufferedReader br =
                     new BufferedReader(new FileReader(archivo))) {

            String linea;

            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    try {
                        vehiculos.add(Vehiculo.desdeArchivo(linea));
                    } catch (RuntimeException e) {
                        System.err.println(
                                "Registro ignorado: " + linea);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println(
                    "Error al cargar vehículos: " + e.getMessage());
        }
    }

    private void guardarVehiculos() throws IOException {
        try (BufferedWriter bw =
                     new BufferedWriter(
                             new FileWriter(archivoVehiculos))) {

            for (Vehiculo v : vehiculos) {
                bw.write(v.toArchivo());
                bw.newLine();
            }
        }
    }

    private void guardarPago(Vehiculo v, LocalDateTime salida,
                            long minutos, long horas, double pago)
            throws IOException {

        try (BufferedWriter bw =
                     new BufferedWriter(
                             new FileWriter(archivoCaja, true))) {

            bw.write(v.getPlaca() + ";"
                    + salida.format(FORMATO_CAJA) + ";"
                    + minutos + ";"
                    + horas + ";"
                    + String.format(Locale.US, "%.2f", pago));

            bw.newLine();
        }
    }

    public double consultarRecaudacionHoy() {
        return consultarRecaudacion(LocalDate.now());
    }

    public double consultarRecaudacionTotal() {
        return consultarRecaudacion(null);
    }

    private double consultarRecaudacion(LocalDate fecha) {
        double total = 0.0;
        File archivo = new File(archivoCaja);

        if (!archivo.exists()) {
            return total;
        }

        try (BufferedReader br =
                     new BufferedReader(new FileReader(archivo))) {

            String linea;

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";", -1);

                if (datos.length < 5) {
                    continue;
                }

                try {
                    LocalDateTime fechaPago =
                            LocalDateTime.parse(
                                    datos[1], FORMATO_CAJA);

                    double pago =
                            Double.parseDouble(datos[4]);

                    if (fecha == null
                            || fechaPago.toLocalDate().equals(fecha)) {
                        total += pago;
                    }

                } catch (RuntimeException e) {
                    System.err.println(
                            "Pago ignorado por formato inválido: "
                            + linea);
                }
            }

        } catch (IOException e) {
            System.err.println(
                    "Error al consultar la caja: " + e.getMessage());
        }

        return total;
    }
}
