package com.sakurafuld.oneness.mixin.oneness;

import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$RebindableTickingBlockEntityWrapper")
public interface RebindableTickingBlockEntityWrapperAccessor {
    @Accessor
    TickingBlockEntity getTicker();
}
