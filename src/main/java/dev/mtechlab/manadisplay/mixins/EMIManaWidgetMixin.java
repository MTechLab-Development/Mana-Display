package dev.mtechlab.manadisplay.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.integration.emi.ManaWidget;

import java.util.List;

import static vazkii.botania.client.gui.HUDHandler.manaBar;

@Mixin(ManaWidget.class)
public abstract class EMIManaWidgetMixin extends Widget {
	@Final
	@Shadow(remap = false)
	private int mana;

	@Final
	@Shadow(remap = false)
	private int maxMana;

    @Redirect(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lvazkii/botania/client/gui/HUDHandler;renderManaBar(Lnet/minecraft/client/gui/GuiGraphics;IIIFII)V"
			)
	)
	private void bypassManaDisplay(GuiGraphics gui, int x, int y, int color, float alpha, int mana, int maxMana) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
		RenderHelper.drawTexturedModalRect(gui, manaBar, x, y, 0, 0, 102, 5);
		int manaPercentage = Math.max(0, (int)((double)mana / maxMana * 100));
		if (manaPercentage == 0 && mana > 0) manaPercentage = 1;

		RenderHelper.drawTexturedModalRect(gui, manaBar, x + 1, y + 1, 0, 5, 100, 3);
		float red = (color >> 16 & 255) / 255.0F;
		float green = (color >> 8 & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		RenderSystem.setShaderColor(red, green, blue, alpha);
		RenderHelper.drawTexturedModalRect(gui, manaBar, x + 1, y + 1, 0, 5, Math.min(100, manaPercentage), 3);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
		Component manaText = Component.translatable(
				"manadisplay.mana",
				Component.literal(String.format("%,d", mana)).withStyle(ChatFormatting.AQUA),
				Component.literal(String.format("%,d", maxMana)).withStyle(ChatFormatting.AQUA)
		).withStyle(ChatFormatting.AQUA);

		FormattedCharSequence sequence = manaText.getVisualOrderText();

		return List.of(ClientTooltipComponent.create(sequence));
	}
}