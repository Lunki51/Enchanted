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

import net.minecraft.block.BlockState;
import net.minecraft.block.VineBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class SpanishMossBlock extends VineBlock {

    public SpanishMossBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(SOUTH, true));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext pContext) {
        BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());
        boolean flag = blockstate.is(this);
        BlockState blockstate1 = flag ? blockstate : this.defaultBlockState().setValue(SOUTH, false);

        for(Direction direction : pContext.getNearestLookingDirections()) {
            if (direction != Direction.DOWN) {
                BooleanProperty booleanproperty = getPropertyForFace(direction);
                boolean flag1 = flag && blockstate.getValue(booleanproperty);
                if (!flag1 && this.canSupportAtFace(pContext.getLevel(), pContext.getClickedPos(), direction)) {
                    return blockstate1.setValue(booleanproperty, true);
                }
            }
        }

        return flag ? blockstate1 : null;
    }

    private boolean canSupportAtFace(IBlockReader pLevel, BlockPos pPos, Direction pDirection) {
        if (pDirection == Direction.DOWN) {
            return false;
        } else {
            BlockPos blockpos = pPos.relative(pDirection);
            if (isAcceptableNeighbour(pLevel, blockpos, pDirection)) {
                return true;
            } else if (pDirection.getAxis() == Direction.Axis.Y) {
                return false;
            } else {
                BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(pDirection);
                BlockState blockstate = pLevel.getBlockState(pPos.above());
                return blockstate.is(this) && blockstate.getValue(booleanproperty);
            }
        }
    }

}
