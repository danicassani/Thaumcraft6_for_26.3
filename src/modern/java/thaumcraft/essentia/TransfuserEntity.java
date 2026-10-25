package thaumcraft.essentia;
import java.util.*;
import com.mojang.serialization.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import thaumcraft.crystals.CrystalAspects;
public final class TransfuserEntity extends thaumcraft.devices.AuraDeviceEntity implements EssentiaTransport {
 private TransfuserState state=TransfuserState.EMPTY;private Tag unsupported;
 public TransfuserEntity(BlockPos pos,BlockState block){super(EssentiaContent.TRANSFUSER_TYPE.get(),pos,block);}
 public boolean filling(){return ((TransfuserBlock)getBlockState().getBlock()).filling;}
 public Direction facing(){return getBlockState().getValue(TransfuserBlock.FACING);}
 public boolean supported(){return unsupported==null;}public TransfuserState snapshot(){return state;}
 private void update(TransfuserState value){state=value;sync();}
 public boolean connects(Direction face){return supported()&&face==facing().getOpposite();}
 public boolean canInput(Direction face){return filling()&&connects(face)&&state.pending().isEmpty();}
 public boolean canOutput(Direction face){return !filling()&&connects(face);}
 public Map<String,Integer> contents(Direction face){return supported()&&!filling()&&!state.pending().isEmpty()?Map.of(state.pending(),1):Map.of();}
 public String suctionType(Direction face){return "";}
 public int suction(Direction face){return canInput(face)?128:0;}
 public int minimumSuction(){return 0;}
 private List<RemoteEssentia.Endpoint> targets(){return level instanceof ServerLevel l?RemoteEssentia.targets(l,worldPosition,facing()):List.of();}
 private RemoteEssentia.Endpoint destination(String aspect,EssentiaTransport donor){return targets().stream().filter(e->e.jar()!=donor&&e.insert(aspect,true)).min(Comparator.comparingInt(e->e.jar().total()==0?1:0)).orElse(null);}
 public boolean insert(String aspect,Direction face,boolean simulate){if(!canInput(face)||!CrystalAspects.COLORS.containsKey(aspect)||destination(aspect,null)==null)return false;if(!simulate){update(state.reserve(aspect));deliverFill();}return true;}
 public boolean extract(String aspect,Direction face,boolean simulate){if(!canOutput(face)||state.pending().isEmpty()||!state.pending().equals(aspect))return false;if(!simulate)update(state.delivered());return true;}
 private void deliverFill(){if(state.pending().isEmpty())return;var target=destination(state.pending(),null);if(target!=null&&target.insert(state.pending(),false)){beam(worldPosition,target.anchor(),state.pending());update(state.delivered());}}
 private void fill(){if(!state.pending().isEmpty()){deliverFill();return;}var rear=facing().getOpposite();var source=EssentiaNetwork.neighbour(level,worldPosition,this,rear);if(source==null||!source.canOutput(facing())||source.suction(facing())>=128||source.minimumSuction()>128)return;for(var aspect:source.contents(facing()).keySet().stream().sorted().toList()){var target=destination(aspect,source);if(target==null||!source.extractFor(aspect,facing(),target.jar(),true))continue;if(source.extractFor(aspect,facing(),target.jar(),false)){update(state.reserve(aspect));if(target.insert(aspect,false)){beam(worldPosition,target.anchor(),aspect);update(state.delivered());}return;}}}
 private void empty(){var rear=facing().getOpposite();var target=EssentiaNetwork.neighbour(level,worldPosition,this,rear);if(target==null||!target.canInput(facing())||target.suction(facing())<=0)return;String wanted=target.suctionType(facing());if(wanted.isEmpty())return;if(!state.pending().isEmpty()){if(wanted.equals(state.pending())&&target.insert(wanted,facing(),true)&&target.insert(wanted,facing(),false))update(state.delivered());return;}if(!target.insert(wanted,facing(),true))return;for(var source:targets()){if(source.jar()==target||!source.extract(wanted,true))continue;if(source.extract(wanted,false)){update(state.reserve(wanted));beam(source.anchor(),worldPosition,wanted);if(target.insert(wanted,facing(),false))update(state.delivered());return;}}}
 public void serverTick(){if(!(level instanceof ServerLevel l)||!supported()||!l.tickRateManager().runsNormally())return;state=state.tick();setChanged();if(state.phase()!=0)return;if(filling())fill();else empty();}
 private void beam(BlockPos from,BlockPos to,String aspect){if(!(level instanceof ServerLevel l))return;var start=Vec3.atCenterOf(from);var end=Vec3.atCenterOf(to);int steps=Math.min(24,Math.max(3,(int)(start.distanceTo(end)*2)));var particle=new net.minecraft.core.particles.DustParticleOptions(0xff000000|CrystalAspects.COLORS.getOrDefault(aspect,0xffffff),.7F);for(int n=0;n<=steps;n++){var at=start.lerp(end,n/(double)steps);l.sendParticles(particle,at.x,at.y,at.z,1,.02,.02,.02,0);}}
 public ItemStack droppedItem(){var item=new ItemStack(getBlockState().getBlock());if((!supported()||!state.pending().isEmpty())&&level!=null)item.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,net.minecraft.world.item.component.TypedEntityData.of(getType(),saveWithFullMetadata(level.registryAccess())));return item;}
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.store("transfuser",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupported==null?TransfuserState.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow():unsupported));}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);state=TransfuserState.EMPTY;unsupported=null;var raw=in.read("transfuser",Codec.PASSTHROUGH);if(raw.isPresent()){var tag=raw.get().convert(NbtOps.INSTANCE).getValue();var parsed=TransfuserState.CODEC.parse(NbtOps.INSTANCE,tag).result();if(parsed.isPresent())state=parsed.get();else unsupported=tag.copy();}}
}
