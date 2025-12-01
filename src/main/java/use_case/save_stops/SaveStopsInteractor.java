package use_case.save_stops;

public class SaveStopsInteractor implements SaveStopsInputBoundary {

    private final SaveStopsDataAccessInterface dao;
    private final SaveStopsOutputBoundary presenter;

    public SaveStopsInteractor(SaveStopsDataAccessInterface dao,
                               SaveStopsOutputBoundary presenter) {
        this.dao = dao;
        this.presenter = presenter;
    }

    @Override
    public void execute(SaveStopsInputData data) {
        try {
            dao.save(data.getNames(), data.getPositions());
            presenter.presentSuccess();
        } catch (Exception e) {
            presenter.presentFailure(e.getMessage());
        }
    }
}