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
import com.favouriteless.enchanted.common.items.poppets.AbstractPoppetItem;
import com.favouriteless.enchanted.common.tileentity.PoppetShelfTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;

public class PoppetShelfContainer extends ContainerBase {

	public PoppetShelfContainer(int id, PlayerInventory playerInventory, PoppetShelfTileEntity tileEntity) {
		super(EnchantedContainers.POPPET_SHELF.get(), id, tileEntity, IWorldPosCallable.create(tileEntity.getLevel(), tileEntity.getBlockPos()), EnchantedBlocks.POPPET_SHELF.get());

		for(int i = 0; i < tileEntity.getInventory().getContainerSize(); i++)
			addSlot(new PoppetSlot(tileEntity.getInventory(), i, 47 + i*22, 18));

		addInventorySlots(playerInventory, 8, 49);
	}

	public PoppetShelfContainer(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
		this(windowId, playerInventory, (PoppetShelfTileEntity)getTileEntity(playerInventory, data, PoppetShelfTileEntity.class));
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
		ItemStack itemstack;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack slotItem = slot.getItem();
			itemstack = slotItem.copy();

			if (index <= 3) { // If container slot
				if (!this.moveItemStackTo(slotItem, 4, 40, true)) {
					return ItemStack.EMPTY;
				}
			} else { // If not a container slot
				if (!this.moveItemStackTo(slotItem, 0, 4, false)) {
					return ItemStack.EMPTY;
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

	public static class PoppetSlot extends Slot {

		public PoppetSlot(IInventory container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack itemStack) {
			return itemStack.getItem() instanceof AbstractPoppetItem;
		}
	}

}
