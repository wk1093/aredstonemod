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

    public int getRgb() {
        return switch (this) {
            case ORANGE -> 0xFFA500;
            case MAGENTA -> 0xFF00FF;
            case LIGHT_BLUE -> 0xADD8E6;
            case YELLOW -> 0xFFFF00;
            case LIME -> 0x00FF00;
            case PINK -> 0xFFC0CB;
            case GRAY -> 0x808080;
            case LIGHT_GRAY -> 0xD3D3D3;
            case CYAN -> 0x00FFFF;
            case PURPLE -> 0x800080;
            case BLUE -> 0x0000FF;
            case BROWN -> 0xA52A2A;
            case GREEN -> 0x008000;
            case RED -> 0xFF0000;
            case BLACK -> 0x000000;
            default -> 0xFFFFFF;
        };
    }
}
