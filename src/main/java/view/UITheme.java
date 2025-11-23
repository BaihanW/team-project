package view;

import java.awt.*;

/**
 * Centralized theme configuration for all UI components.
 * Modify colors and styling in this class to easily customize the entire application appearance.
 */
public class UITheme {

    // ============ SIDEBAR (LEFT PANEL) COLORS ============
    /** Background color for the left sidebar containing the search and stops list */
    public static final Color SIDEBAR_BACKGROUND = new Color(7, 7, 7);

    // ============ SEARCH BAR & INPUT COLORS ============
    /** Background color for suggestion popup border */
    public static final Color SEARCH_POPUP_BORDER = Color.GRAY;

    // ============ STOPS LIST COLORS ============
    /** Background color for the stops list */
    public static final Color STOPS_LIST_BACKGROUND = new Color(55, 55, 55);

    /** Background color for a selected stop in the list */
    public static final Color STOPS_LIST_SELECTED = new Color(30, 30, 30);

    /** Color for stop badges (numbered circles) */
    public static final Color STOP_BADGE_BACKGROUND = new Color(77, 77, 89);

    /** Color for the selected stop's badge (numbered circle) - stands out from other badges */
    public static final Color STOP_BADGE_SELECTED_BACKGROUND = new Color(255, 100, 0);

    /** Border and outline color for stop badges */
    public static final Color STOP_BADGE_BORDER = new Color(255, 255, 255);

    /** Text color for stop badges (numbers) */
    public static final Color STOP_BADGE_TEXT = new Color(255, 255, 255);

    /** Text color for stop names in the list */
    public static final Color STOP_NAME_TEXT = new Color(255, 255, 255);

    // ============ ROUTE COLORS ============
    /** Primary color for routes drawn on the map */
    public static final Color ROUTE_COLOR = new Color(0, 120, 255);

    /** Color for route progress bar */
    public static final Color ROUTE_PROGRESS_BAR = new Color(0, 120, 255);

    // ============ TOP PANEL (SEARCH & ZOOM) COLORS ============
    /** Background color for the top panel containing search progress and zoom buttons */
    public static final Color TOP_PANEL_BACKGROUND = new Color(50, 50, 50);

    // ============ BOTTOM PANEL (PROGRESS & REROUTE) COLORS ============
    /** Background color for the bottom panel containing progress bar and "Start New Map" button */
    public static final Color BOTTOM_PANEL_BACKGROUND = new Color(50, 50, 50);

    // ============ PROGRESS BOX COLORS ============
    /** Background color for the progress/reroute information box */
    public static final Color PROGRESS_BOX_BACKGROUND = new Color(255, 255, 255, 230);

    /** Border color for the progress box */
    public static final Color PROGRESS_BOX_BORDER = new Color(180, 180, 180);

    // ============ MARKER COLORS ============
    /** Background color for numbered markers on the map */
    public static final Color MARKER_BACKGROUND = new Color(220, 60, 60);

    /** Text color for marker numbers */
    public static final Color MARKER_TEXT = new Color(255, 255, 255);

    // ============ DIALOG/NOTIFICATION COLORS ============
    /** Background color for notification labels */
    public static final Color NOTIFICATION_BACKGROUND = new Color(255, 255, 150);

    // ============ BUTTON STYLING ============
    /** Background color for all buttons */
    public static final Color BUTTON_BACKGROUND = new Color(70, 130, 180);

    /** Text/foreground color for all buttons */
    public static final Color BUTTON_TEXT = new Color(255, 255, 255);

    /** Border color for all buttons */
    public static final Color BUTTON_BORDER = new Color(50, 100, 150);

    /** Focus/hover color for buttons */
    public static final Color BUTTON_HOVER = new Color(90, 150, 200);

    // ============ BUTTON TEXT LABELS ============
    /** Text for Search button */
    public static final String BUTTON_TEXT_SEARCH = "Search";

    /** Text for Route button */
    public static final String BUTTON_TEXT_ROUTE = "Route";

    /** Text for Zoom In button */
    public static final String BUTTON_TEXT_ZOOM_IN = "+";

    /** Text for Zoom Out button */
    public static final String BUTTON_TEXT_ZOOM_OUT = "-";

    /** Text for Up button (move stop up in list) */
    public static final String BUTTON_TEXT_UP = "Up";

    /** Text for Down button (move stop down in list) */
    public static final String BUTTON_TEXT_DOWN = "Down";

