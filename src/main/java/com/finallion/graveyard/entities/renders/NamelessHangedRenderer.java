package com.finallion.graveyard.entities.renders;

import com.finallion.graveyard.entities.GhoulEntity;
import com.finallion.graveyard.entities.NamelessHangedEntity;
import com.finallion.graveyard.entities.WraithEntity;
import com.finallion.graveyard.entities.models.BaseGhoulModel;
import com.finallion.graveyard.entities.models.NamelessHangedModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NamelessHangedRenderer extends GeoEntityRenderer<NamelessHangedEntity> {

    public NamelessHangedRenderer(EntityRendererFactory.Context context) {
        super(context, new NamelessHangedModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public RenderLayer getRenderType(NamelessHangedEntity animatable, Identifier texture, @Nullable VertexConsumerProvider bufferSource, float partialTick) {
        return RenderLayer.getEntityTranslucent(texture);
    }

}
