package app;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        AppBuilder appBuilder = new AppBuilder();
        JFrame application = appBuilder
                .addSearchView()
                .addSearchUseCase()
                .addSuggestionUseCase()
                .addSaveStopsUseCase()
                .addRemoveMarkerUseCase()
                .addGenerateRouteUseCase()
                .addAddMarkerUseCase()
                .addReorderUseCase()
                .build();

        application.pack();
        application.setLocationRelativeTo(null);
        application.setVisible(true);
    }
}
