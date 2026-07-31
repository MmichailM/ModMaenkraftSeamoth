package com.seamoth.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.seamoth.Seamoth;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import com.seamoth.entity.SeamothEntity;
import com.mojang.math.Axis;

public class SeamothEntityRenderer extends EntityRenderer<SeamothEntity, SeamothRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("seamoth", "textures/entity/seamoth.png");
    private final SeamothModel model;

    public SeamothEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SeamothModel(context.bakeLayer(SeamothModel.LAYER_LOCATION));
    }

    @Override
    public void extractRenderState(SeamothEntity entity, SeamothRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.packedLight = this.getPackedLightCoords(entity, partialTicks);

        state.xRot = entity.getViewXRot(partialTicks);
        state.yRot = entity.getViewYRot(partialTicks);
    }

    @Override
    public SeamothRenderState createRenderState() {
        return new SeamothRenderState();
    }

    @Override
    public void submit(SeamothRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.scale(1.5F, 1.5F, 1.5F);

        super.submit(state, poseStack, submitNodeCollector, camera);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));

        // ПАСС 1: Рендерим ТОЛЬКО корпус (непрозрачным)
        // Отключаем видимость стекла, чтобы оно не отрисовалось через Cutout
        this.model.renderBodyOnly();

        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(TEXTURE), // Никакой ряби на корпусе!
                state.packedLight,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                0,
                null
        );

        // ПАСС 2: Рендерим ТОЛЬКО стекло (полупрозрачным)
        // Включаем стекло обратно
        this.model.renderGlassOnly();

        submitNodeCollector.submitModel(
                this.model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(TEXTURE), // Красивое прозрачное стекло!
                state.packedLight,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                0,
                null
        );

        poseStack.popPose();
    }
}
