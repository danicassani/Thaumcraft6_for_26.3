package thaumcraft.banners;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.util.*;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

public final class AspectBannerBlock extends Block implements EntityBlock {
 public static final IntegerProperty ROTATION=BlockStateProperties.ROTATION_16;public static final BooleanProperty WALL=BooleanProperty.create("wall");public final DyeColor dye;
 public AspectBannerBlock(DyeColor dye,Properties p){super(p);this.dye=dye;registerDefaultState(stateDefinition.any().setValue(ROTATION,0).setValue(WALL,false));}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ROTATION,WALL);}
 public static Direction facing(BlockState s){return Direction.fromYRot(s.getValue(ROTATION)*22.5);}
 public static BlockPos support(BlockState s,BlockPos p){return s.getValue(WALL)?p.relative(facing(s).getOpposite()):p.below();}
 protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){var at=support(s,p);return !l.hasChunkAt(at)||l.getBlockState(at).isFaceSturdy(l,at,s.getValue(WALL)?facing(s):Direction.UP);}
 public BlockState getStateForPlacement(BlockPlaceContext c){if(c.getClickedFace()==Direction.DOWN)return null;boolean wall=c.getClickedFace()!=Direction.UP;int rotation=wall?Math.floorMod(Math.round(c.getClickedFace().toYRot()/22.5F),16):Mth.floor((c.getRotation()+180)*16/360+.5)&15;var s=defaultBlockState().setValue(ROTATION,rotation).setValue(WALL,wall);return c.getLevel().hasChunkAt(support(s,c.getClickedPos()))&&s.canSurvive(c.getLevel(),c.getClickedPos())?s:null;}
 protected BlockState updateShape(BlockState s,LevelReader l,ScheduledTickAccess ticks,BlockPos p,Direction face,BlockPos other,BlockState neighbour,RandomSource random){return !s.canSurvive(l,p)?Blocks.AIR.defaultBlockState():super.updateShape(s,l,ticks,p,face,other,neighbour,random);}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){if(!s.getValue(WALL))return Block.box(5.28,0,5.28,10.56,32,10.56);return switch(facing(s)){case SOUTH->Block.box(0,-16,0,16,16,4);case NORTH->Block.box(0,-16,12,16,16,16);case EAST->Block.box(0,-16,0,4,16,16);default->Block.box(12,-16,0,16,16,16);};}
 protected RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new AspectBannerEntity(p,s);}
 public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity by,ItemStack item){super.setPlacedBy(l,p,s,by,item);if(!l.isClientSide()&&l.getBlockEntity(p) instanceof AspectBannerEntity b)b.placeContents(item);}
 protected List<ItemStack> getDrops(BlockState s,LootParams.Builder params){return params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof AspectBannerEntity b?List.of(b.droppedItem()):super.getDrops(s,params);}
 protected ItemStack getCloneItemStack(LevelReader l,BlockPos p,BlockState s,boolean includeData){return l.getBlockEntity(p) instanceof AspectBannerEntity b?b.droppedItem():new ItemStack(this);}
 protected InteractionResult useItemOn(ItemStack held,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){return l.getBlockEntity(p) instanceof AspectBannerEntity b?b.decorate(player,hand):InteractionResult.PASS;}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){return l.getBlockEntity(p) instanceof AspectBannerEntity b?b.decorate(player,InteractionHand.MAIN_HAND):InteractionResult.PASS;}
}
