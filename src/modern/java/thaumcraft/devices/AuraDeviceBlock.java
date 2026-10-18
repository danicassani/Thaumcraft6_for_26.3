package thaumcraft.devices;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import thaumcraft.aura.AuraManager;

public final class AuraDeviceBlock extends Block implements EntityBlock {
    public enum Kind { GENERATOR, DIOPTRA, PEDESTAL, SIPHON }
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public final Kind kind;
    public AuraDeviceBlock(Kind kind, BlockBehaviour.Properties properties) {
        super(properties); this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(POWERED, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, POWERED); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch(kind) { case GENERATOR -> new VisGeneratorEntity(pos, state); case DIOPTRA -> new DioptraEntity(pos, state); case PEDESTAL -> new RechargePedestalEntity(pos, state); case SIPHON -> new VoidSiphonEntity(pos,state); };
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (l, p, s, entity) -> { if (entity instanceof AuraDeviceEntity device) device.serverTick(); };
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel(); var pos = context.getClickedPos(); var facing = context.getClickedFace();
        if (kind == Kind.GENERATOR) for (var side : Direction.values()) {
            if (!level.hasChunkAt(pos.relative(side))) continue;
            var tile = level.getBlockEntity(pos.relative(side));
            if (tile != null && tile.getCapability(ForgeCapabilities.ENERGY, side.getOpposite()).map(net.minecraftforge.energy.IEnergyStorage::canReceive).orElse(false)) { facing = side; break; }
        }
        return defaultBlockState().setValue(FACING, facing).setValue(POWERED, level.hasNeighborSignal(pos));
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block source, Orientation orientation, boolean piston) {
        if (!level.isClientSide()) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(POWERED)) level.setBlockAndUpdate(pos, state.setValue(POWERED, powered));
        }
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(kind==Kind.SIPHON)return Block.box(2,0,2,14,16,14);
        if (kind == Kind.GENERATOR) return switch (state.getValue(FACING)) {
            case UP -> Block.box(4,0,4,12,14,12); case DOWN -> Block.box(4,2,4,12,16,12);
            case NORTH -> Block.box(4,4,0,12,12,14); case SOUTH -> Block.box(4,4,2,12,12,16);
            case WEST -> Block.box(0,4,4,14,12,12); case EAST -> Block.box(2,4,4,16,12,12);
        };
        return kind == Kind.PEDESTAL ? Shapes.or(Block.box(0,0,0,16,4,16), Block.box(2,4,2,14,8,14), Block.box(4,8,4,12,16,12)) : Shapes.block();
    }
    @Override protected InteractionResult useItemOn(ItemStack item, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (kind == Kind.PEDESTAL && level.getBlockEntity(pos) instanceof RechargePedestalEntity pedestal) return pedestal.interact(player, hand);
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if(kind==Kind.SIPHON && level.getBlockEntity(pos) instanceof VoidSiphonEntity siphon)return siphon.interact(player);
        if (kind == Kind.PEDESTAL && level.getBlockEntity(pos) instanceof RechargePedestalEntity pedestal) return pedestal.interact(player, InteractionHand.MAIN_HAND);
        if (level instanceof ServerLevel server) {
            if (level.getBlockEntity(pos) instanceof VisGeneratorEntity generator)
                player.sendSystemMessage(Component.translatable("block.thaumcraft.vis_generator.read", generator.getEnergyStored(), state.getValue(POWERED) ? Component.translatable("device.thaumcraft.disabled") : Component.translatable("device.thaumcraft.enabled")));
            else if (kind == Kind.DIOPTRA) {
                if (level.getBlockEntity(pos) instanceof DioptraEntity dioptra) dioptra.scan();
                var cell = AuraManager.get(server, pos);
                if (cell != null) player.sendSystemMessage(Component.translatable("block.thaumcraft.dioptra.read", state.getValue(POWERED) ? "flux" : "vis", cell.base(), String.format(Locale.ROOT,"%.2f",cell.vis()), String.format(Locale.ROOT,"%.2f",cell.flux())));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
