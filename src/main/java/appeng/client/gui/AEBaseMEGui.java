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

package appeng.client.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.SlotME;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.fluids.container.slots.IMEFluidSlot;
import appeng.util.Platform;

public abstract class AEBaseMEGui extends AEBaseGui {

    public AEBaseMEGui(final Container container) {
        super(container);
    }

    @Override
    protected void renderToolTip(final ItemStack stack, final int x, final int y) {
        final Slot s = this.getSlot(x, y);

        if (this.renderItemToolTip(stack, x, y, s)) {
            return;
        }

        if (this.renderFluidToolTip(x, y, s)) {
            return;
        }

        super.renderToolTip(stack, x, y);
    }

    /**
     * Renders the tooltip for an item stack. Returns true if the tooltip was rendered, false otherwise.
     */
    private boolean renderItemToolTip(final ItemStack stack, final int x, final int y, final Slot s) {
        if (stack.isEmpty()) {
            return false;
        }

        final int bigNumber = AEConfig.instance().useTerminalUseLargeFont() ? 999 : 9999;
        final List<String> currentToolTip = this.getItemToolTip(stack);

        if (s instanceof SlotME) {
            IAEItemStack myStack = null;

            try {
                final SlotME theSlotField = (SlotME) s;
                myStack = theSlotField.getAEStack();
            } catch (final Throwable ignore) {
            }

            if (myStack != null) {
                if (myStack.getStackSize() > 1) {
                    final String local = ButtonToolTips.ItemsStored.getLocal();
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                            .format(myStack.getStackSize());
                    final String format = String.format(local, formattedAmount);

                    currentToolTip.add(TextFormatting.GRAY + format);
                }

                if (myStack.getCountRequestable() > 0) {
                    final String local = ButtonToolTips.ItemsRequestable.getLocal();
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                            .format(myStack.getCountRequestable());
                    final String format = String.format(local, formattedAmount);

                    currentToolTip.add(format);
                }

                if (myStack.isCraftable() && AEConfig.instance().isShowCraftableTooltip()) {
                    final String local = ButtonToolTips.ItemsCraftable.getLocal();
                    currentToolTip.add(TextFormatting.GRAY + local);
                }

                this.drawHoveringText(currentToolTip, x, y, this.fontRenderer);
                return true;
            } else if (stack.getCount() > bigNumber) {
                final String local = ButtonToolTips.ItemsStored.getLocal();
                final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(stack.getCount());
                final String format = String.format(local, formattedAmount);

                currentToolTip.add(TextFormatting.GRAY + format);

                this.drawHoveringText(currentToolTip, x, y, this.fontRenderer);
                return true;
            }
        } else if (s instanceof AppEngSlot) {
            if (!(s instanceof SlotPlayerInv) && !(s instanceof SlotPlayerHotBar)) {
                if (!s.getStack().isEmpty()) {
                    final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                            .format(s.getStack().getCount());
                    currentToolTip.add(TextFormatting.GRAY + formattedAmount);
                    this.drawHoveringText(currentToolTip, x, y, this.fontRenderer);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Renders the tooltip for a fluid stack. Returns true if the tooltip was rendered, false otherwise.
     */
    private boolean renderFluidToolTip(final int x, final int y, final Slot s) {
        if (s != null && s instanceof IMEFluidSlot && s.isEnabled()) {
            final IMEFluidSlot fluidSlot = (IMEFluidSlot) s;
            final IAEFluidStack fluidStack = fluidSlot.getAEFluidStack();

            if (fluidStack != null && fluidSlot.shouldRenderAsFluid()) {
                final String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                        .format(fluidStack.getStackSize() / 1000.0) + " B";

                final String modId = Platform.getModId(fluidStack);
                final ModContainer modContainer = Loader.instance().getIndexedModList().get(modId);
                final String modName = modContainer != null
                        ? "" + TextFormatting.BLUE + TextFormatting.ITALIC + modContainer.getName()
                        : "Unknown Mod";

                final List<String> tooltip = new ArrayList<>();
                tooltip.add(fluidStack.getFluidStack().getLocalizedName());
                tooltip.add(formattedAmount);
                tooltip.add(modName);

                this.drawHoveringText(tooltip, x, y);
                return true;
            }
        }
        return false;
    }

}
