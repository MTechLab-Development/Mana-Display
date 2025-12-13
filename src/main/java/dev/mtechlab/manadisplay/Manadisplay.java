package dev.mtechlab.manadisplay;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(Manadisplay.MODID)
public class Manadisplay {
    public static final String MODID = "manadisplay";

    public Manadisplay() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}