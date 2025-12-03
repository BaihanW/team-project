# TripPlanner
A simple Java Swing application for travel route planning and visualization.

## Architecture
Follows Clean Architecture principles (Entities → Use Cases → Interface Adapters → Frameworks).

## Team Members
- Baihan
- DongMyeong Park
- Raymond Zhang
- Yi Zhao

SwingTripPlanner Project Blueprint

Domain: Interactive Map Navigator for Desktop Applications

Description:
Our project is a desktop application that allows users to search for locations, place markers, and navigate between points on a map. It integrates online APIs for geocoding and routing, and presents results on an interactive map GUI. Users can plan routes, visualize paths, and interact with markers, making this tool useful for personal travel planning or urban navigation simulations.

APIs:
- OpenStreetMap Nominatim API (location search / geocoding)
- OSRM API (route planning / driving directions)

User Stories：

As a user, I want to enter a location name in a search bar so that the map can center on that location.

As a user, I want to add a marker at a specific location so that I can mark points of interest for my trip.

As a user, I want to delete a specific marker from my list so that I can remove stops I no longer wish to visit.

As a user, I want to move stops up or down in my list so that I can arrange the order of locations for my trip.

As a user, I want to visualize a connected path between all my selected markers so that I can see the travel directions on the map.

As a user, I want to save my current list of stops to a local file so that I can reload and continue planning my trip later.

As a user, I want to see a list of location suggestions as I type so that I can quickly select the correct place without typing the full name.

Use Cases：

Use Case 1: Search Location
Lead: Baihan
MVP: Yes
Main Flow:
1. User enters a location name (e.g., "Toronto") into the search bar.
2. System sends the query to the OpenStreetMap Nominatim API.
3. System retrieves the latitude and longitude coordinates.
4. System centers the map on the retrieved coordinates.
5. System adds the location to the user's itinerary list.
Alternative Flows:
- Location Not Found: If the API returns no results, the system displays an error popup ("Location does not exist").
- Network Error: If the API cannot be reached, the system displays a network error message.

Use Case 2: Add Marker
Lead: DongMyeong Park
MVP: Yes
Main Flow:
1. User clicks a specific point on the map.
2. System captures the geographic coordinates (latitude/longitude) of the click.
3. System calls the API to perform reverse geocoding to get the location name (optional, based on implementation).
4. System adds a marker to the map at that location.
5. System appends the new location to the ordered list of stops.
Alternative Flows:
- Duplicate Location: If the user attempts to add a marker that already exists (exact coordinates), the system ignores the action or notifies the user.

Use Case 3: Remove Marker
Lead: Raymond Zhang
MVP: Yes
Main Flow:
1. User selects a stop from the itinerary list.
2. User clicks the "Remove" button.
3. System removes the corresponding marker from the map.
4. System removes the stop from the ordered list.
5. System refreshes the route (if one was generated).
Alternative Flows:
- No Selection: If the user clicks "Remove" without selecting a stop, the system displays an error message ("Select a stop to remove").

Use Case 4: Reorder Itinerary
Lead: MP
MVP: Yes
Main Flow:
1. User selects a stop in the list.
2. User clicks the "Up" or "Down" button.
3. System swaps the selected stop with the adjacent stop in the list.
4. System updates the order of the list display.
5. System clears the existing route (forcing the user to regenerate it with the new order).
Alternative Flows:
- Boundary Reorder: If the user tries to move the top item up or the bottom item down, the system ignores the request or displays a message ("Cannot move marker in that direction").

Use Case 5: Generate Route (Connect All Markers)
Lead: Yi Zhao
MVP: Yes
Main Flow:
1. User clicks the "Route" button.
2. System validates that there are at least two stops in the list.
3. System iterates through the list of stops in order.
4. System sends requests to the OSRM API for each segment (Point A to Point B).
5. System aggregates the route segments and draws a polyline connecting all markers on the map.
Alternative Flows:
- Insufficient Stops: If there are fewer than two stops, the system displays an error ("Add at least two stops to compute a full route").
- Route Not Found: If the API cannot find a path between two points (e.g., across an ocean), the system falls back to drawing a straight line for that segment.

Use Case 6: Save Stops
Lead: Baihan
MVP: Yes
Main Flow:
1. User clicks the "Save" button.
2. System retrieves the current list of stop names and coordinates.
3. System writes this data to a local persistent file (stoplist.txt).
4. System displays a success message ("Stops saved!").
Alternative Flows:
- Write Error: If the file cannot be written, the system displays an error message ("Failed to save stops").

Use Case 7: Search Suggestions
Lead: Raymond Zhang
MVP: No (Enhancement)
Main Flow:
1. User begins typing in the search bar.
2. System waits for a brief pause (debounce).
3. System requests matching location names from the API.
4. System displays a dropdown list of suggested locations.
5. User clicks a suggestion to auto-fill the search bar and execute the search.
Alternative Flows:
- No Matches: The system displays no suggestions.

Entities：

Marker
- Instance Variables: position: GeoPosition
- Notes: Represents a location marker on the map

Route
- Instance Variables: start: GeoPosition, end: GeoPosition, path: List<GeoPosition>
- Notes: Stores routing information and coordinates for drawing

UserInput
- Instance Variables: searchQuery: String
- Notes: Captures user actions (search, add marker, route request)

APIs

OpenStreetMap
- Documentation: https://nominatim.org/release-docs/develop/api/Search/
- Usage:
    - Send HTTP GET requests with a location string.
    - Receive JSON with latitude and longitude.
- Status: Successfully integrated and tested.

OSRM API
- Documentation: http://project-osrm.org/docs/v5.24.0/api/
- Usage:
    - Request driving directions between two GeoPositions.
    - Return JSON containing route geometry for visualization.
- Status: Successfully integrated and tested.

Clean Architecture realization challenges
1. Entities:
    - Marker, location
    - Business logic: mostly data representation

2. Use Cases / Interactors:
    - SearchLocationInteractor
    - AddMarkerInteractor
    - RouteCalculationInteractor
    - AlternativeRoutesInteractor
    - ConnectAllMarkersInteractor

3. Interface Adapters:
    - Controllers handling Swing events
    - Presenters formatting route and marker data for display
    - Swing GUI components (JFrame, JPanel, buttons, map)

4. *Frameworks / External Interface (Framework Layer):
    - JXMapViewer2 (map rendering)
    - OpenStreetMap Nominatim API (HTTP)
    - OSRM API (HTTP)
    - org.json (JSON parsing)
