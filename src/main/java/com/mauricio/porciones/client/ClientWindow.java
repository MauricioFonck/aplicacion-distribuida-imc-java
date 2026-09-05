package com.mauricio.porciones.client;

import com.mauricio.porciones.core.PorcionesCalculator;
import com.mauricio.porciones.core.PorcionesProtocol;
import com.mauricio.porciones.core.PorcionesResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Interfaz gráfica del cliente de porciones.
 */
public final class ClientWindow extends JFrame {
    private final JTextField hostField = new JTextField("localhost", 16);
    private final JTextField portField =
            new JTextField(String.valueOf(PorcionesProtocol.DEFAULT_PORT), 8);
    private final JLabel connectionStatus = new JLabel("DESCONECTADO");
    private final JButton connectButton = new JButton("CONECTAR");

    private final JTextField attendeesField = new JTextField(10);
    private final JTextField portionsField = new JTextField(10);
    private final JButton calculateButton = new JButton("CALCULAR");
    private final JLabel totalLabel = new JLabel("0");
    private final JLabel reserveLabel = new JLabel("0");
    private final JLabel messageLabel = new JLabel("Sin resultados todavía.");

    private final PorcionesClient client = new PorcionesClient();

    public ClientWindow() {
        super("Cliente de porciones - TCP");
        buildUi();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                client.close();
                dispose();
            }
        });
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));
        JLabel title = new JLabel("PORCIONES PARA UNA REUNIÓN", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(22f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("CONEXIÓN", buildConnectionPanel());
        tabs.addTab("CALCULAR PORCIONES", buildCalculationPanel());
        add(tabs, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints constraints = baseConstraints();

        addRow(panel, constraints, 0, "Dirección IP o nombre:", hostField);
        addRow(panel, constraints, 1, "Puerto de red:", portField);
        addRow(panel, constraints, 2, "Estado:", connectionStatus);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        connectButton.setForeground(new Color(0, 140, 60));
        connectButton.addActionListener(event -> toggleConnection());
        panel.add(connectButton, constraints);
        return panel;
    }

    private JPanel buildCalculationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints constraints = baseConstraints();

        addRow(panel, constraints, 0, "Personas que asistirán:", attendeesField);
        addRow(panel, constraints, 1, "Porciones por persona:", portionsField);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        calculateButton.setForeground(new Color(0, 140, 60));
        calculateButton.addActionListener(event -> calculate());
        panel.add(calculateButton, constraints);

        constraints.gridwidth = 1;
        addRow(panel, constraints, 3, "Total de porciones:", totalLabel);
        addRow(panel, constraints, 4, "Sugerido con 10 % de reserva:", reserveLabel);
        addRow(panel, constraints, 5, "Mensaje:", messageLabel);
        return panel;
    }

    private static GridBagConstraints baseConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        return constraints;
    }

    private static void addRow(JPanel panel, GridBagConstraints constraints, int row,
                               String label, java.awt.Component component) {
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
        if (client.isConnected()) {
            client.close();
            setDisconnectedState();
            return;
        }

        final int port;
        try {
            port = parsePort(portField.getText());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        setConnectionControlsEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                client.connect(hostField.getText(), port);
                return null;
            }

            @Override
            protected void done() {
                setConnectionControlsEnabled(true);
                try {
                    get();
                    setConnectedState();
                } catch (Exception ex) {
                    client.close();
                    setDisconnectedState();
                    showError("No se pudo conectar: " + rootMessage(ex));
                }
            }
        }.execute();
    }

    private void calculate() {
        if (!client.isConnected()) {
            showError("Conecta el cliente al servidor antes de calcular.");
            return;
        }

        final int asistentes;
        final int porcionesPorPersona;
        try {
            asistentes = parseCount(attendeesField.getText(), "La cantidad de personas");
            porcionesPorPersona = parseCount(portionsField.getText(), "Las porciones por persona");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        calculateButton.setEnabled(false);
        new SwingWorker<PorcionesResult, Void>() {
            @Override
            protected PorcionesResult doInBackground() throws IOException {
                return client.calculate(asistentes, porcionesPorPersona);
            }

            @Override
            protected void done() {
                calculateButton.setEnabled(true);
                try {
                    showResult(get());
                } catch (Exception ex) {
                    client.close();
                    setDisconnectedState();
                    showError("No se pudo obtener la respuesta: " + rootMessage(ex));
                }
            }
        }.execute();
    }

    private void showResult(PorcionesResult result) {
        if (!result.isValid()) {
            totalLabel.setText("0");
            reserveLabel.setText("0");
            messageLabel.setText(result.getMessage());
            showError(result.getMessage());
            return;
        }

        totalLabel.setText(PorcionesCalculator.format(result.getValue()));
        reserveLabel.setText(
                PorcionesCalculator.format(PorcionesCalculator.withReserve(result.getValue())));
        messageLabel.setText(result.getMessage());
    }

    private void setConnectedState() {
        connectButton.setText("DESCONECTAR");
        connectButton.setForeground(Color.RED);
        connectionStatus.setText("CONECTADO");
        connectionStatus.setForeground(new Color(0, 150, 0));
        hostField.setEnabled(false);
        portField.setEnabled(false);
    }

    private void setDisconnectedState() {
        connectButton.setText("CONECTAR");
        connectButton.setForeground(new Color(0, 140, 60));
        connectionStatus.setText("DESCONECTADO");
        connectionStatus.setForeground(Color.RED);
        hostField.setEnabled(true);
        portField.setEnabled(true);
    }

    private void setConnectionControlsEnabled(boolean enabled) {
        connectButton.setEnabled(enabled);
        hostField.setEnabled(enabled && !client.isConnected());
        portField.setEnabled(enabled && !client.isConnected());
    }

    private static int parsePort(String text) {
        try {
            int port = Integer.parseInt(text.trim());
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535.");
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El puerto debe ser un número entero.");
        }
    }

    private static int parseCount(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " es obligatoria.");
        }
        final int value;
        try {
            value = Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " debe ser un número entero.");
        }
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " debe ser mayor que 0.");
        }
        return value;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static String rootMessage(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientWindow().setVisible(true));
    }
}
