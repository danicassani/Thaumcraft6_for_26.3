package thaumcraft.mirrors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import thaumcraft.alchemy.AlchemyMachine;
public final class MirrorEssence {
 private MirrorEssence(){}
 public static boolean source(net.minecraft.world.level.block.entity.BlockEntity entity){return entity instanceof AlchemyMachine jar&&jar.jar()&&!jar.braced()&&jar.supported()||entity instanceof MirrorEntity mirror&&mirror.essence()&&mirror.supported();}
 public static boolean drain(Level level,BlockPos pos,String aspect){if(!level.hasChunkAt(pos))return false;var entity=level.getBlockEntity(pos);return entity instanceof AlchemyMachine jar&&jar.jar()&&!jar.braced()&&jar.extractEssence(aspect,1,false)||entity instanceof MirrorEntity mirror&&mirror.essence()&&mirror.transferEssence(aspect,false,false);}
}
