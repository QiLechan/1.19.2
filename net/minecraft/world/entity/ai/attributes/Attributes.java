package net.minecraft.world.entity.ai.attributes;

import net.minecraft.core.Registry;

public class Attributes {
   public static final Attribute MAX_HEALTH = register("generic.max_health", (new RangedAttribute("attribute.name.generic.max_health", 20.0D, 1.0D, 1024.0D)).setSyncable(true));
   public static final Attribute FOLLOW_RANGE = register("generic.follow_range", new RangedAttribute("attribute.name.generic.follow_range", 32.0D, 0.0D, 2048.0D));
   public static final Attribute KNOCKBACK_RESISTANCE = register("generic.knockback_resistance", new RangedAttribute("attribute.name.generic.knockback_resistance", 0.0D, 0.0D, 1.0D));
   public static final Attribute MOVEMENT_SPEED = register("generic.movement_speed", (new RangedAttribute("attribute.name.generic.movement_speed", 0.699999988079071D, 0.0D, 1024.0D)).setSyncable(true));
   public static final Attribute FLYING_SPEED = register("generic.flying_speed", (new RangedAttribute("attribute.name.generic.flying_speed", 0.4000000059604645D, 0.0D, 1024.0D)).setSyncable(true));
   public static final Attribute ATTACK_DAMAGE = register("generic.attack_damage", new RangedAttribute("attribute.name.generic.attack_damage", 2.0D, 0.0D, 2048.0D));
   public static final Attribute ATTACK_KNOCKBACK = register("generic.attack_knockback", new RangedAttribute("attribute.name.generic.attack_knockback", 0.0D, 0.0D, 5.0D));
   public static final Attribute ATTACK_SPEED = register("generic.attack_speed", (new RangedAttribute("attribute.name.generic.attack_speed", 4.0D, 0.0D, 1024.0D)).setSyncable(true));
   public static final Attribute ARMOR = register("generic.armor", (new RangedAttribute("attribute.name.generic.armor", 0.0D, 0.0D, 30.0D)).setSyncable(true));
   public static final Attribute ARMOR_TOUGHNESS = register("generic.armor_toughness", (new RangedAttribute("attribute.name.generic.armor_toughness", 0.0D, 0.0D, 20.0D)).setSyncable(true));
   public static final Attribute LUCK = register("generic.luck", (new RangedAttribute("attribute.name.generic.luck", 0.0D, -1024.0D, 1024.0D)).setSyncable(true));
   public static final Attribute SPAWN_REINFORCEMENTS_CHANCE = register("zombie.spawn_reinforcements", new RangedAttribute("attribute.name.zombie.spawn_reinforcements", 0.0D, 0.0D, 1.0D));
   public static final Attribute JUMP_STRENGTH = register("horse.jump_strength", (new RangedAttribute("attribute.name.horse.jump_strength", 0.7D, 0.0D, 2.0D)).setSyncable(true));

   public Attributes() {
      super();
   }

   private static Attribute register(String var0, Attribute var1) {
      return (Attribute)Registry.register(Registry.ATTRIBUTE, (String)var0, var1);
   }
}
