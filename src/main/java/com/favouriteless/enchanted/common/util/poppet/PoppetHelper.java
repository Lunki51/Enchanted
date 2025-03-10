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

package com.favouriteless.enchanted.common.util.poppet;

import com.favouriteless.enchanted.common.items.poppets.AbstractDeathPoppetItem;
import com.favouriteless.enchanted.common.items.poppets.AbstractPoppetItem;
import com.favouriteless.enchanted.common.items.poppets.ItemProtectionPoppetItem;
import com.favouriteless.enchanted.common.network.EnchantedPackets;
import com.favouriteless.enchanted.common.network.packets.EnchantedPoppetAnimationPacket;
import com.favouriteless.enchanted.common.util.poppet.PoppetShelfWorldSavedData.PoppetEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Queue;
import java.util.Random;
import java.util.UUID;

public class PoppetHelper {

	public static final Random RANDOM = new Random();

	public static boolean isBound(ItemStack item) {
		if(item.hasTag()) {
			return item.getTag().hasUUID("boundPlayer");
		}
		return false;
	}

	public static boolean belongsTo(ItemStack item, PlayerEntity player) {
		if(item.getItem() instanceof AbstractPoppetItem) {
			if(item.hasTag()) {
				CompoundNBT tag = item.getTag();
				if(tag.hasUUID("boundPlayer")) {
					return tag.getUUID("boundPlayer").equals(player.getUUID());
				}
			}
		}
		return false;
	}

	public static boolean belongsTo(ItemStack item, UUID uuid) {
		if(item.getItem() instanceof AbstractPoppetItem) {
			if(item.hasTag()) {
				CompoundNBT tag = item.getTag();
				if(tag.hasUUID("boundPlayer")) {
					return tag.getUUID("boundPlayer").equals(uuid);
				}
			}
		}
		return false;
	}

	public static PlayerEntity getBoundPlayer(ItemStack item, World world) {
		if(isBound(item)) {
			return world.getPlayerByUUID(item.getTag().getUUID("boundPlayer"));
		}
		return null;
	}

	public static void bind(ItemStack item, PlayerEntity player) {
		if(item.getItem() instanceof AbstractPoppetItem) {
			CompoundNBT tag = item.getOrCreateTag();
			tag.putUUID("boundPlayer", player.getUUID());
			item.setTag(tag);
		}
	}

	public static void unbind(ItemStack item) {
		if(item.getItem() instanceof AbstractPoppetItem) {
			if(item.hasTag()) {
				CompoundNBT tag = item.getTag();
				tag.remove("boundPlayer");
				item.setTag(tag);
			}
		}
	}

