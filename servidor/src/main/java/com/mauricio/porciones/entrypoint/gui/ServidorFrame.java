package com.mauricio.porciones.entrypoint.gui;

import com.mauricio.porciones.adaptadores.red.ObservadorServidor;
import com.mauricio.porciones.adaptadores.red.util.RedUtil;
import com.mauricio.porciones.aplicacion.excepciones.ServidorRedException;
import com.mauricio.porciones.aplicacion.puertos.entrada.GestionarServidorInputPort;
import com.mauricio.porciones.dominio.enums.EstadoServidor;
import com.mauricio.porciones.dominio.excepciones.DominioException;
import com.mauricio.porciones.dominio.modelos.EventoServidor;
import com.mauricio.porciones.dominio.vo.PuertoRed;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Interfaz gráfica del servidor de porciones. Es un entrypoint: solo conoce el puerto de entrada
 * {@link GestionarServidorInputPort} y se entera de lo que ocurre en el núcleo como observador.
 */
public final class ServidorFrame extends JFrame implements ObservadorServidor {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Color VERDE = new Color(0, 140, 60);

    private final transient GestionarServidorInputPort gestionarServidor;

    private final JTextField portField = new JTextField(String.valueOf(PuertoRed.POR_DEFECTO), 8);
    private final JLabel ipLabel = new JLabel(RedUtil.obtenerIpLocal());
    private final JLabel statusLabel = new JLabel("DETENIDO");
    private final JTextArea logArea = new JTextArea(14, 60);
    private final JButton startStopButton = new JButton("INICIAR");

    public ServidorFrame(final GestionarServidorInputPort gestionarServidor) {
        super("Servidor de porciones - UDP");
        this.gestionarServidor =
                Objects.requireNonNull(gestionarServidor, "El caso de uso de gestión es obligatorio.");
        buildUi();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event) {
                if (gestionarServidor.estaCorriendo()) {
                    gestionarServidor.detenerServidor();
                }
                dispose();
            }
        });
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));

        final JLabel title = new JLabel("SERVIDOR DE PORCIONES", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(22f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        add(title, BorderLayout.NORTH);

        final JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Servicio UDP"));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        constraints.gridy = 0;
        connectionPanel.add(new JLabel("IP local:"), constraints);
        constraints.gridx = 1;
        connectionPanel.add(ipLabel, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        connectionPanel.add(new JLabel("Puerto de servicio:"), constraints);
        constraints.gridx = 1;
        connectionPanel.add(portField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        connectionPanel.add(new JLabel("Estado:"), constraints);
        constraints.gridx = 1;
        statusLabel.setForeground(Color.RED);
        connectionPanel.add(statusLabel, constraints);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        startStopButton.setForeground(VERDE);
        startStopButton.addActionListener(event -> toggleServer());
        connectionPanel.add(startStopButton, constraints);

        final JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(connectionPanel);
        add(wrapper, BorderLayout.CENTER);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        final JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log de peticiones"));
        final JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        final JButton clearButton = new JButton("LIMPIAR");
        clearButton.addActionListener(event -> logArea.setText(""));
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        logPanel.add(clearButton, BorderLayout.SOUTH);
        add(logPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void toggleServer() {
        if (gestionarServidor.estaCorriendo()) {
            gestionarServidor.detenerServidor();
            return;
        }

        final int puerto;
        try {
            puerto = Integer.parseInt(portField.getText().trim());
        } catch (final NumberFormatException excepcion) {
            showError("El puerto debe ser un número entero.");
            return;
        }

        try {
            gestionarServidor.iniciarServidor(puerto);
        } catch (final DominioException | ServidorRedException excepcion) {
            showError("No se pudo iniciar el servidor: " + excepcion.getMessage());
        }
    }

    @Override
    public void onEvento(final EventoServidor evento) {
        final String linea = "[" + evento.getFechaHora().format(FORMATO_HORA) + "] "
                + "[" + evento.getCategoria() + "] "
                + evento.getEndpoint() + " - " + evento.getDescripcion();
        SwingUtilities.invokeLater(() -> {
            logArea.append(linea + System.lineSeparator());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void onCambioEstado(final EstadoServidor nuevoEstado, final int puerto) {
        SwingUtilities.invokeLater(() -> {
            final boolean enLinea = nuevoEstado == EstadoServidor.EN_LINEA;
            portField.setEnabled(!enLinea);
            startStopButton.setText(enLinea ? "DETENER" : "INICIAR");
            startStopButton.setForeground(enLinea ? Color.RED : VERDE);
            statusLabel.setText(enLinea ? "ONLINE (puerto " + puerto + ")" : "DETENIDO");
            statusLabel.setForeground(enLinea ? new Color(0, 150, 0) : Color.RED);
        });
    }

    private void showError(final String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
