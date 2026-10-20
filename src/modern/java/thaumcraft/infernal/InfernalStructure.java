package thaumcraft.infernal;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.research.ResearchService;
public final class InfernalStructure{
 private InfernalStructure(){}
 private record Key(ServerLevel level,BlockPos pos){}
 private static final Set<Key> CHANGING=new HashSet<>();
 /** Twelve nether bricks, twelve obsidian; the upper center and front mouth are not frame cells. */
 public static Map<BlockPos,Block> frame(BlockPos p,Direction front){var result=new LinkedHashMap<BlockPos,Block>();for(int y=-1;y<=1;y++)for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){if(x==0&&z==0&&y>=0||y==0&&x==front.getStepX()&&z==front.getStepZ())continue;result.put(p.offset(x,y,z),x!=0&&z!=0?Blocks.NETHER_BRICKS:Blocks.OBSIDIAN);}return result;}
 public static boolean loaded(ServerLevel l,BlockPos p){for(var q:BlockPos.betweenClosed(p.offset(-1,-1,-1),p.offset(1,1,1)))if(!l.hasChunkAt(q))return false;return true;}
 public static boolean intact(ServerLevel l,BlockPos p,Direction front){if(!loaded(l,p))return false;for(var at:frame(p,front).keySet())if(!(l.getBlockEntity(at) instanceof InfernalFrame f)||!p.equals(f.controller))return false;return true;}
 public static boolean assemble(ServerLevel l,Player user,BlockPos clicked,ItemStack salis){if(!user.mayBuild()||!ResearchService.knows(user,"INFERNALFURNACE"))return false;
  for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++){var p=clicked.offset(x,y,z);if(!loaded(l,p)||!l.getBlockState(p).is(Blocks.LAVA)||!l.getFluidState(p).isSource()||!l.getBlockState(p.above()).isAir())continue;for(var front:Direction.Plane.HORIZONTAL){var map=frame(p,front);if(!map.containsKey(clicked)&&!clicked.equals(p.relative(front)))continue;if(!l.getBlockState(p.relative(front)).is(Blocks.IRON_BARS)||map.entrySet().stream().anyMatch(e->!l.getBlockState(e.getKey()).is(e.getValue())||l.getBlockEntity(e.getKey())!=null))continue;
   var all=new ArrayList<>(map.keySet());all.add(p);all.add(p.relative(front));for(var at:all){if(!l.mayInteract(user,at)||!user.mayUseItemAt(at,Direction.UP,salis))return false;var event=new net.minecraftforge.event.level.BlockEvent.BreakEvent(l,at,l.getBlockState(at),user,net.minecraftforge.common.util.Result.DEFAULT);if(net.minecraftforge.event.level.BlockEvent.BreakEvent.BUS.post(event)||event.getResult().isDenied())return false;}
   CHANGING.add(new Key(l,p));try{l.setBlockAndUpdate(p,InfernalContent.FURNACE.get().defaultBlockState().setValue(InfernalBlock.FACING,front));for(var e:map.entrySet()){l.setBlockAndUpdate(e.getKey(),(e.getValue()==Blocks.OBSIDIAN?InfernalContent.OBSIDIAN:InfernalContent.BRICKS).get().defaultBlockState());((InfernalFrame)l.getBlockEntity(e.getKey())).configure(p);}l.removeBlock(p.relative(front),false);}finally{CHANGING.remove(new Key(l,p));}if(!user.getAbilities().instabuild)salis.shrink(1);ResearchService.recordCraft(user,new ItemStack(InfernalContent.ICON.get()));return true;
  }}return false;
 }
 public static void dismantle(ServerLevel l,BlockPos p,BlockPos removed){if(!CHANGING.add(new Key(l,p)))return;try{if(!(l.getBlockEntity(p) instanceof InfernalFurnace oven))return;var front=oven.front();for(var at:frame(p,front).keySet())if(!at.equals(removed)&&l.hasChunkAt(at)&&l.getBlockEntity(at) instanceof InfernalFrame part&&p.equals(part.controller)){part.controller=null;l.setBlockAndUpdate(at,part.original());}if(l.hasChunkAt(p.relative(front))&&l.getBlockState(p.relative(front)).isAir())l.setBlockAndUpdate(p.relative(front),Blocks.IRON_BARS.defaultBlockState());if(!p.equals(removed))l.setBlockAndUpdate(p,Blocks.LAVA.defaultBlockState());}finally{CHANGING.remove(new Key(l,p));}}
}
