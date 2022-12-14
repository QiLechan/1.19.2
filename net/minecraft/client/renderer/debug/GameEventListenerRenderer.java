package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class GameEventListenerRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private static final int LISTENER_RENDER_DIST = 32;
   private static final float BOX_HEIGHT = 1.0F;
   private final List<GameEventListenerRenderer.TrackedGameEvent> trackedGameEvents = Lists.newArrayList();
   private final List<GameEventListenerRenderer.TrackedListener> trackedListeners = Lists.newArrayList();

   public GameEventListenerRenderer(Minecraft var1) {
      super();
      this.minecraft = var1;
   }

   public void render(PoseStack var1, MultiBufferSource var2, double var3, double var5, double var7) {
      ClientLevel var9 = this.minecraft.level;
      if (var9 == null) {
         this.trackedGameEvents.clear();
         this.trackedListeners.clear();
      } else {
         Vec3 var10 = new Vec3(var3, 0.0D, var7);
         this.trackedGameEvents.removeIf(GameEventListenerRenderer.TrackedGameEvent::isExpired);
         this.trackedListeners.removeIf((var2x) -> {
            return var2x.isExpired(var9, var10);
         });
         RenderSystem.disableTexture();
         RenderSystem.enableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         VertexConsumer var11 = var2.getBuffer(RenderType.lines());
         Iterator var12 = this.trackedListeners.iterator();

         while(var12.hasNext()) {
            GameEventListenerRenderer.TrackedListener var13 = (GameEventListenerRenderer.TrackedListener)var12.next();
            var13.getPosition(var9).ifPresent((var9x) -> {
               double var10 = var9x.x() - (double)var13.getListenerRadius();
               double var12 = var9x.y() - (double)var13.getListenerRadius();
               double var14 = var9x.z() - (double)var13.getListenerRadius();
               double var16 = var9x.x() + (double)var13.getListenerRadius();
               double var18 = var9x.y() + (double)var13.getListenerRadius();
               double var20 = var9x.z() + (double)var13.getListenerRadius();
               Vector3f var22 = new Vector3f(1.0F, 1.0F, 0.0F);
               LevelRenderer.renderVoxelShape(var1, var11, Shapes.create(new AABB(var10, var12, var14, var16, var18, var20)), -var3, -var5, -var7, var22.x(), var22.y(), var22.z(), 0.35F);
            });
         }

         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         Tesselator var31 = Tesselator.getInstance();
         BufferBuilder var32 = var31.getBuilder();
         var32.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
         Iterator var14 = this.trackedListeners.iterator();

         GameEventListenerRenderer.TrackedListener var15;
         while(var14.hasNext()) {
            var15 = (GameEventListenerRenderer.TrackedListener)var14.next();
            var15.getPosition(var9).ifPresent((var7x) -> {
               Vector3f var8 = new Vector3f(1.0F, 1.0F, 0.0F);
               LevelRenderer.addChainedFilledBoxVertices(var32, var7x.x() - 0.25D - var3, var7x.y() - var5, var7x.z() - 0.25D - var7, var7x.x() + 0.25D - var3, var7x.y() - var5 + 1.0D, var7x.z() + 0.25D - var7, var8.x(), var8.y(), var8.z(), 0.35F);
            });
         }

         var31.end();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.lineWidth(2.0F);
         RenderSystem.depthMask(false);
         var14 = this.trackedListeners.iterator();

         while(var14.hasNext()) {
            var15 = (GameEventListenerRenderer.TrackedListener)var14.next();
            var15.getPosition(var9).ifPresent((var0) -> {
               DebugRenderer.renderFloatingText("Listener Origin", var0.x(), var0.y() + 1.7999999523162842D, var0.z(), -1, 0.025F);
               DebugRenderer.renderFloatingText((new BlockPos(var0)).toString(), var0.x(), var0.y() + 1.5D, var0.z(), -6959665, 0.025F);
            });
         }

         var14 = this.trackedGameEvents.iterator();

         while(var14.hasNext()) {
            GameEventListenerRenderer.TrackedGameEvent var33 = (GameEventListenerRenderer.TrackedGameEvent)var14.next();
            Vec3 var16 = var33.position;
            double var17 = 0.20000000298023224D;
            double var19 = var16.x - 0.20000000298023224D;
            double var21 = var16.y - 0.20000000298023224D;
            double var23 = var16.z - 0.20000000298023224D;
            double var25 = var16.x + 0.20000000298023224D;
            double var27 = var16.y + 0.20000000298023224D + 0.5D;
            double var29 = var16.z + 0.20000000298023224D;
            renderTransparentFilledBox(new AABB(var19, var21, var23, var25, var27, var29), 1.0F, 1.0F, 1.0F, 0.2F);
            DebugRenderer.renderFloatingText(var33.gameEvent.getName(), var16.x, var16.y + 0.8500000238418579D, var16.z, -7564911, 0.0075F);
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableTexture();
         RenderSystem.disableBlend();
      }
   }

   private static void renderTransparentFilledBox(AABB var0, float var1, float var2, float var3, float var4) {
      Camera var5 = Minecraft.getInstance().gameRenderer.getMainCamera();
      if (var5.isInitialized()) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         Vec3 var6 = var5.getPosition().reverse();
         DebugRenderer.renderFilledBox(var0.move(var6), var1, var2, var3, var4);
      }
   }

   public void trackGameEvent(GameEvent var1, Vec3 var2) {
      this.trackedGameEvents.add(new GameEventListenerRenderer.TrackedGameEvent(Util.getMillis(), var1, var2));
   }

   public void trackListener(PositionSource var1, int var2) {
      this.trackedListeners.add(new GameEventListenerRenderer.TrackedListener(var1, var2));
   }

   private static class TrackedListener implements GameEventListener {
      public final PositionSource listenerSource;
      public final int listenerRange;

      public TrackedListener(PositionSource var1, int var2) {
         super();
         this.listenerSource = var1;
         this.listenerRange = var2;
      }

      public boolean isExpired(Level var1, Vec3 var2) {
         return this.listenerSource.getPosition(var1).filter((var1x) -> {
            return var1x.distanceToSqr(var2) <= 1024.0D;
         }).isPresent();
      }

      public Optional<Vec3> getPosition(Level var1) {
         return this.listenerSource.getPosition(var1);
      }

      public PositionSource getListenerSource() {
         return this.listenerSource;
      }

      public int getListenerRadius() {
         return this.listenerRange;
      }

      public boolean handleGameEvent(ServerLevel var1, GameEvent.Message var2) {
         return false;
      }
   }

   private static record TrackedGameEvent(long a, GameEvent b, Vec3 c) {
      private final long timeStamp;
      final GameEvent gameEvent;
      final Vec3 position;

      TrackedGameEvent(long var1, GameEvent var3, Vec3 var4) {
         super();
         this.timeStamp = var1;
         this.gameEvent = var3;
         this.position = var4;
      }

      public boolean isExpired() {
         return Util.getMillis() - this.timeStamp > 3000L;
      }

      public long timeStamp() {
         return this.timeStamp;
      }

      public GameEvent gameEvent() {
         return this.gameEvent;
      }

      public Vec3 position() {
         return this.position;
      }
   }
}
