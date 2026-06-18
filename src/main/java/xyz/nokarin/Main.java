package xyz.nokarin;

import xyz.nokarin.gui.UpdaterWindow;
import xyz.nokarin.handler.SelfUpdate;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        String currentVersion = args[0];
        String mcVersion = args[1];
        String loader = args[2];
        String versionState = args[3];
        String instancePath = args[4];

        new SelfUpdate().checkAndUpdateAsync();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            UpdaterWindow window = new UpdaterWindow(
                    currentVersion, mcVersion, loader, versionState, instancePath + "/mods"
            );
            window.setVisible(true);
        });
    }
}
