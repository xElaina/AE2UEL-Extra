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
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.util.ISlimReadableNumberConverter;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;

public class AEStackSizeRenderer {
    private static final String[] FLUID_NUMBER_FORMATS = new String[] { "#.000", "#.00", "#.0", "#" };

    private static final ISlimReadableNumberConverter SLIM_CONVERTER = ReadableNumberConverter.INSTANCE;
    private static final IWideReadableNumberConverter WIDE_CONVERTER = ReadableNumberConverter.INSTANCE;

    public void renderStackSize(FontRenderer fontRenderer, Object stack, int xPos, int yPos) {
        if (stack instanceof IAEItemStack) {
            renderStackSizeInternal(fontRenderer, (IAEItemStack) stack, xPos, yPos);
        } else if (stack instanceof IAEFluidStack) {
            renderStackSizeInternal(fontRenderer, (IAEFluidStack) stack, xPos, yPos);
        }
    }

    private <T> void renderStackSizeInternal(FontRenderer fontRenderer, T stack, int xPos, int yPos) {
        if (stack == null)
            return;

        final float scaleFactor = AEConfig.instance().useTerminalUseLargeFont() ? 0.85f : 0.5f;
        final float inverseScaleFactor = 1.0f / scaleFactor;
        final int offset = AEConfig.instance().useTerminalUseLargeFont() ? 0 : -1;

        final boolean unicodeFlag = fontRenderer.getUnicodeFlag();
        fontRenderer.setUnicodeFlag(false);

        try {
            boolean isCraftable = false;
            long stackSize = 0;
            boolean isFluid = false;

            if (stack instanceof IAEItemStack) {
                IAEItemStack itemStack = (IAEItemStack) stack;
                isCraftable = itemStack.isCraftable();
                stackSize = itemStack.getStackSize();
            } else if (stack instanceof IAEFluidStack) {
                IAEFluidStack fluidStack = (IAEFluidStack) stack;
                isCraftable = fluidStack.isCraftable();
                stackSize = fluidStack.getStackSize();
                isFluid = true;
            }

            String displayText;

            if ((stackSize == 0 || GuiScreen.isAltKeyDown()) && isCraftable) {
                displayText = AEConfig.instance().useTerminalUseLargeFont()
                        ? GuiText.LargeFontCraft.getLocal()
                        : GuiText.SmallFontCraft.getLocal();
            } else if (stackSize > 0) {
                if (isFluid) {
                    displayText = getFluidStackSizeText(stackSize);
                } else {
                    displayText = getItemStackSizeText(stackSize);
                }
            } else {
                return;
            }

            renderText(fontRenderer, displayText, xPos, yPos, scaleFactor, inverseScaleFactor, offset);

        } finally {
            fontRenderer.setUnicodeFlag(unicodeFlag);
        }
    }

    private String getItemStackSizeText(final long originalSize) {
        if (AEConfig.instance().useTerminalUseLargeFont()) {
            return SLIM_CONVERTER.toSlimReadableForm(originalSize);
        } else {
            return WIDE_CONVERTER.toWideReadableForm(originalSize);
        }
    }

    private String getFluidStackSizeText(final long originalSize) {
        if (originalSize < 1000 * 100 && AEConfig.instance().useTerminalUseLargeFont()) {
            return getSlimFluidStackSize(originalSize);
        } else if (originalSize < 1000 * 1000 && !AEConfig.instance().useTerminalUseLargeFont()) {
            return getWideFluidStackSize(originalSize);
        }

        if (AEConfig.instance().useTerminalUseLargeFont()) {
            return SLIM_CONVERTER.toSlimReadableForm(originalSize / 1000);
        } else {
            return WIDE_CONVERTER.toWideReadableForm(originalSize / 1000);
        }
    }

    private String getSlimFluidStackSize(final long originalSize) {
        final int log = 1 + (int) Math.floor(Math.log10(originalSize)) / 2;
        return getFormattedFluidStackSize(originalSize, log);
    }

    private String getWideFluidStackSize(final long originalSize) {
        final int log = (int) Math.floor(Math.log10(originalSize)) / 2;
        return getFormattedFluidStackSize(originalSize, log);
    }

    private String getFormattedFluidStackSize(final long originalSize, final int log) {
        final int index = Math.max(0, Math.min(3, log));

        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        final DecimalFormat format = new DecimalFormat(FLUID_NUMBER_FORMATS[index]);
        format.setDecimalFormatSymbols(symbols);
        format.setRoundingMode(RoundingMode.DOWN);

        return format.format(originalSize / 1000d);
    }

    private void renderText(FontRenderer fontRenderer, String text, int xPos, int yPos,
            float scaleFactor, float inverseScaleFactor, int offset) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
        final int X = (int) (((float) xPos + offset + 16.0f
                - fontRenderer.getStringWidth(text) * scaleFactor) * inverseScaleFactor);
        final int Y = (int) (((float) yPos + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
        fontRenderer.drawStringWithShadow(text, X, Y, 16777215);
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
    }

}
