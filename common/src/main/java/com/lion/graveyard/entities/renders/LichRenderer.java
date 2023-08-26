package main.java.com.lion.graveyard.entities.renders;

import com.finallion.graveyard.TheGraveyard;
import main.java.com.lion.graveyard.entities.LichEntity;
import main.java.com.lion.graveyard.entities.models.LichModel;
import main.java.com.lion.graveyard.entities.renders.features.LichEyesFeatureRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LichRenderer extends GeoEntityRenderer<LichEntity> {

    public LichRenderer(EntityRendererFactory.Context context) {
        super(context, new LichModel());
        this.shadowRadius = 1.0F;
        this.addRenderLayer(new LichEyesFeatureRenderer(this));
    }

    @Override
    public RenderLayer getRenderType(LichEntity animatable, Identifier texture, @Nullable VertexConsumerProvider bufferSource, float partialTick) {
        return RenderLayer.getEntityCutoutNoCull(texture);
    }

    // stops the vanilla death animation
    @Override
    protected float getDeathMaxRotation(LichEntity entityLivingBaseIn) {
        return 0.0F;
    }

}