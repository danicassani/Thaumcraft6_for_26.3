package thaumcraft.taint;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
/** Native source/flow fluids; pause before the native slope search can reach absent chunks. */
public final class HazardFluid{
 private static boolean ready(ServerLevel l,BlockPos p){for(int x=(p.getX()-5)>>4;x<=(p.getX()+5)>>4;x++)for(int z=(p.getZ()-5)>>4;z<=(p.getZ()+5)>>4;z++)if(!l.hasChunk(x,z))return false;return true;}
 public static final class Still extends ForgeFlowingFluid.Source{public Still(Properties p){super(p);}public void tick(ServerLevel l,BlockPos p,BlockState b,FluidState f){if(!ready(l,p)){l.scheduleTick(p,this,10);return;}super.tick(l,p,b,f);}protected void spreadTo(LevelAccessor l,BlockPos p,BlockState b,Direction d,FluidState f){if(l instanceof ServerLevel s&&TaintWorld.place(s,p,p.relative(d.getOpposite()),f.createLegacyBlock(),null))return;}}
 public static final class Flow extends ForgeFlowingFluid.Flowing{public Flow(Properties p){super(p);}public void tick(ServerLevel l,BlockPos p,BlockState b,FluidState f){if(!ready(l,p)){l.scheduleTick(p,this,10);return;}super.tick(l,p,b,f);}protected void spreadTo(LevelAccessor l,BlockPos p,BlockState b,Direction d,FluidState f){if(l instanceof ServerLevel s)TaintWorld.place(s,p,p.relative(d.getOpposite()),f.createLegacyBlock(),null);}}
}