	public static PoppetResult tryUseDeathPoppet(PlayerEntity player, ItemStack poppetStack, ServerWorld level, String shelfIdentifier) {
		if(poppetStack.getItem() instanceof AbstractDeathPoppetItem) {
			AbstractDeathPoppetItem poppet = (AbstractDeathPoppetItem)poppetStack.getItem();
			if(PoppetHelper.belongsTo(poppetStack, player)) {
				if(poppet.canProtect(player)) {
					if(RANDOM.nextFloat() > poppet.getFailRate()) {
						poppet.protect(player);
						level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundCategory.PLAYERS, 1.0F, 0.5F);
						return tryDamagePoppet(poppetStack, level, shelfIdentifier) ? PoppetResult.SUCCESS_BREAK : PoppetResult.SUCCESS;
					}
					return PoppetResult.FAIL;
				}
			}
		}
		return PoppetResult.PASS;
	}

	public static boolean tryUseDeathPoppetQueue(Queue<ItemStack> queue, PlayerEntity player) {
		while(!queue.isEmpty()) {
			ItemStack poppetItem = queue.remove();
			if(handleTryUseDeathPoppet(player, poppetItem, null))
				return true;
		}
		return false;
	}

	public static boolean tryUseDeathPoppetEntryQueue(Queue<PoppetEntry> queue, PlayerEntity player) {
		while(!queue.isEmpty()) {
			PoppetEntry entry = queue.remove();
			if(handleTryUseDeathPoppet(player, entry.getItem(), entry.getShelfIdentifier()))
				return true;
		}
		return false;
	}

	public static boolean handleTryUseDeathPoppet(PlayerEntity player, ItemStack item, String shelfIdentifier) {
		ItemStack poppetItemOriginal = item.copy();
		PoppetResult result = PoppetHelper.tryUseDeathPoppet(player, item, (ServerWorld)player.level, shelfIdentifier);
		return handlePoppetResult(result, poppetItemOriginal, player);
	}

	public static PoppetResult tryUseItemProtectionPoppet(PlayerEntity player, ItemStack poppetStack, ItemStack toolStack, ServerWorld level, String shelfIdentifier) {
		if(poppetStack.getItem() instanceof ItemProtectionPoppetItem) {
			ItemProtectionPoppetItem poppet = (ItemProtectionPoppetItem)poppetStack.getItem();
			if(PoppetHelper.belongsTo(poppetStack, player)) {
				if(RANDOM.nextFloat() > poppet.getFailRate()) {
					poppet.protect(toolStack);
					return tryDamagePoppet(poppetStack, level, shelfIdentifier) ? PoppetResult.SUCCESS_BREAK : PoppetResult.SUCCESS;
				}
				return PoppetResult.FAIL;
			}
		}
		return PoppetResult.PASS;
	}

	public static boolean tryUseItemProtectionPoppetQueue(Queue<ItemStack> queue, PlayerEntity player, ItemStack toolStack) {
		while(!queue.isEmpty()) {
			ItemStack poppetItem = queue.remove();
			if(handleTryUseItemProtectionPoppet(player, poppetItem, toolStack, null))
				return true;
		}
		return false;
	}

	public static boolean tryUseItemProtectionPoppetEntryQueue(Queue<PoppetEntry> queue, PlayerEntity player, ItemStack toolStack) {
		while(!queue.isEmpty()) {
			PoppetEntry entry = queue.remove();
			if(handleTryUseItemProtectionPoppet(player, entry.getItem(), toolStack, entry.getShelfIdentifier()))
				return true;
		}
		return false;
	}

	public static boolean handleTryUseItemProtectionPoppet(PlayerEntity player, ItemStack poppetStack, ItemStack toolStack, String shelfIdentifier) {
		ItemStack poppetItemOriginal = poppetStack.copy();
		PoppetResult result = PoppetHelper.tryUseItemProtectionPoppet(player, poppetStack, toolStack, (ServerWorld)player.level, shelfIdentifier);
		return handlePoppetResult(result, poppetItemOriginal, player);
	}


	/**
	 * Attempts to damage poppet
	 * @param item
	 * @return True if poppet is destroyed
	 */
	public static boolean tryDamagePoppet(ItemStack item, ServerWorld level, String shelfIdentifier) {
		item.setDamageValue(item.getDamageValue()+1);
		if(item.getDamageValue() >= item.getMaxDamage()) {
			item.shrink(1);
			if(shelfIdentifier != null && item.getCount() <= 0) {
				PoppetShelfWorldSavedData data = PoppetShelfWorldSavedData.get(level);
				PoppetShelfInventory inventory = data.SHELF_STORAGE.get(shelfIdentifier);
				for(int i = 0; i < inventory.getContainerSize(); i++)
					if(inventory.get(i).equals(item))
						inventory.set(i, ItemStack.EMPTY);
				data.updateShelf(shelfIdentifier);
			}
			return true;
		}
		return false;
	}

	private static boolean handlePoppetResult(PoppetResult result, ItemStack poppetItemOriginal, PlayerEntity player) {
		if(result == PoppetResult.SUCCESS || result == PoppetResult.SUCCESS_BREAK) {
			if(!player.level.isClientSide)
				EnchantedPackets.sendToAllPlayers(new EnchantedPoppetAnimationPacket(result, poppetItemOriginal, player.getId()));
			return true;
		}
		return false;
	}

	public enum PoppetResult {
		SUCCESS,
		SUCCESS_BREAK,
		FAIL,
		PASS
	}
}
