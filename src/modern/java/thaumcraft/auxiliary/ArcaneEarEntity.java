package thaumcraft.auxiliary;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.*;
import thaumcraft.devices.AuraDeviceEntity;
public final class ArcaneEarEntity extends AuraDeviceEntity {
 private int note,pulse;private long lastNoteTick=Long.MIN_VALUE;
 public ArcaneEarEntity(BlockPos p,BlockState s){super(AuxiliaryContent.EAR_TYPE.get(),p,s);}
 public int note(){return note;}public int pulse(){return pulse;}
 public boolean supportLoaded(){return level!=null&&level.hasChunkAt(worldPosition.relative(getBlockState().getValue(ArcaneEarBlock.FACING).getOpposite()));}
 public NoteBlockInstrument instrument(){if(!supportLoaded())return NoteBlockInstrument.HARP;var back=worldPosition.relative(getBlockState().getValue(ArcaneEarBlock.FACING).getOpposite());return level.getBlockState(back).instrument();}
 public void tune(){note=(note+1)%25;sync();if(level instanceof ServerLevel l){var i=instrument();l.playSound(null,worldPosition,i.getSoundEvent().value(),net.minecraft.sounds.SoundSource.BLOCKS,3,net.minecraft.world.level.block.NoteBlock.getPitchFromNote(note));l.sendParticles(net.minecraft.core.particles.ParticleTypes.NOTE,worldPosition.getX()+.5,worldPosition.getY()+.6,worldPosition.getZ()+.5,0,note/24.0,0,0,1);}}
 public void activate(){if(!(level instanceof ServerLevel)||isRemoved()||lastNoteTick==level.getGameTime())return;lastNoteTick=level.getGameTime();boolean toggle=((ArcaneEarBlock)getBlockState().getBlock()).toggle;pulse=toggle?0:10;setActive(toggle?!getBlockState().getValue(ArcaneEarBlock.ACTIVE):true);sync();}
 private void setActive(boolean active){if(getBlockState().getValue(ArcaneEarBlock.ACTIVE)!=active){level.setBlockAndUpdate(worldPosition,getBlockState().setValue(ArcaneEarBlock.ACTIVE,active));level.updateNeighborsAt(worldPosition,getBlockState().getBlock());if(supportLoaded())level.updateNeighborsAt(worldPosition.relative(getBlockState().getValue(ArcaneEarBlock.FACING).getOpposite()),getBlockState().getBlock());}}
 public static void hear(ServerLevel l,BlockPos source,int note,NoteBlockInstrument instrument){for(int x=(source.getX()-64)>>4;x<=(source.getX()+64)>>4;x++)for(int z=(source.getZ()-64)>>4;z<=(source.getZ()+64)>>4;z++){var chunk=l.getChunkSource().getChunkNow(x,z);if(chunk!=null)for(var be:chunk.getBlockEntities().values())if(be instanceof ArcaneEarEntity ear&&!ear.isRemoved()&&ear.getBlockPos().distSqr(source)<=4096&&ear.note==note&&ear.supportLoaded()&&ear.instrument()==instrument)ear.activate();}}
 public void serverTick(){if(pulse>0){pulse--;setChanged();if(pulse==0)setActive(false);}}
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("note",note);out.putInt("pulse",pulse);}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);note=Math.clamp(in.getIntOr("note",0),0,24);pulse=Math.clamp(in.getIntOr("pulse",0),0,10);}
}
