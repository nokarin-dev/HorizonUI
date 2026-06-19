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
    private static final Color SUCCESS_COLOR = new Color(0x55, 0xDD, 0x88);

    private static final int WINDOW_W = 1200;
    private static final int WINDOW_H = 600;
    private static final int BANNER_H = 468;
    private static final int BOTTOM_H = 132;

    private JLabel statusLabel;
    private JLabel speedLabel;
    private JLabel versionLabel;
    private RoundProgressBar progressBar;
    private int animatedProgress = 0;
    private Timer progressTimer;

    private final UpdateHandler updateHandler;
    private final String currentVersion;

    public UpdaterWindow(String currentVersion, String mcVersion, String loader, String versionState, String modsPath) {
        this.currentVersion = currentVersion;
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

        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        root.setOpaque(false);

        JLabel bannerLabel = createBannerLabel();
        bannerLabel.setBounds(0, 0, WINDOW_W, BANNER_H);
        root.add(bannerLabel);

        JPanel bottom = createBottomPanel();
        bottom.setBounds(0, BANNER_H, WINDOW_W, BOTTOM_H);
        root.add(bottom);

        setShape(new RoundRectangle2D.Double(0, 0, WINDOW_W, WINDOW_H, 18, 18));
        setContentPane(root);
        setVisible(true);
    }

    private JLabel createBannerLabel() {
        JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setColor(BG_MID);
                g2.fillRect(0, 0, getWidth(), getHeight());

                Icon ic = getIcon();
                if (ic != null) {
                    int x = (getWidth() - ic.getIconWidth()) / 2;
                    int y = (getHeight() - ic.getIconHeight()) / 2;
                    ic.paintIcon(this, g2, x, y);
                }

                GradientPaint fade = new GradientPaint(
                        0, getHeight() - 80, new Color(0, 0, 0, 0),
                        0, getHeight(), BG_DARK
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
        label.setIcon(new ImageIcon(img.getScaledInstance(WINDOW_W, ih, Image.SCALE_SMOOTH)));
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

        // Separator
        JSeparator sep = new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0x30, 0x20, 0x48));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        sep.setBounds(0, 0, WINDOW_W, 1);
        panel.add(sep);

        // Status
        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(loadFont(18f));
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setBounds(36, 16, WINDOW_W - 400, 28);
        panel.add(statusLabel);

        // Speed/ETA
        speedLabel = new JLabel("");
        speedLabel.setFont(loadFont(14f));
        speedLabel.setForeground(TEXT_DIM);
        speedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        speedLabel.setBounds(WINDOW_W - 340 - 36, 18, 340, 24);
        panel.add(speedLabel);

        // Progress bar
        progressBar = new RoundProgressBar();
        progressBar.setBounds(36, 56, WINDOW_W - 72, 18);
        panel.add(progressBar);

        // Version label
        versionLabel = new JLabel("v" + currentVersion);
        versionLabel.setFont(loadFont(12f));
        versionLabel.setForeground(TEXT_DIM);
        versionLabel.setBounds(36, BOTTOM_H - 30, 400, 20);
        panel.add(versionLabel);

        return panel;
    }

    private void startUpdateWorker() {
        SwingWorker<VersionInfo, UpdateState> worker = new SwingWorker<>() {

            @Override
            protected VersionInfo doInBackground() throws Exception {
                publish(new UpdateState("Checking for updates...", 0, true));
                return updateHandler.checkForUpdates();
            }

            @Override
            protected void process(List<UpdateState> chunks) {
                UpdateState s = chunks.get(chunks.size() - 1);
                applyState(s);
            }

            @Override
            protected void done() {
                try {
                    VersionInfo update = get();

                    if (update == null) {
                        onUpToDate();
                        return;
                    }

                    startInstallWorker(update);

                } catch (Exception e) {
                    Logger.error("Update check failed", e);
                    onError(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void startInstallWorker(VersionInfo update) {
        SwingWorker<Void, UpdateState> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish(new UpdateState("Found " + update.versionNumber() + " - preparing...", 10));
                updateHandler.performUpdate(update, this::publish);
                return null;
            }

            @Override
            protected void process(List<UpdateState> chunks) {
                UpdateState s = chunks.get(chunks.size() - 1);
                applyState(s);
            }

            @Override
            protected void done() {
                try {
                    get();
                    onSuccess(update.versionNumber());
                } catch (Exception e) {
                    Logger.error("Update install failed", e);
                    onError(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void applyState(UpdateState s) {
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setText(s.message());
        progressBar.setIndeterminate(s.indeterminate());

        if (!s.indeterminate()) {
            animateProgressTo(s.progress());
        }

        if (s.speedKBps() > 0) {
            if (s.etaSeconds() > 0) {
                speedLabel.setText(String.format("%.1f KB/s  ·  ETA %ds", s.speedKBps(), s.etaSeconds()));
            } else {
                speedLabel.setText(String.format("%.1f KB/s", s.speedKBps()));
            }
        } else {
            speedLabel.setText("");
        }
    }

    private void onUpToDate() {
        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setText("Already up to date");
        speedLabel.setText("");
        progressBar.setIndeterminate(false);
        animateProgressTo(100);

        Timer t = new Timer(2500, ev -> fadeOutAndExit(0));
        t.setRepeats(false);
        t.start();
    }

    private void onSuccess(String newVersion) {
        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setText("Updated to " + newVersion + " - relaunch Minecraft to apply");
        speedLabel.setText("");
        versionLabel.setForeground(SUCCESS_COLOR);
        versionLabel.setText("v" + currentVersion + "  →  " + newVersion);
        progressBar.setIndeterminate(false);
        animateProgressTo(100);

        Timer t = new Timer(4000, ev -> fadeOutAndExit(0));
        t.setRepeats(false);
        t.start();
    }

    private void onError(String reason) {
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setText("Update failed - " + (reason != null ? reason : "unknown error"));
        speedLabel.setText("Closing in 5s");
        speedLabel.setForeground(ERROR_COLOR);
        progressBar.setIndeterminate(false);
        progressBar.setError();
        progressBar.setValue(100);

        Timer t = new Timer(5000, ev -> fadeOutAndExit(1));
        t.setRepeats(false);
        t.start();
    }

    // Progress bar
    private static class RoundProgressBar extends JComponent {
        private int value = 0;
        private boolean error = false;
        private boolean indeterminate = false;
        private float sweep = 0f;
        private final Timer animTimer;

        RoundProgressBar() {
            setOpaque(true);

            animTimer = new Timer(22, e -> {
                sweep = (sweep + 0.022f) % 1.0f;
                repaint();
            });
        }

        void setValue(int v) {
            this.value = Math.max(0, Math.min(100, v));
            int fillW = (int) ((double) this.value / 100 * getWidth());
            if (!error && !indeterminate && fillW > 24) {
                if (!animTimer.isRunning()) animTimer.start();
            } else if (!indeterminate) {
                animTimer.stop();
            }
            repaint();
        }

        void setError() {
            this.error = true;
            if (!indeterminate) animTimer.stop();
            repaint();
        }

        void setIndeterminate(boolean indet) {
            this.indeterminate = indet;
            if (indet) {
                if (!animTimer.isRunning()) animTimer.start();
            } else {
                int fillW = (int) ((double) this.value / 100 * Math.max(getWidth(), 1));
                if (!error && fillW <= 24) animTimer.stop();
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight(), arc = h;

            // Background
            g2.setColor(BG_DARK);
            g2.fillRect(0, 0, w, h);

            // Track
            g2.setColor(TRACK_BG);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (indeterminate) {
                paintIndeterminate(g2, w, h, arc);
            } else {
                paintDeterminate(g2, w, h, arc);
            }

            g2.dispose();
        }

        private void paintDeterminate(Graphics2D g2, int w, int h, int arc) {
            int fillW = (int) ((double) value / 100 * w);
            if (fillW <= 0) return;

            Color c1 = error ? ERROR_COLOR : ACCENT;
            Color c2 = error ? new Color(0xFF, 0x88, 0x88) : ACCENT_BRIGHT;

            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            g2.setPaint(new GradientPaint(0, 0, c1, Math.max(fillW, 1), 0, c2));
            g2.fillRoundRect(0, 0, fillW, h, arc, arc);

            // Shimmer
            if (!error && fillW > 24) {
                int band = 56;
                int sx = (int) (sweep * (fillW + band)) - band;
                int x1 = Math.max(sx, 0);
                int x2 = Math.min(sx + band, fillW);
                if (x2 > x1) {
                    g2.setPaint(new GradientPaint(
                            sx, 0, new Color(255, 255, 255, 0),
                            sx + band, 0, new Color(255, 255, 255, 38)
                    ));
                    g2.fillRect(x1, 0, x2 - x1, h);
                }
            }
            g2.setClip(null);
        }

        private void paintIndeterminate(Graphics2D g2, int w, int h, int arc) {
            int fillW = (int) (w * 0.35f);
            float pos = (float) (0.5 - 0.5 * Math.cos(sweep * 2 * Math.PI));
            int x = (int) (pos * (w - fillW));

            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
            g2.setPaint(new GradientPaint(x, 0, ACCENT, x + fillW, 0, ACCENT_BRIGHT));
            g2.fillRoundRect(x, 0, fillW, h, arc, arc);

            g2.setPaint(new GradientPaint(x, 0, new Color(0xA0, 0x80, 0xD0, 0), x + 30, 0, ACCENT));
            g2.fillRect(x, 0, 30, h);
            g2.setPaint(new GradientPaint(x + fillW - 30, 0, ACCENT_BRIGHT, x + fillW, 0, new Color(0xC8, 0xA0, 0xFF, 0)));
            g2.fillRect(x + fillW - 30, 0, 30, h);

            g2.setClip(null);
        }
    }

    private void animateProgressTo(int target) {
        if (progressTimer != null && progressTimer.isRunning()) progressTimer.stop();

        progressTimer = new Timer(8, null);
        progressTimer.addActionListener(e -> {
            if (animatedProgress < target) animatedProgress++;
            else if (animatedProgress > target) animatedProgress--;
            else {
                progressTimer.stop();
                return;
            }
            progressBar.setValue(animatedProgress);
        });
        progressTimer.start();
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
            if (is != null) return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, size);
        } catch (Exception ignored) {
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, (int) size);
    }

    private void fadeIn() {
        setOpacity(0f);
        Timer t = new Timer(12, null);
        t.addActionListener(e -> {
            float o = getOpacity() + 0.04f;
            if (o >= 1f) {
                setOpacity(1f);
                t.stop();
            } else setOpacity(o);
        });
        t.start();
    }

    private void fadeOutAndExit(int code) {
        Timer t = new Timer(12, null);
        t.addActionListener(e -> {
            float o = getOpacity() - 0.04f;
            if (o <= 0f) {
                setOpacity(0f);
                t.stop();
                System.exit(code);
            } else setOpacity(o);
        });
        t.start();
    }
}
