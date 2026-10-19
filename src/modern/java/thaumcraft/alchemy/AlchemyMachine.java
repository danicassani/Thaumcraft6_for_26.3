package thaumcraft.alchemy;

import com.mojang.serialization.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.*;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import thaumcraft.aura.AuraManager;
import thaumcraft.crystals.CrystalAspects;
import thaumcraft.research.*;

public final class AlchemyMachine extends thaumcraft.devices.AuraDeviceEntity implements MenuProvider,thaumcraft.essentia.EssentiaTransport {
    public final SimpleContainer inventory=new SimpleContainer(3){@Override public void setItem(int slot,ItemStack item){if(slot==0&&!ItemStack.isSameItemSameComponents(getItem(0),item))cook=0;super.setItem(slot,item);}@Override public void setChanged(){super.setChanged();AlchemyMachine.this.setChanged();}};
    private Map<String,Integer> aspects=Map.of();
    private int heat,burn,burnTotal,cook,decay=-100;
    private Tag unsupported,unsupportedFilter;
    private String filter="";
    public final FluidTank water=new FluidTank(1000,s->s.getFluid()==Fluids.WATER){
        @Override public int fill(FluidStack s,FluidAction a){return supported()?super.fill(s,a):0;}
        @Override public FluidStack drain(int max,FluidAction a){return supported()?super.drain(max,a):FluidStack.EMPTY;}
        @Override protected void onContentsChanged(){changed();}
    };
    private LazyOptional<IFluidHandler> fluid=LazyOptional.of(()->water);
    private LazyOptional<IItemHandler> input=LazyOptional.of(()->handler(0)),fuel=LazyOptional.of(()->handler(1)),output=LazyOptional.of(()->handler(2));
    public AlchemyMachine(BlockPos pos,BlockState state){super(AlchemyContent.TYPE.get(),pos,state);}
    public AlchemyBlock.Kind kind(){return ((AlchemyBlock)getBlockState().getBlock()).kind;}
    public boolean supported(){return unsupported==null&&unsupportedFilter==null;}
    public String filter(){return filter;}
    public Map<String,Integer> aspects(){return aspects;}
    public int total(){return AspectAmounts.total(aspects);}
    public int heat(){return heat;}
    public int burn(){return burn;}
    public int burnTotal(){return burnTotal;}
    public int cook(){return cook;}
    public int capacity(){return kind()==AlchemyBlock.Kind.ALEMBIC?128:kind()==AlchemyBlock.Kind.CRUCIBLE?500:kind()==AlchemyBlock.Kind.SMELTER?256:250;}
    public boolean jar(){return kind()==AlchemyBlock.Kind.JAR||kind()==AlchemyBlock.Kind.VOID_JAR;}
    public boolean valid(Player p){return level instanceof ServerLevel&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64;}
    public AlchemyState snapshot(){return new AlchemyState(1,aspects,water.getFluidAmount(),heat,burn,burnTotal,cook,decay);}
    private void changed(){setChanged();if(level instanceof ServerLevel){sync();level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}}
    @Override public Component getDisplayName(){return Component.translatable("block.thaumcraft."+switch(kind()){case CRUCIBLE->"crucible";case SMELTER->"smelter_basic";case ALEMBIC->"alembic";case JAR->"jar_normal";case VOID_JAR->"jar_void";});}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new AlchemyMenu(id,inv,this);}
    public int comparator(){return total()==0?0:Math.min(15,1+(int)(14F*total()/capacity()));}
    public int color(){if(total()==0)return 0xFF4080FF;long red=0,green=0,blue=0;for(var e:aspects.entrySet()){int c=CrystalAspects.COLORS.get(e.getKey());red+=((c>>16)&255)*e.getValue();green+=((c>>8)&255)*e.getValue();blue+=(c&255)*e.getValue();}return 0xFF000000|((int)(red/total())<<16)|((int)(green/total())<<8)|(int)(blue/total());}
    @Override public void serverTick(){
        if(!(level instanceof ServerLevel server)||!supported()||!server.tickRateManager().runsNormally())return;
        if(kind()==AlchemyBlock.Kind.CRUCIBLE)crucibleTick(server);else if(kind()==AlchemyBlock.Kind.SMELTER)smelterTick(server);else if(jar()&&server.getGameTime()%5==0)thaumcraft.essentia.EssentiaNetwork.pull(level,worldPosition,this,Direction.UP);
    }
    private void lit(boolean lit){if(level!=null&&getBlockState().getValue(AlchemyBlock.LIT)!=lit)level.setBlockAndUpdate(worldPosition,getBlockState().setValue(AlchemyBlock.LIT,lit));}
    private void crucibleTick(ServerLevel server){
        var pos=worldPosition.below();boolean source=false;if(server.hasChunkAt(pos)){var b=server.getBlockState(pos);source=b.getFluidState().is(Fluids.LAVA)||b.is(Blocks.FIRE)||b.is(Blocks.SOUL_FIRE)||b.is(Blocks.MAGMA_BLOCK)||thaumcraft.content.LuminousContent.BLOCKS.entrySet().stream().anyMatch(e->e.getKey().startsWith("nitor_")&&b.is(e.getValue().get()));}
        int old=heat;heat=water.getFluidAmount()>0&&source?Math.min(200,heat+1):Math.max(0,heat-1);if(old!=heat)setChanged();lit(heat>150&&water.getFluidAmount()>0);
        if(total()>500)decayOne(server);if(++decay>=100){decay=0;decayOne(server);}if(!aspects.isEmpty())setChanged();
        if(heat>150&&water.getFluidAmount()>0){
            var area=new AABB(worldPosition.getX()+.15,worldPosition.getY()+.3,worldPosition.getZ()+.15,worldPosition.getX()+.85,worldPosition.getY()+1.05,worldPosition.getZ()+.85);
            for(var item:server.getEntitiesOfClass(ItemEntity.class,area,e->e.isAlive()&&!e.getPersistentData().getBooleanOr("thaumcraft:alchemy_output",false))){
                var player=item.getOwner() instanceof Player p?p:null;int n=Math.min(64,item.getItem().getCount());for(int i=0;i<n;i++){if(!process(item.getItem(),player))break;}if(item.getItem().isEmpty())item.discard();
            }
            if(server.getGameTime()%10==0)for(var living:server.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,area))living.hurt(server.damageSources().hotFloor(),1);
            if(server.getGameTime()%10==0)server.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE,worldPosition.getX()+.5,worldPosition.getY()+.8,worldPosition.getZ()+.5,2,.2,.02,.2,.01);
        }
        if(server.getGameTime()%20==0&&heat!=old)sync();
    }
    private void decayOne(ServerLevel server){if(aspects.isEmpty())return;var keys=aspects.keySet().stream().sorted().toList();String a=keys.get(server.getRandom().nextInt(keys.size()));aspects=AspectAmounts.subtract(aspects,Map.of(a,1));AuraManager.addFlux(server,worldPosition,a.equals("vitium")?1:.25F);changed();}
    public void dump(){if(!(level instanceof ServerLevel server)||!supported())return;float flux=total()*.25F+aspects.getOrDefault("vitium",0)*.75F;AuraManager.addFlux(server,worldPosition,flux);aspects=Map.of();water.setFluid(FluidStack.EMPTY);heat=0;changed();}
    /** Consumes one actual stack item only after matching the server's recipe and costs. */
    public boolean process(ItemStack stack,Player player){
        if(!(level instanceof ServerLevel server)||kind()!=AlchemyBlock.Kind.CRUCIBLE||!supported()||heat<=150||water.getFluidAmount()<=0||stack.isEmpty())return false;
        if(player!=null&&(!valid(player)||ResearchService.state(player)==null))return false;
        var matching=server.recipeAccess().getRecipes().stream().filter(h->h.value() instanceof AlchemyRecipe a&&a.matches(new net.minecraft.world.item.crafting.SingleRecipeInput(stack),server)&&AspectAmounts.contains(aspects,a.aspects())).sorted(Comparator.<RecipeHolder<?>>comparingInt(h->AspectAmounts.total(((AlchemyRecipe)h.value()).aspects())).reversed().thenComparing(h->h.id().toString())).toList();
        AlchemyRecipe recipe=null;for(var h:matching){var r=(AlchemyRecipe)h.value();if(player!=null&&ResearchService.knows(player,r.research())){recipe=r;break;}}
        if(recipe!=null){
            if(water.getFluidAmount()<50)return false;var next=AspectAmounts.subtract(aspects,recipe.aspects());var result=recipe.result().create();if(!ResearchService.knows(player,recipe.research()))return false;
            aspects=next;water.drain(50,IFluidHandler.FluidAction.EXECUTE);stack.shrink(1);ResearchService.recordCraft(player,result);eject(result);decay=-250;changed();return true;
        }
        // A fully prepared but research-locked catalyst is kept rather than silently destroyed.
        if(!matching.isEmpty())return false;
        var value=AspectResolver.item(server,stack);if(value.isEmpty()||(long)total()+AspectAmounts.total(value)>AspectAmounts.LIMIT)return false;
        var remainder=AlchemyContent.remainder(stack);aspects=AspectAmounts.add(aspects,value);stack.shrink(1);if(!remainder.isEmpty())eject(remainder);decay=-150;changed();return true;
    }
    private void eject(ItemStack result){if(level instanceof ServerLevel server){var entity=new ItemEntity(server,worldPosition.getX()+.5,worldPosition.getY()+1.1,worldPosition.getZ()+.5,result.copy());entity.setDeltaMovement(0,.18,0);entity.setDefaultPickUpDelay();entity.getPersistentData().putBoolean("thaumcraft:alchemy_output",true);server.addFreshEntity(entity);}}
    public boolean insertEssence(String aspect,int amount,boolean simulate){
        if(!supported()||amount<1||!filter.isEmpty()&&!filter.equals(aspect)||!CrystalAspects.COLORS.containsKey(aspect)||!(jar()||kind()==AlchemyBlock.Kind.ALEMBIC)||!aspects.isEmpty()&&!aspects.containsKey(aspect))return false;
        int room=capacity()-total();if(room<amount&&kind()!=AlchemyBlock.Kind.VOID_JAR)return false;
        if(!simulate){int keep=Math.min(room,amount);if(keep>0)aspects=AspectAmounts.add(aspects,Map.of(aspect,keep));changed();}return true;
    }
    public boolean extractEssence(String aspect,int amount,boolean simulate){if(!supported()||amount<1||!AspectAmounts.contains(aspects,Map.of(aspect,amount)))return false;if(!simulate){aspects=AspectAmounts.subtract(aspects,Map.of(aspect,amount));changed();}return true;}
    public InteractionResult phial(Player p,InteractionHand hand){
        if(!(jar()||kind()==AlchemyBlock.Kind.ALEMBIC))return InteractionResult.PASS;var held=p.getItemInHand(hand);if(!held.is(AlchemyContent.PHIAL.get()))return InteractionResult.PASS;
        if(level.isClientSide())return InteractionResult.SUCCESS;if(!valid(p)||!supported())return InteractionResult.FAIL;
        String aspect=AlchemyContent.aspect(held);int amount=AlchemyContent.amount(held);ItemStack result;
        if(aspect.isEmpty()&&amount==0){if(total()<10||aspects.size()!=1)return InteractionResult.FAIL;aspect=aspects.keySet().iterator().next();if(!extractEssence(aspect,10,false))return InteractionResult.FAIL;result=AlchemyContent.filled(AlchemyContent.PHIAL.get(),aspect,10);}
        else {if(!jar()||amount!=10||!insertEssence(aspect,10,true))return InteractionResult.FAIL;insertEssence(aspect,10,false);result=new ItemStack(AlchemyContent.PHIAL.get());}
        held.shrink(1);if(held.isEmpty())p.setItemInHand(hand,result);else if(!p.getInventory().add(result))Block.popResource(level,worldPosition,result);p.getInventory().setChanged();return InteractionResult.SUCCESS;
    }
    public InteractionResult interact(Player p,InteractionHand hand){
        var held=p.getItemInHand(hand);if(held.is(thaumcraft.essentia.EssentiaContent.LABEL.get()))return label(p,hand,getBlockState().getValue(AlchemyBlock.FACING));if(held.is(thaumcraft.essentia.EssentiaContent.RESONATOR.get()))return InteractionResult.PASS;
        if(held.isEmpty()&&p.isShiftKeyDown()&&(jar()||kind()==AlchemyBlock.Kind.ALEMBIC)&&!filter.isEmpty()){if(!level.isClientSide()&&valid(p)&&supported()){filter="";Block.popResource(level,worldPosition,new ItemStack(thaumcraft.essentia.EssentiaContent.LABEL.get()));changed();}return InteractionResult.SUCCESS;}
        if(held.is(ResearchContent.METER.get()))return InteractionResult.PASS;
        if(held.is(AlchemyContent.PHIAL.get())&&kind()!=AlchemyBlock.Kind.CRUCIBLE)return phial(p,hand);
        if(level.isClientSide())return InteractionResult.SUCCESS;if(!valid(p)||!supported())return InteractionResult.FAIL;
        if(kind()==AlchemyBlock.Kind.CRUCIBLE){
            if(held.is(Items.WATER_BUCKET)){if(water.getFluidAmount()>=1000)return InteractionResult.FAIL;water.fill(new FluidStack(Fluids.WATER,1000),IFluidHandler.FluidAction.EXECUTE);if(!p.getAbilities().instabuild)p.setItemInHand(hand,new ItemStack(Items.BUCKET));return InteractionResult.SUCCESS;}
            if(held.isEmpty()&&p.isShiftKeyDown()){dump();return InteractionResult.SUCCESS;}
            if(!held.isEmpty()){return process(held,p)?InteractionResult.SUCCESS:InteractionResult.FAIL;}
        }
        p.openMenu(this);return InteractionResult.SUCCESS;
    }
    public InteractionResult label(Player p,InteractionHand hand,Direction face){
        if(!(jar()||kind()==AlchemyBlock.Kind.ALEMBIC)||face.getAxis()==Direction.Axis.Y)return InteractionResult.FAIL;
        if(level.isClientSide())return InteractionResult.SUCCESS;if(!valid(p)||!supported()||!filter.isEmpty())return InteractionResult.FAIL;
        String a=aspects.isEmpty()?AlchemyContent.aspect(p.getItemInHand(hand)):aspects.keySet().iterator().next();if(a.isEmpty()||!CrystalAspects.COLORS.containsKey(a))return InteractionResult.FAIL;
        filter=a;p.getItemInHand(hand).shrink(1);level.setBlockAndUpdate(worldPosition,getBlockState().setValue(AlchemyBlock.FACING,face));changed();return InteractionResult.SUCCESS;
    }
    public ItemStack jarItem(){var item=jar()&&supported()&&aspects.size()==1?AlchemyContent.filled(getBlockState().getBlock().asItem(),aspects.keySet().iterator().next(),total()):new ItemStack(getBlockState().getBlock());if(jar()&&supported()&&!filter.isEmpty())item.set(thaumcraft.essentia.EssentiaContent.FILTER.get(),filter);return item;}
    public void placeContents(ItemStack stack){if(!jar()||!supported())return;String a=AlchemyContent.aspect(stack);int n=AlchemyContent.amount(stack);String f=stack.getOrDefault(thaumcraft.essentia.EssentiaContent.FILTER.get(),"");if(!f.isEmpty()&&(a.isEmpty()||a.equals(f)))filter=f;if(!a.isEmpty()&&n>0&&n<=250)insertEssence(a,n,false);changed();}
    @Override public boolean connects(Direction face){return supported()&&face!=null&&(jar()?face==Direction.UP:kind()==AlchemyBlock.Kind.ALEMBIC&&face!=Direction.DOWN&&face!=getBlockState().getValue(AlchemyBlock.FACING));}
    @Override public boolean canInput(Direction face){return connects(face)&&jar();}
    @Override public boolean canOutput(Direction face){return connects(face);}
    @Override public Map<String,Integer> contents(Direction face){return supported()&&(jar()||kind()==AlchemyBlock.Kind.ALEMBIC)?aspects:Map.of();}
    @Override public String suctionType(Direction face){return jar()?filter.isEmpty()?aspects.keySet().stream().findFirst().orElse(""):filter:"";}
    @Override public int suction(Direction face){if(!connects(face)||!jar())return 0;if(kind()==AlchemyBlock.Kind.VOID_JAR)return !filter.isEmpty()&&total()<250?48:32;return total()>=250?0:filter.isEmpty()?32:64;}
    @Override public int minimumSuction(){return jar()?filter.isEmpty()?32:kind()==AlchemyBlock.Kind.VOID_JAR?48:64:0;}
    @Override public boolean insert(String a,Direction face,boolean simulate){return canInput(face)&&insertEssence(a,1,simulate);}
    @Override public boolean extract(String a,Direction face,boolean simulate){return canOutput(face)&&extractEssence(a,1,simulate);}
    public int fuelTime(ItemStack stack){if(!(level instanceof ServerLevel server)||stack.isEmpty())return 0;
        var context=new LootContext.Builder(new LootParams.Builder(server).withParameter(LootContextParams.BLOCK_STATE,getBlockState()).withParameter(LootContextParams.BLOCK_ENTITY,this).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(worldPosition)).withParameter(LootContextParams.CONTAINER,inventory).create(LootContextParamSets.CONTAINER_PROCESS)).create(Optional.empty());
        int nativeTime=ResolvableInt.getFromItem(stack,DataComponents.COOKING_FUEL,CookingFuel::burnTime,context,0);return Math.clamp(net.minecraftforge.event.ForgeEventFactory.getItemBurnTime(stack,nativeTime,RecipeType.SMELTING),0,1000000);
    }
    public int needed(){if(!(level instanceof ServerLevel server))return 0;return AspectAmounts.total(AspectResolver.item(server,inventory.getItem(0)))*2;}
    private boolean canSmelt(){if(!(level instanceof ServerLevel server)||!supported())return false;var source=inventory.getItem(0);var values=AspectResolver.item(server,source);if(values.isEmpty()||total()+AspectAmounts.total(values)>256)return false;var rest=AlchemyContent.remainder(source);var out=inventory.getItem(2);return rest.isEmpty()||out.isEmpty()||ItemStack.isSameItemSameComponents(out,rest)&&out.getCount()+rest.getCount()<=out.getMaxStackSize();}
    private void smelterTick(ServerLevel server){
        boolean was=burn>0;if(burn>0){burn--;setChanged();}
        boolean can=canSmelt();if(burn==0&&can){var stack=inventory.getItem(1);int ticks=fuelTime(stack);if(ticks>0){var rest=AlchemyContent.remainder(stack);stack.shrink(1);if(stack.isEmpty())inventory.setItem(1,rest);else if(!rest.isEmpty())eject(rest);burn=burnTotal=ticks;setChanged();}}
        if(burn>0&&can){cook++;setChanged();if(cook>=Math.max(1,needed())){
            var source=inventory.getItem(0);var values=AspectResolver.item(server,source);var retained=new TreeMap<String,Integer>();int lost=0;
            for(var e:values.entrySet())for(int n=0;n<e.getValue();n++)if(server.getRandom().nextFloat()>(e.getKey().equals("vitium")?.8F*.66F:.8F))lost++;else retained.merge(e.getKey(),1,Integer::sum);
            var rest=AlchemyContent.remainder(source);source.shrink(1);if(!rest.isEmpty()){if(inventory.getItem(2).isEmpty())inventory.setItem(2,rest);else inventory.getItem(2).grow(rest.getCount());}
            aspects=AspectAmounts.add(aspects,retained);if(lost>0)AuraManager.addFlux(server,worldPosition,lost);cook=0;changed();
        }}else if(cook!=0){cook=0;setChanged();}
        if(was!=(burn>0))changed();lit(burn>0);
        if(server.getGameTime()%15==0&&!aspects.isEmpty())distill();
    }
    private void distill(){
        var targets=new ArrayList<AlchemyMachine>();for(int y=1;y<=5;y++){var p=worldPosition.above(y);if(!level.hasChunkAt(p)||!(level.getBlockEntity(p) instanceof AlchemyMachine m)||m.kind()!=AlchemyBlock.Kind.ALEMBIC)break;targets.add(m);}
        for(var a:aspects.keySet().stream().sorted().toList()){
            for(boolean matching:new boolean[]{true,false})for(var target:targets)if((target.aspects.containsKey(a)==matching)&&target.insertEssence(a,1,true)){target.insertEssence(a,1,false);aspects=AspectAmounts.subtract(aspects,Map.of(a,1));changed();return;}
        }
    }
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState s){super.preRemoveSideEffects(pos,s);if(level instanceof ServerLevel){for(int i=0;i<3;i++)Block.popResource(level,pos,inventory.removeItemNoUpdate(i));if(kind()==AlchemyBlock.Kind.CRUCIBLE)dump();else if(!jar()&&supported()&&total()>0)AuraManager.addFlux((ServerLevel)level,pos,total()*.25F);}}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("essentia_filter",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupportedFilter==null?thaumcraft.essentia.TubeState.FILTER.encodeStart(NbtOps.INSTANCE,filter).getOrThrow():unsupportedFilter));out.store("alchemy",Codec.PASSTHROUGH,unsupported==null?new Dynamic<>(NbtOps.INSTANCE,AlchemyState.CODEC.encodeStart(NbtOps.INSTANCE,snapshot()).getOrThrow()):new Dynamic<>(NbtOps.INSTANCE,unsupported));for(int i=0;i<3;i++)if(!inventory.getItem(i).isEmpty())out.store("slot"+i,ItemStack.CODEC,inventory.getItem(i));}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);unsupportedFilter=null;filter="";var rawFilter=in.read("essentia_filter",Codec.PASSTHROUGH);if(rawFilter.isPresent()){Tag t=rawFilter.get().convert(NbtOps.INSTANCE).getValue();var f=thaumcraft.essentia.TubeState.FILTER.parse(NbtOps.INSTANCE,t).result();if(f.isPresent())filter=f.get();else unsupportedFilter=t.copy();}unsupported=null;var raw=in.read("alchemy",Codec.PASSTHROUGH);var state=AlchemyState.EMPTY;if(raw.isPresent()){Tag tag=raw.get().convert(NbtOps.INSTANCE).getValue();var decoded=AlchemyState.CODEC.parse(NbtOps.INSTANCE,tag).result();if(decoded.isPresent())state=decoded.get();else unsupported=tag.copy();}aspects=state.aspects();heat=state.heat();burn=state.burn();burnTotal=state.burnTotal();cook=state.cook();decay=state.decay();water.setFluid(state.water()==0?FluidStack.EMPTY:new FluidStack(Fluids.WATER,state.water()));if((jar()||kind()==AlchemyBlock.Kind.ALEMBIC)&&(aspects.size()>1||total()>capacity())){unsupported=raw.map(d->d.convert(NbtOps.INSTANCE).getValue().copy()).orElse(null);aspects=Map.of();}for(int i=0;i<3;i++)inventory.setItem(i,in.read("slot"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY));cook=state.cook();if(!filter.isEmpty()&&(!aspects.isEmpty()&&!aspects.containsKey(filter)||!(jar()||kind()==AlchemyBlock.Kind.ALEMBIC)))unsupportedFilter=rawFilter.map(d->d.convert(NbtOps.INSTANCE).getValue().copy()).orElse(null);}
    private IItemHandler handler(int face){return new IItemHandler(){
        public int getSlots(){return 3;}public ItemStack getStackInSlot(int s){return inventory.getItem(s).copy();}public int getSlotLimit(int s){return 64;}
        public boolean isItemValid(int s,ItemStack item){return supported()&&((face==0&&s==0&&level instanceof ServerLevel server&&!AspectResolver.item(server,item).isEmpty())||(face==1&&s==1&&fuelTime(item)>0));}
        public ItemStack insertItem(int s,ItemStack item,boolean simulate){if(s<0||s>2||!isItemValid(s,item)||item.isEmpty())return item;var old=inventory.getItem(s);if(!old.isEmpty()&&!ItemStack.isSameItemSameComponents(old,item))return item;int n=Math.min(item.getCount(),item.getMaxStackSize()-old.getCount());if(n<=0)return item;var rest=item.copy();rest.shrink(n);if(!simulate){if(old.isEmpty())inventory.setItem(s,item.copyWithCount(n));else old.grow(n);inventory.setChanged();}return rest;}
        public ItemStack extractItem(int s,int n,boolean simulate){if(!supported()||face!=2||s<1||s>2||n<=0||s==1&&fuelTime(inventory.getItem(1))>0)return ItemStack.EMPTY;var out=inventory.getItem(s).copyWithCount(Math.min(n,inventory.getItem(s).getCount()));if(!simulate)inventory.removeItem(s,out.getCount());return out;}
    };}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction side){if(!isRemoved()){if(kind()==AlchemyBlock.Kind.CRUCIBLE&&cap==ForgeCapabilities.FLUID_HANDLER)return fluid.cast();if(kind()==AlchemyBlock.Kind.SMELTER&&cap==ForgeCapabilities.ITEM_HANDLER)return (side==Direction.UP?input:side==Direction.DOWN?output:fuel).cast();}return super.getCapability(cap,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();fluid.invalidate();input.invalidate();fuel.invalidate();output.invalidate();}
    @Override public void reviveCaps(){super.reviveCaps();fluid=LazyOptional.of(()->water);input=LazyOptional.of(()->handler(0));fuel=LazyOptional.of(()->handler(1));output=LazyOptional.of(()->handler(2));}
}
