package thaumcraft.thaumaturgy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class FocalBlock extends Block implements EntityBlock {
    public FocalBlock(Properties p){super(p);}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new FocalEntity(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(world,pos,state,be)->{if(be instanceof FocalEntity f)f.serverTick();};}
    protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){if(!l.isClientSide()&&l.getBlockEntity(pos) instanceof FocalEntity f)p.openMenu(f);return InteractionResult.SUCCESS;}
}
