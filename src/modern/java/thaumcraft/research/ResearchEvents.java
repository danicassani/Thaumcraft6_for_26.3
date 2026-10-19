package thaumcraft.research;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.crystals.CrystalContent;

public final class ResearchEvents {
    private ResearchEvents(){}
    public static void register(){
        KnowledgeStore.register();
        ResearchCommands.register();
        PlayerEvent.ItemCraftedEvent.BUS.addListener(event->ResearchService.recordCraft(event.getEntity(),event.getCrafting()));
        PlayerWakeUpEvent.BUS.addListener(event->{var player=event.getEntity();if(!player.level().isClientSide()&&ResearchService.knows(player,"!gotcrystals")&&!ResearchService.knows(player,"!gotdream"))dream(player);});
        TickEvent.PlayerTickEvent.Post.BUS.addListener(event->{
            if(!(event.player() instanceof ServerPlayer player)||player.tickCount%20!=0)return;
            var state=ResearchService.state(player);if(state==null)return;var next=movementKnowledge(player,state);
            boolean crystal=false;for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(CrystalContent.ESSENCE.get())){crystal=true;break;}
            if(crystal)next=next.flag("!gotcrystals");
            if(player.level().dimension().equals(Level.NETHER))next=next.flag("m_hellandback");
            if(player.level().dimension().equals(Level.END))next=next.flag("m_endoftheworld");
            if(ResearchCatalog.knows(next,"UNLOCKAUROMANCY@1")){
                if(player.getY()<10)next=next.flag("m_deepdown");if(player.getY()>player.level().getMaxY()*.4)next=next.flag("m_uphigh");
            }
            if(player.isOnFire())next=next.flag("f_onfire");
            if(!next.equals(state))KnowledgeStore.write(player,next);
            if(ThaumcraftConfig.noSleep.get()&&next.flags().contains("!gotcrystals")&&!next.flags().contains("!gotdream"))dream(player);
            if(player.getMainHandItem().is(ResearchContent.METER.get())||player.getOffhandItem().is(ResearchContent.METER.get())){
                var aura=thaumcraft.aura.AuraManager.get(player.level(),player.blockPosition());
                if(aura!=null&&(aura.flux()>aura.vis()||aura.flux()>aura.base()/3F))ResearchService.flag(player,"FLUX");
            }
        });
    }
    /** Original movement observations, read from persistent vanilla statistics. */
    public static KnowledgeState movementKnowledge(ServerPlayer player,KnowledgeState state){
        var stats=player.getStats();
        if(stats.getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.WALK_ONE_CM))>160000)state=state.flag("m_walker");
        if(stats.getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.SPRINT_ONE_CM))>80000)state=state.flag("m_runner");
        if(stats.getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.SWIM_ONE_CM))>8000)state=state.flag("m_swimmer");
        if(stats.getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.JUMP))>500)state=state.flag("m_jumper");
        return state;
    }
    public static void dream(net.minecraft.world.entity.player.Player player){
        if(ResearchService.knows(player,"!gotdream")||!ResearchService.flag(player,"!gotdream"))return;
        var journal=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WRITTEN_BOOK);
        journal.set(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT,new net.minecraft.world.item.component.WrittenBookContent(
            net.minecraft.server.network.Filterable.passThrough("Strange Dream"),player.getName().getString(),0,
            java.util.List.of(net.minecraft.server.network.Filterable.passThrough(Component.translatable("research.thaumcraft.journal.1")),net.minecraft.server.network.Filterable.passThrough(Component.translatable("research.thaumcraft.journal.2"))),true));
        if(!player.getInventory().add(journal))net.minecraft.world.level.block.Block.popResource(player.level(),player.blockPosition(),journal);
        player.sendSystemMessage(Component.translatable("research.thaumcraft.dream"));
    }
}
