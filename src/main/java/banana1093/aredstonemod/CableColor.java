package banana1093.aredstonemod;

import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;

public enum CableColor implements StringIdentifiable {
    WHITE("white"),
    ORANGE("orange"),
    MAGENTA("magenta"),
    LIGHT_BLUE("light_blue"),
    YELLOW("yellow"),
    LIME("lime"),
    PINK("pink"),
    GRAY("gray"),
    LIGHT_GRAY("light_gray"),
    CYAN("cyan"),
    PURPLE("purple"),
    BLUE("blue"),
    BROWN("brown"),
    GREEN("green"),
    RED("red"),
    BLACK("black");

    private final String name;

    CableColor(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }


    public CableColor next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static CableColor dye(DyeColor c) {
        return values()[c.ordinal()];
    }
}
