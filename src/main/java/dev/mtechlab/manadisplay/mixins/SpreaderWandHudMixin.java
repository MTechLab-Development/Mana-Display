package dev.mtechlab.manadisplay.mixins;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@Mixin(ManaSpreaderBlockEntity.WandHud.class)
public class SpreaderWandHudMixin {

    @Shadow(remap = false)
    @Final
    private ManaSpreaderBlockEntity spreader;

    @Inject(method = "renderHUD", at = @At("HEAD"), cancellable = true, remap = false)
    public void mana_display$renderHUD(GuiGraphics gui, Window window, Font font, float partialTick, CallbackInfo ci) {

        if(spreader instanceof ManaSpreaderBlockEntityAccessor mtechlab_spreader){
            String spreaderName = (new ItemStack(this.spreader.getBlockState().getBlock())).getHoverName().getString();
            ItemStack lensStack = this.spreader.getItemHandler().getItem(0);
            ItemStack recieverStack = mtechlab_spreader.getReceiver() == null ? ItemStack.EMPTY : new ItemStack(Objects.requireNonNull(this.spreader.getLevel()).getBlockState(mtechlab_spreader.getReceiver().getManaReceiverPos()).getBlock());
            int width = 4 + Collections.max(Arrays.asList(102, font.width(spreaderName), RenderHelper.itemWithNameWidth(lensStack, font), RenderHelper.itemWithNameWidth(recieverStack, font)));
            int height = 22 + (lensStack.isEmpty() ? 0 : 18) + (recieverStack.isEmpty() ? 0 : 18);
            int centerX = window.getGuiScaledWidth() / 2;
            int centerY = window.getGuiScaledHeight() / 2;
            RenderHelper.renderHUDBox(gui, centerX - width / 2, centerY - 5, centerX + width / 2, centerY + 8 + height);
            int color = this.spreader.getSpreaderBlock().getHudColor();
            BotaniaAPIClient.instance().drawSimpleManaHUD(gui, window, font, color, this.spreader.getCurrentMana(), this.spreader.getMaxMana(), spreaderName);
            RenderHelper.renderItemWithNameCentered(gui, window, font, recieverStack, centerY + 30, color);
            RenderHelper.renderItemWithNameCentered(gui, window, font, lensStack, centerY + (recieverStack.isEmpty() ? 30 : 48), color);
        }

        ci.cancel();
    }
}