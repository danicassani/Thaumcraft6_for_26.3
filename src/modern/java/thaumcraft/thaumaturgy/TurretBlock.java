package thaumcraft.thaumaturgy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class TurretBlock extends Block implements EntityBlock {
    public final boolean advanced;
    public TurretBlock(Properties p,boolean a){super(p);advanced=a;}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TurretEntity(p,s);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(world,pos,state,be)->{if(be instanceof TurretEntity turret)turret.serverTick();};}
    public void setPlacedBy(Level l,BlockPos q,BlockState s,LivingEntity e,ItemStack item){super.setPlacedBy(l,q,s,e,item);if(e instanceof Player p&&l.getBlockEntity(q) instanceof TurretEntity t)t.claim(p);}
    protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos q,Player p,BlockHitResult hit){if(l.getBlockEntity(q) instanceof TurretEntity t)return t.interact(p,ItemStack.EMPTY);return InteractionResult.PASS;}
    protected InteractionResult useItemOn(ItemStack item,BlockState s,Level l,BlockPos q,Player p,InteractionHand h,BlockHitResult hit){if(l.getBlockEntity(q) instanceof TurretEntity t)return t.interact(p,item);return InteractionResult.PASS;}
}
