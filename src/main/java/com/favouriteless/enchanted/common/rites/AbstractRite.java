/*
 * Copyright (c) 2021. Favouriteless
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

package com.favouriteless.enchanted.common.rites;

import com.favouriteless.enchanted.common.rites.util.CircleSize;
import com.favouriteless.enchanted.common.tileentity.ChalkGoldTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class AbstractRite extends ForgeRegistryEntry<AbstractRite> {

    public final HashMap<CircleSize, Block> CIRCLES_REQUIRED = new HashMap<>();
    public final HashMap<EntityType<?>, Integer> ENTITIES_REQUIRED = new HashMap<>();
    public final HashMap<Item, Integer> ITEMS_REQUIRED = new HashMap<>();
    public final int POWER;
    public final int POWER_TICK;

    private final List<ItemStack> itemsConsumed = new ArrayList<>();

    public World world; // World ritual started in
    public BlockPos pos; // Position ritual started at
    public UUID casterUUID; // Player who started ritual
    public UUID targetUUID; // Target of the ritual

    private boolean isStarting = false;
    private boolean isExecuting = false;
    private int ticks = 0;

    public AbstractRite(int power, int powerTick) {
        this.POWER = power;
        this.POWER_TICK = powerTick;
    }

    protected abstract void execute();
    protected abstract void onTick();
    public abstract AbstractRite create();

    public void tick() {
        if(world != null && !world.isClientSide ) {
            ticks++;
            if (isStarting && ticks % 20 == 0) {
                List<Entity> allEntities = world.getEntities(null, new AxisAlignedBB(pos.offset(-7, 0, -7), pos.offset(7, 1, 7)));

                boolean hasItem = false;
                for(Entity entity : allEntities) {
                    if(entity instanceof ItemEntity) {
                        ItemEntity itemEntity = (ItemEntity)entity;
                        if(ITEMS_REQUIRED.containsKey(itemEntity.getItem().getItem())) {
                            consumeItem(itemEntity);
                            hasItem = true;
                            break;
                        }
                    }
                }

                boolean hasEntity = false;
                if(!hasItem) {
                    if(ITEMS_REQUIRED.isEmpty()) {
                        for(Entity entity : allEntities) {
                            if(ENTITIES_REQUIRED.containsKey(entity.getType())) {
                                hasEntity = true;
                                consumeEntity(entity);
                                break;
                            }
                        }
                    }
                    else {
                        cancel();
                    }

                    if(!hasEntity) {
                        if(ENTITIES_REQUIRED.isEmpty()) {
                            startExecuting();
                        }
                        else {
                            cancel();
                        }
                    }
                }
            }
        }
    }

    public void startExecuting() {
        this.isStarting = false;
        this.isExecuting = true;
        execute();
    }

    public void stopExecuting() {
        this.isStarting = false;
        this.isExecuting = false;
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof ChalkGoldTileEntity) {
            ((ChalkGoldTileEntity)te).clearRite();
        }
    }

    public void cancel() {
        isStarting = false;
        isExecuting = false;

        while(!itemsConsumed.isEmpty()) {
            ItemStack stack = itemsConsumed.get(0);
            ItemEntity entity = new ItemEntity(world, pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, stack);
            world.addFreshEntity(entity);
            itemsConsumed.remove(stack);
        }

        world.playSound(null, pos, SoundEvents.NOTE_BLOCK_SNARE, SoundCategory.MASTER, 1.0F, 1.0F);

        PlayerEntity player = world.getPlayerByUUID(casterUUID);
        if(player != null) player.displayClientMessage(new StringTextComponent("Rite failed.").withStyle(TextFormatting.RED), false);

        for(int i = 0; i < 25; i++) {
            double dx = pos.getX() + Math.random();
            double dy = pos.getY() + Math.random();
            double dz = pos.getZ() + Math.random();
            ((ServerWorld)world).sendParticles(new RedstoneParticleData(254/255F,94/255F,94/255F, 1.0F), dx, dy, dz, 1, 0.0F, 0.0F, 0.0F, 0.0F);
        }

        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof ChalkGoldTileEntity) {
            ((ChalkGoldTileEntity)te).clearRite();
        }
    }

    public void consumeItem(ItemEntity entity) {
        ItemStack stack = entity.getItem();
        Item item = stack.getItem();
        int amountNeeded = ITEMS_REQUIRED.get(stack.getItem());

        if(amountNeeded >= stack.getCount()) { // Not enough/perfect
            ITEMS_REQUIRED.put(item, ITEMS_REQUIRED.get(item)-stack.getCount());
            if(ITEMS_REQUIRED.get(item) <= 0) ITEMS_REQUIRED.remove(item); // Remove if all consumed
            itemsConsumed.add(stack);
            entity.setNeverPickUp();
            entity.remove();
        }
        else { // Too much
            ITEMS_REQUIRED.remove(item);
            itemsConsumed.add(new ItemStack(item, amountNeeded, stack.getTag()));
            stack.shrink(amountNeeded);
            entity.setItem(stack);
        }

        world.playSound(null, entity.blockPosition(), SoundEvents.CHICKEN_EGG, SoundCategory.MASTER, 1.0F, 1.0F);
        for(int i = 0; i < 5; i++) {
            double dx = entity.position().x - 0.15D + (Math.random() * 0.3D);
            double dy = entity.position().y + (Math.random() * 0.3D);
            double dz = entity.position().z - 0.15D + (Math.random() * 0.3D);
            ((ServerWorld)world).sendParticles(ParticleTypes.SMOKE, dx, dy, dz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public void consumeEntity(Entity entity) {
        int newAmount = ENTITIES_REQUIRED.get(entity.getType())-1;
        if(newAmount > 0) {
            ENTITIES_REQUIRED.put(entity.getType(), newAmount);
        }
        else {
            ENTITIES_REQUIRED.remove(entity.getType());
        }
        entity.remove();

        world.playSound(null, entity.blockPosition(), SoundEvents.CHICKEN_EGG, SoundCategory.MASTER, 1.0F, 1.0F);
        for(int i = 0; i < 10; i++) {
            double dx = entity.position().x - (entity.getBbWidth()/2) + (Math.random() * entity.getBbWidth());
            double dy = entity.position().y + (Math.random() * entity.getBbHeight());
            double dz = entity.position().z - (entity.getBbWidth()/2) + (Math.random() * entity.getBbWidth());
            ((ServerWorld)world).sendParticles(ParticleTypes.SMOKE, dx, dy, dz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public boolean is(HashMap<CircleSize, Block> circles, HashMap<EntityType<?>, Integer> entities, HashMap<Item, Integer> items) {
        return CIRCLES_REQUIRED.equals(circles) && ENTITIES_REQUIRED.equals(entities) && ITEMS_REQUIRED.equals(items);
    }

    /**
     * Gets the number of extra requirement entities over the rite requirements at a given position
     * @param world
     * @param pos
     * @return No. of extra requirement entities, -1 if not valid.
     */
    public int differenceAt(World world, BlockPos pos) {
        for(CircleSize circleSize : CIRCLES_REQUIRED.keySet()) {
            if(!circleSize.match(world, pos, CIRCLES_REQUIRED.get(circleSize))) {
                return -1;
            }
        }
        List<Entity> allEntities = world.getEntities(null, new AxisAlignedBB(pos.offset(-7, 0, -7), pos.offset(7, 1, 7)));
        HashMap<Item, Integer> items = new HashMap<>();
        HashMap<EntityType<?>, Integer> entities = new HashMap<>();

        for(Entity entity : allEntities) { // Get items/entities in area
            if(entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity)entity;
                ItemStack itemStack = itemEntity.getItem();
                if(!items.containsKey(itemStack.getItem())) {
                    items.put(itemStack.getItem(), itemStack.getCount());
                }
                else {
                    items.put(itemStack.getItem(), items.get(itemStack.getItem())+itemStack.getCount());
                }
            }
            else {
                if(!entities.containsKey(entity.getType())) {
                    entities.put(entity.getType(), 1);
                }
                else {
                    entities.put(entity.getType(), entities.get(entity.getType()) + 1);
                }
            }
        }

        int diff = 0;
        if(!ITEMS_REQUIRED.isEmpty()) {
            for (Item item : ITEMS_REQUIRED.keySet()) { // Check if enough items
                if (!(items.containsKey(item) && items.get(item) >= ITEMS_REQUIRED.get(item))) return -1;
            }
            for (Item item : items.keySet()) {
                if (!ITEMS_REQUIRED.containsKey(item)) diff += items.get(item);
            }
        }
        if(!ENTITIES_REQUIRED.isEmpty()) {
            for (EntityType<?> type : ENTITIES_REQUIRED.keySet()) { // Check if enough entities
                if (!(entities.containsKey(type) && entities.get(type) >= ENTITIES_REQUIRED.get(type))) return -1;
            }

            for (EntityType<?> type : entities.keySet()) {
                if (!ENTITIES_REQUIRED.containsKey(type)) diff += entities.get(type);
            }
        }

        return diff;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setCaster(PlayerEntity player) {
        this.casterUUID = player.getUUID();
    }

    public void start() {
        this.isStarting = true;
    }

    public void spawnParticles() {
        for(int i = 0; i < 25; i++) {
            double dx = pos.getX() - 1.0D + Math.random() * 3.0D;
            double dy = pos.getY() + Math.random() * 2.0D;
            double dz = pos.getZ() - 1.0D + Math.random() * 3.0D;
            ((ServerWorld)world).sendParticles(ParticleTypes.WITCH, dx, dy, dz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

}
