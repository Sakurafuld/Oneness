package com.sakurafuld.oneness.content.oneness;

import com.mojang.datafixers.util.Pair;
import com.sakurafuld.oneness.api.touch.TouchItem;
import com.sakurafuld.oneness.api.touch.TouchableBlockEntity;
import com.sakurafuld.oneness.content.ModBlockEntities;
import com.sakurafuld.oneness.content.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

import static com.sakurafuld.oneness.Deets.*;
import static net.minecraft.world.level.block.HopperBlock.FACING;

public class OnenessBlockEntity extends TouchableBlockEntity implements IEnergyStorage {
    private static final Capability<IItemHandler> ITEM_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private static final Capability<IEnergyStorage> ENERGY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    private LazyOptional<IEnergyStorage> HANDLER = LazyOptional.of(() -> this);
    private int lastReceive = 0;
    private long lastTick = -1;

    public OnenessBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.ONENESS.get(), pPos, pBlockState);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if(!simulate) {
            if(this.lastTick == this.getLevel().getGameTime()) {
                if((this.lastReceive + maxReceive) >= 0) {
                    this.lastReceive += maxReceive;
                } else {
                    //オーバーフロー！！！.
                    this.lastReceive = Integer.MAX_VALUE;
                }
            } else {
                this.lastReceive = maxReceive;
                this.lastTick = this.getLevel().getGameTime();
            }
        }
        return maxReceive;
    }
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }
    @Override
    public int getEnergyStored() {
        return this.lastReceive;
    }
    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }
    @Override
    public boolean canExtract() {
        return false;
    }
    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.HANDLER.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.HANDLER = LazyOptional.of(() -> this);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == ENERGY_CAPABILITY ? this.HANDLER.cast() : super.getCapability(cap, side);
    }

    public int getRate() {
        return this.lastReceive;
    }
    public void resetRate() {
        this.lastReceive = 0;
        this.setChanged();
        this.sync();
    }

    @Override
    public TouchItem doSnatch(Player player) {
        player.playSound(SoundEvents.DEEPSLATE_BREAK, 1, 1);
        return ModItems.ONENESS_TOUCH.get() instanceof TouchItem touchItem ? touchItem : null;
    }

    @Override
    public InteractionResult doTouchBlock(Player player, BlockPos pos, Direction face) {
        boolean flag;
        for(Pair<BlockPos, Direction> pair : this.getTouchedBlocks().keySet()) {
            flag = pair.getFirst().equals(pos);
            if(flag) {
                this.getTouchedBlocks().remove(pair);
                break;
            }
        }

        Optional<IItemHandler> from;
        if((from = this.checkItemHandler(pos, face).resolve()).isPresent()) {
            Optional<IItemHandler> to;
            if((to = this.checkItemHandler(this.getBlockPos().relative(this.getBlockState().getValue(FACING)), this.getBlockState().getValue(FACING).getOpposite()).resolve())
                    .isEmpty() || to.get() != from.get()) {
                player.playSound(SoundEvents.FLINTANDSTEEL_USE, 1, 0.3f);
                player.playSound(SoundEvents.HONEY_BLOCK_PLACE, 1, 4);

                return InteractionResult.sidedSuccess(side().isClient());
            }
        }

        player.playSound(SoundEvents.NOTE_BLOCK_HAT, 1, 1);
        player.swing(InteractionHand.MAIN_HAND);

        return InteractionResult.FAIL;
    }

    @Override
    public void onPunchBlock(Player player, BlockPos pos, Direction face) {
        LOG.debug("{}-onPunchBlock={}", side(), this.getClass().getSimpleName());

        boolean flag;
        for(Pair<BlockPos, Direction> pair : this.getTouchedBlocks().keySet()) {
            flag = pair.getFirst().equals(pos);
            if(flag) {
                player.playSound(SoundEvents.WOOL_BREAK, 6, 1.5f);
                if(side().isServer()) {
                    this.getTouchedBlocks().remove(pair);
                    this.setChanged();
                    this.sync();
                    break;
                }
            }
        }
    }

    @Override
    public void reach(@Nullable Player player) {
        super.reach(player);

        if(player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSoundPacket(SoundEvents.ARMOR_EQUIP_ELYTRA, player.getSoundSource(), player.getX(), player.getY(), player.getZ(), 1, 2));
        }

        LOG.debug("{}-Reach={}!!!", side(), player != null);
    }

    private LazyOptional<IItemHandler> checkItemHandler(BlockPos pos, Direction face){
        BlockEntity check = this.getLevel().getBlockEntity(pos);
        return check != null ? check.getCapability(ITEM_CAPABILITY, face) : LazyOptional.empty();
    }
    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if(side().isClient())
            return;

        OnenessBlockEntity self = (OnenessBlockEntity) blockEntity;

        //        if(level.getGameTime() % 60 == 0)
        //        LOG.debug("{}-Tick!!!={}", side(), self.getRate());
        int rate = self.getRate();
        self.resetRate();
        for(Map.Entry<Pair<BlockPos, Direction>, Integer> entry : self.getTouchedBlocks().entrySet()) {

            if(!level.isLoaded(entry.getKey().getFirst()))
                continue;

            LazyOptional<IItemHandler> fromHandler = self.checkItemHandler(entry.getKey().getFirst(), entry.getKey().getSecond());
            LazyOptional<IItemHandler> toHandler = self.checkItemHandler(pos.relative(state.getValue(FACING)), state.getValue(FACING).getOpposite());

            if(level.getGameTime() % 5 == 0 && !fromHandler.isPresent()) {
                self.getTouchedBlocks().remove(entry.getKey());
                self.sync();
                self.setChanged();
                return;
            }


            fromHandler.ifPresent(from-> toHandler.ifPresent(to-> {
                if(rate <= 0) {
                    return;
                }
                if(from == to) {
                    self.getTouchedBlocks().remove(entry.getKey());
                    self.setChanged();
                    self.sync();
                    return;
                }

                for(int count = 0; count < entry.getValue(); ++count) {
                    int current = rate;

                    for(int index = from.getSlots() - 1; 0 <= index; index--){
                        while(0 < current) {

                            ItemStack simulate = from.extractItem(index, current, true);

                            if(simulate.isEmpty()) {
                                break;
                            }

                            int original = simulate.getCount();
                            simulate = ItemHandlerHelper.insertItemStacked(to, simulate, false);
                            int extract = original - (!simulate.isEmpty() ? simulate.getCount() : 0);
                            from.extractItem(index, extract, false);

                            current -= extract;
                            if(!simulate.isEmpty()) {
                                break;
                            }
                        }
                    }
                }
            }));
        }

    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("LastReceive", this.lastReceive);
        pTag.putLong("LastTick", this.lastTick);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.lastReceive = pTag.getInt("LastReceive");
        this.lastTick = pTag.getLong("LastTick");
    }
}
