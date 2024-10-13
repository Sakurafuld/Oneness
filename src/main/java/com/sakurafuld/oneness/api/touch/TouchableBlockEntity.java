package com.sakurafuld.oneness.api.touch;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.sakurafuld.oneness.Deets.*;

public abstract class TouchableBlockEntity extends BlockEntity implements ITouchable {
    @NotNull
    private UUID owner = Util.NIL_UUID;
    private final Map<Pair<BlockPos, Direction>, Integer> TOUCHED_BLOCKS = new HashMap<>();

    public TouchableBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);

    }

    public @NotNull UUID getOwner() {
        return this.owner;
    }
    public Map<Pair<BlockPos, Direction>, Integer> getTouchedBlocks() {
        return TOUCHED_BLOCKS;
    }
    @Override
    public final TouchItem snatch(Player player) {
        if(!this.getOwner().equals(Util.NIL_UUID) && this.getLevel().getPlayerByUUID(this.getOwner()) != null) {
            LOG.debug("{}-HasOwnerCancel", side());
            return null;
        }
        LOG.debug("{}-NilOwner", side());
        required(LogicalSide.SERVER).run(() -> {
            this.owner = player.getUUID();
            this.setChanged();
            this.sync();
        });

        return this.doSnatch(player);
    }
    public abstract TouchItem doSnatch(Player player);
    @Override
    public void reach(@Nullable Player player) {
        this.owner = Util.NIL_UUID;
        required(LogicalSide.SERVER).run(() -> {
            this.setChanged();
            this.sync();
        });
    }

    @Override
    public InteractionResult onTouchBlock(Player player, BlockPos pos, Direction face) {
        LOG.debug("{}-onTouchBlock={}", side(), this.getClass().getSimpleName());
        InteractionResult result = this.doTouchBlock(player, pos, face);
        required(LogicalSide.SERVER).run(() -> {
            if(result.consumesAction()) {
                Pair<BlockPos, Direction> key = new Pair<>(pos, face);
                this.getTouchedBlocks().computeIfPresent(key, (pair, count) -> ++count);
                this.getTouchedBlocks().putIfAbsent(key, 1);
                this.setChanged();
                this.sync();
            }
        });

        return result;
    }
    public InteractionResult doTouchBlock(Player player, BlockPos pos, Direction face) {return InteractionResult.sidedSuccess(side().isClient());}

    @Override
    public void onPunchBlock(Player player, BlockPos pos, Direction face) {
        LOG.debug("{}-onPunchBlock={}", side(), this.getClass().getSimpleName());
        required(LogicalSide.SERVER).run(() -> {
            if(this.doPunchBlock(player, pos, face)) {
                Pair<BlockPos, Direction> key = new Pair<>(pos, face);
                this.getTouchedBlocks().computeIfPresent(key, (pair, count) -> --count > 0 ? count : null);
            }
            this.setChanged();
            this.sync();
        });
    }
    public boolean doPunchBlock(Player player, BlockPos pos, Direction face) {return true;}

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

//        LOG.debug("{}-saveAddition=-Pos{}-={}!!!", side(), this.getBlockPos().asLong(), this.owner);

        pTag.putUUID("Owner", this.getOwner());
        pTag.put("SavedTouch", this.saveTouched());
    }
    @Override
    public @NotNull CompoundTag getUpdateTag() {
        LOG.debug("{}-getTouchTag!!!", side());
        CompoundTag tag = new CompoundTag();
//        LOG.debug("{}-updateTag=-Pos{}-={}!!!", side(), this.getBlockPos().asLong(), this.owner);

        tag.putUUID("Owner", this.getOwner());
        tag.put("SavedTouch", this.saveTouched());
        return tag;
    }
    private CompoundTag saveTouched() {
        LOG.debug("{}-saveTouched={}", side(), this.getTouchedBlocks().size());
        CompoundTag savedTouch = new CompoundTag();

        ListTag list = new ListTag();
        for(Map.Entry<Pair<BlockPos, Direction>, Integer> entry : this.getTouchedBlocks().entrySet()){
            LOG.debug("{}-forEachSave", side());
            CompoundTag touched = new CompoundTag();
            touched.putLong("First", entry.getKey().getFirst().asLong());
            touched.putInt("Second", entry.getKey().getSecond().ordinal());
            touched.putInt("Count", entry.getValue());
            list.add(touched);
        }
        savedTouch.put("TouchedBlockMap", list);

        return savedTouch;
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        this.owner = pTag.getUUID("Owner");

//        LOG.debug("{}-load=-Pos{}-={}!!!", side(), this.getBlockPos().asLong(), this.owner);
        this.getTouchedBlocks().clear();

        CompoundTag savedTouch = pTag.getCompound("SavedTouch");
//        LOG.debug("{}-loadContainsList={}", side(), savedTouch.contains("TouchedBlockMap"));
        for(Tag entry : savedTouch.getList("TouchedBlockMap", Tag.TAG_COMPOUND)){
            LOG.debug("{}-forEachLoad", side());
            if(entry instanceof CompoundTag compound)
                this.getTouchedBlocks().put(new Pair<>(BlockPos.of(compound.getLong("First")), Direction.values()[compound.getInt("Second")]), compound.getInt("Count"));
        }
    }
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        LOG.debug("{}-getTouchPacket!!!", side());
        return ClientboundBlockEntityDataPacket.create(this);
    }
    public void sync(){
        if (this.getLevel() instanceof ServerLevel level) {
            level.getChunkSource().chunkMap.getPlayers(new ChunkPos(this.getBlockPos()), false)
                    .forEach(e -> e.connection.send(this.getUpdatePacket()));

        }
    }
}
