package com.emergencyrouter.enums;

/**
 * Supported routing algorithms exposed to the Swing user interface.
 *
 * <p>Impact on the system: this enum gives the Swing algorithm selector a
 * stable list of choices instead of relying on raw strings. The controller uses
 * these values to create the correct {@code RouteStrategy} implementation.</p>
 *
 * <p>Main methods in this file:</p>
 * <ul>
 *     <li>{@link #getDisplayName()} returns user-friendly text for labels and
 *     logs.</li>
 *     <li>{@link #toString()} lets {@code JComboBox} display the friendly name
 *     automatically.</li>
 * </ul>
 */
public enum RoutingAlgorithm {
    DIJKSTRA("Dijkstra"),
    HUB_LABEL("Hub Label"),
    FASTEST("Fastest"),
    SHORTEST_DISTANCE("Shortest Distance");

    private final String displayName;

    RoutingAlgorithm(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the user-facing algorithm name.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the display name for Swing combo boxes.
     *
     * @return display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
