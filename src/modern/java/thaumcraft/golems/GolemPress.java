package thaumcraft.golems;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.research.ResearchService;
/** Original 2x2 footprint: piston/bars, cauldron, anvil and stone table; reversible assembly. */
public final class GolemPress {
 private GolemPress(){}private static final Set<BlockPos> CHANGING=new HashSet<>();
 public static List<BlockPos> parts(BlockPos p,Direction front){var left=front.getClockWise();return List.of(p.above(),p.relative(left),p.relative(left).relative(front),p.relative(front));}
 public static boolean assemble(ServerLevel l,Player player,BlockPos piston,ItemStack salis){if(!l.hasChunkAt(piston)||!l.getBlockState(piston).is(Blocks.PISTON)||l.getBlockState(piston).getValue(PistonBaseBlock.FACING)!=Direction.UP||!player.mayBuild()||!ResearchService.knows(player,"MINDCLOCKWORK"))return false;
  for(var front:Direction.Plane.HORIZONTAL){var at=parts(piston,front);var expected=List.of(Blocks.IRON_BARS,Blocks.CAULDRON,Blocks.ANVIL,thaumcraft.content.SimpleContent.BLOCKS.get("table_stone").get());boolean valid=true;for(int i=0;i<4;i++)if(!l.hasChunkAt(at.get(i))||!l.getBlockState(at.get(i)).is(expected.get(i))||l.getBlockEntity(at.get(i))!=null){valid=false;break;}if(!valid)continue;
   var all=new ArrayList<>(at);all.add(piston);for(var pos:all){if(!l.mayInteract(player,pos)||!player.mayUseItemAt(pos,Direction.UP,salis))return false;var event=new net.minecraftforge.event.level.BlockEvent.BreakEvent(l,pos,l.getBlockState(pos),player,net.minecraftforge.common.util.Result.DEFAULT);if(net.minecraftforge.event.level.BlockEvent.BreakEvent.BUS.post(event)||event.getResult().isDenied())return false;}
   var old=new ArrayList<BlockState>();at.forEach(pos->old.add(l.getBlockState(pos)));CHANGING.add(piston);try{l.setBlockAndUpdate(piston,GolemContent.BUILDER.get().defaultBlockState().setValue(GolemBuilderBlock.ASSEMBLED,true).setValue(GolemBuilderBlock.FACING,front));for(int i=0;i<4;i++){l.setBlockAndUpdate(at.get(i),GolemContent.PRESS_PART.get().defaultBlockState());var part=(PressPart)l.getBlockEntity(at.get(i));part.configure(piston,old.get(i));}}finally{CHANGING.remove(piston);}if(!player.getAbilities().instabuild)salis.shrink(1);ResearchService.recordCraft(player,new ItemStack(GolemContent.BUILDER.get()));return true;
  }return false;
 }
 public static boolean loaded(ServerLevel l,BlockPos p){var state=l.getBlockState(p);return !state.getValue(GolemBuilderBlock.ASSEMBLED)||parts(p,state.getValue(GolemBuilderBlock.FACING)).stream().allMatch(l::hasChunkAt);}
 public static boolean intact(ServerLevel l,BlockPos p){var state=l.getBlockState(p);if(!(state.getBlock() instanceof GolemBuilderBlock)||!state.getValue(GolemBuilderBlock.ASSEMBLED))return true;for(var at:parts(p,state.getValue(GolemBuilderBlock.FACING)))if(!l.hasChunkAt(at))return true;else if(!(l.getBlockEntity(at) instanceof PressPart part)||!p.equals(part.controller))return false;return true;}
 public static void dismantle(ServerLevel l,BlockPos p,BlockPos removed){if(!CHANGING.add(p))return;try{var state=l.getBlockState(p);if(state.getBlock() instanceof GolemBuilderBlock&&state.getValue(GolemBuilderBlock.ASSEMBLED)){for(var at:parts(p,state.getValue(GolemBuilderBlock.FACING)))if(!at.equals(removed)&&l.hasChunkAt(at)&&l.getBlockEntity(at) instanceof PressPart part&&p.equals(part.controller)){var original=part.original;part.controller=null;l.setBlockAndUpdate(at,original);}if(!p.equals(removed))l.setBlockAndUpdate(p,Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING,Direction.UP));}}finally{CHANGING.remove(p);}}
}
