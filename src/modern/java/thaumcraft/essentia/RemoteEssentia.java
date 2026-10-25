package thaumcraft.essentia;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import thaumcraft.alchemy.AlchemyMachine;
import thaumcraft.mirrors.MirrorEntity;
/** The original forward prism: lateral offsets +/-16, forward depth 0..15. */
public final class RemoteEssentia {
 public static boolean contains(BlockPos origin,BlockPos at,Direction facing){int x=at.getX()-origin.getX(),y=at.getY()-origin.getY(),z=at.getZ()-origin.getZ();int depth=x*facing.getStepX()+y*facing.getStepY()+z*facing.getStepZ();return !at.equals(origin)&&Math.abs(x)<=16&&Math.abs(y)<=16&&Math.abs(z)<=16&&depth>=0&&depth<16;}
 public record Endpoint(AlchemyMachine jar,MirrorEntity mirror,BlockPos anchor){
  public boolean valid(){var l=jar.getLevel();return l instanceof ServerLevel&&l.hasChunkAt(jar.getBlockPos())&&l.getBlockEntity(jar.getBlockPos())==jar&&jar.jar()&&jar.supported()&&!jar.braced()&&(mirror==null||mirror.remoteEssentiaJars().contains(jar));}
  public boolean insert(String aspect,boolean simulate){if(!valid()||!jar.insertEssence(aspect,1,simulate))return false;if(!simulate&&mirror!=null)mirror.recordEssentiaTransfer();return true;}
  public boolean extract(String aspect,boolean simulate){if(!valid()||!jar.extractEssence(aspect,1,simulate))return false;if(!simulate&&mirror!=null)mirror.recordEssentiaTransfer();return true;}
 }
 public static List<Endpoint> targets(ServerLevel level,BlockPos origin,Direction facing){var found=new ArrayList<Endpoint>();for(int x=(origin.getX()-16)>>4;x<=(origin.getX()+16)>>4;x++)for(int z=(origin.getZ()-16)>>4;z<=(origin.getZ()+16)>>4;z++){var chunk=level.getChunkSource().getChunkNow(x,z);if(chunk==null)continue;for(var be:chunk.getBlockEntities().values()){if(!contains(origin,be.getBlockPos(),facing))continue;if(be instanceof AlchemyMachine jar&&jar.jar()&&jar.supported()&&!jar.braced())found.add(new Endpoint(jar,null,jar.getBlockPos()));else if(be instanceof MirrorEntity mirror&&mirror.essence()&&mirror.supported())for(var jar:mirror.remoteEssentiaJars())found.add(new Endpoint(jar,mirror,mirror.getBlockPos()));}}found.sort(Comparator.comparingDouble((Endpoint p)->p.anchor.distSqr(origin)).thenComparingLong(p->p.anchor.asLong()).thenComparingLong(p->p.jar.getBlockPos().asLong()));var seen=Collections.newSetFromMap(new IdentityHashMap<AlchemyMachine,Boolean>());return found.stream().filter(p->seen.add(p.jar)).toList();}
 private RemoteEssentia(){}
}
