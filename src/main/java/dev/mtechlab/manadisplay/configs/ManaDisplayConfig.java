package dev.mtechlab.manadisplay.configs;

import net.minecraftforge.common.ForgeConfigSpec;

public class ManaDisplayConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue OVERLAY_ENABLED;

    public static final ForgeConfigSpec.BooleanValue SHOW_POOLS;
    public static final ForgeConfigSpec.BooleanValue SHOW_SPREADERS;
    public static final ForgeConfigSpec.BooleanValue SHOW_FLOWERS;

    static {
        BUILDER.comment("Mana Display client config").push("overlay");

        OVERLAY_ENABLED = BUILDER.comment("Enable mana display overlay globally").define("enabled", true);
        SHOW_POOLS = BUILDER.comment("Show overlay above mana pools").define("pools", true);
        SHOW_SPREADERS = BUILDER.comment("Show overlay above mana spreaders").define("spreaders", false);
        SHOW_FLOWERS = BUILDER.comment("Show overlay above generating flowers").define("flowers", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}