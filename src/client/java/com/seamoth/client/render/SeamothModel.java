package com.seamoth.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class SeamothModel extends EntityModel<EntityRenderState> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Identifier.fromNamespaceAndPath("seamoth", "seamoth"), "main");

	private final ModelPart root;
	private final ModelPart base;
	private final ModelPart glass;
	private final ModelPart borders;
	private final ModelPart seat;

	public SeamothModel(ModelPart root) {
		super(root);
		this.root = root.getChild("root");
		this.base = this.root.getChild("Base");
		this.glass = this.root.getChild("Glass");
		this.borders = this.root.getChild("Borders");
		this.seat = this.root.getChild("seat");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.0F, 0.0F, (float) Math.PI)
		);

		PartDefinition Base = root.addOrReplaceChild("Base", CubeListBuilder.create().texOffs(36, 64).addBox(-9.0F, -15.0F, 5.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(64, 64).addBox(3.0F, -15.0F, 5.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 13).addBox(-13.0F, -15.0F, -7.0F, 4.0F, 6.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(0, 34).addBox(9.0F, -15.0F, -7.0F, 4.0F, 6.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(0, 65).addBox(5.0F, -15.0F, -13.0F, 4.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(76, 0).addBox(-9.0F, -15.0F, -13.0F, 4.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Glass = root.addOrReplaceChild("Glass", CubeListBuilder.create().texOffs(38, 46).addBox(-17.0F, -8.0F, 9.0F, 18.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(56, 6).addBox(-11.0F, 0.0F, 10.0F, 6.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(56, 6).addBox(-11.0F, 4.995F, 9.0F, 6.0F, 0.01F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -15.0F, -4.0F));

		PartDefinition cube_r1 = Glass.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(70, 8).addBox(-9.0F, -2.0F, 0.995F, 2.0F, 2.0F, 0.01F, new CubeDeformation(0.0F))
				.texOffs(72, 62).addBox(-9.0F, 0.0F, 1.0F, 4.0F, 2.0F, 0.1F, new CubeDeformation(0.0F))
				.texOffs(24, 65).addBox(-9.0F, 2.0F, 1.0F, 6.0F, 2.0F, 0.1F, new CubeDeformation(0.0F))
				.texOffs(56, 2).addBox(-9.0F, 4.0F, 1.0F, 10.0F, 2.0F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.0F, -6.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r2 = Glass.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(70, 6).addBox(-9.0F, -2.0F, 0.995F, 2.0F, 2.0F, 0.01F, new CubeDeformation(0.0F))
				.texOffs(72, 60).addBox(-9.0F, 0.0F, 1.0F, 4.0F, 2.0F, 0.1F, new CubeDeformation(0.0F))
				.texOffs(24, 67).addBox(-9.0F, 2.0F, 1.0F, 6.0F, 2.0F, 0.1F, new CubeDeformation(0.0F))
				.texOffs(56, 0).addBox(-9.0F, 4.0F, 1.0F, 10.0F, 2.0F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition Borders = root.addOrReplaceChild("Borders", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -2.0F, -10.0F, 18.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(38, 33).addBox(-7.0F, -13.0F, -16.0F, 14.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(36, 55).addBox(-9.0F, -15.0F, -10.0F, 18.0F, 9.0F, 0.1F, new CubeDeformation(0.0F))
				.texOffs(76, 14).addBox(-3.5F, -9.0F, -23.0F, 8.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(38, 13).addBox(-5.0F, -11.0F, -19.0F, 10.0F, 11.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(1, 1).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 1).addBox(-2.0F, -5.0F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 1).addBox(-2.0F, -7.0F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 5.0F));

		PartDefinition cube_r3 = Borders.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 1).addBox(-2.0F, -1.0F, 0.1F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -7.0F, -1.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r4 = Borders.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 1).addBox(-2.0F, -1.0F, 0.1F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -7.0F, -1.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r5 = Borders.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(24, 73).addBox(-9.0F, -2.0F, 0.995F, 4.0F, 2.0F, 0.01F, new CubeDeformation(0.0F))
				.texOffs(24, 71).addBox(-11.0F, -4.0F, 1.0F, 6.0F, 2.0F, 0.1F, new CubeDeformation(0.0F))
				.texOffs(72, 58).addBox(-13.0F, -6.0F, 1.0F, 8.0F, 2.0F, 0.1F, new CubeDeformation(0.0F))
				.texOffs(56, 4).addBox(-15.0F, -7.0F, 1.0F, 10.0F, 1.0F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -8.0F, -15.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r6 = Borders.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(24, 75).addBox(-9.0F, -2.0F, 0.995F, 4.0F, 2.0F, 0.01F, new CubeDeformation(0.0F))
				.texOffs(24, 69).addBox(-11.0F, -4.0F, 1.0F, 6.0F, 2.0F, 0.1F, new CubeDeformation(0.0F))
				.texOffs(72, 56).addBox(-13.0F, -6.0F, 1.0F, 8.0F, 2.0F, 0.1F, new CubeDeformation(0.0F))
				.texOffs(56, 5).addBox(-15.0F, -7.0F, 1.0F, 10.0F, 1.0F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -8.0F, -15.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r7 = Borders.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(72, 55).addBox(-15.0F, -1.0F, 1.0F, 18.0F, 1.0F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -14.0F, 1.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r8 = Borders.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 55).addBox(-9.0F, -9.0F, 1.0F, 18.0F, 10.0F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, -9.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition seat = root.addOrReplaceChild("seat", CubeListBuilder.create().texOffs(-5, -4).addBox(-2.0F, -13.0F, -3.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r9 = seat.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(1, 1).addBox(-1.0F, -8.0F, -1.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -12.0F, -2.5F, 0.1745F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(EntityRenderState state) {
		super.setupAnim(state);
	}

	// Включаем или выключаем видимость стекла перед рендером
	public void renderBodyOnly() {
		this.glass.visible = false;
	}

	public void renderGlassOnly() {
		this.glass.visible = true;
	}
}
