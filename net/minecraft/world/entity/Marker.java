package net.minecraft.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;

public class Marker extends Entity {
   private static final String DATA_TAG = "data";
   private CompoundTag data = new CompoundTag();

   public Marker(EntityType<?> var1, Level var2) {
      super(var1, var2);
      this.noPhysics = true;
   }

   public void tick() {
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag var1) {
      this.data = var1.getCompound("data");
   }

   protected void addAdditionalSaveData(CompoundTag var1) {
      var1.put("data", this.data.copy());
   }

   public Packet<?> getAddEntityPacket() {
      throw new IllegalStateException("Markers should never be sent");
   }

   protected void addPassenger(Entity var1) {
      var1.stopRiding();
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }
}
