package thaumcraft.infusion;

import com.mojang.serialization.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import thaumcraft.alchemy.*;
import thaumcraft.aura.AuraManager;
import thaumcraft.content.*;
import thaumcraft.research.*;

public final class InfusionMatrix extends thaumcraft.devices.AuraDeviceEntity{
    private InfusionJob job;
    private Tag unsupported;
    private float stability,replenish,cost=1;
    private int ticks,countdown,offeringTicks,cycleDelay=5;
    private String status="idle";
    private List<BlockPos> pedestalPositions=List.of(),jarPositions=List.of();
    public InfusionMatrix(BlockPos p,BlockState s){super(InfusionContent.MATRIX_TYPE.get(),p,s);}
    public boolean supported(){return unsupported==null;}
    public InfusionJob job(){return job;}
    public float stability(){return stability;}
    public float replenish(){return replenish;}
    public int cycleDelay(){return cycleDelay;}
    public float costMultiplier(){return cost;}
    public String status(){return status;}
    public InfusionState snapshot(){return new InfusionState(1,stability,countdown,Optional.ofNullable(job),offeringTicks);}
    private void changed(){sync();if(level!=null)level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}
    private void flags(boolean active){var s=getBlockState().setValue(InfusionBlock.ACTIVE,active).setValue(InfusionBlock.CRAFTING,job!=null);if(level!=null&&s!=getBlockState())level.setBlockAndUpdate(worldPosition,s);}
    private boolean loaded(BlockPos p){return level!=null&&level.hasChunkAt(p);}
    private InfusionPedestal central(){var p=worldPosition.below(2);return loaded(p)&&level.getBlockEntity(p) instanceof InfusionPedestal t&&t.supported()?t:null;}
    private boolean structureLoaded(){if(!loaded(worldPosition.below(2)))return false;for(int x:new int[]{-1,1})for(int z:new int[]{-1,1})if(!loaded(worldPosition.offset(x,-2,z)))return false;return true;}
    private String style(){var center=central();if(center==null)return "";String style=center.getBlockState().is(InfusionContent.ARCANE.get())?"arcane":center.getBlockState().is(InfusionContent.ANCIENT.get())?"ancient":"eldritch";for(int x:new int[]{-1,1})for(int z:new int[]{-1,1}){var p=worldPosition.offset(x,-2,z);if(!loaded(p)||!level.getBlockState(p).is(SimpleContent.BLOCKS.get("pillar_"+style).get()))return "";}return style;}
    public boolean assemble(Player player,ItemStack salis){
        if(!(level instanceof ServerLevel)||!supported()||job!=null||central()==null)return false;
        String style=central().getBlockState().is(InfusionContent.ARCANE.get())?"arcane":central().getBlockState().is(InfusionContent.ANCIENT.get())?"ancient":"eldritch";
        if(!ResearchService.knows(player,style.equals("arcane")?"INFUSION":style.equals("ancient")?"INFUSIONANCIENT":"INFUSIONELDRITCH"))return false;
        var stone=SimpleContent.BLOCKS.get(style.equals("arcane")?"stone_arcane":style.equals("ancient")?"stone_ancient":"stone_eldritch_tile").get();
        boolean raw=true;for(int x:new int[]{-1,1})for(int z:new int[]{-1,1})for(int y:new int[]{-2,-1}){var p=worldPosition.offset(x,y,z);raw&=loaded(p)&&level.getBlockState(p).is(stone);}
        if(!raw&&style().isEmpty())return false;
        if(raw)for(int x:new int[]{-1,1})for(int z:new int[]{-1,1}){level.setBlockAndUpdate(worldPosition.offset(x,-1,z),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(worldPosition.offset(x,-2,z),SimpleContent.BLOCKS.get("pillar_"+style).get().defaultBlockState());}
        if(!player.getAbilities().instabuild)salis.shrink(1);flags(true);surroundings();status="idle";changed();return true;
    }
    /** Loaded block entities only: scanning never forces chunks. */
    private List<BlockEntity> nearby(int radius){var found=new ArrayList<BlockEntity>();if(!(level instanceof ServerLevel s))return found;for(int x=(worldPosition.getX()-radius)>>4;x<=(worldPosition.getX()+radius)>>4;x++)for(int z=(worldPosition.getZ()-radius)>>4;z<=(worldPosition.getZ()+radius)>>4;z++){var chunk=s.getChunkSource().getChunkNow(x,z);if(chunk!=null)for(var e:chunk.getBlockEntities().values()){var p=e.getBlockPos();if(Math.abs(p.getX()-worldPosition.getX())<=radius&&Math.abs(p.getZ()-worldPosition.getZ())<=radius&&Math.abs(p.getY()-worldPosition.getY())<=radius)found.add(e);}}return found;}
    public void surroundings(){
        var pedestals=new ArrayList<BlockPos>();var jars=new ArrayList<BlockPos>();float multiplier=1;replenish=0;
        String type=style();int cycleTime=type.equals("ancient")?9:type.equals("eldritch")?7:10;for(int x:new int[]{-1,1})for(int z:new int[]{-1,1}){var p=worldPosition.offset(x,-3,z);if(!loaded(p))continue;var b=level.getBlockState(p);if(b.is(SimpleContent.BLOCKS.get("matrix_speed").get())){cycleTime--;multiplier+=.01F;}if(b.is(SimpleContent.BLOCKS.get("matrix_cost").get())){cycleTime++;multiplier-=.02F;}}cycleDelay=Math.max(1,cycleTime/2);if(type.equals("ancient")){multiplier-=.1F;replenish-=.1F;}else if(type.equals("eldritch")){multiplier+=.05F;replenish+=.2F;}
        for(var e:nearby(12)){var p=e.getBlockPos();if(thaumcraft.mirrors.MirrorEssence.source(e))jars.add(p.immutable());if(e instanceof InfusionPedestal pedestal&&pedestal.supported()&&(p.getX()!=worldPosition.getX()||p.getZ()!=worldPosition.getZ())&&Math.abs(p.getX()-worldPosition.getX())<=8&&Math.abs(p.getZ()-worldPosition.getZ())<=8&&p.getY()>=worldPosition.getY()-7&&p.getY()<=worldPosition.getY()+3){pedestals.add(p.immutable());if(pedestal.getBlockState().is(InfusionContent.ANCIENT.get()))multiplier-=.01F;else if(pedestal.getBlockState().is(InfusionContent.ELDRITCH.get()))multiplier+=.0025F;}}
        pedestalPositions=pedestals.stream().sorted(Comparator.comparingLong(BlockPos::asLong)).toList();jarPositions=jars.stream().sorted(Comparator.<BlockPos>comparingDouble(p->p.distSqr(worldPosition)).thenComparingLong(BlockPos::asLong)).toList();cost=Math.max(.5F,multiplier);
        var stabilizers=new TreeMap<Long,BlockPos>();for(var p:BlockPos.betweenClosed(worldPosition.offset(-8,-7,-8),worldPosition.offset(8,3,8)))if((p.getX()!=worldPosition.getX()||p.getZ()!=worldPosition.getZ())&&loaded(p)&&stabilizing(level.getBlockState(p).getBlock()))stabilizers.put(p.asLong(),p.immutable());
        var repeats=new HashMap<Block,Integer>();while(!stabilizers.isEmpty()){var p=stabilizers.pollFirstEntry().getValue();var opposite=new BlockPos(2*worldPosition.getX()-p.getX(),p.getY(),2*worldPosition.getZ()-p.getZ());if(!loaded(opposite))continue;var a=level.getBlockState(p).getBlock();var b=level.getBlockState(opposite).getBlock();float amount=stabilization(a);if(stabilityKey(a)==stabilityKey(b)){boolean occupancy=false;if(level.getBlockEntity(p) instanceof InfusionPedestal one&&level.getBlockEntity(opposite) instanceof InfusionPedestal two)occupancy=one.item().isEmpty()!=two.item().isEmpty();if(occupancy)replenish-=.1F;else{int n=repeats.getOrDefault(stabilityKey(a),0);replenish+=amount*(float)Math.pow(.75,n);repeats.put(stabilityKey(a),n+1);}}else replenish-=Math.max(amount,stabilization(b));stabilizers.remove(opposite.asLong());}
    }
    private Block stabilityKey(Block b){return b instanceof AbstractSkullBlock?Blocks.SKELETON_SKULL:b;}
    private boolean stabilizing(Block b){return b==InfusionContent.STABILIZER.get()||b==InfusionContent.INLAY.get()||b instanceof AbstractSkullBlock||b instanceof InfusionBlock i&&i.kind==InfusionBlock.Kind.PEDESTAL||b instanceof LuminousBlock light&&!light.isNitor();}
    private float stabilization(Block b){return b==InfusionContent.STABILIZER.get()?.25F:b==InfusionContent.INLAY.get()?.025F:b instanceof InfusionBlock i&&i.kind==InfusionBlock.Kind.PEDESTAL?b==InfusionContent.ELDRITCH.get()?.1F:0:.1F;}
    public boolean start(Player player){
        if(!(level instanceof ServerLevel server)||!supported()||job!=null||!getBlockState().getValue(InfusionBlock.ACTIVE)||style().isEmpty()||!ResearchService.knows(player,"INFUSION"))return false;surroundings();var center=central();if(center==null||center.item().isEmpty())return false;
        var occupied=pedestalPositions.stream().filter(p->level.getBlockEntity(p) instanceof InfusionPedestal t&&!t.item().isEmpty()).toList();var input=new InfusionInput(center.item(),occupied.stream().map(p->((InfusionPedestal)level.getBlockEntity(p)).item()).toList());
        var recipes=server.recipeAccess().getRecipes().stream().filter(h->h.value() instanceof InfusionRecipe r&&ResearchService.knows(player,r.research())&&r.matches(input,server)).sorted(Comparator.comparing(h->h.id().toString())).toList();if(recipes.isEmpty())return false;
        var holder=recipes.getFirst();var recipe=(InfusionRecipe)holder.value();int[] assignment=recipe.assignment(input);var offerings=new ArrayList<InfusionJob.Offering>();for(int slot:assignment){var p=occupied.get(slot);offerings.add(new InfusionJob.Offering(p.getX()-worldPosition.getX(),p.getY()-worldPosition.getY(),p.getZ()-worldPosition.getZ(),((InfusionPedestal)level.getBlockEntity(p)).item()));}
        var amounts=new TreeMap<String,Integer>();recipe.aspects(input).forEach((a,n)->{int amount=(int)(n*cost);if(amount>0)amounts.put(a,amount);});if(AspectAmounts.total(amounts)>20000)return false;
        var result=recipe.assemble(input);if(result.getCount()!=1)return false;if(InfusionEnchantments.Kind.of(recipe.enchantment())!=null)result=InfusionEnchantments.commitWarp(input.primary(),result,server.getRandom().nextInt(10));job=new InfusionJob(holder.id().toString(),player.getUUID().toString(),input.primary(),result,amounts,offerings,0,recipe.instability(input),AspectAmounts.total(amounts));countdown=0;offeringTicks=0;status="essence";flags(true);server.playSound(null,worldPosition,InfusionSounds.START.get(),net.minecraft.sounds.SoundSource.BLOCKS,.5F,1);changed();return true;
    }
    public void abort(){if(!supported())return;if(job!=null&&level instanceof ServerLevel server)server.playSound(null,worldPosition,InfusionSounds.FAIL.get(),net.minecraft.sounds.SoundSource.BLOCKS,1,.6F);job=null;offeringTicks=0;status="cancelled";flags(!style().isEmpty());changed();}
    @Override public void serverTick(){
        if(!(level instanceof ServerLevel server)||!supported()||!server.tickRateManager().runsNormally())return;ticks++;if(!structureLoaded()){status="unloaded";return;}
        if(ticks%100==0)surroundings();if(job!=null&&(!loaded(worldPosition.below(2))||job.offerings().stream().anyMatch(o->!loaded(o.position(worldPosition))))){status="unloaded";return;}
        if(ticks%20==0&&style().isEmpty()){if(job!=null)abort();flags(false);return;}
        if(!getBlockState().getValue(InfusionBlock.ACTIVE))return;
        if(job==null){if(ticks%5==0&&stability<25){stability=Math.min(25,stability+Math.max(.1F,replenish));changed();}return;}
        if(++countdown<cycleDelay){setChanged();return;}countdown=0;cycle(server);changed();
    }
    private void cycle(ServerLevel server){
        var center=central();if(center==null||!ItemStack.isSameItemSameComponents(center.item(),job.primary())){abort();return;}
        if(style().isEmpty()){abort();flags(false);return;}
        float modifier=stability>12.5?5:stability>=0?6:stability>-25?7:8;
        stability=Math.clamp(stability-server.getRandom().nextFloat()*job.instability()/modifier+replenish,-100,25);
        if(stability<0&&server.getRandom().nextInt(1500)<=Math.abs(stability)){mishap(server);stability=Math.min(25,stability+5+server.getRandom().nextFloat()*5);return;}
        if(!job.remaining().isEmpty()){
            for(var a:job.remaining().keySet().stream().sorted().toList())for(var p:jarPositions)if(loaded(p)&&thaumcraft.mirrors.MirrorEssence.drain(level,p,a)){job=job.drain(a);status="essence";particles(server,p);return;}
            status="missing_essence";stability=Math.max(-100,stability-.25F);return;
        }
        if(job.consumed()<job.offerings().size()){
            var offering=job.offerings().get(job.consumed());var candidates=new ArrayList<BlockPos>();candidates.add(offering.position(worldPosition));for(var p:pedestalPositions)if(!candidates.contains(p))candidates.add(p);
            for(var pos:candidates)if(loaded(pos)&&level.getBlockEntity(pos) instanceof InfusionPedestal pedestal&&pedestal.supported()&&ItemStack.isSameItemSameComponents(pedestal.item(),offering.item())){
                if(offeringTicks==0)particles(server,pos);status="ingredients";
                if(offeringTicks<5){offeringTicks++;return;}
                var remainder=AlchemyContent.remainder(pedestal.item());if(remainder.getCount()>1){status="invalid_remainder";return;}
                if(pedestal.setItem(remainder)){job=job.consume();offeringTicks=0;surroundings();particles(server,pos);}return;
            }
            offeringTicks=0;status="missing_item";stability=Math.max(-100,stability-.25F);return;
        }
        var output=job.result().copy();var player=server.getServer().getPlayerList().getPlayer(UUID.fromString(job.owner()));job=null;center.setItem(output);status="complete";offeringTicks=0;flags(true);server.playSound(null,worldPosition,InfusionSounds.COMPLETE.get(),net.minecraft.sounds.SoundSource.BLOCKS,.5F,1);if(player!=null)ResearchService.recordCraft(player,output);server.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,worldPosition.getX()+.5,worldPosition.getY(),worldPosition.getZ()+.5,30,.5,.5,.5,.1);
    }
    private void particles(ServerLevel server,BlockPos from){var target=Vec3.atCenterOf(worldPosition);var source=Vec3.atCenterOf(from).add(0,.6,0);int steps=Math.max(2,(int)Math.ceil(source.distanceTo(target)*3));for(int n=0;n<=steps;n++){var at=source.lerp(target,(double)n/steps);server.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,at.x,at.y,at.z,1,.02,.02,.02,0);}server.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,from.getX()+.5,from.getY()+1.1,from.getZ()+.5,5,.1,.1,.1,.1);}
    private void mishap(ServerLevel server){status=InfusionMishaps.apply(server,worldPosition,pedestalPositions,server.getRandom().nextInt(24))?"mitigated":"mishap";}

    public InteractionResult interact(Player player,InteractionHand hand){var held=player.getItemInHand(hand);if(held.is(thaumcraft.crafting.ArcaneContent.SALIS.get()))return InteractionResult.PASS;if(held.is(ResearchContent.METER.get()))return InteractionResult.PASS;if(level.isClientSide())return InteractionResult.SUCCESS;if(!supported()){player.sendSystemMessage(Component.translatable("alchemy.thaumcraft.invalid"));return InteractionResult.FAIL;}if(player.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)>64)return InteractionResult.FAIL;
        if(player.isShiftKeyDown()&&held.getItem() instanceof thaumcraft.thaumaturgy.CasterItem&&job!=null){if(!job.owner().equals(player.getUUID().toString()))return InteractionResult.FAIL;abort();return InteractionResult.SUCCESS;}
        if(held.getItem() instanceof thaumcraft.thaumaturgy.CasterItem&&job==null&&start(player))return InteractionResult.SUCCESS;
        player.sendSystemMessage(Component.translatable("infusion.thaumcraft.status",Component.translatable("infusion.thaumcraft.state."+status),Math.round(stability*100)/100F,Math.round(replenish*1000)/1000F,job==null?0:AspectAmounts.total(job.remaining()),job==null?0:job.offerings().size()-job.consumed()));return InteractionResult.SUCCESS;
    }
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(unsupported==null)out.store("infusion",InfusionState.CODEC,snapshot());else out.store("infusion",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupported));}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);unsupported=null;job=null;stability=0;countdown=0;offeringTicks=0;var raw=in.read("infusion",Codec.PASSTHROUGH);if(raw.isPresent()){var tag=raw.get().convert(NbtOps.INSTANCE).getValue();var parsed=InfusionState.CODEC.parse(raw.get()).result();if(parsed.isPresent()){var s=parsed.get();job=s.job().orElse(null);stability=s.stability();countdown=s.countdown();offeringTicks=s.offeringTicks();}else unsupported=tag.copy();}status=job==null?"idle":"essence";ticks=99;}
}
