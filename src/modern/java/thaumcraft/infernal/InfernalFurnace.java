package thaumcraft.infernal;
import java.util.*;
import com.mojang.serialization.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import thaumcraft.alchemy.SmelterUpgradeBlock;
import thaumcraft.aura.AuraManager;
public final class InfernalFurnace extends thaumcraft.devices.AuraDeviceEntity{
 private int remaining,maximum,slot=-1,speed,xp,ticks;private Tag unsupported;
 public final ItemStackHandler inventory=new ItemStackHandler(32){public boolean isItemValid(int n,ItemStack item){return supported()&&recipe(item)!=null;}public ItemStack extractItem(int n,int amount,boolean simulate){return ItemStack.EMPTY;}protected void onContentsChanged(int n){setChanged();}};
 private final List<ItemStack> outputs=new ArrayList<>();
 private LazyOptional<IItemHandler> capability=LazyOptional.of(()->inventory);
 public InfernalFurnace(BlockPos p,BlockState s){super(InfernalContent.TYPE.get(),p,s);}
 public boolean supported(){return unsupported==null;}public Direction front(){return getBlockState().getValue(InfernalBlock.FACING);}
 public InfernalWork work(){return new InfernalWork(1,remaining,maximum,slot,speed,xp);}
 public List<ItemStack> pending(){return outputs.stream().map(ItemStack::copy).toList();}
 public int inputCount(){int n=0;for(int i=0;i<32;i++)n+=inventory.getStackInSlot(i).getCount();return n;}
 public int bellows(){int n=0;for(var d:Direction.values())if(d!=Direction.UP){var p=worldPosition.relative(d,2);if(level.hasChunkAt(p)){var s=level.getBlockState(p);if(s.getBlock() instanceof SmelterUpgradeBlock b&&b.kind==SmelterUpgradeBlock.Kind.BELLOWS&&s.getValue(SmelterUpgradeBlock.FACING)==d.getOpposite()&&!level.hasNeighborSignal(p))n++;}}return Math.min(4,n);}
 private SmeltingRecipe recipe(ItemStack item){if(item.isEmpty()||!(level instanceof ServerLevel l))return null;var found=l.recipeAccess().getRecipeFor(RecipeType.SMELTING,new SingleRecipeInput(item),l);return found.map(RecipeHolder::value).orElse(null);}
 public ItemStack accept(ItemStack item){if(!supported())return item;if(recipe(item)==null){if(level instanceof ServerLevel l){l.sendParticles(ParticleTypes.SMOKE,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,4,.1,.1,.1,.02);l.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.LAVA_EXTINGUISH,net.minecraft.sounds.SoundSource.BLOCKS,.3F,2.6F);}return ItemStack.EMPTY;}return ItemHandlerHelper.insertItemStacked(inventory,item.copy(),false);}
 private void intake(ServerLevel l){var box=new AABB(worldPosition.getX()+.2,worldPosition.getY()+.5,worldPosition.getZ()+.2,worldPosition.getX()+.8,worldPosition.getY()+2,worldPosition.getZ()+.8);if(l.getBlockState(worldPosition.above()).isAir())for(var item:l.getEntitiesOfClass(ItemEntity.class,box)){var before=item.getItem();var rest=accept(before);if(rest.isEmpty())item.discard();else if(rest.getCount()!=before.getCount())item.setItem(rest);}
  for(var e:l.getEntitiesOfClass(LivingEntity.class,box))if(!e.fireImmune()){e.hurtServer(l,l.damageSources().lava(),3);e.igniteForSeconds(10);}
 }
 private boolean release(ServerLevel l){var mouth=worldPosition.relative(front());if(!l.hasChunkAt(mouth))return false;var target=mouth;var be=l.getBlockEntity(target);IItemHandler handler=be==null?null:be.getCapability(ForgeCapabilities.ITEM_HANDLER,front().getOpposite()).orElse(null);if(handler==null&&!l.getBlockState(mouth).isAir())return false;if(handler==null){var outer=mouth.relative(front());if(!l.hasChunkAt(outer))return false;be=l.getBlockEntity(outer);handler=be==null?null:be.getCapability(ForgeCapabilities.ITEM_HANDLER,front().getOpposite()).orElse(null);}
  while(!outputs.isEmpty()){var item=outputs.getFirst();if(handler!=null){var rest=ItemHandlerHelper.insertItemStacked(handler,item.copy(),false);if(rest.getCount()==item.getCount())return false;if(rest.isEmpty())outputs.removeFirst();else outputs.set(0,rest);sync();if(!rest.isEmpty())return false;}else{var at=Vec3.atCenterOf(mouth);var drop=new ItemEntity(l,at.x,at.y,at.z,item.copy());drop.setDeltaMovement(front().getStepX()*.13,.05,front().getStepZ()*.13);if(!l.addFreshEntity(drop))return false;outputs.removeFirst();sync();}}
  if(xp>0){ExperienceOrb.award(l,Vec3.atCenterOf(mouth),xp);xp=0;sync();}return true;
 }
 private void complete(ServerLevel l){var in=inventory.getStackInSlot(slot);var r=recipe(in);if(r==null){slot=-1;remaining=maximum=0;sync();return;}var result=r.assemble(new SingleRecipeInput(in));if(result.isEmpty()||!result.isItemEnabled(l.enabledFeatures())||result.getCount()>32*Math.min(64,result.getMaxStackSize())){slot=-1;remaining=maximum=0;sync();return;}for(int left=result.getCount(),limit=Math.min(64,result.getMaxStackSize());left>0;left-=limit)outputs.add(result.copyWithCount(Math.min(limit,left)));outputs.addAll(InfernalBonuses.roll(in,bellows(),l.getRandom()));float exact=result.getCount()*r.experience();xp=Math.clamp((int)Math.floor(exact)+(l.getRandom().nextFloat()<exact-Math.floor(exact)?1:0),0,1000000);inventory.setStackInSlot(slot,in.copyWithCount(in.getCount()-1));if(speed>0)speed--;slot=-1;remaining=maximum=0;if(l.getRandom().nextInt(20)==0)AuraManager.addFlux(l,worldPosition.relative(front().getOpposite()),1);l.sendParticles(ParticleTypes.LAVA,worldPosition.getX()+.5+front().getStepX(),worldPosition.getY()+.5,worldPosition.getZ()+.5+front().getStepZ(),3,.15,.15,.15,.02);sync();}
 public void serverTick(){if(!(level instanceof ServerLevel l)||!l.tickRateManager().runsNormally()||!supported())return;ticks++;if(!InfernalStructure.loaded(l,worldPosition))return;if(!InfernalStructure.intact(l,worldPosition,front())){InfernalStructure.dismantle(l,worldPosition,null);return;}if(ticks%10==0)intake(l);if(!release(l)){active(false);return;}
  if(slot>=0){if(recipe(inventory.getStackInSlot(slot))==null){slot=-1;remaining=maximum=0;sync();}else{if(remaining>0){remaining--;setChanged();}if(remaining==0)complete(l);}}else{for(int i=0;i<32;i++)if(recipe(inventory.getStackInSlot(i))!=null){if(speed==0){int available=(int)AuraManager.drainVis(l,worldPosition,20,true);if(available>0)speed=(int)AuraManager.drainVis(l,worldPosition,available,false);}slot=i;remaining=maximum=InfernalRules.cookTicks(speed>0,bellows());sync();break;}}
  active(slot>=0);
 }
 private void active(boolean on){var s=getBlockState();if(s.getValue(InfernalBlock.ACTIVE)!=on)level.setBlockAndUpdate(worldPosition,s.setValue(InfernalBlock.ACTIVE,on));}
 public void inspect(Player p){p.sendSystemMessage(Component.translatable(supported()?"infernal.thaumcraft.status":"alchemy.thaumcraft.invalid",inputCount(),remaining,maximum,speed,bellows(),outputs.stream().mapToInt(ItemStack::getCount).sum()));}
 public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction face){return !isRemoved()&&supported()&&cap==ForgeCapabilities.ITEM_HANDLER&&face==Direction.UP?capability.cast():super.getCapability(cap,face);}
 public void invalidateCaps(){super.invalidateCaps();capability.invalidate();}public void reviveCaps(){super.reviveCaps();capability=LazyOptional.of(()->inventory);}
 public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(level instanceof ServerLevel l){if(supported()){for(int i=0;i<32;i++){Block.popResource(l,p.offset(1,2,1),inventory.getStackInSlot(i).copy());inventory.setStackInSlot(i,ItemStack.EMPTY);}for(var item:outputs)Block.popResource(l,p.offset(1,2,1),item);outputs.clear();if(xp>0)ExperienceOrb.award(l,Vec3.atCenterOf(p.offset(1,2,1)),xp);xp=0;}InfernalStructure.dismantle(l,p,p);}}
 protected void saveAdditional(ValueOutput o){super.saveAdditional(o);var input=new ArrayList<ItemStack>();for(int i=0;i<32;i++)input.add(inventory.getStackInSlot(i).copy());if(unsupported==null)o.store("infernal",InfernalSave.CODEC,new InfernalSave(work(),input,pending()));else o.store("infernal",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupported));}
 protected void loadAdditional(ValueInput i){super.loadAdditional(i);unsupported=null;slot=-1;remaining=maximum=speed=xp=0;outputs.clear();for(int n=0;n<32;n++)inventory.setStackInSlot(n,ItemStack.EMPTY);var raw=i.read("infernal",Codec.PASSTHROUGH);if(raw.isPresent()){Tag t=raw.get().convert(NbtOps.INSTANCE).getValue();var value=InfernalSave.CODEC.parse(net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE,i.lookup()),t).result();if(value.isEmpty()){unsupported=t.copy();return;}var s=value.get();var w=s.work();slot=w.slot();remaining=w.remaining();maximum=w.maximum();speed=w.speed();xp=w.xp();for(int n=0;n<32;n++)inventory.setStackInSlot(n,s.input().get(n).copy());s.output().forEach(v->outputs.add(v.copy()));}}
}
