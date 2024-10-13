package com.sakurafuld.oneness.network;

import com.sakurafuld.oneness.Deets;
import com.sakurafuld.oneness.network.touch.ClientboundRemoveTouchItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE
            = NetworkRegistry.newSimpleChannel(new ResourceLocation(Deets.ONENESS, "main"), ()-> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void initialize() {
        int id = 0;

        INSTANCE.registerMessage(id++, ClientboundRemoveTouchItem.class, ClientboundRemoveTouchItem::encode, ClientboundRemoveTouchItem::decode, ClientboundRemoveTouchItem::handle);
    }
}
