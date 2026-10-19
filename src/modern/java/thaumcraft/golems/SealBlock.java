package thaumcraft.golems;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
public final class SealBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;public final SealKind kind;
    public SealBlock(Properties p,SealKind kind){super(p);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    public BlockState getStateForPlacement(BlockPlaceContext c){var s=defaultBlockState().setValue(FACING,c.getClickedFace());return canSurvive(s,c.getLevel(),c.getClickedPos())?s:null;}
    protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){var q=p.relative(s.getValue(FACING).getOpposite());return l.hasChunkAt(q)&&!l.getBlockState(q).isAir();}
    protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(FACING)){case UP->Block.box(3,0,3,13,1,13);case DOWN->Block.box(3,15,3,13,16,13);case NORTH->Block.box(3,3,15,13,13,16);case SOUTH->Block.box(3,3,0,13,13,1);case EAST->Block.box(0,3,3,1,13,13);case WEST->Block.box(15,3,3,16,13,13);};}
    protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new SealEntity(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(a,b,c,be)->{if(be instanceof SealEntity seal)seal.serverTick();};}
    public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity e,ItemStack stack){super.setPlacedBy(l,p,s,e,stack);if(e instanceof Player player&&l.getBlockEntity(p) instanceof SealEntity seal)seal.claimOwner(player);}
    protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult h){if(!l.isClientSide()&&l.getBlockEntity(p) instanceof SealEntity seal&&seal.owned(player))player.openMenu(seal);return InteractionResult.SUCCESS;}
}
