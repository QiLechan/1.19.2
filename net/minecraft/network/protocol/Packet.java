package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public interface Packet<T extends PacketListener> {
   void write(FriendlyByteBuf var1);

   void handle(T var1);

   default boolean isSkippable() {
      return false;
   }
}
