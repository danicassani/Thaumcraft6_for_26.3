package thaumcraft.thaumaturgy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import thaumcraft.research.*;
public final class PechWand extends Item {
    public PechWand(Properties p){super(p);}
    public InteractionResult use(Level level,Player player,InteractionHand hand){if(!(player instanceof ServerPlayer p))return InteractionResult.SUCCESS;if(!ResearchService.knows(p,"BASEAUROMANCY"))return InteractionResult.FAIL;var state=ResearchService.state(p);if(state==null||!KnowledgeStore.write(p,state.flag("!Pechwand").flag("FOCUSPECH").addPoints("OBSERVATION","AUROMANCY",5+p.getRandom().nextInt(4)).addPoints("THEORY","AUROMANCY",6+p.getRandom().nextInt(3))))return InteractionResult.FAIL;p.getItemInHand(hand).consume(1,p);p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("device.thaumcraft.pech_studied"),true);return InteractionResult.SUCCESS;}
}
