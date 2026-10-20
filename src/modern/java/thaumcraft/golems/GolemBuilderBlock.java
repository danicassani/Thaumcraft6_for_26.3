package thaumcraft.golems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class GolemBuilderBlock extends Block implements EntityBlock {
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty ASSEMBLED=net.minecraft.world.level.block.state.properties.BooleanProperty.create("assembled");
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.core.Direction> FACING=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
    public GolemBuilderBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(ASSEMBLED,false).setValue(FACING,net.minecraft.core.Direction.NORTH));}
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> b){b.add(ASSEMBLED,FACING);}
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    protected RenderShape getRenderShape(BlockState s){return s.getValue(ASSEMBLED)?RenderShape.INVISIBLE:RenderShape.MODEL;}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new GolemBuilder(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(a,b,c,be)->{if(be instanceof GolemBuilder g)g.serverTick();};}
    protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult h){if(!l.isClientSide()&&l.getBlockEntity(pos) instanceof GolemBuilder g)p.openMenu(g);return InteractionResult.SUCCESS;}
}
