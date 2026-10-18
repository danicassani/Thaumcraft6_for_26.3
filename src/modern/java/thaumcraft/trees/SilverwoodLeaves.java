package thaumcraft.trees;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.aura.AuraManager;
public final class SilverwoodLeaves extends TintedParticleLeavesBlock{
    public SilverwoodLeaves(Properties p){super(.01F,p);}
    @Override protected boolean isRandomlyTicking(BlockState state){return !state.getValue(PERSISTENT)||super.isRandomlyTicking(state);}
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random){if(!state.getValue(PERSISTENT)){var c=AuraManager.get(level,pos);if(c!=null&&c.vis()<c.base())AuraManager.addVis(level,pos,Math.min(.01F,c.base()-c.vis()));}super.randomTick(state,level,pos,random);}
}
