package thaumcraft.thaumatorium;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.alchemy.AlchemyContent;
import thaumcraft.content.SimpleContent;
import thaumcraft.research.ResearchService;
public final class ThaumatoriumStructure{
 private record Key(ServerLevel level,BlockPos pos){}
 private static final Set<Key> CHANGING=new HashSet<>();
 public static boolean assemble(ServerLevel l,Player user,BlockPos clicked,ItemStack salis){if(!user.mayBuild()||!ResearchService.knows(user,"THAUMATORIUM"))return false;for(int n=0;n<2;n++){var p=clicked.below(n);if(!l.hasChunkAt(p.below())||!l.hasChunkAt(p.above())||!l.getBlockState(p.below()).is(AlchemyContent.CRUCIBLE.get())||!l.getBlockState(p).is(SimpleContent.BLOCKS.get("metal_alchemical").get())||!l.getBlockState(p.above()).is(SimpleContent.BLOCKS.get("metal_alchemical").get()))continue;
  for(var q:List.of(p,p.above())){if(!l.mayInteract(user,q)||!user.mayUseItemAt(q,Direction.UP,salis))return false;var e=new net.minecraftforge.event.level.BlockEvent.BreakEvent(l,q,l.getBlockState(q),user,net.minecraftforge.common.util.Result.DEFAULT);if(net.minecraftforge.event.level.BlockEvent.BreakEvent.BUS.post(e)||e.getResult().isDenied())return false;}
  var key=new Key(l,p);CHANGING.add(key);try{var front=user.getDirection().getOpposite();l.setBlockAndUpdate(p,ThaumatoriumContent.BASE.get().defaultBlockState().setValue(ThaumatoriumBlock.FACING,front));l.setBlockAndUpdate(p.above(),ThaumatoriumContent.TOP.get().defaultBlockState().setValue(ThaumatoriumBlock.FACING,front));}finally{CHANGING.remove(key);}if(!user.getAbilities().instabuild)salis.shrink(1);ResearchService.recordCraft(user,new ItemStack(ThaumatoriumContent.ICON.get()));return true;
 }return false;}
 public static void dismantle(ServerLevel l,BlockPos p,BlockPos removed){var key=new Key(l,p);if(!CHANGING.add(key))return;try{for(var q:List.of(p,p.above()))if(!q.equals(removed)&&l.hasChunkAt(q)&&l.getBlockState(q).getBlock() instanceof ThaumatoriumBlock)l.setBlockAndUpdate(q,SimpleContent.BLOCKS.get("metal_alchemical").get().defaultBlockState());}finally{CHANGING.remove(key);}}
}
