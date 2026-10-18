package thaumcraft.theory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import thaumcraft.research.ResearchService;
public final class ResearchTableBlock extends Block implements EntityBlock {
    private final boolean research;
    public ResearchTableBlock(boolean research,Properties p){super(p);this.research=research;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return research?new ResearchTableEntity(pos,state):null;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext c){return Shapes.or(Block.box(0,12,0,16,16,16),Block.box(1,0,1,5,12,5),Block.box(11,0,1,15,12,5),Block.box(1,0,11,5,12,15),Block.box(11,0,11,15,12,15));}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){if(!research)return InteractionResult.PASS;if(!level.isClientSide()&&level.getBlockEntity(pos) instanceof ResearchTableEntity table)player.openMenu(table);return InteractionResult.SUCCESS;}
    public static InteractionResult convert(UseOnContext c){
        var level=c.getLevel();var pos=c.getClickedPos();if(!level.getBlockState(pos).is(TheoryContent.WOOD.get()))return InteractionResult.PASS;
        if(!level.isClientSide()){
            var player=c.getPlayer();if(player==null||!ResearchService.knows(player,"THEORYRESEARCH@1")||!TheoryContent.ink(c.getItemInHand(),1))return InteractionResult.FAIL;
            var tool=c.getItemInHand().copyWithCount(1);if(!level.setBlockAndUpdate(pos,TheoryContent.TABLE.get().defaultBlockState()))return InteractionResult.FAIL;
            if(level.getBlockEntity(pos) instanceof ResearchTableEntity table)table.inventory.setItem(0,tool);
            c.getItemInHand().shrink(1);ResearchService.recordCraft(player,new ItemStack(TheoryContent.TABLE.get()));
        }
        return InteractionResult.SUCCESS;
    }
}
