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

package com.favouriteless.enchanted.common.blocks.crops;

import com.favouriteless.enchanted.common.init.EnchantedItems;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class ArtichokeBlock extends CropsBlockAgeFive {

    public ArtichokeBlock(Properties properties) {
        super(properties);
    }

    protected IItemProvider getBaseSeedId() {
        return EnchantedItems.ARTICHOKE_SEEDS.get();
    }

    @Override
    protected boolean mayPlaceOn(BlockState pState, IBlockReader pWorldIn, BlockPos pPos) {
        FluidState fluidstate = pWorldIn.getFluidState(pPos);
        FluidState fluidstate1 = pWorldIn.getFluidState(pPos.above());
        return fluidstate.getType() == Fluids.WATER && fluidstate.isSource() && fluidstate1.getType() == Fluids.EMPTY;
    }

    @Override
    public boolean canSurvive(BlockState pState, IWorldReader pWorldIn, BlockPos pPos) {
        BlockPos blockpos = pPos.below();
        return this.mayPlaceOn(pWorldIn.getBlockState(blockpos), pWorldIn, blockpos);
    }

}
