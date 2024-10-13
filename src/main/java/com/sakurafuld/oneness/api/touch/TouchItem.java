package com.sakurafuld.oneness.api.touch;

import com.sakurafuld.oneness.api.capability.TouchItemStack;
import com.sakurafuld.oneness.network.PacketHandler;
import com.sakurafuld.oneness.network.touch.ClientboundRemoveTouchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import static com.sakurafuld.oneness.Deets.LOG;
import static com.sakurafuld.oneness.Deets.side;

public abstract class TouchItem extends Item {
    public TouchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        pStack.getCapability(TouchItemStack.CAPABILITY).ifPresent(touch -> {
            if(pEntity instanceof ServerPlayer player) {
                if(touch.getFromPos() == null) {
                    LOG.debug("{}-inventoryNull", side());
                    player.getInventory().removeItemNoUpdate(pSlotId);
                    return;
                }

//                LOG.debug("{}-inventoryTurn-dim={}-slo={}-block={}", side(), !pLevel.dimension().equals(touch.getDimension()), pSlotId != touch.getSlot(), !(pLevel.getBlockEntity(touch.getFromPos()) instanceof ITouchable));
                if(Math.sqrt(touch.getFromPos().distToCenterSqr(player.position())) >= 100
                        || (!pLevel.dimension().equals(touch.getDimension())
                        || pSlotId != touch.getSlot()
                        || !(pLevel.getBlockEntity(touch.getFromPos()) instanceof ITouchable itouchable)
                        || (itouchable instanceof TouchableBlockEntity touchable && !touchable.getOwner().equals(player.getUUID())))) {

                    touch.reach(player, ServerLifecycleHooks.getCurrentServer().getLevel(touch.getDimension()));
                    player.getInventory().removeItemNoUpdate(pSlotId);
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundRemoveTouchItem(player.getUUID(), pSlotId));
                }
            }
        });
    }
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        stack.getCapability(TouchItemStack.CAPABILITY).ifPresent(touch -> {
            LOG.debug("{}-entityTurn!!!!", side());
            touch.reach(entity.getThrower() != null ? entity.getLevel().getPlayerByUUID(entity.getThrower()) : null, entity.getLevel());
            if(entity.tickCount > 1) {
                entity.discard();
            }
        });
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        Player player;
        if((player = context.getPlayer()) == null) return InteractionResult.FAIL;

        if(stack.getCapability(TouchItemStack.CAPABILITY).isPresent()) {
            LOG.debug("{}-useIsPresent", side());
            TouchItemStack touch = stack.getCapability(TouchItemStack.CAPABILITY).orElseThrow(IllegalStateException::new);
            if(touch.getFromPos() == null) {
                return InteractionResult.FAIL;
            }
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            if(level.getBlockEntity(touch.getFromPos()) instanceof ITouchable touchable) {
                if(pos.equals(touch.getFromPos())) {
                    LOG.debug("{}-equalPosTurn", side());
                    touch.reach(player, level);
                    player.getInventory().removeItemNoUpdate(touch.getSlot());

                    return InteractionResult.sidedSuccess(side().isClient());
                }

                return touchable.onTouchBlock(player, pos, context.getClickedFace());
            } else {
                touch.reach(player, level);
                player.getInventory().removeItemNoUpdate(touch.getSlot());
            }
        }

        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        return InteractionResult.FAIL;
    }
}
