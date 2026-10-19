package thaumcraft.infusion;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import java.util.List;
public final class InfusionBlock extends Block implements EntityBlock{
    public enum Kind{PEDESTAL,MATRIX,BRAIN,STABILIZER,INLAY}
    public static final BooleanProperty ACTIVE=BooleanProperty.create("active"),CRAFTING=BooleanProperty.create("crafting");
    public static final IntegerProperty CHARGE=IntegerProperty.create("charge",0,15);
    public static final BooleanProperty NORTH=BooleanProperty.create("north"),SOUTH=BooleanProperty.create("south"),EAST=BooleanProperty.create("east"),WEST=BooleanProperty.create("west");
    public final Kind kind;
    public InfusionBlock(Kind k,Properties p){super(p);kind=k;registerDefaultState(stateDefinition.any().setValue(ACTIVE,false).setValue(CRAFTING,false).setValue(CHARGE,0).setValue(NORTH,false).setValue(SOUTH,false).setValue(EAST,false).setValue(WEST,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(ACTIVE,CRAFTING,CHARGE,NORTH,SOUTH,EAST,WEST);}
    @Override protected RenderShape getRenderShape(BlockState s){return kind==Kind.MATRIX?RenderShape.INVISIBLE:RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return switch(kind){case PEDESTAL->new InfusionPedestal(p,s);case MATRIX->new InfusionMatrix(p,s);case BRAIN->new BrainJar(p,s);case STABILIZER,INLAY->new StabilizationEntity(p,s);};}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(w,p,b,e)->{if(e instanceof thaumcraft.devices.AuraDeviceEntity a)a.serverTick();};}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(kind){case INLAY->Block.box(0,0,0,16,1,16);case STABILIZER->Shapes.or(Block.box(1,0,1,15,4,15),Block.box(3,4,3,13,13,13),Block.box(5,13,5,11,16,11));case MATRIX->Block.box(3,3,3,13,13,13);case BRAIN->Block.box(3,0,3,13,12,13);case PEDESTAL->Shapes.or(Block.box(0,0,0,16,4,16),Block.box(4,4,4,12,12,12),Block.box(0,12,0,16,16,16));};}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return kind==Kind.INLAY?Shapes.empty():super.getCollisionShape(s,l,p,c);}
    @Override protected boolean canSurvive(BlockState s,LevelReader l,BlockPos p){return kind!=Kind.INLAY||l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP);}
    private InteractionResult use(Level l,BlockPos p,Player player,InteractionHand hand){var e=l.getBlockEntity(p);if(e instanceof StabilizationEntity a){if(!l.isClientSide())player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("infusion.thaumcraft.charge",safelyCharge(a),15));return InteractionResult.SUCCESS;}if(e instanceof InfusionPedestal a)return a.interact(player,hand);if(e instanceof InfusionMatrix m)return m.interact(player,hand);if(e instanceof BrainJar b)return b.interact(player);return InteractionResult.PASS;}
    private static int safelyCharge(StabilizationEntity a){return a.energy();}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand h,BlockHitResult hit){return use(l,p,player,h);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){return use(l,p,player,InteractionHand.MAIN_HAND);}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity by,ItemStack item){super.setPlacedBy(l,p,s,by,item);if(l.getBlockEntity(p) instanceof BrainJar jar)jar.place(item);}
    @Override protected List<ItemStack> getDrops(BlockState s,LootParams.Builder params){if(kind==Kind.BRAIN&&params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof BrainJar jar)return List.of(jar.drop());return super.getDrops(s,params);}
    @Override protected boolean hasAnalogOutputSignal(BlockState s){return true;}
    @Override protected int getAnalogOutputSignal(BlockState s,Level l,BlockPos p,Direction d){var e=l.getBlockEntity(p);return e instanceof StabilizationEntity a?a.energy():e instanceof BrainJar b?b.xp()==0?0:1+14*b.xp()/2000:e instanceof InfusionPedestal a?a.item().isEmpty()?0:15:e instanceof InfusionMatrix m?m.job()==null?0:15:0;}
}
