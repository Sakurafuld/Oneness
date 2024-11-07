package com.sakurafuld.oneness.api.capability;

import com.sakurafuld.oneness.api.touch.ITouchable;
import com.sakurafuld.oneness.api.touch.TouchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.sakurafuld.oneness.Deets.ONENESS;
import static com.sakurafuld.oneness.Deets.identifier;

@Mod.EventBusSubscriber(modid = ONENESS, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TouchItemStack {
    public static final Capability<TouchItemStack> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    private ResourceKey<Level> dimension = null;
    private int slot = -1;
    private BlockPos fromPos = null;


    private TouchItemStack(){}

    public void set(ResourceKey<Level> dimension, int slot, BlockPos fromPos) {
        this.dimension = dimension;
        this.slot = slot;
        this.fromPos = fromPos;

    }

    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }
    public int getSlot() {
        return this.slot;
    }
    public BlockPos getFromPos() {
        return this.fromPos;
    }
    public Optional<ITouchable> get(Level level) {
        if(this.getFromPos() != null && this.getDimension().equals(level.dimension()) && level.hasChunkAt(this.getFromPos()) && level.getBlockEntity(this.getFromPos()) instanceof ITouchable touchable) {
            return Optional.of(touchable);
        }
        return Optional.empty();
    }
    public void reach(@Nullable Player player, Level level) {
        this.get(level).ifPresent(touchable -> {
            touchable.reach(player);
        });
    }

    @Mod.EventBusSubscriber(modid = ONENESS)
    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private TouchItemStack touch = null;
        private final LazyOptional<TouchItemStack> CAPABILITY = LazyOptional.of(this::create);

        private TouchItemStack create() {
            return this.touch == null ? this.touch = new TouchItemStack() : this.touch;
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == TouchItemStack.CAPABILITY ? this.CAPABILITY.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            this.CAPABILITY.ifPresent(touch -> {
                if(touch.getFromPos() != null) {
                    tag.putString("Dimension", touch.getDimension().location().toString());
                    tag.putInt("Slot", touch.getSlot());
                    tag.putLong("Pos", touch.getFromPos().asLong());
                }
            });
            return tag;
        }
        @Override
        public void deserializeNBT(CompoundTag tag) {
            this.CAPABILITY.ifPresent(touch -> {
                if(tag.contains("Pos"))
                    touch.set(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("Dimension"))), tag.getInt("Slot"), BlockPos.of(tag.getLong("Pos")));
            });
        }

        @SubscribeEvent
        public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
            if(!(event.getObject().getItem() instanceof TouchItem)) return;
            event.addCapability(identifier(ONENESS, "touch"), new Provider());

        }
    }
    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(TouchItemStack.class);
    }
}
