package com.mauricio.porciones.entrypoint.gui;

import com.mauricio.porciones.adaptadores.notificacion.ObservadorCliente;
import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.aplicacion.dto.ConectarCommand;
import com.mauricio.porciones.aplicacion.puertos.entrada.CalcularPorcionesInputPort;
import com.mauricio.porciones.aplicacion.puertos.entrada.GestionarConexionInputPort;
import com.mauricio.porciones.dominio.enums.EstadoConexion;
import com.mauricio.porciones.dominio.excepciones.DominioException;
import com.mauricio.porciones.dominio.modelos.EventoCliente;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;
import com.mauricio.porciones.dominio.vo.DestinoServidor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Interfaz gráfica del cliente de porciones. Es un entrypoint: invoca los casos de uso a través de
 * los puertos de entrada y recibe los resultados como observador, sin conocer nada de UDP.
 */
public final class ClienteFrame extends JFrame implements ObservadorCliente {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Locale LOCALE = Locale.forLanguageTag("es-CO");
    private static final Color VERDE = new Color(0, 140, 60);

    private final transient GestionarConexionInputPort gestionarConexion;
    private final transient CalcularPorcionesInputPort calcularPorciones;

    private final JTextField hostField = new JTextField("localhost", 16);
    private final JTextField portField =
            new JTextField(String.valueOf(DestinoServidor.PUERTO_POR_DEFECTO), 8);
    private final JLabel connectionStatus = new JLabel("DESCONECTADO");
    private final JButton connectButton = new JButton("CONECTAR");

    private final JTextField attendeesField = new JTextField(10);
    private final JTextField portionsField = new JTextField(10);
    private final JButton calculateButton = new JButton("CALCULAR");
    private final JLabel totalLabel = new JLabel("0");
    private final JLabel reserveLabel = new JLabel("0");
    private final JLabel classificationLabel = new JLabel("-");
    private final JTextArea messageArea = new JTextArea("Sin resultados todavía.", 3, 34);
    private final JTextArea logArea = new JTextArea(6, 50);

