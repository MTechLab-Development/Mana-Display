package dev.mtechlab.manadisplay.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.client.core.handler.TooltipHandler;

@Mixin(TooltipHandler.class)
public class TooltipHandlerMixin {

    @Inject(method = "onTooltipEvent", at = @At("TAIL"), remap = false)
    private static void mana_display$onTooltipEvent(ItemTooltipEvent event, CallbackInfo ci) {
        ItemStack stack = event.getItemStack();

        if (!(stack.getItem() instanceof IManaItem)) {
            return;
        }

        IManaItem manaItem = (IManaItem) stack.getItem();
        int mana = manaItem.getMana(stack);
        int maxMana = manaItem.getMaxMana(stack);

        if (maxMana <= 0) {
            return;
        }

        ITextComponent text = new TranslationTextComponent(
                "manadisplay.mana",
                mana,
                maxMana
        ).withStyle(TextFormatting.AQUA);

        event.getToolTip().add(text);
    }
}
