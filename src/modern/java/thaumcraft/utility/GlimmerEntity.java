package thaumcraft.utility;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
public final class GlimmerEntity extends BlockEntity {
 private boolean owned;private BlockPos owner;private String identity="";
 public GlimmerEntity(BlockPos p,BlockState s){super(UtilityContent.LIGHT.get(),p,s);}public boolean owned(){return owned;}public void bind(BlockPos p,String id){owned=true;owner=p.immutable();identity=id;setChanged();if(level instanceof ServerLevel l)l.scheduleTick(worldPosition,getBlockState().getBlock(),20);}public boolean belongs(BlockPos p,String id){return p.equals(owner)&&id.equals(identity);}
 public void validateOwner(){if(!(level instanceof ServerLevel l)||!owned)return;if(owner==null){l.removeBlock(worldPosition,false);return;}if(!l.hasChunkAt(owner))return;if(!(l.getBlockEntity(owner) instanceof LampEntity lamp)||!lamp.identity().equals(identity)||!lamp.kind().equals("lamp_arcane")||!lamp.running())l.removeBlock(worldPosition,false);}
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putBoolean("owned",owned);if(owner!=null)out.store("owner",BlockPos.CODEC,owner);out.putString("identity",identity);}protected void loadAdditional(ValueInput in){super.loadAdditional(in);owned=in.getBooleanOr("owned",false);owner=in.read("owner",BlockPos.CODEC).orElse(null);identity=in.getStringOr("identity","");}
}
