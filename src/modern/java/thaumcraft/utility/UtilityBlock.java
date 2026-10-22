package thaumcraft.utility;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
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
public final class UtilityBlock extends Block implements EntityBlock {
 public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;public static final BooleanProperty ACTIVE=BlockStateProperties.ENABLED;public final String kind;
 public UtilityBlock(String n,Properties p){super(p);kind=n;registerDefaultState(stateDefinition.any().setValue(FACING,n.startsWith("lamp_")?Direction.DOWN:Direction.NORTH).setValue(ACTIVE,false));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING,ACTIVE);}
 public BlockState getStateForPlacement(BlockPlaceContext c){var s=defaultBlockState().setValue(FACING,kind.startsWith("lamp_")?c.getClickedFace().getOpposite():c.getNearestLookingDirection().getOpposite());return !kind.startsWith("lamp_")||c.getLevel().hasChunkAt(c.getClickedPos().relative(s.getValue(FACING)))&&s.canSurvive(c.getLevel(),c.getClickedPos())?s:null;}
 protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){var q=p.relative(s.getValue(FACING));return !kind.startsWith("lamp_")||!l.hasChunkAt(q)||!l.isEmptyBlock(q);}
 protected BlockState updateShape(BlockState s,LevelReader l,ScheduledTickAccess ticks,BlockPos p,Direction d,BlockPos q,BlockState other,RandomSource r){return s.canSurvive(l,p)?s:Blocks.AIR.defaultBlockState();}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return kind.startsWith("lamp_")?Block.box(4,2,4,12,14,12):kind.equals("everfull_urn")?Block.box(3,0,3,13,16,13):Shapes.block();}
 protected InteractionResult useItemOn(ItemStack item,BlockState s,Level l,BlockPos p,Player user,InteractionHand hand,BlockHitResult hit){if(!user.mayBuild()||!l.mayInteract(user,p))return InteractionResult.FAIL;if(l.getBlockEntity(p) instanceof WaterDevice water&&!item.isEmpty()){if(l.isClientSide())return InteractionResult.SUCCESS;if(net.minecraftforge.fluids.FluidUtil.interactWithFluidHandler(user,hand,water.handler()))return InteractionResult.SUCCESS;}return super.useItemOn(item,s,l,p,user,hand,hit);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player user,BlockHitResult hit){if(!user.mayBuild()||!l.mayInteract(user,p))return InteractionResult.FAIL;if(!l.isClientSide()){var be=l.getBlockEntity(p);if(be instanceof InventoryDevice device&&(!kind.equals("everfull_urn"))&&user instanceof ServerPlayer real)real.openMenu(device);else if(be instanceof WaterDevice water)user.sendSystemMessage(net.minecraft.network.chat.Component.translatable("utility.thaumcraft.water",water.amount()));else if(be instanceof LampEntity lamp)user.sendSystemMessage(net.minecraft.network.chat.Component.translatable("utility.thaumcraft.charges",lamp.charges()));}return InteractionResult.SUCCESS;}
 protected java.util.List<ItemStack> getDrops(BlockState s,net.minecraft.world.level.storage.loot.LootParams.Builder params){var be=params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);boolean disabled=be instanceof InventoryDevice d&&!d.supported()||be instanceof LampEntity lamp&&!lamp.supported();if(disabled&&be.getLevel()!=null){var out=new ItemStack(this);out.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA,net.minecraft.world.item.component.TypedEntityData.of(be.getType(),be.saveWithFullMetadata(be.getLevel().registryAccess())));return java.util.List.of(out);}return super.getDrops(s,params);}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return kind.startsWith("lamp_")?new LampEntity(p,s):kind.equals("potion_sprayer")?new SprayerEntity(p,s):new WaterDevice(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(world,p,state,e)->{if(e instanceof thaumcraft.devices.AuraDeviceEntity device)device.serverTick();};}
}
