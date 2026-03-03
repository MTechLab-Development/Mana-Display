package dev.mtechlab.manadisplay.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.mtechlab.manadisplay.configs.ManaDisplayConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber
public class ManaDisplayCommand {

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("manadisplay")
                        .then(Commands.literal("on").executes(ctx -> {
                            setAndSave(ManaDisplayConfig.OVERLAY_ENABLED, true);
                            ctx.getSource().sendSuccess(() -> Component.literal("Mana display: ON"), false);
                            return 1;
                        }))
                        .then(Commands.literal("off").executes(ctx -> {
                            setAndSave(ManaDisplayConfig.OVERLAY_ENABLED, false);
                            ctx.getSource().sendSuccess(() -> Component.literal("Mana display: OFF"), false);
                            return 1;
                        }))

                        .then(categoryToggle("pool", ManaDisplayConfig.SHOW_POOLS, "Pools"))
                        .then(categoryToggle("spreader", ManaDisplayConfig.SHOW_SPREADERS, "Spreaders"))
                        .then(categoryToggle("flower", ManaDisplayConfig.SHOW_FLOWERS, "Flowers"))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> categoryToggle(String literal, ModConfigSpec.BooleanValue value, String label) {
        return Commands.literal(literal)
                .then(Commands.literal("on").executes(ctx -> {
                    setAndSave(value, true);
                    ctx.getSource().sendSuccess(() -> Component.literal(label + ": ON"), false);
                    return 1;
                }))
                .then(Commands.literal("off").executes(ctx -> {
                    setAndSave(value, false);
                    ctx.getSource().sendSuccess(() -> Component.literal(label + ": OFF"), false);
                    return 1;
                }));
    }

    private static void setAndSave(ModConfigSpec.BooleanValue value, boolean enabled) {
        value.set(enabled);
        value.save();
    }
}