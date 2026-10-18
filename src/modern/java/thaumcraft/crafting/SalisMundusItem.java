package thaumcraft.crafting;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

public final class SalisMundusItem extends Item {
    public SalisMundusItem(Properties properties) { super(properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level=context.getLevel();var pos=context.getClickedPos();
        if(!level.getBlockState(pos).is(Blocks.CRAFTING_TABLE))return InteractionResult.PASS;
        if(level instanceof ServerLevel server) {
            if(!server.setBlockAndUpdate(pos,ArcaneContent.WORKBENCH.get().defaultBlockState()))return InteractionResult.FAIL;
            if(context.getPlayer()==null || !context.getPlayer().getAbilities().instabuild)context.getItemInHand().shrink(1);
            server.sendParticles(ParticleTypes.ENCHANT,pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,40,.5,.5,.5,.1);
        }
        return InteractionResult.SUCCESS;
    }
}
