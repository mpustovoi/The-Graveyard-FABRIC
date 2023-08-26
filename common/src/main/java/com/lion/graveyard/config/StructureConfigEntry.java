package main.java.com.lion.graveyard.config;

public class StructureConfigEntry {
    public final boolean enabled;

    private StructureConfigEntry(boolean enabled) {
        this.enabled = enabled;
    }

    public static StructureConfigEntry of() {
        return new StructureConfigEntry(true);
    }

}