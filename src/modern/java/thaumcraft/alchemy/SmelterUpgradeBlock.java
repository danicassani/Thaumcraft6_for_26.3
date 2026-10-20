package thaumcraft.alchemy;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
/** Direction always points toward the consumer. Bellows are physical blocks, disabled by redstone. */
public final class SmelterUpgradeBlock extends Block {
 public enum Kind{BELLOWS,AUX,VENT}
 public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;
 public static final BooleanProperty COMPRESSED=BooleanProperty.create("compressed"),ENABLED=BlockStateProperties.ENABLED;
 public final Kind kind;
 public SmelterUpgradeBlock(Kind k,Properties p){super(p);kind=k;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(COMPRESSED,false).setValue(ENABLED,true));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,COMPRESSED,ENABLED);}
 public BlockState getStateForPlacement(BlockPlaceContext c){var facing=c.getClickedFace().getOpposite();if(kind!=Kind.BELLOWS){var at=c.getClickedPos().relative(facing);if(facing.getAxis()==Direction.Axis.Y||!c.getLevel().hasChunkAt(at)||!(c.getLevel().getBlockEntity(at) instanceof AlchemyMachine m)||m.kind()!=AlchemyBlock.Kind.SMELTER||m.getBlockState().getValue(AlchemyBlock.FACING)==facing.getOpposite())return null;}return defaultBlockState().setValue(FACING,facing).setValue(ENABLED,!c.getLevel().hasNeighborSignal(c.getClickedPos()));}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return kind==Kind.BELLOWS?Block.box(2,2,2,14,14,14):Shapes.block();}
 protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moved){super.onPlace(s,l,p,old,moved);if(kind==Kind.BELLOWS&&!l.isClientSide())l.scheduleTick(p,this,2);}
 protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource random){if(kind!=Kind.BELLOWS)return;boolean enabled=!l.hasNeighborSignal(p);boolean compressed=enabled&&l.getGameTime()%32<8;var next=s.setValue(ENABLED,enabled).setValue(COMPRESSED,compressed);if(next!=s)l.setBlockAndUpdate(p,next);var target=p.relative(s.getValue(FACING));if(enabled&&l.hasChunkAt(target)&&l.getBlockEntity(target) instanceof AbstractFurnaceBlockEntity furnace&&l.getBlockState(target).hasProperty(AbstractFurnaceBlock.LIT)&&l.getBlockState(target).getValue(AbstractFurnaceBlock.LIT))FurnaceAccess.boost(furnace);l.scheduleTick(p,this,2);}
 public static int count(Level l,BlockPos consumer,Direction[] directions){int count=0;for(var d:directions){var at=consumer.relative(d);if(!l.hasChunkAt(at))continue;var s=l.getBlockState(at);if(s.getBlock() instanceof SmelterUpgradeBlock b&&b.kind==Kind.BELLOWS&&s.getValue(FACING)==d.getOpposite()&&!l.hasNeighborSignal(at))count++;}return count;}
 /** Forge's automatic Minecraft module opens this native data view; no recipe, inventory or fuel is replaced. */
 private static final class FurnaceAccess {
  private static final java.lang.reflect.Field DATA=data();
  private static java.lang.reflect.Field data(){try{var f=AbstractFurnaceBlockEntity.class.getDeclaredField("dataAccess");f.setAccessible(true);return f;}catch(ReflectiveOperationException e){throw new IllegalStateException("Native furnace cooking data unavailable",e);}}
  static void boost(AbstractFurnaceBlockEntity furnace){try{var d=(net.minecraft.world.inventory.ContainerData)DATA.get(furnace);int current=d.get(2),total=d.get(3);if(current>0&&current<total-1){d.set(2,current+1);furnace.setChanged();}}catch(IllegalAccessException e){throw new IllegalStateException("Cannot accelerate native furnace",e);}}
 }
}
