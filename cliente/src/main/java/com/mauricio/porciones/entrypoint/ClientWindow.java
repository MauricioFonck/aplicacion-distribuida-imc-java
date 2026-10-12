package com.mauricio.porciones.entrypoint;

import com.mauricio.porciones.cliente.PorcionesClient;
import com.mauricio.porciones.remoto.PorcionesResponse;
import javax.swing.*;
import java.awt.*;

/** Ventana del cliente que ejecuta las llamadas RMI fuera del hilo de Swing. */
public final class ClientWindow extends JFrame {
    private final JTextField host = new JTextField("localhost", 12);
    private final JTextField port = new JTextField("1099", 6);
    private final JTextField service = new JTextField("PorcionesService", 14);
    private final JTextField people = new JTextField(8);
    private final JTextField portions = new JTextField(8);
    private final JLabel status = new JLabel("DESCONECTADO");
    private final JLabel result = new JLabel("Sin resultados.");
    private final PorcionesClient client = new PorcionesClient();
    public ClientWindow() {
        super("Cliente de porciones - RMI");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(panel, BorderLayout.CENTER);
        row(panel, "Host del registro:", host); row(panel, "Puerto:", port); row(panel, "Servicio:", service);
        JButton connect = new JButton("CONECTAR"); panel.add(connect); panel.add(status);
        row(panel, "Personas:", people); row(panel, "Porciones por persona:", portions);
        JButton calculate = new JButton("CALCULAR"); panel.add(calculate); panel.add(result);
        connect.addActionListener(event -> toggle(connect)); calculate.addActionListener(event -> calculate());
        addWindowListener(new java.awt.event.WindowAdapter() { public void windowClosing(java.awt.event.WindowEvent e) { client.close(); dispose(); } });
        pack(); setLocationRelativeTo(null);
    }
    private static void row(JPanel p, String label, JComponent field) { p.add(new JLabel(label)); p.add(field); }
    private void toggle(JButton button) {
        if (client.isConnected()) { client.close(); status.setText("DESCONECTADO"); button.setText("CONECTAR"); return; }
        try {
            int p = Integer.parseInt(port.getText().trim());
            new SwingWorker<Void, Void>() { protected Void doInBackground() throws Exception { client.connect(host.getText(), p, service.getText(), "cliente-swing"); return null; }
                protected void done() { try { get(); status.setText("CONECTADO"); button.setText("DESCONECTAR"); } catch (Exception ex) { client.close(); error(ex); } } }.execute();
        } catch (Exception ex) { error(ex); }
    }
    private void calculate() {
        if (!client.isConnected()) { error(new IllegalStateException("Conecte el cliente antes de calcular.")); return; }
        try {
            int p = Integer.parseInt(people.getText().trim()); int q = Integer.parseInt(portions.getText().trim());
            new SwingWorker<PorcionesResponse, Void>() { protected PorcionesResponse doInBackground() throws Exception { return client.calcular(p, q); }
                protected void done() { try { PorcionesResponse r = get(); result.setText(r.getMensaje()); } catch (Exception ex) { client.close(); status.setText("DESCONECTADO"); error(ex); } } }.execute();
        } catch (Exception ex) { error(ex); }
    }
    private void error(Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new ClientWindow().setVisible(true)); }
}
