package com.sakurafuld.oneness.mixin.oneness;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public interface BoundTickingBlockEntityAccessor {
    @Accessor
    BlockEntity getBlockEntity();
}
