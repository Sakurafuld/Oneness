package com.sakurafuld.oneness.mixin.oneness;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.sakurafuld.oneness.api.capability.TouchItemStack;
import com.sakurafuld.oneness.api.touch.TouchHandler;
import com.sakurafuld.oneness.api.touch.TouchableBlockEntity;
import com.sakurafuld.oneness.content.oneness.OnenessBlockEntityRenderer;
import com.sakurafuld.oneness.content.oneness.OnenessTouchItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Unique
    private final Color BLOCK = new Color(0.8f, 0, 0, 0.1f);
    @Unique
    private final Color ORIGIN = new Color(0.5f, 0.5f, 0.5f, 0.1f);

    @Inject(method = "renderLevel", at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;applyModelViewMatrix()V", ordinal = 1))
    private void renderLevelOneness(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Player player = mc.player;
        if(level == null || player == null)
            return;

        TouchHandler.get(player).ifPresent(stack -> stack.getCapability(TouchItemStack.CAPABILITY).ifPresent(touch -> {
            if(stack.getItem() instanceof OnenessTouchItem && level.getBlockEntity(touch.getFromPos()) instanceof TouchableBlockEntity touchable) {

                VertexConsumer vertexConsumer = mc.renderBuffers().bufferSource().getBuffer(OnenessBlockEntityRenderer.Render.TYPE);

                Vec3 view = pCamera.getPosition();
                BlockPos origin = touchable.getBlockPos().relative(touchable.getBlockState().getValue(BlockStateProperties.FACING_HOPPER));
                double x = origin.getX() - view.x();
                double y = origin.getY() - view.y();
                double z = origin.getZ() - view.z();

                Origin : {
                    pPoseStack.pushPose();
                    pPoseStack.translate(x, y, z);

                    pPoseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                    this.render(pPoseStack.last().pose(), vertexConsumer, ORIGIN, 1);

                    pPoseStack.popPose();
                }

                Set<Pair<BlockPos, Direction>> keySet = touchable.getTouchedBlocks().keySet();
                Set<BlockPos> posSet = keySet.parallelStream().map(Pair::getFirst).collect(Collectors.toSet());


                for(Pair<BlockPos, Direction> pair : touchable.getTouchedBlocks().keySet()) {
                    BlockPos pos = pair.getFirst();
                     x = pos.getX() - view.x();
                     y = pos.getY() - view.y();
                     z = pos.getZ() - view.z();

                    pPoseStack.pushPose();
                    pPoseStack.translate(x, y, z);


                    Cube : {
                        pPoseStack.pushPose();

                        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                        this.render(pPoseStack.last().pose(), vertexConsumer, BLOCK, 1, pos, posSet);

                        pPoseStack.popPose();
                    }
                    
                    Face : {
                        pPoseStack.pushPose();

                        Vec3i normal = pair.getSecond().getNormal();
                        pPoseStack.translate((double) normal.getX() / 3, (double) normal.getY() / 3, (double) normal.getZ() / 3);
                        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                        this.render(pPoseStack.last().pose(), vertexConsumer, Color.YELLOW, 0.6f);

                        pPoseStack.popPose();
                    }
                    
                    pPoseStack.popPose();
                }
                mc.renderBuffers().bufferSource().endBatch(OnenessBlockEntityRenderer.Render.TYPE);
            }
        }));
    }

    @Unique
    private void render(Matrix4f matrix, VertexConsumer builder, Color color, float scale) {
        this.render(matrix, builder, color, scale, null, null);
    }
    @Unique
    private void render(Matrix4f matrix, VertexConsumer builder, Color color, float scale, BlockPos pos, Set<BlockPos> posSet) {
        float red = color.getRed() / 255f, green = color.getGreen() / 255f, blue = color.getBlue() / 255f, alpha = .5f;

        float startX = 0 + (1 - scale) / 2, startY = 0 + (1 - scale) / 2, startZ = -1 + (1 - scale) / 2, endX = 1 - (1 - scale) / 2, endY = 1 - (1 - scale) / 2, endZ = 0 - (1 - scale) / 2;

        boolean flag = pos == null || posSet == null || scale != 1;

        if(flag || !posSet.contains(pos.relative(Direction.DOWN))) {
            //down
            builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.UP))) {
            //up
            builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.EAST))) {
            //east
            builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.WEST))) {
            //west
            builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.SOUTH))) {
            //south
            builder.vertex(matrix, endX, startY, startZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, endY, startZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        }

        if(flag || !posSet.contains(pos.relative(Direction.NORTH))) {
            //north
            builder.vertex(matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, startX, startY, endZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, startX, endY, endZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        }
    }
}
