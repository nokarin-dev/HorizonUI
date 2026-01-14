package com.nokarin.gui;

import com.nokarin.api.VersionInfo;
import com.nokarin.handler.UpdateHandler;
import com.nokarin.util.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class UpdaterWindow extends JFrame {
    private JLabel statusLabel;
    private JProgressBar progressBar;

    private final UpdateHandler updateHandler;

    public UpdaterWindow(String currentVersion, String mcVersion, String loader, String versionState, String modsPath) {
        this.updateHandler = new UpdateHandler(currentVersion, mcVersion, loader, versionState, modsPath);

        initUI();
        startUpdateWorker();
    }

    private void initUI() {
        setTitle("HorizonUI Updater");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        // Main panel with banner
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(233, 190, 249));

        mainPanel.add(createBannerPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createBannerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(233, 190, 249));

        JLabel banner = new JLabel();
        banner.setHorizontalAlignment(JLabel.CENTER);
        banner.setVerticalAlignment(JLabel.CENTER);

        BufferedImage image = loadBannerImage();
        if (image == null) {
            Logger.error("Banner image not found");
            System.exit(-1);
        }

        banner.setIcon(new ImageIcon(image));
        panel.add(banner, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(233, 190, 249));
        bottom.setPreferredSize(new Dimension(1200, 75));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        bottom.add(Box.createVerticalStrut(10));
        bottom.add(createStatusContainer());
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(createProgressContainer());
        bottom.add(Box.createVerticalStrut(10));

        return bottom;
    }

    private JPanel createStatusContainer() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setOpaque(false);

        statusLabel = new JLabel("Initializing updater...");
        statusLabel.setFont(new Font("Open Sans", Font.BOLD, 26));
        statusLabel.setForeground(Color.WHITE);

        panel.add(statusLabel);
        return panel;
    }

    private JPanel createProgressContainer() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panel.setOpaque(false);

        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(1200, 40));
        progressBar.setForeground(new Color(248, 103, 251));
        progressBar.setBackground(Color.WHITE);

        progressBar.setUI(new BasicProgressBarUI() {
            @Override protected Color getSelectionForeground() { return Color.WHITE; }
            @Override protected Color getSelectionBackground() { return Color.WHITE; }
        });

        progressBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 0), 0),
                BorderFactory.createEmptyBorder(0,0,0,0)
        ));

        panel.add(progressBar);
        return panel;
    }

    private void startUpdateWorker() {
        SwingWorker<Void, UpdateState> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                publish(new UpdateState("Checking for updates...", 10));
                VersionInfo update = updateHandler.checkForUpdates();

                if (update == null) {
                    publish(new UpdateState("Already up to date!", 100));
                    Thread.sleep(2000);
                    System.exit(0);
                    return null;
                }

                publish(new UpdateState("Update found: " + update.versionNumber(), 15));
                updateHandler.performUpdate(update, this::publish);

                publish(new UpdateState("Update complete!", 100));
                Thread.sleep(3000);
                System.exit(0);
                return null;
            }

            @Override
            protected void process(List<UpdateState> chunks) {
                UpdateState last = chunks.get(chunks.size() - 1);
                statusLabel.setText(last.message());
                progressBar.setValue(last.progress());
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    Logger.error("Update failed", e);
                    statusLabel.setText("Update failed! Closing in 5 seconds...");
                    progressBar.setForeground(Color.RED);
                    progressBar.setValue(100);

                    Timer timer = new Timer(5000, ev -> System.exit(1));
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        };

        worker.execute();
    }

    private BufferedImage loadBannerImage() {
        try (InputStream is = getClass().getResourceAsStream("/banner.png")) {
            if (is != null) {
                return ImageIO.read(is);
            }

            File fallback = new File("banner.png");
            if (fallback.exists()) {
                return ImageIO.read(fallback);
            }
        } catch (Exception e) {
            Logger.error("Failed to load banner image", e);
        }
        return null;
    }
}
