package com.sakurafuld.oneness.content.oneness;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import com.sakurafuld.oneness.content.ModItems;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OnenessBlockEntityRenderer implements BlockEntityRenderer<OnenessBlockEntity> {
    public OnenessBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(OnenessBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if(!pBlockEntity.getOwner().equals(Util.NIL_UUID) && pBlockEntity.getLevel().getPlayerByUUID(pBlockEntity.getOwner()) != null)
            return;

        Minecraft mc = Minecraft.getInstance();
        ItemStack stack = new ItemStack(ModItems.ONENESS_TOUCH.get());

        pPoseStack.pushPose();
        pPoseStack.scale(1.5f, 1.5f, 1.5f);

        switch(Math.abs(pBlockEntity.getBlockPos().hashCode()) % 4) {
            case 0 -> {
                pPoseStack.translate(0.175, 0.8, 0.175);
                pPoseStack.mulPose(Vector3f.YP.rotationDegrees(-45));
            }
            case 1 -> {
                pPoseStack.translate(0.175, 0.8, 0.5);
                pPoseStack.mulPose(Vector3f.YP.rotationDegrees(45));
            }
            case 2 -> {
                pPoseStack.translate(0.5, 0.8, 0.5);
                pPoseStack.mulPose(Vector3f.YP.rotationDegrees(135));
            }
            case 3 -> {
                pPoseStack.translate(0.5, 0.8, 0.175);
                pPoseStack.mulPose(Vector3f.YP.rotationDegrees(-135));
            }
        }

        mc.getItemRenderer().render(stack, ItemTransforms.TransformType.GROUND, false, pPoseStack, pBufferSource, pPackedLight, OverlayTexture.NO_OVERLAY, mc.getItemRenderer().getModel(stack, pBlockEntity.getLevel(), null, 0));
        pPoseStack.popPose();
    }
    public static class Render extends RenderType {

        public Render(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
            super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
        }
        public static final RenderType TYPE = create("oneness",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
                CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));
    }
}
