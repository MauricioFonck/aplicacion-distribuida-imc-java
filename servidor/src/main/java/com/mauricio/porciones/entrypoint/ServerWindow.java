package com.mauricio.porciones.entrypoint;

import com.mauricio.porciones.servidor.RmiServer;
import javax.swing.*;
import java.awt.*;

/** Ventana para iniciar y detener el registro RMI. */
public final class ServerWindow extends JFrame {
    private final JTextField port = new JTextField("1099", 8);
    private final JTextField service = new JTextField("PorcionesService", 14);
    private final JLabel status = new JLabel("DETENIDO");
    private final JTextArea log = new JTextArea(12, 48);
    private RmiServer server;
    public ServerWindow() {
        super("Servidor de porciones - RMI");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Puerto del registro:")); form.add(port);
        form.add(new JLabel("Nombre del servicio:")); form.add(service);
        form.add(new JLabel("Estado:")); form.add(status);
        JButton button = new JButton("INICIAR / DETENER"); form.add(button);
        button.addActionListener(event -> toggle());
        add(form, BorderLayout.NORTH);
        log.setEditable(false); add(new JScrollPane(log), BorderLayout.CENTER);
        setSize(580, 360); setLocationRelativeTo(null);
    }
    private void toggle() {
        if (server != null && server.isRunning()) { server.stop(); server = null; status.setText("DETENIDO"); return; }
        try {
            int p = Integer.parseInt(port.getText().trim());
            server = new RmiServer(p, service.getText(), this::append);
            server.start(); status.setText("ONLINE"); port.setEnabled(false); service.setEnabled(false);
        } catch (Exception ex) { append("Error: " + ex.getMessage()); server = null; }
    }
    private void append(String text) { SwingUtilities.invokeLater(() -> log.append(text + System.lineSeparator())); }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new ServerWindow().setVisible(true)); }
}
