package com.sakurafuld.oneness;

import com.sakurafuld.oneness.api.client.SpecialKeyEvent;
import com.sakurafuld.oneness.content.ModBlockEntities;
import com.sakurafuld.oneness.content.ModBlocks;
import com.sakurafuld.oneness.content.ModItems;
import com.sakurafuld.oneness.content.oneness.OnenessBlockEntityRenderer;
import com.sakurafuld.oneness.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.sakurafuld.oneness.Deets.*;

@Mod(ONENESS)
public class Oneness {

    public Oneness() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::registerBlockEntityRenderers);

        ModBlockEntities.REGISTRY.register(bus);
        ModBlocks.REGISTRY.register(bus);
        ModItems.REGISTRY.register(bus);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        PacketHandler.initialize();
    }
    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> new SpecialKeyEvent().setup(Minecraft.getInstance()));
    }
    public void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        required(DRACONICEVOLUTION).run(() -> {
            event.registerBlockEntityRenderer(ModBlockEntities.ONENESS.get(), OnenessBlockEntityRenderer::new);
        });
    }
}
