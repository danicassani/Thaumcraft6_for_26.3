package thaumcraft.research;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public final class ScanningService {
    private ScanningService(){}
    public static boolean observe(Player player,ItemStack stack){return observe(player,false,ResearchCatalog.proof(stack),BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),player.level() instanceof ServerLevel server?thaumcraft.alchemy.AspectResolver.item(server,stack):AspectCatalog.item(stack));}
    public static boolean observe(Player player,Entity entity){if(entity instanceof ItemEntity item)return observe(player,item.getItem());var key=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();return observe(player,true,key,key,AspectCatalog.entity(entity));}
    private static boolean observe(Player player,boolean entity,String identity,String id,Map<String,Integer> aspects){
        if(!(player.level() instanceof ServerLevel))return false;
        var state=ResearchService.state(player);if(state==null)return false;
        String scan=(entity?"entity:":"item:")+identity;var triggers=AspectCatalog.triggers(entity,id);
        if(state.scans().contains(scan)||aspects.isEmpty()&&triggers.isEmpty())return false;
        var next=state.scan(scan).flag("!"+id);
        for(var aspect:aspects.keySet())if(!state.flags().contains("!"+aspect)){
            next=next.flag("!"+aspect);for(var category:List.of("BASICS","ALCHEMY","AUROMANCY"))next=next.addPoints("OBSERVATION",category,1);
        }
        for(var category:ResearchCatalog.CATEGORIES){int points=AspectCatalog.observation(category,aspects);if(points>0)next=next.addPoints("OBSERVATION",category,points);}
        for(var flag:triggers)next=next.flag(flag);
        if(!KnowledgeStore.write(player,next))return false;
        var message=Component.translatable("research.thaumcraft.scanned");
        aspects.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e->message.append(" ").append(Component.translatable("tc.aspect."+e.getKey())).append(" "+e.getValue()));
        player.sendOverlayMessage(message);return true;
    }
    /** The held object opposite the meter; never scan the meter itself. */
    public static ItemStack otherHand(Player player){return player.getMainHandItem().is(ResearchContent.METER.get())?player.getOffhandItem():player.getMainHandItem();}
    /** The server picks the target, checks all ray chunks and respects block occlusion. */
    public static boolean scan(Player player){return scan(player,player.isShiftKeyDown());}
    public static boolean scan(Player player,boolean scanOtherHand){
        if(!(player.level() instanceof ServerLevel level))return false;
        if(scanOtherHand){
            var other=otherHand(player);
            if(other.isEmpty()||other.is(ResearchContent.METER.get())){player.sendOverlayMessage(Component.translatable("research.thaumcraft.scan_other_empty"));return false;}
            ScanEffects.play(level,player.getBoundingBox().inflate(.05));
            boolean result=observe(player,other);
            if(!result)player.sendOverlayMessage(Component.translatable("research.thaumcraft.nothing"));
            return result;
        }
        var target=ScanTarget.find(player);
        if(target==null)return thaumcraft.theory.CelestialObservations.scan(player);
        ScanEffects.play(level,target.bounds());
        boolean result;
        if(target.entity()!=null)result=observe(player,target.entity());
        else {
            var pos=target.blockPos();
            result=target.fluxGoo()?observe(player,false,"thaumcraft:flux_goo","thaumcraft:flux_goo",Map.of("vitium",10)):observe(player,target.stack());
            var tile=level.getBlockEntity(pos);if(tile!=null){var capability=tile.getCapability(ForgeCapabilities.ITEM_HANDLER,net.minecraft.core.Direction.UP);if(capability.isPresent()){var handler=capability.orElseThrow(IllegalStateException::new);for(int i=0;i<Math.min(handler.getSlots(),100);i++)if(!handler.getStackInSlot(i).isEmpty())result=observe(player,handler.getStackInSlot(i))||result;}}
        }
        if(!result)player.sendOverlayMessage(Component.translatable("research.thaumcraft.nothing"));
        return result;
    }
}
