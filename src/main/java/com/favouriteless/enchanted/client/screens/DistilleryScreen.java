/*
 * Copyright (c) 2022. Favouriteless
 * Enchanted, a minecraft mod.
 * GNU GPLv3 License
 *
 *     This file is part of Enchanted.
 *
 *     Enchanted is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Enchanted is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Enchanted.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.favouriteless.enchanted.client.screens;

import com.favouriteless.enchanted.Enchanted;
import com.favouriteless.enchanted.common.containers.DistilleryContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class DistilleryScreen extends ContainerScreen<DistilleryContainer> {

    private final DistilleryContainer container;

    private static final ResourceLocation TEXTURE = new ResourceLocation(Enchanted.MOD_ID, "textures/gui/distillery.png");

    public static final int COOK_BAR_XPOS = 69;
    public static final int COOK_BAR_YPOS = 12;
    public static final int COOK_BAR_ICON_U = 176;
    public static final int COOK_BAR_ICON_V = 29;
    public static final int COOK_BAR_WIDTH = 56;
    public static final int COOK_BAR_HEIGHT = 62;

    public static final int BUBBLES_XPOS = 93;
    public static final int BUBBLES_YPOS = 56;
    public static final int BUBBLES_ICON_U = 176;
    public static final int BUBBLES_ICON_V = 28;

    private static final int[] BUBBLELENGTHS = new int[]{0, 6, 11, 16, 20, 24, 29};

    public DistilleryScreen(DistilleryContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;

        imageWidth = 176;
        imageHeight = 166;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    protected void renderHoveredTooltip(MatrixStack matrixStack, int x, int y) {
        if (!minecraft.player.inventory.getCarried().isEmpty()) return;  // no tooltip if the player is dragging something
            super.renderTooltip(matrixStack, x, y);
    }


    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bind(TEXTURE);

        int edgeSpacingX = (width - imageWidth) / 2;
        int edgeSpacingY = (height - imageHeight) / 2;
        blit(matrixStack, edgeSpacingX, edgeSpacingY, 0, 0, imageWidth, imageHeight);

        // Draw cook progress bar
        int cookTime = container.getData().get(0);
        int cookTimeTotal = container.getData().get(1);
        int cookProgressionScaled = cookTimeTotal != 0 && cookTime != 0 ? cookTime * COOK_BAR_WIDTH / cookTimeTotal : 0;


        blit(matrixStack, leftPos + COOK_BAR_XPOS, topPos + COOK_BAR_YPOS, COOK_BAR_ICON_U, COOK_BAR_ICON_V, cookProgressionScaled + 1, COOK_BAR_HEIGHT);

        int bubbleOffset = BUBBLELENGTHS[cookTime / 2 % 7];
        if (bubbleOffset > 0) {
            blit(matrixStack, leftPos + BUBBLES_XPOS, topPos + BUBBLES_YPOS - bubbleOffset, BUBBLES_ICON_U, BUBBLES_ICON_V - bubbleOffset, 12, bubbleOffset);
        }

    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        font.draw(matrixStack, title, (float)(imageWidth / 2 - font.width(title) / 2), (float)titleLabelY, Color.DARK_GRAY.getRGB());
        font.draw(matrixStack, inventory.getDisplayName(), (float)inventoryLabelX, (float)inventoryLabelY, Color.DARK_GRAY.getRGB());
    }

}