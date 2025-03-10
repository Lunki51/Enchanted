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

package com.favouriteless.enchanted.common.containers;

import com.favouriteless.enchanted.common.init.EnchantedBlocks;
import com.favouriteless.enchanted.common.init.EnchantedContainers;
import com.favouriteless.enchanted.common.init.EnchantedItems;
import com.favouriteless.enchanted.common.tileentity.DistilleryTileEntity;
import com.favouriteless.enchanted.common.tileentity.InventoryTileEntityBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;

public class DistilleryContainer extends ProcessingContainerBase {

    public DistilleryContainer(final int windowId, final PlayerInventory playerInventory, final InventoryTileEntityBase tileEntity, final IIntArray data) {
        super(EnchantedContainers.DISTILLERY.get(), windowId, tileEntity, IWorldPosCallable.create(tileEntity.getLevel(), tileEntity.getBlockPos()), EnchantedBlocks.DISTILLERY.get(), data);

        // Container Inventory
        addSlot(new SlotJarInput(tileEntity, 0, 32, 35)); // Jar input
        addSlot(new SlotInput(tileEntity, 1, 54, 25)); // Ingredient input
        addSlot(new SlotInput(tileEntity, 2, 54, 45)); // Ingredient input
        addSlot(new SlotOutput(tileEntity, 3, 127, 7)); // Distillery output
        addSlot(new SlotOutput(tileEntity, 4, 127, 26)); // Distillery output
        addSlot(new SlotOutput(tileEntity, 5, 127, 45)); // Distillery output
        addSlot(new SlotOutput(tileEntity, 6, 127, 64)); // Distillery output

        addInventorySlots(playerInventory, 8, 84);
    }

    public DistilleryContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
        this(windowId, playerInventory, (InventoryTileEntityBase)getTileEntity(playerInventory, data, DistilleryTileEntity.class), new IntArray(3));
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        ItemStack itemstack;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {

            ItemStack slotItem = slot.getItem();
            itemstack = slotItem.copy();

            if (index <= 6) { // If container slot
                if (!this.moveItemStackTo(slotItem, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // If not a container slot
                if (itemstack.getItem() == EnchantedItems.CLAY_JAR.get()) { // Item is clay jar
                    if (!this.moveItemStackTo(slotItem, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotItem, 1, 3, false)) { // Item is in player inventory, attempt to fit
                    return ItemStack.EMPTY;
                } else if (index < 35) { // Item is in main player inventory and cannot fit
                    if (!this.moveItemStackTo(slotItem, 34, 43, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 43) { // Item is in player hotbar and cannot fit
                    if(!this.moveItemStackTo(slotItem, 7, 34, false)) {
                        return ItemStack.EMPTY;
                    }
                }

                if (slotItem.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }

                if (slotItem.getCount() == itemstack.getCount()) {
                    return ItemStack.EMPTY;
                }

                slot.onTake(playerIn, slotItem);
            }
        }
        return super.quickMoveStack(playerIn, index);
    }

}