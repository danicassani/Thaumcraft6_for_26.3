package thaumcraft.condenser;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
public final class LatticeBlock extends Block{
 public static final BooleanProperty[] SIDES={net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN,net.minecraft.world.level.block.state.properties.BlockStateProperties.UP,net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH,net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH,net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST,net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST};
 public final boolean dirty;
 public LatticeBlock(boolean dirty,Properties p){super(p);this.dirty=dirty;var s=stateDefinition.any();for(var side:SIDES)s=s.setValue(side,false);registerDefaultState(s);}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(SIDES);}
 private static boolean connection(BlockState s,Direction d){return s.getBlock() instanceof LatticeBlock||d==Direction.DOWN&&s.is(CondenserContent.BASE.get());}
 public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){var s=defaultBlockState();for(var d:Direction.values())if(c.getLevel().hasChunkAt(c.getClickedPos().relative(d)))s=s.setValue(SIDES[d.ordinal()],connection(c.getLevel().getBlockState(c.getClickedPos().relative(d)),d));return s;}
 protected BlockState updateShape(BlockState s,LevelReader l,ScheduledTickAccess ticks,BlockPos p,Direction d,BlockPos q,BlockState other,RandomSource random){if(s.getValue(SIDES[d.ordinal()])!=connection(other,d))ticks.scheduleTick(p,this,1);return s.setValue(SIDES[d.ordinal()],connection(other,d));}
 protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moved){super.onPlace(s,l,p,old,moved);if(!l.isClientSide())l.scheduleTick(p,this,1);}
 /** A valid network fits a radius-squared 74 around its base, hence 296 between any two cells. */
 public static int support(Level l,BlockPos p){var visited=new HashSet<BlockPos>();var pending=new ArrayDeque<BlockPos>();pending.add(p);visited.add(p);boolean incomplete=false;while(!pending.isEmpty()){if(visited.size()>4096)return -1;var at=pending.remove();for(var d:Direction.values()){var q=at.relative(d);if(!l.hasChunkAt(q)){incomplete=true;continue;}var s=l.getBlockState(q);if(d==Direction.DOWN&&s.is(CondenserContent.BASE.get())&&q.getY()<p.getY()&&p.distSqr(q)<=74)return 1;if(s.getBlock() instanceof LatticeBlock&&p.distSqr(q)<=296&&visited.add(q))pending.add(q);}}return incomplete?-1:0;}
 protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource random){int support=support(l,p);if(support==0)l.destroyBlock(p,true);else if(support<0)l.scheduleTick(p,this,20);}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){var shape=Block.box(5,5,5,11,11,11);for(var d:Direction.values())if(s.getValue(SIDES[d.ordinal()]))shape=Shapes.or(shape,switch(d){case DOWN->Block.box(7,0,7,9,8,9);case UP->Block.box(7,8,7,9,16,9);case NORTH->Block.box(7,7,0,9,9,8);case SOUTH->Block.box(7,7,8,9,9,16);case WEST->Block.box(0,7,7,8,9,9);case EAST->Block.box(8,7,7,16,9,9);});return shape;}
 protected InteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player user,InteractionHand hand,BlockHitResult hit){if(!dirty||!stack.is(thaumcraft.content.SimpleContent.ITEMS.get("filter").get()))return InteractionResult.PASS;if(l.isClientSide())return InteractionResult.SUCCESS;if(!user.mayBuild()||!l.mayInteract(user,p)||!user.mayUseItemAt(p,hit.getDirection(),stack))return InteractionResult.FAIL;var event=new net.minecraftforge.event.level.BlockEvent.BreakEvent(l,p,s,user,net.minecraftforge.common.util.Result.DEFAULT);if(net.minecraftforge.event.level.BlockEvent.BreakEvent.BUS.post(event)||event.getResult().isDenied())return InteractionResult.FAIL;var next=CondenserContent.CLEAN.get().defaultBlockState();for(var side:SIDES)next=next.setValue(side,s.getValue(side));if(!l.setBlockAndUpdate(p,next))return InteractionResult.FAIL;if(!user.getAbilities().instabuild)stack.shrink(1);if(l.getRandom().nextBoolean())Block.popResource(l,p,thaumcraft.crystals.CrystalContent.essence("vitium",1));l.playSound(null,p,net.minecraft.sounds.SoundEvents.ITEM_PICKUP,net.minecraft.sounds.SoundSource.BLOCKS,.2F,1.6F);return InteractionResult.SUCCESS;}
}
