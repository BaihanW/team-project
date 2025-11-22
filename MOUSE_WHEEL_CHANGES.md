# Mouse Wheel Scroll Sensitivity Adjustment

## Summary of Changes

Modified `MapPanel.java` to adjust mouse scroll wheel sensitivity and fix panning behavior.

### Changes Made:

1. **Doubled Mouse Wheel Zoom Sensitivity**
   - Changed `HARDWARE_WHEEL_STEP` constant from `0.6` to `1.2` (line 28)
   - This makes the mouse scroll wheel 2x more sensitive for zooming
   - Scrolling forward now zooms in by twice the magnitude
   - Scrolling backward now zooms out by twice the magnitude

2. **Fixed Hardware Wheel Panning Issue**
   - Modified the scroll event handler logic (line 230)
   - Added condition `else if (!isHardwareWheel)` to ensure pan logic only executes for trackpad gestures
   - This prevents hardware mouse wheel scrolls from causing unwanted map panning
   - Hardware wheel now performs PURE zoom operations centered on the map display

3. **Preserved Trackpad Functionality**
   - Trackpad pinch-to-zoom continues to work as before
   - Trackpad two-finger scroll (panning) continues to work as before
   - No changes to trackpad sensitivity or behavior

### Technical Details:

- **Hardware Wheel Detection**: The code distinguishes between hardware mouse wheel events (discrete wheel rotations) and trackpad events (continuous precise scroll events)
- **Pan Offset Reset**: For hardware wheel events, pan offsets (`panOffsetX`, `panOffsetY`, `pendingHorizontalScroll`) are explicitly reset to prevent panning
- **Zoom Preservation**: The zoom is always performed with the new doubled sensitivity only for hardware wheel events
- **Center-Relative Zoom**: Zoom operations maintain the map center position without shifting

### Behavior After Changes:

| Input | Before | After |
|-------|--------|-------|
| **Mouse Wheel Forward** | Zoom in (~0.6x) + slight map pan down | Zoom in (1.2x) + NO panning |
| **Mouse Wheel Backward** | Zoom out (~0.6x) + slight map pan up | Zoom out (1.2x) + NO panning |
| **Trackpad Pinch** | Zoom in/out with smooth pinch gesture | ✓ Unchanged - works as before |
| **Trackpad Two-Finger Scroll** | Pan the map smoothly | ✓ Unchanged - works as before |

### Files Modified:
- `src/main/java/view/MapPanel.java`
  - Line 28: `HARDWARE_WHEEL_STEP` constant doubled
  - Line 230: Added `else if (!isHardwareWheel)` condition to skip pan logic for hardware wheel

### Testing:
The changes have been compiled and packaged successfully with Maven.

