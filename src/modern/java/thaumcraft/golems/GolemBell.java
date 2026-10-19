package thaumcraft.golems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
public final class GolemBell extends Item {
    public GolemBell(Properties p){super(p.stacksTo(1));}
    public InteractionResult use(Level l,Player p,InteractionHand h){if(!l.isClientSide()){var s=p.getItemInHand(h);int ch=(s.getOrDefault(GolemContent.CHANNEL.get(),0)+1)%16;s.set(GolemContent.CHANNEL.get(),ch);p.sendOverlayMessage(Component.translatable("golem.thaumcraft.channel",ch));}return InteractionResult.SUCCESS;}
    public InteractionResult useOn(UseOnContext c){if(!(c.getLevel() instanceof ServerLevel l))return InteractionResult.SUCCESS;var p=c.getPlayer();if(p==null)return InteractionResult.FAIL;var be=l.getBlockEntity(c.getClickedPos());if(be instanceof SealEntity s){if(!s.valid(p))return InteractionResult.FAIL;if(p.isShiftKeyDown())p.openMenu(s);else{s.channel=c.getItemInHand().getOrDefault(GolemContent.CHANNEL.get(),0);s.setChanged();p.sendOverlayMessage(Component.translatable("golem.thaumcraft.channel",s.channel));}return InteractionResult.SUCCESS;}
        int n=0,ch=c.getItemInHand().getOrDefault(GolemContent.CHANNEL.get(),0);for(var g:l.getEntitiesOfClass(WorkerGolem.class,new AABB(c.getClickedPos()).inflate(16)))if(p.getUUID().equals(g.ownerId())&&g.channel()==ch){g.setHome(c.getClickedPos().above());g.setMode(0);n++;}p.sendOverlayMessage(Component.translatable("golem.thaumcraft.home",n));return InteractionResult.SUCCESS;}
}
