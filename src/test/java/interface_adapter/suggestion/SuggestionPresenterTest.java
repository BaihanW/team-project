package interface_adapter.suggestion;

import interface_adapter.search.SearchState;
import interface_adapter.search.SearchViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.suggestion.SuggestionOutputData;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SuggestionPresenterTest {

    private SearchViewModel viewModel;
    private AtomicReference<PropertyChangeEvent> lastEvent;

    @BeforeEach
    void setUp() {
        viewModel = new SearchViewModel();
        lastEvent = new AtomicReference<>();
        viewModel.addPropertyChangeListener(lastEvent::set);
    }

    @Test
    void presentSuggestionsUpdatesStateAndClearsError() {
        SearchState initialState = viewModel.getState();
        initialState.setSuggestionError("old error");
        viewModel.setState(initialState);

        SuggestionPresenter presenter = new SuggestionPresenter(viewModel);
        presenter.presentSuggestions(new SuggestionOutputData(List.of("One", "Two")));

        SearchState updated = viewModel.getState();
        assertEquals(List.of("One", "Two"), updated.getSuggestions());
        assertNull(updated.getSuggestionError());
        assertNotNull(lastEvent.get());
        assertEquals(viewModel, lastEvent.get().getSource());
    }

    @Test
    void presentErrorSetsErrorMessage() {
        SuggestionPresenter presenter = new SuggestionPresenter(viewModel);
        presenter.presentError("something went wrong");

        SearchState updated = viewModel.getState();
        assertEquals("something went wrong", updated.getSuggestionError());
        assertNotNull(lastEvent.get());
    }
}