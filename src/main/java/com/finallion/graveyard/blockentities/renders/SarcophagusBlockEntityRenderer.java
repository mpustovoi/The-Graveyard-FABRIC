package com.finallion.graveyard.blockentities.renders;

import com.finallion.graveyard.TheGraveyard;
import com.finallion.graveyard.blockentities.SarcophagusBlockEntity;
import com.finallion.graveyard.blockentities.enums.SarcophagusPart;
import com.finallion.graveyard.blocks.GravestoneBlock;
import com.finallion.graveyard.blocks.SarcophagusBlock;
import com.finallion.graveyard.init.TGBlocks;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.block.ChestAnimationProgress;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class SarcophagusBlockEntityRenderer<T extends BlockEntity & ChestAnimationProgress> implements BlockEntityRenderer<SarcophagusBlockEntity> {
    public static final Identifier SARCOPHAGUS_FOOT = new Identifier(TheGraveyard.MOD_ID, "block/sarcophagus_foot");
    public static final Identifier SARCOPHAGUS_FOOT_LID = new Identifier(TheGraveyard.MOD_ID, "block/sarcophagus_foot_lid");
    public static final Identifier SARCOPHAGUS_HEAD_LID = new Identifier(TheGraveyard.MOD_ID, "block/sarcophagus_head_lid");
    public static final Identifier SARCOPHAGUS_HEAD = new Identifier(TheGraveyard.MOD_ID, "block/sarcophagus_head");
    private final BakedModel sarcophagusModelHead;
    private final BakedModel sarcophagusModelFoot;
    private final BakedModel sarcophagusModelFootLid;
    private final BakedModel sarcophagusModelHeadLid;
    //TODO: load models in constructor instead on tick
    /*
    private final BakedModel oakCoffinModelHead;
    private final BakedModel oakCoffinModelFoot;
    private final BakedModel oakCoffinModelFootLid;
    private final BakedModel oakCoffinModelHeadLid;
    private final BakedModel darkOakCoffinModelHead;
    private final BakedModel darkOakCoffinModelFoot;
    private final BakedModel darkOakCoffinModelFootLid;
    private final BakedModel darkOakCoffinModelHeadLid;
    private final BakedModel birchCoffinModelHead;
    private final BakedModel birchCoffinModelFoot;
    private final BakedModel birchCoffinModelFootLid;
    private final BakedModel birchCoffinModelHeadLid;
    private final BakedModel jungleCoffinModelHead;
    private final BakedModel jungleCoffinModelFoot;
    private final BakedModel jungleCoffinModelFootLid;
    private final BakedModel jungleCoffinModelHeadLid;
    private final BakedModel acaciaCoffinModelHead;
    private final BakedModel acaciaCoffinModelFoot;
    private final BakedModel acaciaCoffinModelFootLid;
    private final BakedModel acaciaCoffinModelHeadLid;
    private final BakedModel spruceCoffinModelHead;
    private final BakedModel spruceCoffinModelFoot;
    private final BakedModel spruceCoffinModelFootLid;
    private final BakedModel spruceCoffinModelHeadLid;
    private final BakedModel crimsonCoffinModelHead;
    private final BakedModel crimsonCoffinModelFoot;
    private final BakedModel crimsonCoffinModelFootLid;
    private final BakedModel crimsonCoffinModelHeadLid;
    private final BakedModel warpedCoffinModelHead;
    private final BakedModel warpedCoffinModelFoot;
    private final BakedModel warpedCoffinModelFootLid;
    private final BakedModel warpedCoffinModelHeadLid;
     */


    public SarcophagusBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        this.sarcophagusModelHead = BakedModelManagerHelper.getModel(client.getBakedModelManager(), SARCOPHAGUS_HEAD);
        this.sarcophagusModelFoot = BakedModelManagerHelper.getModel(client.getBakedModelManager(), SARCOPHAGUS_FOOT);
        this.sarcophagusModelFootLid = BakedModelManagerHelper.getModel(client.getBakedModelManager(), SARCOPHAGUS_FOOT_LID);
        this.sarcophagusModelHeadLid = BakedModelManagerHelper.getModel(client.getBakedModelManager(), SARCOPHAGUS_HEAD_LID);
    }


    @Override
    public void render(SarcophagusBlockEntity entity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();

        BlockState blockState = entity.getCachedState();
        String name = entity.getCachedState().getBlock().getTranslationKey();
        boolean isCoffin = entity.isCoffin();

        DoubleBlockProperties.PropertySource<? extends SarcophagusBlockEntity> propertySource = DoubleBlockProperties.toPropertySource(TGBlocks.SARCOPHAGUS_BLOCK_ENTITY, SarcophagusBlock::getSarcophagusPart, SarcophagusBlock::getOppositePartDirection, ChestBlock.FACING, blockState, world, entity.getPos(), (worldx, pos) -> {
            return false;
        });
        float g = ((Float2FloatFunction) propertySource.apply(SarcophagusBlock.getAnimationProgressRetriever((ChestAnimationProgress) entity))).get(tickDelta);
        g = 1.0F - g;
        g = 1.0F - g * g * g;
        int k = ((Int2IntFunction) propertySource.apply(new LightmapCoordinatesRetriever())).get(light);

        BakedModel footLid = getModel(name, isCoffin, 0);
        BakedModel headLid = getModel(name, isCoffin, 1);
        BakedModel head = getModel(name, isCoffin, 2);
        BakedModel foot = getModel(name, isCoffin, 3);

        if (world != null) {
            if (isCoffin) {
                this.renderPart(entity, matrixStack, vertexConsumers, blockState.get(SarcophagusBlock.PART) == SarcophagusPart.HEAD ? head : foot, (Direction) blockState.get(SarcophagusBlock.FACING), vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, false);
                this.renderLid(entity, matrixStack, vertexConsumers, blockState.get(SarcophagusBlock.PART) == SarcophagusPart.HEAD ? headLid : footLid, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, false, g);
            } else {
                this.renderPart(entity, matrixStack, vertexConsumers, blockState.get(SarcophagusBlock.PART) == SarcophagusPart.HEAD ? this.sarcophagusModelHead : this.sarcophagusModelFoot, (Direction) blockState.get(SarcophagusBlock.FACING), vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, false);
                this.renderLid(entity, matrixStack, vertexConsumers, blockState.get(SarcophagusBlock.PART) == SarcophagusPart.HEAD ? this.sarcophagusModelHeadLid : this.sarcophagusModelFootLid, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, false, g);
            }
        } else {
            if (isCoffin) {
                this.renderLid(entity, matrixStack, vertexConsumers, footLid, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, true, g);
                this.renderPart(entity, matrixStack, vertexConsumers, foot, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, true);
                this.renderLid(entity, matrixStack, vertexConsumers, headLid, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, false, g);
                this.renderPart(entity, matrixStack, vertexConsumers, head, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, false);
            } else {
                this.renderLid(entity, matrixStack, vertexConsumers, sarcophagusModelFootLid, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, true, g);
                this.renderPart(entity, matrixStack, vertexConsumers, sarcophagusModelFoot, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, true);
                this.renderLid(entity, matrixStack, vertexConsumers, sarcophagusModelHeadLid, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, false, g);
                this.renderPart(entity, matrixStack, vertexConsumers, sarcophagusModelHead, Direction.SOUTH, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.getCachedState())), k, overlay, false);
            }
        }
    }


    private void renderPart(SarcophagusBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, BakedModel model, Direction direction, VertexConsumer vertexConsumer, int light, int overlay, boolean isFoot) {
        BlockModelRenderer renderer = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
        World world = entity.getWorld();

        matrices.push();
        matrices.translate(0.0D, 0.0D, isFoot ? -1.0D : 0.0D);

        float f = ((Direction) entity.getCachedState().get(SarcophagusBlock.FACING)).getOpposite().asRotation();
        matrices.translate(0.5D, 0.5D, 0.5D);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-f));

        matrices.translate(-0.5D, -0.5D, -0.5D);
        renderer.render(world, model, entity.getCachedState(), entity.getPos(), matrices, vertexConsumer, false, new Random(), 0, overlay);


        matrices.pop();
    }

    private void renderLid(SarcophagusBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, BakedModel model, Direction direction, VertexConsumer vertexConsumer, int light, int overlay, boolean isFoot, float openFactor) {
        BlockModelRenderer renderer = MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer();
        World world = entity.getWorld();

        matrices.push();
        matrices.translate(0.0D, 0.0D, isFoot ? -1.0D : 0.0D);

        float f = ((Direction) entity.getCachedState().get(SarcophagusBlock.FACING)).getOpposite().asRotation();
        matrices.translate(0.5D, 0.5D, 0.5D);
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-f));

        // ANIMATION START
        matrices.multiply(Vec3f.NEGATIVE_Z.getDegreesQuaternion(openFactor * 70)); // lid rotation
        matrices.translate(isFoot ? -(openFactor * 0.25) : openFactor * 0.25, openFactor * 0.25, 0.0F); // lid offset to the ground and away from body
        // ANIMATION END

        matrices.translate(-0.5D, -0.5D, -0.5D);
        renderer.render(world, model, entity.getCachedState(), entity.getPos(), matrices, vertexConsumer, false, new Random(), 0, overlay);


        matrices.pop();
    }

    private BakedModel getModel(String name, boolean isCoffin, int part) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (isCoffin) {
            String woodType = name.split("\\.")[2];
            switch (part) {
                default -> {
                    return BakedModelManagerHelper.getModel(client.getBakedModelManager(), new Identifier(TheGraveyard.MOD_ID, "block/" + woodType + "_head_lid"));
                }
                case 1 -> {
                    return BakedModelManagerHelper.getModel(client.getBakedModelManager(), new Identifier(TheGraveyard.MOD_ID, "block/" + woodType + "_foot_lid"));
                }
                case 2 -> {
                    return BakedModelManagerHelper.getModel(client.getBakedModelManager(), new Identifier(TheGraveyard.MOD_ID, "block/" + woodType + "_foot"));
                }
                case 3 -> {
                    return BakedModelManagerHelper.getModel(client.getBakedModelManager(), new Identifier(TheGraveyard.MOD_ID, "block/" + woodType + "_head"));
                }
            }
        } else {
            return BakedModelManagerHelper.getModel(client.getBakedModelManager(), new Identifier(TheGraveyard.MOD_ID, "block/oak_coffin_head"));
        }

    }


}

