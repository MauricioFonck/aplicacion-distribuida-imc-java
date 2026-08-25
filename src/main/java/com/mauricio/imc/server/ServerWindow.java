package com.mauricio.imc.server;

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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Interfaz gráfica del servidor.
 */
public final class ServerWindow extends JFrame {
    private final JTextField portField = new JTextField("9007", 8);
    private final JLabel statusLabel = new JLabel("DETENIDO");
    private final JTextArea logArea = new JTextArea(14, 52);
    private final JButton startStopButton = new JButton("INICIAR");

    private ImcServer server;

    public ServerWindow() {
        super("Servidor IMC - TCP");
        buildUi();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                stopServer();
                dispose();
            }
        });
    }

    private void buildUi() {
        setLayout(new BorderLayout(12, 12));

        JLabel title = new JLabel("SERVIDOR IMC", JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(22f));
        title.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        add(title, BorderLayout.NORTH);

        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Conexión TCP"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        constraints.gridy = 0;
        connectionPanel.add(new JLabel("Puerto de servicio:"), constraints);
        constraints.gridx = 1;
        connectionPanel.add(portField, constraints);
        constraints.gridx = 0;
        constraints.gridy = 1;
        connectionPanel.add(new JLabel("Estado:"), constraints);
        constraints.gridx = 1;
        statusLabel.setForeground(Color.RED);
        connectionPanel.add(statusLabel, constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        startStopButton.setForeground(new Color(0, 140, 60));
        startStopButton.addActionListener(event -> toggleServer());
        connectionPanel.add(startStopButton, constraints);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(connectionPanel);
        add(wrapper, BorderLayout.CENTER);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Log de conexiones"));
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        JButton clearButton = new JButton("LIMPIAR");
        clearButton.addActionListener(event -> logArea.setText(""));
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        logPanel.add(clearButton, BorderLayout.SOUTH);
        add(logPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void toggleServer() {
        if (server != null && server.isRunning()) {
            stopServer();
        } else {
            startServer();
        }
    }

    private void startServer() {
        final int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("El puerto debe ser un número entero.");
            return;
        }

        try {
            server = new ImcServer(port, this::appendLog);
            server.start();
            portField.setEnabled(false);
            startStopButton.setText("DETENER");
            startStopButton.setForeground(Color.RED);
            statusLabel.setText("ONLINE");
            statusLabel.setForeground(new Color(0, 150, 0));
        } catch (IllegalArgumentException | java.io.IOException ex) {
            server = null;
            showError("No se pudo iniciar el servidor: " + ex.getMessage());
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
        portField.setEnabled(true);
        startStopButton.setText("INICIAR");
        startStopButton.setForeground(new Color(0, 140, 60));
        statusLabel.setText("DETENIDO");
        statusLabel.setForeground(Color.RED);
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + System.lineSeparator());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerWindow().setVisible(true));
    }
}
