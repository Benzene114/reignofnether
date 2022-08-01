package com.solegendary.reignofnether.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.solegendary.reignofnether.ReignOfNether;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class MyRenderer {

    private static final Minecraft MC = Minecraft.getInstance();

    public static void drawBlockOutline(PoseStack matrixStack, BlockPos blockpos, float a) {
        AABB aabb = new AABB(blockpos).move(0,0.01,0);
        drawLineBox(matrixStack, aabb, 1.0f,1.0f,1.0f, a);
    }

    public static void drawEntityOutline(PoseStack matrixStack, Entity entity, float a) {
        drawLineBox(matrixStack, entity.getBoundingBox(), 1.0f,1.0f,1.0f, a);
    }

    public static void drawEntityOutline(PoseStack matrixStack, Entity entity, float r, float g, float b, float a) {
        drawLineBox(matrixStack, entity.getBoundingBox(), r, g, b, a);
    }

    // like drawEntityOutline but only the bottom square
    public static void drawOutlineBottom(PoseStack matrixStack, AABB aabb, float r, float g, float b, float a) {
        aabb = aabb.setMaxY(aabb.minY);
        drawLineBox(matrixStack, aabb, r, g, b, a);
    }

    public static void drawLineBox(PoseStack matrixStack, AABB aabb, float r, float g, float b, float a) {
        Entity camEntity = MC.getCameraEntity();
        double d0 = camEntity.getX();
        double d1 = camEntity.getY() + camEntity.getEyeHeight();
        double d2 = camEntity.getZ();

        RenderSystem.depthMask(false); // disable showing lines through blocks
        VertexConsumer vertexConsumer = MC.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        matrixStack.pushPose();
        matrixStack.translate(-d0, -d1, -d2); // because we start at 0,0,0 relative to camera
        LevelRenderer.renderLineBox(matrixStack, vertexConsumer, aabb, r, g, b, a);
        matrixStack.popPose();
    }

    // remember white.png can still be used with RGBA values
    public static void drawWhiteBox(PoseStack matrixStack, BlockPos bp, float a) {
        AABB aabb = new AABB(bp);
        aabb = aabb.setMaxY(aabb.maxY + 0.01f);
        drawSolidBox(matrixStack, aabb, null, 1.0f, 1.0f, 1.0f, a, new ResourceLocation("forge:textures/white.png"));
    }
    public static void drawBlackBox(PoseStack matrixStack, BlockPos bp, float a) {
        AABB aabb = new AABB(bp);
        aabb = aabb.setMaxY(aabb.maxY + 0.01f);
        drawSolidBox(matrixStack, aabb, null, 1.0f, 1.0f, 1.0f, a, new ResourceLocation("forge:textures/black.png"));
    }
    public static void drawWhiteBlockFace(PoseStack matrixStack, Direction dir, BlockPos bp, float a) {
        AABB aabb = new AABB(bp);
        aabb = aabb.setMaxY(aabb.maxY + 0.01f);
        drawSolidBox(matrixStack, aabb, dir, 1.0f, 1.0f, 1.0f, a, new ResourceLocation("forge:textures/white.png"));
    }
    public static void drawBlackBlockFace(PoseStack matrixStack, Direction dir, BlockPos bp, float a) {
        AABB aabb = new AABB(bp);
        aabb = aabb.setMaxY(aabb.maxY + 0.01f);
        drawSolidBox(matrixStack, aabb, dir, 1.0f, 1.0f, 1.0f, a, new ResourceLocation("forge:textures/black.png"));
    }

    public static void drawSolidBox(PoseStack matrixStack, AABB aabb, Direction dir, float r, float g, float b, float a, ResourceLocation rl) {
        Entity camEntity = MC.getCameraEntity();
        double d0 = camEntity.getX();
        double d1 = camEntity.getY() + camEntity.getEyeHeight();
        double d2 = camEntity.getZ();

        RenderSystem.depthMask(false); // disable showing lines through blocks

        matrixStack.pushPose();
        matrixStack.translate(-d0, -d1, -d2); // because we start at 0,0,0 relative to camera
        Matrix4f matrix4f = matrixStack.last().pose();
        Matrix3f matrix3f = matrixStack.last().normal();

        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;

        // Note that error: 'not filled all elements of vertex' means the vertex needs more elements,
        // eg. ENTITY_TRANSLUCENT needs vertex(x,y,z).color(rgba).uv(0,0).overlayCoords(0,0).uv2(light).normal(x,y,z)
        // you can trace this all the way back to the DefaultVertexFormat class where these vertex elements are defined
        // normal is the vector perpendicular to the plane, if not used all quads will always be flat facing

        // uv() are the texture coordinates, if you dont use a texture, they can be (0, 0).
        // uv2() are the block and skylight (packed with LightTexture.pack() to one integer).
        // overlayCoords() refers to overlay effects:
        //      (0,10) is no overlay
        //      (0,0) is 'entity hurt', ie. the red overlaid when entities take damage

        VertexConsumer vertexConsumer = MC.renderBuffers().bufferSource().getBuffer(RenderType.entityTranslucent(rl));

        // all vertices are in order: BR, TR, TL, BL

        int light = 255;

        // +y top face
        if (dir == null || dir == Direction.UP) {
            vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        }
        // +x side face
        if (dir == null || dir == Direction.EAST) {
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        }
        // +z side face
        if (dir == null || dir == Direction.SOUTH) {
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        }
        // -x side face
        if (dir == null || dir == Direction.WEST) {
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, maxZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).uv(0,0).overlayCoords(0,10).uv2(light).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
        }
        // -z side face
        if (dir == null || dir == Direction.NORTH) {
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, maxY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, maxY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
        }
        // -y bottom face
        if (dir == null || dir == Direction.DOWN) {
            vertexConsumer.vertex(matrix4f, minX, minY, maxZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, minX, minY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, minZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
            vertexConsumer.vertex(matrix4f, maxX, minY, maxZ).color(r, g, b, a).uv(0, 0).overlayCoords(0, 10).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        }
        matrixStack.popPose();
    }

    public static void renderFrameWithBg(PoseStack poseStack, int x, int y, int width, int height, int bgCol) {
        // draw icon frame with dark transparent bg
        GuiComponent.fill(poseStack, // x1,y1, x2,y2,
                x + 2, y + 2,
                x + width - 2,
                y + height - 2,
                bgCol); //ARGB(hex); note that alpha ranges between ~0-16 in RenderOverlayEvent, not 0-255

        ResourceLocation iconFrameResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/unit_frame_no_bg.png");
        RenderSystem.setShaderTexture(0, iconFrameResource);
        GuiComponent.blit(poseStack,
                x, y, 0,
                0,0, // where on texture to start drawing from
                width, height, // dimensions of blit texture
                width, height // size of texture itself (if < dimensions, texture is repeated)
        );
    }

    public static void renderIconFrameWithBg(PoseStack poseStack, int x, int y, int size, int bg) {
        //transparent background
        GuiComponent.fill(poseStack, // x1,y1, x2,y2,
                x, y,
                x + size,
                y + size,
                bg); //ARGB(hex); note that alpha ranges between ~0-16, not 0-255

        // icon frame
        ResourceLocation iconFrameResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame.png");
        RenderSystem.setShaderTexture(0, iconFrameResource);
        GuiComponent.blit(poseStack,
                x, y, 0,
                0,0, // where on texture to start drawing from
                size, size, // dimensions of blit texture
                size, size // size of texture itself (if < dimensions, texture is repeated)
        );
    }

    public static void renderIcon(PoseStack poseStack, ResourceLocation resourceLocation, int x, int y, int size) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        GuiComponent.blit(poseStack,
                x, y, 0,
                0,0, // where on texture to start drawing from
                size, size, // dimensions of blit texture
                size, size // size of texture itself (if < dimensions, texture is repeated)
        );
    }


}
