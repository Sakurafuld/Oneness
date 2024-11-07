package com.sakurafuld.oneness.network.touch;

import com.sakurafuld.oneness.api.capability.TouchItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundRemoveTouchItem {
    private final UUID PLAYER;
    private final int SLOT;


    public ClientboundRemoveTouchItem(UUID player, int slot) {
        this.PLAYER = player;
        this.SLOT = slot;

    }
    
    public static void encode(ClientboundRemoveTouchItem msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.PLAYER);
        buf.writeInt(msg.SLOT);

    }
    public static ClientboundRemoveTouchItem decode(FriendlyByteBuf buf) {
        return new ClientboundRemoveTouchItem(buf.readUUID(), buf.readInt());
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().level.getPlayerByUUID(this.PLAYER);
            if(player == null) return;
            player.getInventory().getItem(this.SLOT).getCapability(TouchItemStack.CAPABILITY).ifPresent(touch -> {
                touch.reach(player, player.getLevel());
                player.getInventory().removeItemNoUpdate(this.SLOT);
            });

        });
        ctx.get().setPacketHandled(true);
    }
}