    public ClienteFrame(
            final GestionarConexionInputPort gestionarConexion,
            final CalcularPorcionesInputPort calcularPorciones) {
        super("Cliente de porciones - UDP");
        this.gestionarConexion =
                Objects.requireNonNull(gestionarConexion, "El caso de uso de conexión es obligatorio.");
        this.calcularPorciones =
                Objects.requireNonNull(calcularPorciones, "El caso de uso de cálculo es obligatorio.");
        buildUi();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                cerrar();
            }
        });
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));
        final JLabel title = new JLabel("PORCIONES PARA UNA REUNIÓN", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(22f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        add(title, BorderLayout.NORTH);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("CONEXIÓN", buildConnectionPanel());
        tabs.addTab("CALCULAR PORCIONES", buildCalculationPanel());
        add(tabs, BorderLayout.CENTER);

        logArea.setEditable(false);
        final JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Eventos"));
        add(logScrollPane, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildConnectionPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        final GridBagConstraints constraints = baseConstraints();

        addRow(panel, constraints, 0, "Dirección IP o nombre:", hostField);
        addRow(panel, constraints, 1, "Puerto de red:", portField);
        connectionStatus.setForeground(Color.RED);
        addRow(panel, constraints, 2, "Estado:", connectionStatus);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        connectButton.setForeground(VERDE);
        connectButton.addActionListener(event -> toggleConnection());
        panel.add(connectButton, constraints);
        return panel;
    }

    private JPanel buildCalculationPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        final GridBagConstraints constraints = baseConstraints();

        addRow(panel, constraints, 0, "Personas que asistirán:", attendeesField);
        addRow(panel, constraints, 1, "Porciones por persona:", portionsField);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        calculateButton.setForeground(VERDE);
        calculateButton.addActionListener(event -> calculate());
        panel.add(calculateButton, constraints);

        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);

        addRow(panel, constraints, 3, "Total de porciones:", totalLabel);
        addRow(panel, constraints, 4, "Sugerido con 10 % de reserva:", reserveLabel);
        addRow(panel, constraints, 5, "Clasificación:", classificationLabel);
        addRow(panel, constraints, 6, "Mensaje:", messageArea);
        return panel;
    }

    private static GridBagConstraints baseConstraints() {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        return constraints;
    }

    private static void addRow(final JPanel panel, final GridBagConstraints constraints, final int row,
                               final String label, final Component component) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, constraints);
    }

    private void toggleConnection() {
        if (gestionarConexion.estaConectado()) {
            connectButton.setEnabled(false);
            gestionarConexion.desconectar();
            return;
        }

        try {
            final int puerto = parseInteger(portField.getText(), "El puerto");
            gestionarConexion.conectar(new ConectarCommand(hostField.getText(), puerto));
        } catch (final IllegalArgumentException | DominioException excepcion) {
            showError(excepcion.getMessage());
        }
    }

    private void calculate() {
        if (!gestionarConexion.estaConectado()) {
            showError("Conecta el cliente al servidor antes de calcular.");
            return;
        }

        try {
            final int asistentes = parseInteger(attendeesField.getText(), "La cantidad de personas");
            final int porciones = parseInteger(portionsField.getText(), "Las porciones por persona");
            calculateButton.setEnabled(false);
            calcularPorciones.calcular(new CalcularPorcionesCommand(asistentes, porciones))
                    .whenComplete((resultado, error) ->
                            SwingUtilities.invokeLater(() -> calculateButton.setEnabled(true)));
        } catch (final IllegalArgumentException | DominioException excepcion) {
            calculateButton.setEnabled(true);
            showError(excepcion.getMessage());
        }
    }

    @Override
    public void onEstado(final EstadoConexion estado, final String endpoint) {
        SwingUtilities.invokeLater(() -> {
            final boolean conectado = estado == EstadoConexion.CONECTADO;
            final boolean desconectado = estado == EstadoConexion.DESCONECTADO;
            connectButton.setEnabled(estado != EstadoConexion.CONECTANDO);
            connectButton.setText(conectado ? "DESCONECTAR" : "CONECTAR");
            connectButton.setForeground(conectado ? Color.RED : VERDE);
            connectionStatus.setText(conectado ? "CONECTADO a " + endpoint : estado.name());
            connectionStatus.setForeground(conectado ? new Color(0, 150, 0) : Color.RED);
            hostField.setEnabled(desconectado);
            portField.setEnabled(desconectado);
        });
    }

    @Override
    public void onEvento(final EventoCliente evento) {
        appendLog("[" + evento.fechaHora().format(FORMATO_HORA) + "] [" + evento.categoria() + "] "
                + evento.descripcion());
    }

    @Override
    public void onResultado(final ResultadoPorciones resultado) {
        SwingUtilities.invokeLater(() -> {
            totalLabel.setText(format(resultado.total()));
            reserveLabel.setText(format(resultado.totalConReserva()));
            classificationLabel.setText(resultado.clasificacion());
            messageArea.setText(resultado.mensaje());
        });
        appendLog("[CÁLCULO] Total recibido del servidor: " + format(resultado.total()) + " porciones.");
    }

    @Override
    public void onError(final String mensaje) {
        appendLog("[ERROR] " + mensaje);
        SwingUtilities.invokeLater(() -> showError(mensaje));
    }

    private void cerrar() {
        if (gestionarConexion.estaConectado()) {
            // Se espera a que salga el datagrama DESCONECTAR antes de cerrar la ventana.
            gestionarConexion.desconectar().handle((ok, error) -> null).join();
        }
        dispose();
    }

    private void appendLog(final String linea) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(linea + System.lineSeparator());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private static int parseInteger(final String text, final String fieldName) {
        if (Objects.isNull(text) || text.isBlank()) {
            throw new IllegalArgumentException(fieldName + " es un dato obligatorio.");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (final NumberFormatException excepcion) {
            throw new IllegalArgumentException(fieldName + " debe ser un número entero.", excepcion);
        }
    }

    private static String format(final long value) {
        return NumberFormat.getIntegerInstance(LOCALE).format(value);
    }

    private void showError(final String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
