package thaumcraft.essentia;
import com.mojang.serialization.*;
import java.util.Map;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import thaumcraft.aura.AuraManager;
/** Original vertical separator: one compound becomes one randomly selected component. */
public final class CentrifugeEntity extends thaumcraft.devices.AuraDeviceEntity implements EssentiaTransport{
 private CentrifugeState state=CentrifugeState.EMPTY;private Tag unsupported;private int ticks;
 public CentrifugeEntity(BlockPos p,BlockState s){super(EssentiaContent.CENTRIFUGE_TYPE.get(),p,s);}
 public CentrifugeState snapshot(){return state;}public boolean supported(){return unsupported==null;}
 public boolean powered(){return level!=null&&level.hasNeighborSignal(worldPosition);}
 private void update(CentrifugeState next){if(next.equals(state))return;state=next;sync();if(level!=null)level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}
 @Override public boolean connects(Direction face){return supported()&&(face==Direction.DOWN||face==Direction.UP);}
 @Override public boolean canInput(Direction face){return supported()&&face==Direction.DOWN&&!powered()&&state.input().isEmpty();}
 @Override public boolean canOutput(Direction face){return supported()&&face==Direction.UP;}
 @Override public int suction(Direction face){return supported()&&face==Direction.DOWN&&!powered()?(state.input().isEmpty()?128:64):0;}
 @Override public String suctionType(Direction face){return "";}
 @Override public int minimumSuction(){return 0;}
 @Override public Map<String,Integer> contents(Direction face){return supported()&&!state.output().isEmpty()?Map.of(state.output(),1):Map.of();}
 @Override public boolean insert(String aspect,Direction face,boolean simulate){if(!canInput(face)||!CentrifugeState.compound(aspect))return false;if(!simulate)update(state.accept(aspect));return true;}
 @Override public boolean extract(String aspect,Direction face,boolean simulate){if(!canOutput(face)||state.output().isEmpty()||!state.output().equals(aspect))return false;if(!simulate)update(state.drain());return true;}
 @Override public void serverTick(){if(!(level instanceof ServerLevel server)||!server.tickRateManager().runsNormally())return;ticks++;boolean active=supported()&&!powered()&&!state.input().isEmpty();if(supported()&&!powered()){
  if(state.input().isEmpty()&&state.output().isEmpty()&&ticks%5==0)EssentiaNetwork.pull(level,worldPosition,this,Direction.DOWN);
  if(!state.input().isEmpty())update(state.advance(false,state.remaining()<=1&&state.output().isEmpty()?server.getRandom().nextInt(2):0));
 }
 var block=getBlockState();var next=block.setValue(CentrifugeBlock.ACTIVE,active);if(active&&ticks%2==0)next=next.setValue(CentrifugeBlock.PHASE,(next.getValue(CentrifugeBlock.PHASE)+1)%8);if(next!=block)server.setBlockAndUpdate(worldPosition,next);
 }
 @Override public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(level instanceof ServerLevel server&&supported()){int stored=(state.input().isEmpty()?0:1)+(state.output().isEmpty()?0:1);if(stored>0)AuraManager.addFlux(server,p,stored);}}
 @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("centrifuge",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupported==null?CentrifugeState.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow():unsupported));}
 @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);unsupported=null;state=CentrifugeState.EMPTY;var raw=in.read("centrifuge",Codec.PASSTHROUGH);if(raw.isPresent()){Tag t=raw.get().convert(NbtOps.INSTANCE).getValue();var decoded=CentrifugeState.CODEC.parse(NbtOps.INSTANCE,t).result();if(decoded.isPresent())state=decoded.get();else unsupported=t.copy();}}
}
