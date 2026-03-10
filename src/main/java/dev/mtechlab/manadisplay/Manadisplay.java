package dev.mtechlab.manadisplay;

import dev.mtechlab.manadisplay.configs.ManaDisplayConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Manadisplay.MODID)
public class Manadisplay {
    public static final String MODID = "manadisplay";

    public Manadisplay() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ManaDisplayConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }
}