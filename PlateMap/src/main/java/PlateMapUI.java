package main.java;

import javax.swing.*;
import java.awt.*;

public class PlateMapUI {

    public static void main(String[] args) {
    	System.out.println("PROJECT ROOT = " + new java.io.File(".").getAbsolutePath());
    	
        JFrame splash = new JFrame();
        JLabel msg = new JLabel("Starting database...", JLabel.CENTER);
        msg.setFont(new Font("Arial", Font.PLAIN, 16));
        splash.add(msg);
        splash.setSize(300, 80);
        splash.setLocationRelativeTo(null);
        splash.setUndecorated(true);
        splash.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        splash.setVisible(true);

        new Thread(() -> {
            SwingUtilities.invokeLater(() -> msg.setText("Connecting..."));
            HibernateIO.connect();

            SwingUtilities.invokeLater(() -> msg.setText("Loading plates..."));
            HibernateIO.importPlateVariants("/main/resources/us-license-plates.csv");
            if (HibernateIO.getSaveNames().length == 0) {
                HibernateIO.newSave("Main_Save");
            }
            
            SwingUtilities.invokeLater(() -> msg.setText("Launching PlateMap..."));

            SwingUtilities.invokeLater(() -> {
                new PlateMap();
                splash.dispose();
            });
        }).start();
    }
}