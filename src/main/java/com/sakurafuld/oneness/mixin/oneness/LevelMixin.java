package com.sakurafuld.oneness.mixin.oneness;

import com.google.common.collect.Lists;
import com.sakurafuld.oneness.content.oneness.OnenessBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Shadow private boolean tickingBlockEntities;

    @Shadow @Final private List<TickingBlockEntity> pendingBlockEntityTickers;

    @Shadow @Final protected List<TickingBlockEntity> blockEntityTickers;

    //JadeとかgetEnergy表示用.
    @Inject(method = "addBlockEntityTicker", at = @At("HEAD"), cancellable = true)
    private void lineup(TickingBlockEntity tickingBlockEntity, CallbackInfo ci) {
        ci.cancel();
        List<TickingBlockEntity> instance = this.tickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers;

        instance.add(tickingBlockEntity);

        List<TickingBlockEntity> oneness = Lists.newArrayList(instance);

        for(TickingBlockEntity value : Lists.newArrayList(instance)) {
            if(value instanceof RebindableTickingBlockEntityWrapperAccessor tickerAccessor
                    && tickerAccessor.getTicker() instanceof BoundTickingBlockEntityAccessor blockAccessor
                    && blockAccessor.getBlockEntity() instanceof OnenessBlockEntity) {
                oneness.remove(value);
            } else {
                instance.remove(value);
            }
        }
        instance.addAll(oneness);
    }
}
