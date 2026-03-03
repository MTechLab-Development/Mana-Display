package dev.mtechlab.manadisplay;

import dev.mtechlab.manadisplay.configs.ManaDisplayConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(Manadisplay.MODID)
public class Manadisplay {
    public static final String MODID = "manadisplay";

    public Manadisplay(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ClientModEvents::onClientSetup);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ManaDisplayConfig.SPEC);
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}