// src/test/java/use_case/add_marker/AddMarkerInteractorTest.java
package use_case.add_marker;

import entity.Location;
import entity.Marker;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;


class AddMarkerInteractorTest {

    public class InMemoryAddMarkerDataAccess implements AddMarkerDataAccessInterface {

        private final List<Marker> markers = new ArrayList<>();

        @Override
        public boolean exists(Location location) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            return markers.stream().anyMatch(m ->
                    m.getLatitude() == lat &&
                            m.getLongitude() == lng
            );
        }

        @Override
        public void save(Marker marker) {
            markers.add(marker);
        }

        @Override
        public List<Marker> getAllMarkers() {
            return new ArrayList<>(markers);
        }
    }

        @Nested
        public class TestAddMarkerPresenter implements AddMarkerOutputBoundary {

            AddMarkerOutputData successData;
            String failMessage;

            @Override
            public void prepareSuccessView(AddMarkerOutputData response) {
                this.successData = response;
            }

            @Override
            public void prepareFailView(String error) {
                this.failMessage = error;
            }


            @Test
            void addsNewMarkerWhenNotDuplicate() {
                // given
                InMemoryAddMarkerDataAccess repo = new InMemoryAddMarkerDataAccess();
                TestAddMarkerPresenter presenter = new TestAddMarkerPresenter();
                AddMarkerInteractor interactor = new AddMarkerInteractor(repo, presenter);

                double lat = 43.6532;
                double lon = -79.3832;
                AddMarkerInputData input = new AddMarkerInputData(lat, lon);

                // when
                interactor.execute(input);

                assertNotNull(presenter.successData.toString(), "Success output should not be null");
                assertNull(presenter.failMessage, "Fail message should be null on success");

                assertEquals(lat, presenter.successData.getLatitude(), 1e-9);
                assertEquals(lon, presenter.successData.getLongitude(), 1e-9);
            }
        }
    }

