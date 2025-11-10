# TripPlanner
A simple Java Swing application for travel route planning and visualization.

## Architecture
Follows Clean Architecture principles (Entities → Use Cases → Interface Adapters → Frameworks).

## Team Members
- Baihan -
- DongMyeong Park
- Raymond Zhang

SwingTripPlanner Project Blueprint

Domain: Interactive Map Navigator for Desktop Applications

Description:
Our project is a desktop application that allows users to search for locations, place markers, and navigate between points on a map. It integrates online APIs for geocoding and routing, and presents results on an interactive map GUI. Users can plan routes, visualize paths, and interact with markers, making this tool useful for personal travel planning or urban navigation simulations.

APIs:
- OpenStreetMap Nominatim API (location search / geocoding)
- OSRM API (route planning / driving directions)

User Stories：

As a user, I want to enter a location name in a search bar so that the map can center on that location.

As a user, I want to add a marker at a specific location so that I can mark points of interest.

As a user, I want to select two markers and see the route between them so that I can visualize directions.

As a user, I want to zoom in and out and pan the map so that I can explore the area in detail.

Use Cases：

Use Case 1: Search Location
- Lead:
- Main Flow:
    1. User enters a location in the search bar.
    2. System calls the OpenStreetMap Nominatim API to get coordinates.
    3. Map centers on the returned coordinates.
    4. Optionally, a marker is added.
- Alternative Flow: Location not found → show error message.

Use Case 2: Add Marker at Center
- Lead:
- Main Flow:
    1. User clicks "Add Marker" button.
    2. System records map center coordinates.
    3. System creates a marker at that location.
- Alternative Flow: Duplicate marker → ignore。

Use Case 3: Route Between Two Markers
- Lead:
- Main Flow:
    1. User selects two markers.
    2. System calls OSRM API for the route.
    3. System renders route on map.
- Alternative Flow: Fewer than two markers → prompt user to add more.

Use Case 4: Browse / Select Alternative Routes
- Lead:
- Main Flow:
    1. User selects two markers with multiple route options.
    2. System calls OSRM API with alternative routes enabled.
    3. System displays multiple routes on the map (different colors or styles).
    4. User can select one route to visualize prominently.
- Alternative Flow: API does not return alternatives → do nothing

Use Case 5: Connect All Markers into a Single Route
- Lead:
- Main Flow:
    1. User clicks "Connect All Markers" button.
    2. System retrieves all markers in the order added (or nearest-neighbor order).
    3. System calls OSRM API to generate a full connected path.
    4. System draws the combined route on the map.
- Alternative Flow: Fewer than two markers → notify user to add more.

Entities：

MapViewer
- Instance Variables: zoomLevel: int, centerPosition: GeoPosition, tileFactory: TileFactory
- Notes: Handles map rendering and tile management

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
    - Marker, Route, GeoPosition
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
