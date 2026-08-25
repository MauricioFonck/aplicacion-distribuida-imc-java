package com.mauricio.imc.client;

import com.mauricio.imc.core.ImcCalculator;
import com.mauricio.imc.core.ImcResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
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
 * Interfaz gráfica del cliente.
 */
public final class ClientWindow extends JFrame {
    private final JTextField hostField = new JTextField("localhost", 16);
    private final JTextField portField = new JTextField("9007", 8);
    private final JLabel connectionStatus = new JLabel("DESCONECTADO");
    private final JButton connectButton = new JButton("CONECTAR");
    private final JTextField weightField = new JTextField(10);
    private final JTextField heightField = new JTextField(10);
    private final JButton calculateButton = new JButton("CALCULAR");
    private final JLabel resultLabel = new JLabel("0.00");
    private final JLabel messageLabel = new JLabel("Sin resultados todavía.");

    private final ImcClient client = new ImcClient();

    public ClientWindow() {
        super("Cliente IMC - TCP");
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
        JLabel title = new JLabel("CLIENTE IMC", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(22f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("CONEXIÓN", buildConnectionPanel());
        tabs.addTab("CALCULAR IMC", buildCalculationPanel());
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

        addRow(panel, constraints, 0, "Peso (kg):", weightField);
        addRow(panel, constraints, 1, "Altura (m):", heightField);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        calculateButton.setForeground(new Color(0, 140, 60));
        calculateButton.addActionListener(event -> calculate());
        panel.add(calculateButton, constraints);

        constraints.gridwidth = 1;
        addRow(panel, constraints, 3, "IMC:", resultLabel);
        addRow(panel, constraints, 4, "Mensaje:", messageLabel);
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

        final float weight;
        final float height;
        try {
            weight = parseDecimal(weightField.getText(), "El peso");
            height = parseDecimal(heightField.getText(), "La altura");
            if (weight <= 0 || height <= 0) {
                throw new IllegalArgumentException("El peso y la altura deben ser mayores que 0.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        calculateButton.setEnabled(false);
        new SwingWorker<ImcResult, Void>() {
            @Override
            protected ImcResult doInBackground() throws IOException {
                return client.calculate(weight, height);
            }

            @Override
            protected void done() {
                calculateButton.setEnabled(true);
                try {
                    ImcResult result = get();
                    resultLabel.setText(ImcCalculator.format(result.getValue()));
                    messageLabel.setText(result.getMessage());
                } catch (Exception ex) {
                    client.close();
                    setDisconnectedState();
                    showError("No se pudo obtener la respuesta: " + rootMessage(ex));
                }
            }
        }.execute();
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

    private static float parseDecimal(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " es obligatorio.");
        }
        try {
            return Float.parseFloat(text.trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " debe ser un número válido.");
        }
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
        javax.swing.SwingUtilities.invokeLater(() -> new ClientWindow().setVisible(true));
    }
}
