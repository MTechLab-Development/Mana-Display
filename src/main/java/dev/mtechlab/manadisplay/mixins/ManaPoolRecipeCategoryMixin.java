package dev.mtechlab.manadisplay.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.api.recipe.IManaInfusionRecipe;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.integration.jei.ManaPoolRecipeCategory;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;

@Mixin(ManaPoolRecipeCategory.class)
public abstract class ManaPoolRecipeCategoryMixin implements IRecipeCategory<IManaInfusionRecipe> {

    @Unique private static final int MANA_BAR_X = 20;
    @Unique private static final int MANA_BAR_Y = 50;
    @Unique private static final int MANA_BAR_WIDTH = 102;
    @Unique private static final int MANA_BAR_HEIGHT = 5;

    /* ===== Перехват рендера полоски ===== */

    @Redirect(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lvazkii/botania/client/core/handler/HUDHandler;renderManaBar(Lcom/mojang/blaze3d/matrix/MatrixStack;IIIFII)V"
            ),
            remap = false
    )
    private void mana_display$renderManaBar(
            MatrixStack ms,
            int x,
            int y,
            int color,
            float alpha,
            int mana,
            int maxMana
    ) {
        Minecraft mc = Minecraft.getInstance();

        // фон
        RenderSystem.color4f(1F, 1F, 1F, alpha);
        mc.textureManager.bind(HUDHandler.manaBar);
        RenderHelper.drawTexturedModalRect(ms, x, y, 0, 0, 102, 5);
        RenderHelper.drawTexturedModalRect(ms, x + 1, y + 1, 0, 5, 100, 3);

        // вычисляем процент
        int pct = Math.max(1, (int) ((double) mana / maxMana * 100));

        // ВОТ ОНО — цвет маны
        float red   = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8  & 255) / 255.0F;
        float blue  = (color       & 255) / 255.0F;

        RenderSystem.color4f(red, green, blue, alpha);

        // заполнение
        RenderHelper.drawTexturedModalRect(
                ms,
                x + 1,
                y + 1,
                0,
                5,
                Math.min(100, pct),
                3
        );

        // сброс
        RenderSystem.color4f(1F, 1F, 1F, 1F);
    }

    /* ===== Тултип при наведении ===== */

    @Inject(
            method = "draw",
            at = @At("TAIL"),
            remap = false
    )
    private void mana_display$drawTooltip(
            IManaInfusionRecipe recipe,
            MatrixStack ms,
            double mouseX,
            double mouseY,
            CallbackInfo ci
    ) {
        if (!mana_display$isMouseOverManaBar(mouseX, mouseY)) {
            return;
        }

        int mana = recipe.getManaToConsume();
        int max = 100000;

        ITextComponent text = new TranslationTextComponent(
                "manadisplay.mana",
                mana_display$format(mana),
                mana_display$format(max)
        ).withStyle(TextFormatting.AQUA);

        FontRenderer font = Minecraft.getInstance().font;

        Minecraft mc = Minecraft.getInstance();

        GuiUtils.drawHoveringText(
                ms,
                Collections.singletonList(text),
                (int) mouseX,
                (int) mouseY,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                300,
                mc.font
        );

    }

    @Unique
    private boolean mana_display$isMouseOverManaBar(double x, double y) {
        return x >= MANA_BAR_X && x <= MANA_BAR_X + MANA_BAR_WIDTH
                && y >= MANA_BAR_Y && y <= MANA_BAR_Y + MANA_BAR_HEIGHT;
    }

    @Unique
    private static String mana_display$format(int value) {
        return NumberFormat.getNumberInstance(Locale.ROOT)
                .format(value)
                .replace(",", " ");
    }
}
