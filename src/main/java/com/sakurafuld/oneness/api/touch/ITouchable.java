package com.sakurafuld.oneness.api.touch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;


public interface ITouchable {
    TouchItem snatch(Player player);

    default InteractionResult onTouchBlock(Player player, BlockPos pos, Direction face) {return InteractionResult.FAIL;}
//    default InteractionResult onTouchEntity(Player player, Entity target) {return InteractionResult.FAIL;}

    default void onPunchBlock(Player player, BlockPos pos, Direction face) {}
//    default void onPunchEntity(Player player, Entity target) {}

    //サーバーのみの処理が望ましい.
    void reach(@Nullable Player player);
}
