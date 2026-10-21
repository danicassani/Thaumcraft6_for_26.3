package thaumcraft.taint;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.*;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import net.minecraftforge.fluids.*;
import thaumcraft.*;
import thaumcraft.content.*;
public final class TaintContent{
 private static final DeferredRegister<Fluid> FLUIDS=DeferredRegister.create(Registries.FLUID,Thaumcraft.MODID);
 private static final DeferredRegister<FluidType> TYPES=DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES,Thaumcraft.MODID);
 public static final RegistryObject<MobEffect> DISEASE=effect("flux_taint",TaintEffect.Kind.DISEASE),EXHAUST=effect("vis_exhaust",TaintEffect.Kind.EXHAUST),INFECTIOUS=effect("infectious_vis_exhaust",TaintEffect.Kind.INFECTIOUS),WARD=effect("warp_ward",TaintEffect.Kind.WARD);
 public static final Map<String,RegistryObject<TaintBlock>> BLOCKS=new LinkedHashMap<>();
 static{for(var kind:TaintBlock.Kind.values()){String id=kind.id;var b=Registrar.BLOCKS.register(id,()->TaintBlock.create(kind,props(id).randomTicks().strength(kind==TaintBlock.Kind.FEATURE?.1F:kind==TaintBlock.Kind.FIBRE?1:kind==TaintBlock.Kind.LOG?3:10,100).noOcclusion().lightLevel(s->kind==TaintBlock.Kind.FEATURE?9:kind==TaintBlock.Kind.FIBRE&&s.getValue(TaintBlock.GROWTH)==3?12:0)));BLOCKS.put(id,b);itemBlock(id,b);}}
 public static final RegistryObject<Block> POROUS=Registrar.BLOCKS.register("stone_porous",()->new Block(props("stone_porous").strength(1,5).sound(SoundType.STONE)));
 static{itemBlock("stone_porous",POROUS);}
 public static final RegistryObject<GooBlock> GOO=Registrar.BLOCKS.register("flux_goo",()->new GooBlock(props("flux_goo").noCollision().strength(100).replaceable().noOcclusion()));
 public static final RegistryObject<FluidType> GOO_TYPE=TYPES.register("flux_goo",()->new FluidType(FluidType.Properties.create().viscosity(6000).density(8).canConvertToSource(false)){public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> c){thaumcraft.client.TaintFluids.goo(c);}});
 public static final RegistryObject<GooFluid> GOO_FLUID=FLUIDS.register("flux_goo",()->new GooFluid(new ForgeFlowingFluid.Properties(GOO_TYPE,()->TaintContent.GOO_FLUID.get(),()->TaintContent.GOO_FLUID.get()).tickRate(10)));
 public static final FluidSet DEATH=new FluidSet("liquid_death",0xf0440099,"thaumcraft:blocks/animatedglow",2,1500),PURE=new FluidSet("purifying_fluid",0x77FFCCAA,"minecraft:block/water_still",1,1000);
 public static final RegistryObject<Item> BOTTLE=Registrar.ITEMS.register("bottle_taint",()->new Item(item("bottle_taint").stacksTo(8)){
  public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level l,net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand h){if(l instanceof ServerLevel server){var shot=new TaintBottle(TaintContent.BOTTLE_ENTITY.get(),l);shot.setPos(p.getX(),p.getEyeY()-.1,p.getZ());shot.setOwner(p);shot.setItem(p.getItemInHand(h));shot.shootFromRotation(p,p.getXRot(),p.getYRot(),-5,.66F,1);if(server.addFreshEntity(shot)){p.getCooldowns().addCooldown(p.getItemInHand(h),10);p.getItemInHand(h).consume(1,p);}}return net.minecraft.world.InteractionResult.SUCCESS;}});
 public static final RegistryObject<Item> SALTS=Registrar.ITEMS.register("bath_salts",()->new BathSalts(item("bath_salts")));
 public static final RegistryObject<EntityType<TaintBottle>> BOTTLE_ENTITY=Registrar.ENTITIES.register("bottle_taint",()->EntityType.Builder.<TaintBottle>of(TaintBottle::new,MobCategory.MISC).sized(.25F,.25F).clientTrackingRange(4).updateInterval(1).build(key("bottle_taint")));
 public static final Map<TaintedMob.Kind,RegistryObject<EntityType<TaintedMob>>> MOBS=new EnumMap<>(TaintedMob.Kind.class);
 static{for(var k:TaintedMob.Kind.values())MOBS.put(k,Registrar.ENTITIES.register(k.id,()->EntityType.Builder.<TaintedMob>of((type,l)->new TaintedMob(type,l,k),MobCategory.MONSTER).sized(k.width,k.height).clientTrackingRange(10).build(key(k.id))));}
 public static final RegistryObject<EntityType<ThaumicSlime>> SLIME=Registrar.ENTITIES.register("thaumic_slime",()->EntityType.Builder.<ThaumicSlime>of(ThaumicSlime::new,MobCategory.MONSTER).sized(.52F,.52F).clientTrackingRange(10).build(key("thaumic_slime")));
 static{BasicContent.display(BOTTLE);BasicContent.display(SALTS);}
 public static final class FluidSet{
  public final String id;public final int color;public final String texture;public final RegistryObject<FluidType> type;public final RegistryObject<ForgeFlowingFluid> source,flowing;public final RegistryObject<HazardLiquidBlock> block;public final RegistryObject<Item> bucket;
  FluidSet(String id,int color,String texture,int drop,int viscosity){this.id=id;this.color=color;this.texture=texture;type=TYPES.register(id,()->new FluidType(FluidType.Properties.create().density(1000).viscosity(viscosity).canConvertToSource(false).lightLevel(id.equals("purifying_fluid")?5:0)){
   public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> c){thaumcraft.client.TaintFluids.initialize(FluidSet.this,c);}});
   source=FLUIDS.register(id,()->new HazardFluid.Still(properties(drop)));flowing=FLUIDS.register("flowing_"+id,()->new HazardFluid.Flow(properties(drop)));block=Registrar.BLOCKS.register(id,()->new HazardLiquidBlock(this,props(id).noCollision().noLootTable().liquid().replaceable().strength(100).noOcclusion()));bucket=Registrar.ITEMS.register(id.equals("liquid_death")?"bucket_death":"bucket_pure",()->new BucketItem(source.get(),item(id.equals("liquid_death")?"bucket_death":"bucket_pure").stacksTo(1).craftRemainder(Items.BUCKET)));BasicContent.display(bucket);
  }
  private ForgeFlowingFluid.Properties properties(int drop){return new ForgeFlowingFluid.Properties(type,source,flowing).block(block).bucket(bucket).slopeFindDistance(drop==2?2:4).levelDecreasePerBlock(drop).tickRate(10).explosionResistance(100);}
 }
 private static RegistryObject<MobEffect> effect(String id,TaintEffect.Kind k){return Registrar.EFFECTS.register(id,()->new TaintEffect(k));}
 public static BlockBehaviour.Properties props(String id){return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(id))).sound(SoundType.SLIME_BLOCK);}
 public static Item.Properties item(String id){return new Item.Properties().setId(ResourceKey.create(Registries.ITEM,SimpleContent.id(id)));}
 private static void itemBlock(String id,RegistryObject<? extends Block>b){var i=Registrar.ITEMS.register(id,()->new BlockItem(b.get(),item(id).useBlockDescriptionPrefix()));BasicContent.display(i);}
 private static <T extends Entity> ResourceKey<EntityType<?>> key(String id){return ResourceKey.create(Registries.ENTITY_TYPE,SimpleContent.id(id));}
 public static DamageSource damage(ServerLevel l,String id){return new DamageSource(l.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,SimpleContent.id(id))));}
 public static boolean discoverBath(net.minecraft.server.level.ServerPlayer p){var warp=thaumcraft.persistence.PlayerWarpStore.read(p).result();return warp.isPresent()&&warp.get().permanent()+warp.get().normal()>10&&thaumcraft.research.ResearchService.flag(p,"!BATHSALTS");}
 public static void register(BusGroup b){TaintMilk.register();net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(e->{if(e.player() instanceof net.minecraft.server.level.ServerPlayer p&&p.tickCount%200==0&&!thaumcraft.config.ThaumcraftConfig.wussMode.get())discoverBath(p);});net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(e->{if(e.getEntity().level() instanceof ServerLevel l&&e.getSource().is(ResourceKey.create(Registries.DAMAGE_TYPE,SimpleContent.id("dissolve")))){var aspects=thaumcraft.research.AspectCatalog.entity(e.getEntity());if(!aspects.isEmpty()){int total=aspects.values().stream().mapToInt(Integer::intValue).sum();var keys=new ArrayList<>(aspects.keySet());for(int i=0,n=1+l.getRandom().nextInt(1+total/10);i<n;i++)e.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(l,e.getEntity().getX(),e.getEntity().getY(),e.getEntity().getZ(),thaumcraft.crystals.CrystalContent.essence(keys.get(l.getRandom().nextInt(keys.size())),1)));}}});FLUIDS.register(b);TYPES.register(b);net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener(e->{for(var k:TaintedMob.Kind.values())e.put(MOBS.get(k).get(),TaintedMob.attributes(k));e.put(SLIME.get(),net.minecraft.world.entity.monster.cubemob.Slime.createMobAttributes().add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).build());});}
}
