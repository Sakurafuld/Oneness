package com.sakurafuld.oneness.api.touch;

import com.sakurafuld.oneness.api.capability.TouchItemStack;
import com.sakurafuld.oneness.api.client.SpecialKeyEvent;
import com.sakurafuld.oneness.network.PacketHandler;
import com.sakurafuld.oneness.network.touch.ClientboundRemoveTouchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

import static com.sakurafuld.oneness.Deets.*;

@Mod.EventBusSubscriber(modid = ONENESS)
public class TouchHandler {
    @SubscribeEvent// ほんとはcatchって名前にしたかった.
    public static void snatch(PlayerInteractEvent.RightClickBlock event) {
        if(event.getPlayer().getMainHandItem().isEmpty()) {
            ItemLike item;
            if(get(event.getPlayer()).isEmpty() && event.getWorld().getBlockEntity(event.getPos()) instanceof ITouchable touchable && (item = touchable.snatch(event.getPlayer())) != null) {
                ItemStack stack = new ItemStack(item);
                LOG.debug("{}-touchIsPresent={}", side(), stack.getCapability(TouchItemStack.CAPABILITY).isPresent());

                stack.getCapability(TouchItemStack.CAPABILITY).ifPresent(touch -> {
                    touch.set(event.getWorld().dimension(), event.getPlayer().getInventory().selected, event.getPos());
                    event.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, stack);
                    event.getPlayer().swing(InteractionHand.MAIN_HAND);
                });
            }
        }
    }

    @SubscribeEvent
    public static void punchBlock(PlayerInteractEvent.LeftClickBlock event) {
        if(event.getItemStack().getItem() instanceof TouchItem) {
            long time = event.getPlayer().getLevel().getGameTime();
            CompoundTag tag = event.getPlayer().getPersistentData();
            if((time - tag.getLong("LastPunch")) > 2) {
                event.getItemStack().getCapability(TouchItemStack.CAPABILITY).ifPresent(touch -> {
                    LOG.info("{}-Punch={}", side(), time);
                    if(touch.getFromPos() == null) return;
                    if(event.getWorld().getBlockEntity(touch.getFromPos()) instanceof ITouchable touchable) {
                        touchable.onPunchBlock(event.getPlayer(), event.getPos(), event.getFace());
                    }
                });
            }
            tag.putLong("LastPunch", time);
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void key(SpecialKeyEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Options options = mc.options;
        Player player = mc.player;

        if(player == null) return;

        ItemStack stack = player.getMainHandItem();
        if(!stack.isEmpty() && stack.getItem() instanceof TouchItem) {
            if(options.keySwapOffhand.matches(event.getKey(), event.getScancode())) {
                event.setCanceled(true);
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void container(ScreenEvent.InitScreenEvent.Pre event) {
        if(Minecraft.getInstance().player != null && event.getScreen() instanceof AbstractContainerScreen<?> && get(Minecraft.getInstance().player).isPresent()) {
            event.setCanceled(true);
            Minecraft.getInstance().screen = null;
            Minecraft.getInstance().mouseHandler.grabMouse();
        }
    }
    @SubscribeEvent
    public static void drop(ItemTossEvent event) {
        event.getEntityItem().getItem().getCapability(TouchItemStack.CAPABILITY).ifPresent(touch -> required(LogicalSide.SERVER).run(() -> {
            event.setCanceled(true);
            touch.reach(event.getPlayer(), event.getEntityItem().getLevel());
            event.getPlayer().getInventory().removeItemNoUpdate(touch.getSlot());
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getPlayer()), new ClientboundRemoveTouchItem(event.getPlayer().getUUID(), touch.getSlot()));
        }));
    }
    @SubscribeEvent
    public static void rightBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockEntity blockEntity;
        if(get(event.getPlayer()).isPresent() && (blockEntity = event.getWorld().getBlockEntity(event.getPos())) != null && !(event.getPlayer().getMainHandItem().getItem() instanceof TouchItem)) {
            boolean flag = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent();
            for(Direction face : Direction.values()) {
                if(flag)
                    break;
                flag = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face).isPresent();
            }
            if(flag)
                event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void rightItem(PlayerInteractEvent.RightClickItem event) {
        if(get(event.getPlayer()).isPresent() && !(event.getItemStack().getItem() instanceof TouchItem)) {
            boolean flag = event.getItemStack().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent();
            for(Direction face : Direction.values()) {
                if(flag)
                    break;
                flag = event.getItemStack().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face).isPresent();
            }
            if(flag)
                event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void rightEntity(PlayerInteractEvent.EntityInteract event) {
        if(event.getPlayer().getMainHandItem().getItem() instanceof TouchItem) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void rightEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if(event.getPlayer().getMainHandItem().getItem() instanceof TouchItem) {
            event.setCanceled(true);
        }
    }


    public static Optional<ItemStack> get(Player player) {
        for(ItemStack stack : player.getInventory().items) {
            if(!stack.isEmpty() && stack.getItem() instanceof TouchItem)
                return Optional.of(stack);
        }
        return Optional.empty();
    }
}
