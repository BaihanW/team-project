# Project Plan: TripPlanner

## 1. Purpose
The purpose of TripPlanner is to help users plan routes between locations with different travel modes (driving, public transit, flights, walking, etc.) and visualize these routes on a simple map.

## 2. Goals
- Provide an intuitive Swing-based interface for trip planning.
- Implement Clean Architecture with clear layer boundaries.
- Support multiple route types via external APIs ().
- Allow saving and viewing past plans.
- Resulting a map with all trip details.

## 3. Features
| Feature           | Description                                   | Assigned To |
|-------------------|-----------------------------------------------|-------------|
| Search Location   | Find and brows a location by keyword          |             |
| Mark Location     | Mark a location                               |             |
| Get Route Options | Retrieve travel options between two locations |             |
| Select Route      | Choose one route to include in the plan       |             |
| Save Trip Plan    | Persist the trip plan locally                 |             |
| View Past Plans   | Display and reopen saved trips                |             |

## 4. Architecture Overview
- **Entity Layer:** `Location`, `Route`, `TripPlan`
- **Use Case Layer:** independent logic for search, retrieval, selection, saving
- **Interface Adapters:** controllers, presenters, gateways
- **Frameworks:** Swing GUI and external APIs

## 5. Timeline
| Week | Task |
|------|------|
|      |