    /** Text for Remove button (remove stop from list) */
    public static final String BUTTON_TEXT_REMOVE = "Remove";

    /** Text for Start New Map button */
    public static final String BUTTON_TEXT_START_NEW_MAP = "Start new map";

    // ============ BUTTON SIZES ============
    /** Width of Search button */
    public static final int BUTTON_SEARCH_WIDTH = 60;

    /** Height of Search button */
    public static final int BUTTON_SEARCH_HEIGHT = 24;

    /** Width of Route button */
    public static final int BUTTON_ROUTE_WIDTH = 60;

    /** Height of Route button */
    public static final int BUTTON_ROUTE_HEIGHT = 24;

    /** Width of Zoom In button */
    public static final int BUTTON_ZOOM_WIDTH = 36;

    /** Height of Zoom In button */
    public static final int BUTTON_ZOOM_HEIGHT = 24;

    /** Width of Zoom Out button (same as Zoom In) */
    // Uses BUTTON_ZOOM_WIDTH and BUTTON_ZOOM_HEIGHT

    /** Width of Up button */
    public static final int BUTTON_CONTROL_WIDTH = 70;

    /** Height of Up button */
    public static final int BUTTON_CONTROL_HEIGHT = 24;

    // Uses BUTTON_CONTROL_WIDTH and BUTTON_CONTROL_HEIGHT for Down and Remove buttons

    /** Width of Start New Map button */
    public static final int BUTTON_START_NEW_MAP_WIDTH = 120;

    /** Height of Start New Map button */
    public static final int BUTTON_START_NEW_MAP_HEIGHT = 34;

    /** Font size for all buttons */
    public static final float BUTTON_FONT_SIZE = 12f;

    // ============ SPACING CONSTANTS ============
    /** Standard padding/margin inside panels */
    public static final int STANDARD_PADDING = 10;

    /** Standard gap between components */
    public static final int COMPONENT_GAP = 5;

    /** Spacing above the map area */
    public static final int MAP_TOP_SPACING = 0;

    /** Spacing below the map area */
    public static final int MAP_BOTTOM_SPACING = 0;

    // ============ SIZING CONSTANTS ============
    /** Width of the sidebar (left panel) */
    public static final int SIDEBAR_WIDTH = 350;

    // ============ WINDOW RESOLUTION & DISPLAY ============
    /** Default window width (in pixels) - change this to customize width */
    public static final int WINDOW_WIDTH = 1200;

    /** Default window height (in pixels) - change this to customize height */
    public static final int WINDOW_HEIGHT = 800;

    /** Enable fullscreen mode (true = fullscreen, false = windowed) */
    public static final boolean FULLSCREEN_ENABLED = false;

    /** Maximize window on startup (only applies if FULLSCREEN_ENABLED is false) */
    public static final boolean MAXIMIZE_WINDOW = false;

    /** Window decoration style (true = show title bar and borders, false = no decoration) */
    public static final boolean SHOW_WINDOW_DECORATION = true;

    /** Default window width (legacy constant - use WINDOW_WIDTH instead) */
    public static final int DEFAULT_WINDOW_WIDTH = WINDOW_WIDTH;

    /** Default window height (legacy constant - use WINDOW_HEIGHT instead) */
    public static final int DEFAULT_WINDOW_HEIGHT = WINDOW_HEIGHT;

    /** Height of the progress/reroute panel at the bottom */
    public static final int PROGRESS_PANEL_HEIGHT = 72;

    /** Height of the top search panel */
    public static final int SEARCH_PANEL_HEIGHT = 44;

    // ============ FONT CONSTANTS ============
    /** Font size for labels and small text */
    public static final float SMALL_FONT_SIZE = 12f;

    /** Font size for standard text */
    public static final float STANDARD_FONT_SIZE = 14f;

    /** Font size for bold headers */
    public static final float HEADER_FONT_SIZE = 16f;

    // ============ STROKE CONSTANTS ============
    /** Stroke width for route lines */
    public static final float ROUTE_STROKE_WIDTH = 6f;

    /** Stroke width for badge outlines */
    public static final float BADGE_STROKE_WIDTH = 2f;

    // ============ MISC CONSTANTS ============
    /** Stop badge radius for circular badges */
    public static final int STOP_BADGE_RADIUS = 14;

    /** Stop list cell height */
    public static final int STOP_LIST_CELL_HEIGHT = 52;
}

