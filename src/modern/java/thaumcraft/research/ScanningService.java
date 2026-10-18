package thaumcraft.research;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public final class ScanningService {
    private ScanningService(){}
    public static boolean observe(Player player,ItemStack stack){return observe(player,false,ResearchCatalog.proof(stack),BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),AspectCatalog.item(stack));}
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
    /** The server picks the target, checks all ray chunks and respects block occlusion. */
    public static boolean scan(Player player){
        if(!(player.level() instanceof ServerLevel level))return false;
        if(player.isShiftKeyDown()&&!player.getOffhandItem().isEmpty())return observe(player,player.getOffhandItem());
        var start=player.getEyePosition();var end=start.add(player.getLookAngle().scale(9));
        for(int i=0;i<=18;i++)if(!level.hasChunkAt(BlockPos.containing(start.lerp(end,i/18D))))return false;
        var block=level.clip(new ClipContext(start,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.ANY,player));
        double nearest=block.getType()==HitResult.Type.MISS?81:start.distanceToSqr(block.getLocation());Entity target=null;
        for(var entity:level.getEntities(player,new AABB(start,end).inflate(1),e->e.isAlive()&&!e.isSpectator())){
            var intersection=entity.getBoundingBox().inflate(.1).clip(start,end);if(intersection.isPresent()){double distance=start.distanceToSqr(intersection.get());if(distance<nearest){nearest=distance;target=entity;}}
        }
        boolean result=false;Vec3 effects;
        if(target!=null){result=observe(player,target);effects=target.position();}
        else if(block.getType()==HitResult.Type.BLOCK){
            var pos=block.getBlockPos();var state=level.getBlockState(pos);var item=state.getBlock().asItem();
            ItemStack stack=new ItemStack(item);if(stack.isEmpty()){if(!state.getFluidState().isEmpty())stack=new ItemStack(state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)?Items.LAVA_BUCKET:Items.WATER_BUCKET);}
            result=observe(player,stack);effects=Vec3.atCenterOf(pos);
            var tile=level.getBlockEntity(pos);if(tile!=null){var capability=tile.getCapability(ForgeCapabilities.ITEM_HANDLER,net.minecraft.core.Direction.UP);if(capability.isPresent()){var handler=capability.orElseThrow(IllegalStateException::new);for(int i=0;i<Math.min(handler.getSlots(),100);i++)if(!handler.getStackInSlot(i).isEmpty())result=observe(player,handler.getStackInSlot(i))||result;}}
        }else {return thaumcraft.theory.CelestialObservations.scan(player);}
        if(result)level.sendParticles(ParticleTypes.ENCHANT,effects.x,effects.y+.5,effects.z,20,.4,.4,.4,.1);
        else player.sendOverlayMessage(Component.translatable("research.thaumcraft.nothing"));
        return result;
    }
}
