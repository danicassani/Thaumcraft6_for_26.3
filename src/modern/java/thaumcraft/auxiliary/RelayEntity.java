package thaumcraft.auxiliary;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import thaumcraft.devices.AuraDeviceEntity;
public final class RelayEntity extends AuraDeviceEntity {
 private int input=1,output=15;
 public RelayEntity(BlockPos p,BlockState s){super(AuxiliaryContent.RELAY_TYPE.get(),p,s);}
 public int input(){return input;}public int output(){return output;}
 public void adjust(boolean in){if(in)input=input%15+1;else output=output%15+1;sync();if(level!=null){((RelayBlock)getBlockState().getBlock()).notifyOutput(level,worldPosition,getBlockState());((RelayBlock)getBlockState().getBlock()).reevaluate(level,worldPosition,getBlockState());}}
 public void serverTick(){}
 protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("input",input);out.putInt("output",output);}
 protected void loadAdditional(ValueInput in){super.loadAdditional(in);input=Math.clamp(in.getIntOr("input",1),1,15);output=Math.clamp(in.getIntOr("output",15),1,15);}
}
