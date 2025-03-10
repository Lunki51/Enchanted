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

package com.favouriteless.enchanted.common.recipes;

import com.favouriteless.enchanted.common.init.EnchantedRecipeTypes;
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class WitchOvenRecipe implements IRecipe<IInventory> {

    private final IRecipeType<?> type;
    private final ResourceLocation id;

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int jarsNeeded;

    public WitchOvenRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int jarsNeeded) {
        this.type = EnchantedRecipeTypes.WITCH_OVEN;
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.jarsNeeded = jarsNeeded;
    }

    public int getJarsNeeded() {
        return this.jarsNeeded;
    }

    public Ingredient getInput() {
        return this.ingredient;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return this.ingredient.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(IInventory inv) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return EnchantedRecipeTypes.WITCH_OVEN_SERIALIZER.get();
    }

    @Override
    public IRecipeType<?> getType() {
        return type;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }


    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<WitchOvenRecipe> {

        @Override
        public WitchOvenRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient ingredientIn = Ingredient.fromJson(JSONUtils.getAsJsonObject(json, "ingredient"));
            ItemStack itemOut = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(JSONUtils.getAsString(json, "result"))));

            int jarsNeeded = JSONUtils.getAsInt(json, "jarsneeded", 1);

            return new WitchOvenRecipe(recipeId, ingredientIn, itemOut, jarsNeeded);
        }

        @Nullable
        @Override
        public WitchOvenRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient ingredientIn = Ingredient.fromNetwork(buffer);
            ItemStack itemOut = buffer.readItem();
            int jarsNeeded = buffer.readInt();

            return new WitchOvenRecipe(recipeId, ingredientIn, itemOut, jarsNeeded);
        }

        @Override
        public void toNetwork(PacketBuffer buffer, WitchOvenRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.getResultItem());
            buffer.writeInt(recipe.getJarsNeeded());
        }

    }
}
