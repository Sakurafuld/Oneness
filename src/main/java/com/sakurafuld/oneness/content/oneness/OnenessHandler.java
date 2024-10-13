package com.sakurafuld.oneness.content.oneness;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.oneness.api.touch.TouchHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

import static com.sakurafuld.oneness.Deets.ONENESS;
import static com.sakurafuld.oneness.Deets.identifier;

@Mod.EventBusSubscriber(modid = ONENESS, value = Dist.CLIENT)
public class OnenessHandler {
    private static final Set<IIngameOverlay> CANCEL =
            Sets.newHashSet(
                    ForgeIngameGui.EXPERIENCE_BAR_ELEMENT);
    private static final ResourceLocation LOCATION = identifier(ONENESS, "textures/gui/oneness.png");

    @SubscribeEvent
    public static void hud(RenderGameOverlayEvent.PreLayer event) {
        Minecraft mc = Minecraft.getInstance();

        if(mc.player == null || TouchHandler.get(mc.player).isEmpty())
            return;

        if(event.getOverlay() == ForgeIngameGui.EXPERIENCE_BAR_ELEMENT) {
            PoseStack stack = event.getMatrixStack();

            Window window = event.getWindow();
            //真ん中X.
            int centerX = window.getScreenWidth() / 4;
            //一番下Y.
            int centerY = window.getScreenHeight() / 2;

            int leftX = centerX - (18 * 5) - 1;
            int upY = centerY - 22 - 5 - 2;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, LOCATION);
            GuiComponent.blit(stack, leftX, upY, 0, 0, 182, 5, 182, 5);

        }

        if(CANCEL.contains(event.getOverlay()))
            event.setCanceled(true);
    }
}
