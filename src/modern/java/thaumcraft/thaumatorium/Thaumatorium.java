package thaumcraft.thaumatorium;
import com.mojang.serialization.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import net.minecraftforge.items.wrapper.InvWrapper;
import thaumcraft.alchemy.*;
import thaumcraft.devices.AuraDeviceEntity;
import thaumcraft.essentia.*;
import thaumcraft.research.ResearchService;
public final class Thaumatorium extends AuraDeviceEntity implements MenuProvider,EssentiaTransport{
 public final SimpleContainer inventory=new SimpleContainer(1){public void setChanged(){super.setChanged();Thaumatorium.this.setChanged();if(level!=null&&!level.isClientSide())level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}};
 private List<ThaumatoriumSave.Selection> selections=new ArrayList<>();
 private ItemStack reserved=ItemStack.EMPTY;private List<ItemStack> products=new ArrayList<>();private Map<String,Integer> cost=Map.of(),remaining=Map.of();private String active="";private Tag unsupported;
 private final LazyOptional<IItemHandler> capability=LazyOptional.of(()->new InvWrapper(inventory));
 public Thaumatorium(BlockPos p,BlockState s){super(ThaumatoriumContent.TYPE.get(),p,s);}
 public boolean top(){return getBlockState().is(ThaumatoriumContent.TOP.get());}
 public Direction front(){return getBlockState().getValue(ThaumatoriumBlock.FACING);}
 public Thaumatorium controller(){if(!top())return this;return level!=null&&level.hasChunkAt(worldPosition.below())&&level.getBlockEntity(worldPosition.below()) instanceof Thaumatorium m&&!m.top()&&m.front()==front()?m:null;}
 public boolean supported(){return unsupported==null;}
 public boolean intact(){return level!=null&&level.hasChunkAt(worldPosition.above())&&level.getBlockState(worldPosition.below()).is(AlchemyContent.CRUCIBLE.get())&&level.getBlockState(worldPosition.above()).is(ThaumatoriumContent.TOP.get())&&level.getBlockState(worldPosition.above()).getValue(ThaumatoriumBlock.FACING)==front();}
 public boolean heated(){if(level==null)return false;var s=level.getBlockState(worldPosition.below(2));return s.is(Blocks.LAVA)||s.is(Blocks.FIRE)||s.is(Blocks.SOUL_FIRE)||s.is(Blocks.MAGMA_BLOCK)||net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString().startsWith("thaumcraft:nitor_");}
 public boolean powered(){return level!=null&&(level.hasNeighborSignal(worldPosition)||level.hasNeighborSignal(worldPosition.above())||level.hasNeighborSignal(worldPosition.below()));}
 public boolean running(){return supported()&&intact()&&heated()&&!powered();}
 public int capacity(){if(level==null)return 1;int n=1;for(int y=0;y<2;y++)for(var d:Direction.values()){if(d==Direction.DOWN||d==front()||y==0&&d==Direction.UP)continue;var at=worldPosition.above(y).relative(d);if(!level.hasChunkAt(at))continue;var s=level.getBlockState(at);if(s.is(ThaumatoriumContent.BRAIN.get())&&s.getValue(BrainBoxBlock.FACING)==d.getOpposite())n+=2;}return n;}
 public List<ThaumatoriumSave.Selection> selections(){return List.copyOf(selections);}
 public Map<String,Integer> remaining(){return remaining;}public int paid(){return AspectAmounts.total(cost)-AspectAmounts.total(remaining);}public int total(){return AspectAmounts.total(cost);}public String active(){return active;}public List<ItemStack> products(){return products.stream().map(ItemStack::copy).toList();}
 public List<RecipeHolder<?>> recipes(){return level instanceof ServerLevel l?l.recipeAccess().getRecipes().stream().filter(h->h.value() instanceof AlchemyRecipe).sorted(Comparator.comparing(h->h.id().toString())).toList():List.of();}
 public boolean valid(Player p){return supported()&&level==p.level()&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))<=64;}
 public boolean toggle(Player p,RecipeHolder<?> h){if(!valid(p)||!p.mayBuild()||!level.mayInteract(p,worldPosition)||!(h.value() instanceof AlchemyRecipe r)||!ResearchService.knows(p,"THAUMATORIUM"))return false;String id=h.id().identifier().toString();int at=-1;for(int i=0;i<selections.size();i++)if(selections.get(i).id().equals(id))at=i;if(at>=0){if(active.equals(id))return false;selections.remove(at);sync();return true;}if(selections.size()>=capacity()||!ResearchService.knows(p,r.research()))return false;selections.add(new ThaumatoriumSave.Selection(id,p.getUUID().toString(),r.research()));sync();return true;}
 private void start(ServerLevel l){var input=inventory.getItem(0);if(input.isEmpty())return;var recipes=recipes();for(var selected:selections.stream().limit(capacity()).toList())for(var holder:recipes){if(!holder.id().identifier().toString().equals(selected.id()))continue;var r=(AlchemyRecipe)holder.value();if(!r.research().equals(selected.research())||!r.matches(new SingleRecipeInput(input),l))continue;reserved=input.copyWithCount(1);products=new ArrayList<>();products.add(r.assemble(new SingleRecipeInput(reserved)));var remainder=AlchemyContent.remainder(reserved);if(!remainder.isEmpty())products.add(remainder);cost=r.aspects();remaining=cost;active=selected.id();inventory.removeItem(0,1);sync();return;}}
 private void finish(ServerLevel l){if(reserved.isEmpty()||!remaining.isEmpty())return;var selected=selections.stream().filter(s->s.id().equals(active)).findFirst();if(selected.isPresent()){var owner=l.getServer().getPlayerList().getPlayer(UUID.fromString(selected.get().owner()));if(owner!=null)ResearchService.recordCraft(owner,products.getFirst());}reserved=ItemStack.EMPTY;cost=Map.of();remaining=Map.of();active="";sync();}
 private void release(ServerLevel l){var at=worldPosition.relative(front());if(!l.hasChunkAt(at))return;var be=l.getBlockEntity(at);var handler=be==null?null:be.getCapability(ForgeCapabilities.ITEM_HANDLER,front().getOpposite()).orElse(null);if(handler==null&&!l.getBlockState(at).isAir())return;var next=new ArrayList<ItemStack>();for(var stack:products){var left=handler==null?stack.copy():ItemHandlerHelper.insertItemStacked(handler,stack.copy(),false);if(handler==null){var drop=new net.minecraft.world.entity.item.ItemEntity(l,at.getX()+.5,at.getY()+.5,at.getZ()+.5,left);drop.setDeltaMovement(front().getStepX()*.1,.05,front().getStepZ()*.1);if(l.addFreshEntity(drop))left=ItemStack.EMPTY;}if(!left.isEmpty())next.add(left);}products=next;sync();}
 public void serverTick(){if(!(level instanceof ServerLevel l)||!l.tickRateManager().runsNormally())return;if(top()){if(l.hasChunkAt(worldPosition.below())&&controller()==null)l.setBlockAndUpdate(worldPosition,thaumcraft.content.SimpleContent.BLOCKS.get("metal_alchemical").get().defaultBlockState());return;}if(!l.hasChunkAt(worldPosition.above())||!l.hasChunkAt(worldPosition.below()))return;if(!intact()){ThaumatoriumStructure.dismantle(l,worldPosition,null);return;}if(!supported())return;if(reserved.isEmpty()&&!products.isEmpty()){release(l);return;}if(!running())return;if(reserved.isEmpty())start(l);if(reserved.isEmpty())return;if(remaining.isEmpty()){finish(l);return;}if(l.getGameTime()%5==0){for(var d:Direction.values())if(EssentiaNetwork.pull(l,worldPosition,this,d))return;var upper=(Thaumatorium)l.getBlockEntity(worldPosition.above());for(var d:Direction.values())if(EssentiaNetwork.pull(l,worldPosition.above(),upper,d))return;}}
 public boolean connects(Direction d){return d!=front()&&d!=Direction.DOWN&&(top()||d!=Direction.UP);}
 public boolean canInput(Direction d){var m=controller();return connects(d)&&m!=null&&m.running()&&!m.reserved.isEmpty()&&!m.remaining.isEmpty();}
 public boolean canOutput(Direction d){return false;}public Map<String,Integer> contents(Direction d){var m=controller();return m==null?Map.of():AspectAmounts.subtract(m.cost,m.remaining);}
 public String suctionType(Direction d){var m=controller();return m==null?"":m.remaining.keySet().stream().sorted().findFirst().orElse("");}
 public int suction(Direction d){return canInput(d)?128:0;}public int minimumSuction(){return 0;}
 public boolean insert(String a,Direction d,boolean simulate){var m=controller();if(!canInput(d)||!a.equals(suctionType(d)))return false;if(!simulate){m.remaining=AspectAmounts.subtract(m.remaining,Map.of(a,1));m.sync();}return true;}
 public boolean extract(String a,Direction d,boolean simulate){return false;}
 public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction face){var m=controller();return !isRemoved()&&m!=null&&m.supported()&&cap==ForgeCapabilities.ITEM_HANDLER?m.capability.cast():super.getCapability(cap,face);}
 public void invalidateCaps(){super.invalidateCaps();capability.invalidate();}
 public Component getDisplayName(){return Component.translatable("block.thaumcraft.thaumatorium");}public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new ThaumatoriumMenu(id,inv,this);}
 public ThaumatoriumSave snapshot(){return new ThaumatoriumSave(1,List.copyOf(selections),inventory.getItem(0).copy(),reserved.copy(),products(),cost,remaining,active);}
 public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(level instanceof ServerLevel l){if(!top()&&supported()){Block.popResource(l,p,inventory.getItem(0));Block.popResource(l,p,reserved);if(reserved.isEmpty())products.forEach(v->Block.popResource(l,p,v));if(paid()>0)thaumcraft.aura.AuraManager.addFlux(l,p,paid());inventory.clearContent();reserved=ItemStack.EMPTY;products.clear();}ThaumatoriumStructure.dismantle(l,top()?p.below():p,p);}}
 protected void saveAdditional(ValueOutput o){super.saveAdditional(o);if(top())return;if(unsupported==null)o.store("thaumatorium",ThaumatoriumSave.CODEC,snapshot());else o.store("thaumatorium",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupported));}
 protected void loadAdditional(ValueInput i){super.loadAdditional(i);unsupported=null;selections=new ArrayList<>();inventory.clearContent();reserved=ItemStack.EMPTY;products=new ArrayList<>();cost=Map.of();remaining=Map.of();active="";var raw=i.read("thaumatorium",Codec.PASSTHROUGH);if(raw.isEmpty())return;var t=raw.get().convert(NbtOps.INSTANCE).getValue();var decoded=ThaumatoriumSave.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE,i.lookup()),t).result();if(decoded.isEmpty()){unsupported=t.copy();return;}var s=decoded.get();selections=new ArrayList<>(s.selections());inventory.setItem(0,s.input());reserved=s.reserved();products=new ArrayList<>(s.products());cost=s.cost();remaining=s.remaining();active=s.active();}
}
