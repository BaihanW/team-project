package app;

import javax.swing.*;
import java.awt.*;
import view.UITheme;

public class Main {
    public static void main(String[] args) {
        AppBuilder appBuilder = new AppBuilder();
        JFrame application = appBuilder
                .addSearchView()
                .addSearchUseCase()
                .build();

        // Apply window decoration settings
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.setUndecorated(!UITheme.SHOW_WINDOW_DECORATION);

        // Apply fullscreen or windowed mode
        if (UITheme.FULLSCREEN_ENABLED) {
            // Fullscreen mode
            GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .setFullScreenWindow(application);
        } else {
            // Windowed mode
            application.setSize(UITheme.WINDOW_WIDTH, UITheme.WINDOW_HEIGHT);
            application.setLocationRelativeTo(null);

            // Apply maximize setting
            if (UITheme.MAXIMIZE_WINDOW) {
                application.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        }

        application.setVisible(true);
    }
}



