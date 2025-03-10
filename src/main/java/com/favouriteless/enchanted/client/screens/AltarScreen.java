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
import com.favouriteless.enchanted.common.containers.AltarContainer;
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
public class AltarScreen extends ContainerScreen<AltarContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Enchanted.MOD_ID, "textures/gui/altar.png");
    private AltarContainer container;

    public AltarScreen(AltarContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;

        this.imageWidth = 176;
        this.imageHeight = 88;
    }

    @Override
    protected void renderBg(MatrixStack pMatrixStack, float pPartialTicks, int pX, int pY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(TEXTURE);

        int edgeSpacingX = (this.width - this.imageWidth) / 2;
        int edgeSpacingY = (this.height - this.imageHeight) / 2;
        this.blit(pMatrixStack, edgeSpacingX, edgeSpacingY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(MatrixStack pMatrixStack, int pX, int pY) {
        String powerString = String.format("%s/%s (%sx)", container.getCurrentPower(), container.getMaxPower(), container.getRechargeMultiplier());
        int xOffset = this.font.width(powerString) / 2;
        int yOffset = this.font.lineHeight / 2;
        this.font.draw(pMatrixStack, powerString, (float)this.imageWidth / 2 - xOffset, (float)this.imageHeight / 2 - yOffset, Color.darkGray.getRGB());
    }

}
