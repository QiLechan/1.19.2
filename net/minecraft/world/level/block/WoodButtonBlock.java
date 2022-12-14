package net.minecraft.world.level.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoodButtonBlock extends ButtonBlock {
   protected WoodButtonBlock(BlockBehaviour.Properties var1) {
      super(true, var1);
   }

   protected SoundEvent getSound(boolean var1) {
      return var1 ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
   }
}
