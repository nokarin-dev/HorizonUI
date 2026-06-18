package xyz.nokarin.gui;

import xyz.nokarin.api.VersionInfo;
import xyz.nokarin.handler.UpdateHandler;
import xyz.nokarin.util.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class UpdaterWindow extends JFrame {
    private static final Color BG_DARK = new Color(0x0A, 0x03, 0x18);
    private static final Color BG_MID = new Color(0x12, 0x05, 0x23);
    private static final Color ACCENT = new Color(0xA0, 0x80, 0xD0);
    private static final Color ACCENT_BRIGHT = new Color(0xC8, 0xA0, 0xFF);
    private static final Color TEXT_PRIMARY = new Color(0xF0, 0xEB, 0xFF);
    private static final Color TEXT_DIM = new Color(0x90, 0x85, 0xA8);
    private static final Color TRACK_BG = new Color(0x28, 0x1A, 0x3C);
    private static final Color ERROR_COLOR = new Color(0xFF, 0x55, 0x55);

    private static final int WINDOW_W = 1200;
    private static final int WINDOW_H = 600;
    private static final int BANNER_H = 468;
    private static final int BOTTOM_H = 132;

    private JLabel statusLabel;
    private JLabel speedLabel;
    private RoundProgressBar progressBar;
    private int animatedProgress = 0;
    private Timer progressTimer;
    private boolean isError = false;

    private final UpdateHandler updateHandler;

    public UpdaterWindow(String currentVersion, String mcVersion, String loader, String versionState, String modsPath) {
        this.updateHandler = new UpdateHandler(currentVersion, mcVersion, loader, versionState, modsPath);
        initUI();
        fadeIn();
        startUpdateWorker();
    }

    private void initUI() {
        setTitle("HorizonUI Updater");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(WINDOW_W, WINDOW_H);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = getJPanel();

        // Banner
        JLabel bannerLabel = createBannerLabel();
        bannerLabel.setBounds(0, 0, WINDOW_W, BANNER_H);
        root.add(bannerLabel);

        // Bottom strip
        JPanel bottom = createBottomPanel();
        bottom.setBounds(0, BANNER_H, WINDOW_W, BOTTOM_H);
        root.add(bottom);

        // Rounded window shape
        setShape(new RoundRectangle2D.Double(0, 0, WINDOW_W, WINDOW_H, 18, 18));
        setContentPane(root);
        setVisible(true);
    }

    private static JPanel getJPanel() {
        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Clip to rounded window shape
                g2.setColor(BG_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        return root;
    }

    private JLabel createBannerLabel() {
        JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Dark bg fallback in case banner fails
                g2.setColor(BG_MID);
                g2.fillRect(0, 0, getWidth(), getHeight());

                Icon ic = getIcon();
                if (ic != null) {
                    int x = (getWidth() - ic.getIconWidth()) / 2;
                    int y = (getHeight() - ic.getIconHeight()) / 2;
                    ic.paintIcon(this, g2, x, y);
                }

                // Fade gradient at bottom to blend into the bottom strip
                GradientPaint fade = new GradientPaint(
                        0, getHeight() - 80, new Color(0, 0, 0, 0),
                        0, getHeight(),      BG_DARK
                );
                g2.setPaint(fade);
                g2.fillRect(0, getHeight() - 80, getWidth(), 80);

                g2.dispose();
            }
        };

        BufferedImage img = loadBannerImage();
        if (img == null) {
            Logger.error("Banner image not found, exiting");
            System.exit(-1);
        }

        double scale = (double) WINDOW_W / img.getWidth();
        int ih = (int) (img.getHeight() * scale);
        Image scaled = img.getScaledInstance(WINDOW_W, ih, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(scaled));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        return label;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                g2.setColor(BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        // Top separator
        JSeparator sep = new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0x30, 0x20, 0x48));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        sep.setBounds(0, 0, WINDOW_W, 1);
        panel.add(sep);

        // Status label
        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(loadFont(18f));
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setBounds(36, 16, WINDOW_W - 400, 28);
        panel.add(statusLabel);

        // Speed/ETA
        speedLabel = new JLabel("");
        speedLabel.setFont(loadFont(14f));
        speedLabel.setForeground(TEXT_DIM);
        speedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        speedLabel.setBounds(WINDOW_W - 320 - 36, 18, 320, 24);
        panel.add(speedLabel);

        // Progress bar
        progressBar = new RoundProgressBar();
        progressBar.setBounds(36, 56, WINDOW_W - 72, 18);
        panel.add(progressBar);

        // Credit bottom-left
        JLabel credit = new JLabel("HorizonUI Updater");
        credit.setFont(loadFont(12f));
        credit.setForeground(TEXT_DIM);
        credit.setBounds(36, BOTTOM_H - 30, 300, 20);
        panel.add(credit);

        return panel;
    }

    private void startUpdateWorker() {
        SwingWorker<Void, UpdateState> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                publish(new UpdateState("Checking for updates...", 5));
                VersionInfo update = updateHandler.checkForUpdates();

                if (update == null) {
                    publish(new UpdateState("Already up to date!", 100));
                    Thread.sleep(2000);
                    fadeOutAndExit(0);
                    return null;
                }

                publish(new UpdateState("Found " + update.versionNumber(), 15));
                updateHandler.performUpdate(update, this::publish);

                publish(new UpdateState("Update complete!", 100));
                Thread.sleep(3000);
                SwingUtilities.invokeLater(() -> fadeOutAndExit(0));
                return null;
            }

            @Override
            protected void process(List<UpdateState> chunks) {
                UpdateState last = chunks.get(chunks.size() - 1);

                statusLabel.setText(last.message());

                if (last.speedKBps() > 0) {
                    speedLabel.setText(String.format("%.1f KB/s  ·  ETA %ds", last.speedKBps(), last.etaSeconds()));
                } else {
                    speedLabel.setText("");
                }

                animateProgressTo(last.progress());
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    Logger.error("Update failed", e);
                    isError = true;
                    statusLabel.setForeground(ERROR_COLOR);
                    statusLabel.setText("Update failed — closing in 5s");
                    speedLabel.setText("");
                    progressBar.setError();
                    progressBar.setValue(100);

                    Timer t = new Timer(5000, ev -> fadeOutAndExit(1));
                    t.setRepeats(false);
                    t.start();
                }
            }
        };

        worker.execute();
    }

    // Progress bar
    private static class RoundProgressBar extends JComponent {
        private int value = 0;
        private boolean error = false;
        private float shimmer = 0f;
        private final Timer shimmerTimer;

        RoundProgressBar() {
            setOpaque(true);

            shimmerTimer = new Timer(30, e -> {
                shimmer = (shimmer + 0.018f) % 1.0f;
                repaint();
            });
        }

        void setValue(int v) {
            this.value = Math.max(0, Math.min(100, v));
            int fillW = (int) ((double) this.value / 100 * getWidth());
            if (!error && fillW > 20) {
                if (!shimmerTimer.isRunning()) shimmerTimer.start();
            } else {
                shimmerTimer.stop();
            }
            repaint();
        }

        void setError() {
            this.error = true;
            shimmerTimer.stop();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int h = getHeight();
            int w = getWidth();

            g2.setColor(BG_DARK);
            g2.fillRect(0, 0, w, h);

            // Track
            g2.setColor(TRACK_BG);
            g2.fillRoundRect(0, 0, w, h, h, h);

            int fillW = (int) ((double) value / 100 * w);
            if (fillW > 0) {
                Color c1 = error ? ERROR_COLOR : ACCENT;
                Color c2 = error ? new Color(0xFF, 0x88, 0x88) : ACCENT_BRIGHT;
                GradientPaint gp = new GradientPaint(0, 0, c1, fillW, 0, c2);

                Shape clip = new RoundRectangle2D.Float(0, 0, w, h, h, h);
                g2.setClip(clip);

                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, fillW, h, h, h);

                if (!error && fillW > 20) {
                    int sweep = 60;
                    int shimX = (int) (shimmer * (fillW + sweep)) - sweep;
                    int drawX  = Math.max(shimX, 0);
                    int drawX2 = Math.min(shimX + sweep, fillW);
                    if (drawX2 > drawX) {
                        GradientPaint shine = new GradientPaint(
                                shimX,         0, new Color(255, 255, 255, 0),
                                shimX + sweep, 0, new Color(255, 255, 255, 40)
                        );
                        g2.setPaint(shine);
                        g2.fillRect(drawX, 0, drawX2 - drawX, h);
                    }
                }

                g2.setClip(null);
            }

            g2.dispose();
        }
    }

    private BufferedImage loadBannerImage() {
        try (InputStream is = getClass().getResourceAsStream("/banner.png")) {
            if (is != null) return ImageIO.read(is);
            File fallback = new File("banner.png");
            if (fallback.exists()) return ImageIO.read(fallback);
        } catch (Exception e) {
            Logger.error("Failed to load banner", e);
        }
        return null;
    }

    private Font loadFont(float size) {
        try (InputStream is = getClass().getResourceAsStream("/fonts/Inter-Regular.ttf")) {
            if (is != null) {
                return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, size);
            }
        } catch (Exception ignored) {}
        return new Font(Font.SANS_SERIF, Font.PLAIN, (int) size);
    }

    private void animateProgressTo(int target) {
        if (progressTimer != null && progressTimer.isRunning()) {
            progressTimer.stop();
        }

        progressTimer = new Timer(8, null);
        progressTimer.addActionListener(e -> {
            if (animatedProgress < target) {
                animatedProgress++;
            } else if (animatedProgress > target) {
                animatedProgress--;
            } else {
                progressTimer.stop();
                return;
            }
            progressBar.setValue(animatedProgress);
        });

        progressTimer.start();
    }

    private void fadeIn() {
        setOpacity(0f);
        Timer t = new Timer(12, null);
        t.addActionListener(e -> {
            float o = getOpacity() + 0.04f;
            if (o >= 1f) { setOpacity(1f); t.stop(); }
            else setOpacity(o);
        });
        t.start();
    }

    private void fadeOutAndExit(int code) {
        Timer t = new Timer(12, null);
        t.addActionListener(e -> {
            float o = getOpacity() - 0.04f;
            if (o <= 0f) { setOpacity(0f); t.stop(); System.exit(code); }
            else setOpacity(o);
        });
        t.start();
    }
}
