package thaumcraft.bore;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;
import thaumcraft.aura.AuraManager;
import thaumcraft.infusion.InfusionEnchantments;

/** Loaded-world drilling. A cancelled break never removes a block or pays vis. */
public final class ArcaneBore extends PathfinderMob {
 private static final EntityDataAccessor<Integer> FACE=SynchedEntityData.defineId(ArcaneBore.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<BlockPos> TARGET=SynchedEntityData.defineId(ArcaneBore.class,EntityDataSerializers.BLOCK_POS);
 private static final EntityDataAccessor<Boolean> DIGGING=SynchedEntityData.defineId(ArcaneBore.class,EntityDataSerializers.BOOLEAN);
 public final SimpleContainer tool=new SimpleContainer(1);
 private UUID owner;private boolean activated;private float charge;private int cursor,wear,delay;private BlockPos target;private BlockState targetState;private ItemStack targetTool=ItemStack.EMPTY;
 public ArcaneBore(EntityType<? extends ArcaneBore> t,Level l){super(t,l);setNoAi(true);setPersistenceRequired();}
 protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(FACE,Direction.NORTH.ordinal());b.define(TARGET,BlockPos.ZERO);b.define(DIGGING,false);}
 protected void registerGoals(){}
 public void configure(UUID owner,Direction d){this.owner=owner;face(d);}
 public void face(Direction d){entityData.set(FACE,(d==Direction.DOWN?Direction.UP:d).ordinal());clearTarget();cursor=0;}
 public Direction facing(){return Direction.values()[Math.clamp(entityData.get(FACE),0,5)];}
 public boolean digging(){return entityData.get(DIGGING);}public BlockPos target(){return entityData.get(TARGET);}public float charge(){return charge;}
 public boolean owned(Player p){return owner!=null&&owner.equals(p.getUUID());}
 public static boolean pickaxe(ItemStack s){return !s.isEmpty()&&s.is(ItemTags.PICKAXES);}
 public boolean validTool(){var s=tool.getItem(0);return pickaxe(s)&&(!s.isDamageableItem()||s.getDamageValue()+1<s.getMaxDamage());}
 public int radius(){var s=tool.getItem(0);var enchantable=s.get(DataComponents.ENCHANTABLE);return Math.clamp(Math.max(2,enchantable==null?2:enchantable.value()/3)+InfusionEnchantments.level(s,InfusionEnchantments.Kind.DESTRUCTIVE)*2,2,16);}
 public int depth(){return radius()*8+InfusionEnchantments.level(tool.getItem(0),InfusionEnchantments.Kind.BURROWING)*16;}
 private void clearTarget(){target=null;targetState=null;targetTool=ItemStack.EMPTY;entityData.set(DIGGING,false);}
 protected InteractionResult mobInteract(Player p,InteractionHand hand){if(level().isClientSide())return InteractionResult.SUCCESS;if(!owned(p))return InteractionResult.PASS;
  if(p.isShiftKeyDown()){var stack=tool.removeItemNoUpdate(0);if(!stack.isEmpty())spawnAtLocation((ServerLevel)level(),stack);spawnAtLocation((ServerLevel)level(),new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("arcane_bore").get()));discard();}
  else p.openMenu(new SimpleMenuProvider((id,inv,who)->new BoreMenu(id,inv,tool,this),getDisplayName()));return InteractionResult.SUCCESS;
 }
 public boolean hurtServer(ServerLevel l,net.minecraft.world.damagesource.DamageSource source,float amount){if(source.getEntity() instanceof Player p&&owned(p)){face(p.getY()>getY()+2?Direction.UP:p.getDirection().getOpposite());return false;}return super.hurtServer(l,source,amount);}
 protected void dropCustomDeathLoot(ServerLevel l,net.minecraft.world.damagesource.DamageSource source,boolean hit){super.dropCustomDeathLoot(l,source,hit);var s=tool.removeItemNoUpdate(0);if(!s.isEmpty())spawnAtLocation(l,s);}
 public void tick(){super.tick();if(level().isClientSide()){if(digging()&&tickCount%2==0){var from=position().add(facing().getStepX()*.5,.7+facing().getStepY()*.5,facing().getStepZ()*.5);var delta=Vec3.atCenterOf(target()).subtract(from);for(int n=1;n<=16;n++){var v=from.add(delta.scale(n/16.0));level().addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,v.x,v.y,v.z,0,0,0);}}return;}
  var l=(ServerLevel)level();if(tickCount%50==0)heal(1);var rail=l.getBlockState(isPassenger()?getVehicle().blockPosition():blockPosition());if(!rail.is(thaumcraft.content.SimpleContent.BLOCKS.get("activator_rail").get()))rail=l.getBlockState((isPassenger()?getVehicle().blockPosition():blockPosition()).below());if(rail.is(thaumcraft.content.SimpleContent.BLOCKS.get("activator_rail").get()))activated=!rail.getValue(PoweredRailBlock.POWERED);else if(!isPassenger())activated=l.hasNeighborSignal(blockPosition().below());ServerPlayer p=owner==null?null:(ServerPlayer)l.getPlayerByUUID(owner);if(p==null||p.level()!=l||!p.mayBuild()||!validTool()||!activated){clearTarget();return;}
  if(tickCount%10==0&&charge<10)charge+=AuraManager.drainVis(l,blockPosition(),10-charge,false);
  if(target!=null){if(!l.hasChunkAt(target)||!l.getBlockState(target).equals(targetState)||!ItemStack.isSameItemSameComponents(targetTool,tool.getItem(0))||target.distSqr(blockPosition())>(depth()+radius()+2)*(depth()+radius()+2)){clearTarget();return;}if(charge<.25F)return;if(--delay>0)return;dig(l,p);clearTarget();return;}
  if(charge>=.25F&&tickCount%2==0)find(l,p);
 }
 private void find(ServerLevel l,ServerPlayer p){int r=radius(),side=r*2+1,total=side*side;var d=facing();var origin=blockPosition().relative(d);for(int samples=0;samples<4;samples++){int index=Math.floorMod(cursor++,total),a=index%side-r,b=index/side-r;if(a*a+b*b>r*r)continue;BlockPos start=d.getAxis()==Direction.Axis.X?origin.offset(0,a,b):d.getAxis()==Direction.Axis.Z?origin.offset(a,b,0):origin.offset(a,0,b);
   for(int distance=0;distance<depth();distance++){var q=start.relative(d,distance);if(!l.isInWorldBounds(q)||!l.hasChunkAt(q))break;var s=l.getBlockState(q);if(s.isAir()||s.getCollisionShape(l,q).isEmpty())continue;
    if(q.equals(blockPosition())||q.equals(blockPosition().below())||s.getDestroySpeed(l,q)<0||!s.getFluidState().isEmpty()||!l.mayInteract(p,q)||!p.mayUseItemAt(q,d.getOpposite(),tool.getItem(0)))break;
    var from=position().add(d.getStepX(),.7+d.getStepY(),d.getStepZ());var to=Vec3.atCenterOf(q);if(!thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,from,to))break;var hit=l.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));if(hit.getType()!=HitResult.Type.BLOCK)break;q=hit.getBlockPos();s=l.getBlockState(q);if(q.equals(blockPosition())||q.equals(blockPosition().below())||s.getDestroySpeed(l,q)<0||!s.getFluidState().isEmpty())break;
    target=q.immutable();targetState=s;targetTool=tool.getItem(0).copy();int speed=(int)(targetTool.getDestroySpeed(s)/2)+net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(l.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY),targetTool);delay=Math.max(1,Math.max(10-speed,(int)(s.getDestroySpeed(l,q)*2)-speed*2));entityData.set(TARGET,target);entityData.set(DIGGING,true);return;
   }
  }
 }
 private boolean allowedBreak(ServerLevel l,ServerPlayer owner){var actor=new BoreActor(l,owner.getGameProfile());var held=actor.getMainHandItem();boolean sneaking=actor.isShiftKeyDown();actor.setPos(position());actor.setShiftKeyDown(true);actor.setItemInHand(InteractionHand.MAIN_HAND,tool.getItem(0).copy());try{return net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(l,GameType.SURVIVAL,actor,target)>=0;}finally{actor.setItemInHand(InteractionHand.MAIN_HAND,held);actor.setShiftKeyDown(sneaking);actor.getTextFilter().leave();}}
 private void dig(ServerLevel l,ServerPlayer p){if(!l.getBlockState(target).equals(targetState))return;if(!l.mayInteract(p,target)||!p.mayUseItemAt(target,facing().getOpposite(),tool.getItem(0))||!allowedBreak(l,p)||!l.getBlockState(target).equals(targetState))return;
  var actual=tool.getItem(0);var lootTool=actual.copy();var registry=l.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);int sounding=InfusionEnchantments.level(actual,InfusionEnchantments.Kind.SOUNDING);var fortune=registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE);if(sounding>net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(fortune,lootTool))lootTool.enchant(fortune,sounding);
  var box=new AABB(target).inflate(1);var existing=new HashSet<Integer>();for(var item:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box))existing.add(item.getId());var drops=(!targetState.requiresCorrectToolForDrops()||actual.isCorrectToolForDrops(targetState))?Block.getDrops(targetState,l,target,l.getBlockEntity(target),p,lootTool):List.<ItemStack>of();if(!l.removeBlock(target,false))return;var routed=new ArrayList<ItemStack>(drops);for(var item:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box))if(!existing.contains(item.getId())){routed.add(item.getItem().copy());item.discard();}charge-=.25F;l.levelEvent(2001,target,Block.getId(targetState));for(var drop:routed){int rank=InfusionEnchantments.level(actual,InfusionEnchantments.Kind.REFINING);boolean silk=net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH),actual)>0;String metal=drop.is(Items.RAW_IRON)?"iron":drop.is(Items.RAW_GOLD)?"gold":drop.is(Items.RAW_COPPER)?"copper":"";if(!silk&&rank>0&&!metal.isEmpty()&&random.nextFloat()<(rank+1)*.125F)drop=new ItemStack(thaumcraft.content.SimpleContent.ITEMS.get("cluster_"+metal).get(),drop.getCount());eject(l,drop);}
  if(++wear>=50){wear=0;actual.hurtAndBreak(1,this,EquipmentSlot.MAINHAND);}tool.setChanged();
 }
 private void eject(ServerLevel l,ItemStack stack){for(var d:Direction.values()){var q=blockPosition().relative(d);if(!l.hasChunkAt(q))continue;var be=l.getBlockEntity(q);if(be==null)continue;var h=be.getCapability(ForgeCapabilities.ITEM_HANDLER,d.getOpposite()).orElse(null);if(h!=null){stack=ItemHandlerHelper.insertItemStacked(h,stack,false);if(stack.isEmpty())return;}}Block.popResource(l,blockPosition().relative(facing().getOpposite()),stack);}
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);if(owner!=null)o.putString("bore_owner",owner.toString());o.putBoolean("bore_active",activated);o.putInt("bore_face",facing().ordinal());o.putFloat("bore_charge",charge);o.putInt("bore_wear",wear);o.putInt("bore_cursor",cursor);o.store("bore_tool",ItemStack.OPTIONAL_CODEC,tool.getItem(0));}
 protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);try{owner=UUID.fromString(i.getStringOr("bore_owner",""));}catch(IllegalArgumentException e){owner=null;}activated=i.getBooleanOr("bore_active",false);face(Direction.values()[Math.clamp(i.getIntOr("bore_face",2),0,5)]);float stored=i.getFloatOr("bore_charge",0);charge=Float.isFinite(stored)?Math.clamp(stored,0,10):0;wear=Math.clamp(i.getIntOr("bore_wear",0),0,49);cursor=i.getIntOr("bore_cursor",0);tool.setItem(0,i.read("bore_tool",ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));}
}
