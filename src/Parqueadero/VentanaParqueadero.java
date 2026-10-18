package Parqueadero;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;

public class VentanaParqueadero extends JFrame {

    private final Parqueadero parqueadero;

    private JTextField txtPlacaEntrada;
    private JTextField txtPlacaSalida;
    private JComboBox<String> cmbTipo;

    private DefaultTableModel modelo;
    private JTable tabla;

    private JLabel lblFecha;
    private JLabel lblVehiculos;
    private JLabel lblRecaudacionHoy;
    private JLabel lblRecaudacionTotal;

    private final NumberFormat moneda =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("es-EC"));

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public VentanaParqueadero() {
    	setBackground(new Color(0, 204, 255));
        parqueadero = new Parqueadero();

        setTitle("Parqueadero");
        setSize(992, 629);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout(10, 10));

        crearInterfaz();
        actualizarPantalla();

        
        Timer temporizador = new Timer(30000, e -> actualizarPantalla());
        temporizador.start();
    }

    private void crearInterfaz() {

        JPanel panelNorte = new JPanel(new GridLayout(2, 1, 5, 5));

        JLabel titulo = new JLabel(
                "PARQUEADERO",
                SwingConstants.CENTER);
        titulo.setBackground(new Color(0, 128, 64));

        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        lblFecha = new JLabel("", SwingConstants.CENTER);
        lblFecha.setBackground(new Color(0, 128, 64));

        panelNorte.add(titulo);
        panelNorte.add(lblFecha);

        getContentPane().add(panelNorte, BorderLayout.NORTH);

        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));

        JPanel panelControles = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel panelEntrada = new JPanel();
        panelEntrada.setBackground(new Color(192, 192, 192));

        panelEntrada.add(new JLabel("Placa:"));
        txtPlacaEntrada = new JTextField(10);
        panelEntrada.add(txtPlacaEntrada);

        panelEntrada.add(new JLabel("Tipo:"));
        cmbTipo = new JComboBox<>(
                new String[]{
                    "Automóvil",
                    "Motocicleta",
                    "Camioneta"
                });
        panelEntrada.add(cmbTipo);

        JButton btnEntrada = new JButton("Registrar entrada");
        panelEntrada.add(btnEntrada);

        JPanel panelSalida = new JPanel();
        panelSalida.setBackground(new Color(192, 192, 192));

        panelSalida.add(new JLabel("Placa de salida:"));
        txtPlacaSalida = new JTextField(10);
        panelSalida.add(txtPlacaSalida);

        JButton btnSalida = new JButton("Registrar salida");
        panelSalida.add(btnSalida);

        JButton btnActualizar = new JButton("Actualizar");
        panelSalida.add(btnActualizar);

        JButton btnCaja = new JButton("Consultar recaudación");
        panelSalida.add(btnCaja);

        panelControles.add(panelEntrada);
        panelControles.add(panelSalida);

        panelPrincipal.add(panelControles, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new String[]{
                    "Placa",
                    "Tipo",
                    "Entrada",
                    "Salida",
                    "Pago",
                    "Estado"
                }, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modelo);

        panelPrincipal.add(
                new JScrollPane(tabla), BorderLayout.CENTER);

        getContentPane().add(panelPrincipal, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new GridLayout(3, 1, 3, 3));

        lblVehiculos = new JLabel();
        lblRecaudacionHoy = new JLabel();
        lblRecaudacionTotal = new JLabel();

        panelInferior.add(lblVehiculos);
        panelInferior.add(lblRecaudacionHoy);
        panelInferior.add(lblRecaudacionTotal);

        getContentPane().add(panelInferior, BorderLayout.SOUTH);

        btnEntrada.addActionListener(e -> registrarEntrada());
        btnSalida.addActionListener(e -> registrarSalida());
        btnActualizar.addActionListener(e -> actualizarPantalla());
        btnCaja.addActionListener(e -> consultarCaja());
    }

    private void registrarEntrada() {
        String placa = txtPlacaEntrada.getText().trim();
        String tipo = cmbTipo.getSelectedItem().toString();

        try {
            parqueadero.registrarEntrada(placa, tipo);

            JOptionPane.showMessageDialog(
                    this,
                    "Vehículo registrado correctamente.");

            txtPlacaEntrada.setText("");
            actualizarPantalla();

        } catch (IllegalArgumentException | IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void registrarSalida() {
        String placa = txtPlacaSalida.getText().trim();

        try {
            Vehiculo v = parqueadero.registrarSalida(placa);

            JOptionPane.showMessageDialog(
                    this,
                    "Salida registrada correctamente.\n"
                    + "Placa: " + v.getPlaca() + "\n"
                    + "Entrada: " + v.getEntradaTexto() + "\n"
                    + "Salida: " + v.getSalidaTexto() + "\n"
                    + "Total a pagar: "
                    + moneda.format(v.getPago()));

            txtPlacaSalida.setText("");
            actualizarPantalla();

        } catch (IllegalArgumentException | IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void consultarCaja() {
        actualizarPantalla();

        JOptionPane.showMessageDialog(
                this,
                "Fecha: " + LocalDate.now().format(formatoFecha)
                + "\nRecaudación de hoy: "
                + moneda.format(
                        parqueadero.consultarRecaudacionHoy())
                + "\nRecaudación histórica: "
                + moneda.format(
                        parqueadero.consultarRecaudacionTotal()),
                "Resumen de caja",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizarPantalla() {
        LocalDate hoy = LocalDate.now();

        lblFecha.setText(
                "Jornada actual: " + hoy.format(formatoFecha));

        modelo.setRowCount(0);

        int dentro = 0;

        for (Vehiculo v : parqueadero.getVehiculos()) {

            String estado;

            if (v.estaDentro()) {
                estado = "Dentro";
                dentro++;
            } else {
                estado = "Salió";
            }

            modelo.addRow(new Object[]{
                v.getPlaca(),
                v.getTipo(),
                v.getEntradaTexto(),
                v.getSalidaTexto(),
                moneda.format(v.getPago()),
                estado
            });
        }

        lblVehiculos.setText(
                "Vehículos actualmente dentro: " + dentro);

        lblRecaudacionHoy.setText(
                "Recaudación de hoy: "
                + moneda.format(
                        parqueadero.consultarRecaudacionHoy()));

        lblRecaudacionTotal.setText(
                "Recaudación histórica: "
                + moneda.format(
                        parqueadero.consultarRecaudacionTotal()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaParqueadero().setVisible(true);
        });
    }
}

