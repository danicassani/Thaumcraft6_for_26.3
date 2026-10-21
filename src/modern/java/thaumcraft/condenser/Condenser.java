package thaumcraft.condenser;
import com.mojang.serialization.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import thaumcraft.aura.AuraManager;
import thaumcraft.essentia.*;
public final class Condenser extends thaumcraft.devices.AuraDeviceEntity implements EssentiaTransport{
 private CondenserState state=CondenserState.EMPTY;private Tag unsupported;private LatticeNetwork.Result network;private int ticks;
 public Condenser(BlockPos p,BlockState s){super(CondenserContent.TYPE.get(),p,s);}
 public CondenserState snapshot(){return state;}public boolean supported(){return unsupported==null;}
 public boolean powered(){return level!=null&&level.hasNeighborSignal(worldPosition);}
 private int kind(BlockPos p){if(!level.hasChunkAt(p))return LatticeNetwork.UNLOADED;var s=level.getBlockState(p);return s.getBlock() instanceof LatticeBlock b?b.dirty?LatticeNetwork.DIRTY:LatticeNetwork.CLEAN:s.is(CondenserContent.BASE.get())?LatticeNetwork.BASE:LatticeNetwork.AIR;}
 public LatticeNetwork.Result refresh(){var next=LatticeNetwork.scan(worldPosition,this::kind);if(network!=null&&network.interval()!=next.interval()&&next.interval()>0)update(state.retime(next.interval()));network=next;return next;}
 public LatticeNetwork.Result network(){return network==null?refresh():network;}
 public boolean running(){return supported()&&!powered()&&network().active();}
 private void update(CondenserState next){if(next.equals(state))return;int old=state.output();state=next;sync();if(level!=null&&old!=state.output())level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}
 public boolean connects(Direction d){return supported()&&d!=null&&d!=Direction.UP;}
 public boolean canInput(Direction d){return connects(d)&&d!=Direction.DOWN&&running()&&state.fuel()<100;}
 public boolean canOutput(Direction d){return connects(d)&&d==Direction.DOWN;}
 public Map<String,Integer> contents(Direction d){return canOutput(d)&&state.output()>0?Map.of("vitium",state.output()):Map.of();}
 public String suctionType(Direction d){return "";}public int suction(Direction d){return canInput(d)?128:0;}public int minimumSuction(){return 0;}
 public boolean insert(String aspect,Direction d,boolean simulate){if(!canInput(d)||aspect.equals("vitium")||!thaumcraft.crystals.CrystalAspects.COLORS.containsKey(aspect))return false;if(!simulate)update(state.accept());return true;}
 public boolean extract(String aspect,Direction d,boolean simulate){if(!canOutput(d)||!aspect.equals("vitium")||state.output()==0)return false;if(!simulate)update(state.extract());return true;}
 public boolean dirtyOne(){var clean=refresh().clean();if(clean.isEmpty()||!(level instanceof ServerLevel l))return false;int n=l.getRandom().nextInt(clean.size());if(n==0)n=l.getRandom().nextInt(clean.size());var at=clean.get(n);var s=l.getBlockState(at);if(!s.is(CondenserContent.CLEAN.get()))return false;var next=CondenserContent.DIRTY.get().defaultBlockState();for(var side:LatticeBlock.SIDES)next=next.setValue(side,s.getValue(side));boolean changed=l.setBlockAndUpdate(at,next);refresh();return changed;}
 private void fill(){for(var d:Direction.Plane.HORIZONTAL){if(!canInput(d))return;if(EssentiaNetwork.pull(level,worldPosition,this,d))continue;var source=EssentiaNetwork.neighbour(level,worldPosition,this,d);var opposite=d.getOpposite();if(source!=null&&source.canOutput(opposite)&&source.suction(opposite)<128&&source.minimumSuction()<=128&&source.contents(opposite).getOrDefault("vitium",0)>0)dirtyOne();}}
 public void serverTick(){if(!(level instanceof ServerLevel l)||!l.tickRateManager().runsNormally())return;ticks++;if(network==null||ticks%20==0)refresh();if(!supported()){visual(false);return;}if(running()&&ticks%5==0)fill();var graph=network();boolean eligible=running()&&state.fuel()>=graph.cost()&&state.output()<100&&AuraManager.drainFlux(l,worldPosition,1,true)>=1;visual(eligible);if(!eligible)return;var advanced=state.advance(true);state=advanced;setChanged();if(ticks%20==0)sync();if(state.progress()>=graph.interval()){graph=refresh();if(!running()||state.fuel()<graph.cost()||state.progress()<graph.interval()||AuraManager.drainFlux(l,worldPosition,1,true)<1)return;if(AuraManager.drainFlux(l,worldPosition,1,false)!=1)throw new IllegalStateException("Aura changed during server-thread condensation");update(state.complete(graph.cost(),graph.interval()));if(l.getRandom().nextInt(50)==0)dirtyOne();}
  if(!graph.clean().isEmpty()&&ticks%Math.max(3,graph.interval()/50)==0){var at=graph.clean().get(l.getRandom().nextInt(graph.clean().size()));l.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,at.getX()+.5,at.getY()+.5,at.getZ()+.5,2,.2,.2,.2,.02);}
 }
 private void visual(boolean active){if(level==null||level.isClientSide())return;var old=getBlockState();var next=old.setValue(CondenserBlock.ENABLED,supported()&&!powered()).setValue(CondenserBlock.ACTIVE,active);if(next!=old)level.setBlockAndUpdate(worldPosition,next);}
 public void inspect(Player p){if(!supported()){p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("alchemy.thaumcraft.invalid"));return;}var g=refresh();String status=!g.complete()?"unloaded":g.shared()?"shared":!g.active()?"clogged":powered()?"paused":"ready";p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("condenser.thaumcraft.status",state.fuel(),state.output(),g.cost(),state.progress(),g.interval(),g.cells().size(),net.minecraft.network.chat.Component.translatable("condenser.thaumcraft."+status)));}
 public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(level instanceof ServerLevel l&&supported()&&(state.fuel()>0||state.output()>0))AuraManager.addFlux(l,p,state.fuel()+state.output());}
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(unsupported==null)out.store("condenser",CondenserState.CODEC,state);else out.store("condenser",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupported));}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);network=null;unsupported=null;state=CondenserState.EMPTY;var raw=in.read("condenser",Codec.PASSTHROUGH);if(raw.isPresent()){var t=raw.get().convert(NbtOps.INSTANCE).getValue();var decoded=CondenserState.CODEC.parse(NbtOps.INSTANCE,t).result();if(decoded.isPresent())state=decoded.get();else unsupported=t.copy();}}
}
