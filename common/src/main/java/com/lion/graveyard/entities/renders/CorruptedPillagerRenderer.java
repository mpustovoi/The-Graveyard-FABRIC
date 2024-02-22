package com.lion.graveyard.entities.renders;

import com.lion.graveyard.Graveyard;
import com.lion.graveyard.entities.CorruptedPillager;
import com.lion.graveyard.entities.models.CorruptedIllagerModel;
import com.lion.graveyard.init.TGEntityModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CorruptedPillagerRenderer extends CorruptedIllagerRenderer<CorruptedPillager> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Graveyard.MOD_ID,"textures/entity/corrupted_pillager.png");

    public CorruptedPillagerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CorruptedIllagerModel<>(ctx.bakeLayer(TGEntityModelLayers.CORRUPTED_ILLAGER_MODEL_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CorruptedPillager entity) {
        return TEXTURE;
    }


}
