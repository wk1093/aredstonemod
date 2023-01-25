package banana1093.aredstonemod;

import net.minecraft.util.StringIdentifiable;

public enum CableSide implements StringIdentifiable {
    NONE("none"),
    CABLE("cable"),
    REPEATER("repeater");

    private final String name;

    CableSide(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public String asString() {
        return this.name;
    }



}
