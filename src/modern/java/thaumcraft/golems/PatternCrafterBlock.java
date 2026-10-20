package thaumcraft.golems;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class PatternCrafterBlock extends Block implements EntityBlock {
 public PatternCrafterBlock(Properties p){super(p);}
 public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new PatternCrafter(p,s);}
 public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide()?null:(a,b,c,e)->{if(e instanceof PatternCrafter machine)machine.serverTick();};}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos at,Player p,BlockHitResult h){if(!l.isClientSide()&&l.getBlockEntity(at) instanceof PatternCrafter machine&&p.mayBuild()&&l.mayInteract(p,at)&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(at))<=64&&thaumcraft.research.ResearchService.knows(p,"ARCANEPATTERNCRAFTER")){machine.cycle();p.sendOverlayMessage(Component.translatable("golem.thaumcraft.pattern",machine.pattern()+1));}return InteractionResult.SUCCESS;}
}
