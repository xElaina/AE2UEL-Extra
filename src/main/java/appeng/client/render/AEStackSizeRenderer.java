/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.util.ISlimReadableNumberConverter;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;

/**
 * Renders the stack size (or craft label) for item and fluid stacks in AE2 terminals.
 */
public class AEStackSizeRenderer {

    private static final ISlimReadableNumberConverter SLIM_CONVERTER = ReadableNumberConverter.INSTANCE;
    private static final IWideReadableNumberConverter WIDE_CONVERTER = ReadableNumberConverter.INSTANCE;
    private static final String[] FLUID_NUMBER_FORMATS = new String[] { "#.000", "#.00", "#.0", "#" };

    /**
     * Renders the stack size for a generic AE stack. Dispatches to the appropriate specific method based on the actual
     * type.
     *
     * @param fontRenderer font renderer
     * @param stack        the stack (item or fluid)
     * @param xPos         x position
     * @param yPos         y position
     */
    public void renderStackSize(FontRenderer fontRenderer, IAEStack stack, int xPos, int yPos) {
        if (stack == null) {
            return;
        }

        if (stack instanceof IAEItemStack aeItemStack) {
            renderStackSize(fontRenderer, aeItemStack, xPos, yPos);
        }

        if (stack instanceof IAEFluidStack aeFluidStack) {
            renderStackSize(fontRenderer, aeFluidStack, xPos, yPos);
        }
    }

    /**
     * Renders the stack size for an item stack. May also show a "Craft" label when the stack is craftable and empty or
     * Alt is held.
     *
     * @param fontRenderer font renderer
     * @param aeStack      the item stack
     * @param xPos         x position
     * @param yPos         y position
     */
    public void renderStackSize(FontRenderer fontRenderer, IAEItemStack aeStack, int xPos, int yPos) {
        if (aeStack == null) {
            return;
        }

        final float scaleFactor = AEConfig.instance().useTerminalUseLargeFont() ? 0.85f : 0.5f;
        final float inverseScaleFactor = 1.0f / scaleFactor;
        final int offset = AEConfig.instance().useTerminalUseLargeFont() ? 0 : -1;

        final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
        fontRenderer.setUnicodeFlag(false);

        if ((aeStack.getStackSize() == 0 || GuiScreen.isAltKeyDown()) && aeStack.isCraftable()) {
            final String craftLabelText = AEConfig.instance().useTerminalUseLargeFont()
                    ? GuiText.LargeFontCraft.getLocal()
                    : GuiText.SmallFontCraft.getLocal();

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
            final int X = (int) (((float) xPos + offset + 16.0f
                    - fontRenderer.getStringWidth(craftLabelText) * scaleFactor) * inverseScaleFactor);
            final int Y = (int) (((float) yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
            fontRenderer.drawStringWithShadow(craftLabelText, X, Y, 16777215);
            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        } else if (aeStack.getStackSize() > 0) {
            final String stackSize = getItemStackSizeText(aeStack.getStackSize());

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
            final int X = (int) (((float) xPos + offset + 16.0f
                    - fontRenderer.getStringWidth(stackSize) * scaleFactor) * inverseScaleFactor);
            final int Y = (int) (((float) yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
            fontRenderer.drawStringWithShadow(stackSize, X, Y, 16777215);
            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        }

        fontRenderer.setUnicodeFlag(unicodeFlag);
    }

    /**
     * Renders the stack size for a fluid stack. Fluids are displayed in whole or fractional buckets (1000 mB per
     * bucket).
     *
     * @param fontRenderer font renderer
     * @param aeStack      the fluid stack
     * @param xPos         x position
     * @param yPos         y position
     */
    public void renderStackSize(FontRenderer fontRenderer, IAEFluidStack aeStack, int xPos, int yPos) {
        if (aeStack == null) {
            return;
        }

        final float scaleFactor = AEConfig.instance().useTerminalUseLargeFont() ? 0.85f : 0.5f;
        final float inverseScaleFactor = 1.0f / scaleFactor;
        final int offset = AEConfig.instance().useTerminalUseLargeFont() ? 0 : -1;

        final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
        fontRenderer.setUnicodeFlag(false);

        if (aeStack.getStackSize() > 0) {
            final String stackSize = getFluidStackSizeText(aeStack.getStackSize());

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
            final int X = (int) (((float) xPos + offset + 16.0f
                    - fontRenderer.getStringWidth(stackSize) * scaleFactor) * inverseScaleFactor);
            final int Y = (int) (((float) yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
            fontRenderer.drawStringWithShadow(stackSize, X, Y, 16777215);
            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        }

        fontRenderer.setUnicodeFlag(unicodeFlag);
    }

    private String getItemStackSizeText(final long originalSize) {
        if (AEConfig.instance().useTerminalUseLargeFont()) {
            return SLIM_CONVERTER.toSlimReadableForm(originalSize);
        } else {
            return WIDE_CONVERTER.toWideReadableForm(originalSize);
        }
    }

    private String getFluidStackSizeText(final long originalSize) {
        final boolean largeFont = AEConfig.instance().useTerminalUseLargeFont();

        if (largeFont && originalSize < 100 * 1000) {
            return getSlimRenderedFluidStacksize(originalSize);
        } else if (!largeFont && originalSize < 1000 * 1000) {
            return getWideRenderedFluidStacksize(originalSize);
        }

        final long bucketAmount = originalSize / 1000;
        if (largeFont) {
            return SLIM_CONVERTER.toSlimReadableForm(bucketAmount);
        } else {
            return WIDE_CONVERTER.toWideReadableForm(bucketAmount);
        }
    }

    private String getSlimRenderedFluidStacksize(final long originalSize) {
        final int log = 1 + (int) Math.floor(Math.log10(originalSize)) / 2;
        return getRenderedFluidStackSize(originalSize, log);
    }

    private String getWideRenderedFluidStacksize(final long originalSize) {
        final int log = (int) Math.floor(Math.log10(originalSize)) / 2;
        return getRenderedFluidStackSize(originalSize, log);
    }

    private String getRenderedFluidStackSize(final long originalSize, final int log) {
        final int index = Math.max(0, Math.min(3, log));
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        final DecimalFormat format = new DecimalFormat(FLUID_NUMBER_FORMATS[index]);
        format.setDecimalFormatSymbols(symbols);
        format.setRoundingMode(RoundingMode.DOWN);

        return format.format(originalSize / 1000.0);
    }

}
