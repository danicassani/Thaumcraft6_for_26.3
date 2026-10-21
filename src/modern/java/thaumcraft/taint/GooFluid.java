package thaumcraft.taint;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraftforge.fluids.ForgeFlowingFluid;
public final class GooFluid extends ForgeFlowingFluid.Flowing{
 public GooFluid(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(LEVEL,8).setValue(FALLING,false));}
 public boolean isSource(FluidState f){return f.getAmount()==8;}
 protected BlockState createLegacyBlock(FluidState f){return TaintContent.GOO.get().defaultBlockState().setValue(GooBlock.QUANTA,f.getAmount());}
 public void tick(ServerLevel l,BlockPos p,BlockState s,FluidState f){if(s.is(TaintContent.GOO.get()))GooBlock.step(l,p,l.getRandom());}
}
