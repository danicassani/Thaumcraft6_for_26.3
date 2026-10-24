package thaumcraft.eldritch;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.RegistryObject;
import thaumcraft.Registrar;
import thaumcraft.content.SimpleContent;

/** The remaining registered TC6 creatures. Outer Lands has no generator in the source release. */
public final class EldritchContent {
 public static final RegistryObject<net.minecraft.world.level.block.Block> SAP=Registrar.BLOCKS.register("effect_sap",()->new SapField(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id("effect_sap"))).noCollision().noOcclusion().replaceable().strength(-1,999).lightLevel(s->7).noLootTable()));
 public static final Map<EldritchMob.Kind,RegistryObject<EntityType<BrainyZombie>>> ZOMBIES=new EnumMap<>(EldritchMob.Kind.class);
 public static final Map<EldritchMob.Kind,RegistryObject<EntityType<EldritchMob>>> TYPES=new EnumMap<>(EldritchMob.Kind.class);
 public static final RegistryObject<EntityType<FireBat>> FIREBAT=Registrar.ENTITIES.register("firebat",()->EntityType.Builder.<FireBat>of(FireBat::new,MobCategory.MONSTER).sized(.5F,.9F).clientTrackingRange(8).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id("firebat"))));
 public static final List<RegistryObject<Item>> EGGS=new ArrayList<>();
 static {EGGS.add(Registrar.ITEMS.register("firebat_spawn_egg",()->new SpawnEggItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id("firebat_spawn_egg"))).spawnEgg(FIREBAT.get()))));for(var k:EldritchMob.Kind.values()){
  if(k.ordinal()<3){var zombie=Registrar.ENTITIES.register(k.id,()->EntityType.Builder.<BrainyZombie>of((t,l)->new BrainyZombie(t,l,k),MobCategory.MONSTER).sized(k.width,k.height).clientTrackingRange(10).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id(k.id))));ZOMBIES.put(k,zombie);EGGS.add(Registrar.ITEMS.register(k.id+"_spawn_egg",()->new SpawnEggItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(k.id+"_spawn_egg"))).spawnEgg(zombie.get()))));continue;}
  var type=Registrar.ENTITIES.register(k.id,()->EntityType.Builder.<EldritchMob>of((t,l)->new EldritchMob(t,l,k),MobCategory.MONSTER).sized(k.width,k.height).clientTrackingRange(10).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id(k.id))));TYPES.put(k,type);
  EGGS.add(Registrar.ITEMS.register(k.id+"_spawn_egg",()->new SpawnEggItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(k.id+"_spawn_egg"))).spawnEgg(type.get()))));
 }
 }
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){SeasonalSpawns.register(bus);EldritchSounds.register(bus);EldritchSpawning.register();net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener(e->{e.put(FIREBAT.get(),FireBat.attributes());ZOMBIES.forEach((k,t)->e.put(t.get(),BrainyZombie.attributes(k)));TYPES.forEach((k,t)->e.put(t.get(),EldritchMob.attributes(k)));});}
 private EldritchContent(){}
}
