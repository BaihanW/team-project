package app;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppBuilder builder = new AppBuilder();

            JFrame app = builder
                    .addSearchView()
                    .addSearchUseCase()
                    .addSaveStopsUseCase()
                    .addSuggestionUseCase()
                    .addRemoveMarkerUseCase()
                    .addGenerateRouteUseCase()
                    .loadStopsOnStartup()
                    .build();
            app.pack();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}
