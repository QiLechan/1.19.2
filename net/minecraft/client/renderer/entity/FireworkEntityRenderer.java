package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

public class FireworkEntityRenderer extends EntityRenderer<FireworkRocketEntity> {
   private final ItemRenderer itemRenderer;

   public FireworkEntityRenderer(EntityRendererProvider.Context var1) {
      super(var1);
      this.itemRenderer = var1.getItemRenderer();
   }

   public void render(FireworkRocketEntity var1, float var2, float var3, PoseStack var4, MultiBufferSource var5, int var6) {
      var4.pushPose();
      var4.mulPose(this.entityRenderDispatcher.cameraOrientation());
      var4.mulPose(Vector3f.YP.rotationDegrees(180.0F));
      if (var1.isShotAtAngle()) {
         var4.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
         var4.mulPose(Vector3f.YP.rotationDegrees(180.0F));
         var4.mulPose(Vector3f.XP.rotationDegrees(90.0F));
      }

      this.itemRenderer.renderStatic(var1.getItem(), ItemTransforms.TransformType.GROUND, var6, OverlayTexture.NO_OVERLAY, var4, var5, var1.getId());
      var4.popPose();
      super.render(var1, var2, var3, var4, var5, var6);
   }

   public ResourceLocation getTextureLocation(FireworkRocketEntity var1) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
