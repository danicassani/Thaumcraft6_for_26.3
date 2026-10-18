package thaumcraft.essentia;

import com.mojang.serialization.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.alchemy.*;
import thaumcraft.aura.AuraManager;
import thaumcraft.crystals.CrystalAspects;

public final class TubeEntity extends thaumcraft.devices.AuraDeviceEntity implements EssentiaTransport{
    private TubeState state=TubeState.EMPTY;
    private Tag unsupported;
    private int ticks,suction;
    private String suctionType="";
    public TubeEntity(BlockPos p,BlockState s){super(EssentiaContent.TYPE.get(),p,s);}
    public TubeBlock.Kind kind(){return ((TubeBlock)getBlockState().getBlock()).kind;}
    public TubeState snapshot(){return state;}
    public boolean supported(){return unsupported==null;}
    public int total(){return AspectAmounts.total(state.aspects());}
    public int capacity(){return kind()==TubeBlock.Kind.BUFFER?10:1;}
    public String filter(){return state.filter();}
    public int vent(){return state.vent();}
    private Direction facing(){return getBlockState().getValue(TubeBlock.FACING);}
    private boolean flows(){return supported()&&state.vent()==0&&(kind()!=TubeBlock.Kind.VALVE||state.flow());}
    private void update(Map<String,Integer> amounts,int open,List<Integer> chokes,String filter,boolean flow,boolean powered,int vent){state=new TubeState(1,amounts,open,chokes,filter,flow,powered,vent);sync();if(level!=null)level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}
    @Override public boolean connects(Direction face){return supported()&&face!=null&&(state.open()&(1<<face.ordinal()))!=0&&(kind()!=TubeBlock.Kind.VALVE||face!=facing());}
    @Override public boolean canInput(Direction face){return flows()&&connects(face)&&(kind()!=TubeBlock.Kind.ONEWAY||face!=facing());}
    @Override public boolean canOutput(Direction face){return flows()&&connects(face)&&(kind()!=TubeBlock.Kind.ONEWAY||face==facing());}
    @Override public Map<String,Integer> contents(Direction face){return supported()?state.aspects():Map.of();}
    @Override public String suctionType(Direction face){return kind()==TubeBlock.Kind.BUFFER?"":suctionType;}
    @Override public int suction(Direction face){if(!flows()||!connects(face))return 0;return kind()==TubeBlock.Kind.BUFFER?(state.chokes().get(face.ordinal())==2?0:1):suction;}
    @Override public int minimumSuction(){return 0;}
    @Override public boolean insert(String aspect,Direction face,boolean simulate){
        if(!canInput(face)||!CrystalAspects.COLORS.containsKey(aspect)||total()>=capacity()||!filter().isEmpty()&&!filter().equals(aspect)||kind()!=TubeBlock.Kind.BUFFER&&total()>0&&!state.aspects().containsKey(aspect))return false;
        if(!simulate)update(AspectAmounts.add(state.aspects(),Map.of(aspect,1)),state.open(),state.chokes(),filter(),state.flow(),state.powered(),state.vent());return true;
    }
    @Override public boolean extract(String aspect,Direction face,boolean simulate){
        if(!canOutput(face)||state.aspects().getOrDefault(aspect,0)<1)return false;
        if(kind()==TubeBlock.Kind.BUFFER){var receiver=EssentiaNetwork.neighbour(level,worldPosition,this,face);int demand=receiver==null?0:receiver.suction(face.getOpposite());for(var d:Direction.values())if(d!=face){var other=EssentiaNetwork.neighbour(level,worldPosition,this,d);if(other!=null&&other.canInput(d.getOpposite())&&(other.suctionType(d.getOpposite()).isEmpty()||other.suctionType(d.getOpposite()).equals(aspect))&&other.suction(d.getOpposite())>Math.max(demand,suction(d)))return false;}}
        if(!simulate)update(AspectAmounts.subtract(state.aspects(),Map.of(aspect,1)),state.open(),state.chokes(),filter(),state.flow(),state.powered(),state.vent());return true;
    }
    public void refreshConnections(){if(level==null||level.isClientSide())return;var next=getBlockState().setValue(TubeBlock.OPEN,state.flow());for(var d:Direction.values())next=next.setValue(TubeBlock.SIDES[d.ordinal()],EssentiaNetwork.neighbour(level,worldPosition,this,d)!=null);if(next!=getBlockState())level.setBlockAndUpdate(worldPosition,next);}
    @Override public void serverTick(){
        if(!(level instanceof ServerLevel server)||!supported()||!server.tickRateManager().runsNormally())return;ticks++;
        if(ticks%5==0){
            if(kind()==TubeBlock.Kind.VALVE){boolean powered=server.hasNeighborSignal(worldPosition);boolean flow=state.flow();if(powered!=state.powered())flow=!powered;if(powered!=state.powered()||flow!=state.flow())update(state.aspects(),state.open(),state.chokes(),filter(),flow,powered,state.vent());}
            refreshConnections();
        }
        if(state.vent()>0){state=new TubeState(1,state.aspects(),state.open(),state.chokes(),filter(),state.flow(),state.powered(),state.vent()-1);setChanged();if(ticks%10==0)server.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5,3,.15,.15,.15,.01);if(state.vent()==0)sync();return;}
        if(!flows()){suction=0;suctionType="";return;}
        if(kind()!=TubeBlock.Kind.BUFFER&&ticks%2==0){calculateSuction();checkVenting();}
        if(ticks%5==0&&flows())for(var d:Direction.values())if(EssentiaNetwork.pull(level,worldPosition,this,d))break;
    }
    private void calculateSuction(){
        int next=0;String type="";
        for(var d:Direction.values()){
            if(kind()==TubeBlock.Kind.ONEWAY&&d!=facing())continue;
            var other=EssentiaNetwork.neighbour(level,worldPosition,this,d);if(other==null)continue;
            String demand=other.suctionType(d.getOpposite());
            if(!filter().isEmpty()&&!demand.isEmpty()&&!filter().equals(demand)||total()>0&&!demand.isEmpty()&&!state.aspects().containsKey(demand))continue;
            int strength=other.suction(d.getOpposite());int candidate=kind()==TubeBlock.Kind.RESTRICT?strength/2:strength-1;
            if(strength>0&&candidate>next){next=candidate;type=demand.isEmpty()?filter():demand;}
        }
        if(suction!=next||!suctionType.equals(type)){suction=next;suctionType=type;sync();}
    }
    private void checkVenting(){
        if(suction==0)return;
        for(var d:Direction.values()){
            var other=EssentiaNetwork.neighbour(level,worldPosition,this,d);if(other==null||other instanceof TubeEntity t&&t.kind()==TubeBlock.Kind.FILTER)continue;
            int s=other.suction(d.getOpposite());String type=other.suctionType(d.getOpposite());
            if((s==suction||s==suction-1)&&!suctionType.equals(type)){
                update(state.aspects(),state.open(),state.chokes(),filter(),state.flow(),state.powered(),40);
                level.playSound(null,worldPosition,net.minecraft.sounds.SoundEvents.LAVA_EXTINGUISH,net.minecraft.sounds.SoundSource.BLOCKS,.15F,1F);return;
            }
        }
    }
    public boolean setFilter(String aspect){if(!supported()||kind()!=TubeBlock.Kind.FILTER||!aspect.isEmpty()&&!CrystalAspects.COLORS.containsKey(aspect)||total()>0&&!aspect.isEmpty()&&!state.aspects().containsKey(aspect))return false;update(state.aspects(),state.open(),state.chokes(),aspect,state.flow(),state.powered(),state.vent());suction=0;suctionType="";return true;}
    public void toggleSide(Direction face){if(!supported())return;boolean open=(state.open()&(1<<face.ordinal()))==0;update(state.aspects(),state.open()^(1<<face.ordinal()),state.chokes(),filter(),state.flow(),state.powered(),state.vent());var p=worldPosition.relative(face);if(level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof TubeEntity t&&t.supported()){int mask=1<<face.getOpposite().ordinal();if(((t.state.open()&mask)!=0)!=open)t.update(t.state.aspects(),t.state.open()^mask,t.state.chokes(),t.filter(),t.state.flow(),t.state.powered(),t.state.vent());t.refreshConnections();}suction=0;suctionType="";refreshConnections();}
    public void cycleChoke(Direction face){if(!supported()||kind()!=TubeBlock.Kind.BUFFER)return;var chokes=new ArrayList<>(state.chokes());chokes.set(face.ordinal(),(chokes.get(face.ordinal())+1)%3);update(state.aspects(),state.open(),chokes,filter(),state.flow(),state.powered(),state.vent());}
    public void toggleValve(){if(!supported()||kind()!=TubeBlock.Kind.VALVE)return;update(state.aspects(),state.open(),state.chokes(),filter(),!state.flow(),state.powered(),state.vent());suction=0;suctionType="";refreshConnections();}
    public InteractionResult interact(Player p,InteractionHand hand,BlockHitResult hit){
        var held=p.getItemInHand(hand);if(held.is(thaumcraft.research.ResearchContent.METER.get()))return InteractionResult.PASS;
        if(level.isClientSide())return InteractionResult.SUCCESS;if(!supported()||p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)>64)return InteractionResult.FAIL;
        if(held.is(EssentiaContent.RESONATOR.get())){
            if(p.isShiftKeyDown()){
                var local=hit.getLocation().subtract(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5);
                if((kind()==TubeBlock.Kind.ONEWAY||kind()==TubeBlock.Kind.VALVE)&&Math.max(Math.abs(local.x),Math.max(Math.abs(local.y),Math.abs(local.z)))<=.26){level.setBlockAndUpdate(worldPosition,getBlockState().setValue(TubeBlock.FACING,Direction.values()[(facing().ordinal()+1)%6]));suction=0;suctionType="";refreshConnections();}
                else toggleSide(hit.getDirection());
            }else EssentiaContent.inspect(p,this,hit.getDirection());return InteractionResult.SUCCESS;
        }
        if(kind()==TubeBlock.Kind.FILTER){String aspect=AlchemyContent.aspect(held);if(p.isShiftKeyDown()&&held.isEmpty())return setFilter("")?InteractionResult.SUCCESS:InteractionResult.FAIL;if((held.is(AlchemyContent.PHIAL.get())||held.is(EssentiaContent.LABEL.get()))&&!aspect.isEmpty())return setFilter(aspect)?InteractionResult.SUCCESS:InteractionResult.FAIL;}
        if(held.isEmpty()&&kind()==TubeBlock.Kind.VALVE){toggleValve();return InteractionResult.SUCCESS;}
        if(held.isEmpty()&&p.isShiftKeyDown()&&kind()==TubeBlock.Kind.BUFFER){cycleChoke(hit.getDirection());return InteractionResult.SUCCESS;}
        if(held.isEmpty()){EssentiaContent.inspect(p,this,hit.getDirection());return InteractionResult.SUCCESS;}
        return InteractionResult.PASS;
    }
    @Override public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(level instanceof ServerLevel server&&supported()&&total()>0)AuraManager.addFlux(server,p,total());}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("essentia_network",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupported==null?TubeState.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow():unsupported));out.putInt("suction",suction);out.putString("suction_type",suctionType);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);unsupported=null;state=TubeState.EMPTY;var raw=in.read("essentia_network",Codec.PASSTHROUGH);if(raw.isPresent()){Tag tag=raw.get().convert(NbtOps.INSTANCE).getValue();var parsed=TubeState.CODEC.parse(NbtOps.INSTANCE,tag).result();if(parsed.isPresent()&&AspectAmounts.total(parsed.get().aspects())<=capacity()&&(kind()==TubeBlock.Kind.BUFFER||parsed.get().aspects().size()<=1)&&(parsed.get().filter().isEmpty()||kind()==TubeBlock.Kind.FILTER&&parsed.get().aspects().keySet().stream().allMatch(a->a.equals(parsed.get().filter()))))state=parsed.get();else unsupported=tag.copy();}suction=0;suctionType="";if(level!=null&&level.isClientSide()){suction=Math.clamp(in.getIntOr("suction",0),0,256);suctionType=in.getStringOr("suction_type","");}}
}
