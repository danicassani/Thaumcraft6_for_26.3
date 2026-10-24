package thaumcraft.research;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Shared targeting for the scan and its preview. Never forces chunks or sees through blocks. */
public record ScanTarget(int kind,int entityId,BlockPos blockPos,ItemStack stack,Entity entity,AABB bounds,boolean fluxGoo) {
    public static final int BLOCK=1,ENTITY=2,HAND=3;
    public static boolean holdingMeter(Player p){return p.getMainHandItem().is(ResearchContent.METER.get())||p.getOffhandItem().is(ResearchContent.METER.get());}
    public static ScanTarget hand(Player p){var stack=ScanningService.otherHand(p);return stack.isEmpty()||stack.is(ResearchContent.METER.get())?null:new ScanTarget(HAND,-1,p.blockPosition(),stack,null,p.getBoundingBox(),false);}
    public static ScanTarget find(Player p){
        var level=p.level();var start=p.getEyePosition();var end=start.add(p.getLookAngle().scale(9));
        for(int i=0;i<=18;i++)if(!level.hasChunkAt(BlockPos.containing(start.lerp(end,i/18D))))return null;
        var block=level.clip(new ClipContext(start,end,ClipContext.Block.OUTLINE,ClipContext.Fluid.ANY,p));
        double nearest=block.getType()==HitResult.Type.MISS?81:start.distanceToSqr(block.getLocation());Entity target=null;
        for(var entity:level.getEntities(p,new AABB(start,end).inflate(1),e->e.isAlive()&&!e.isSpectator())){
            var intersection=entity.getBoundingBox().inflate(.1).clip(start,end);
            if(intersection.isPresent()){double distance=start.distanceToSqr(intersection.get());if(distance<nearest){nearest=distance;target=entity;}}
        }
        if(target!=null)return new ScanTarget(ENTITY,target.getId(),target.blockPosition(),target instanceof ItemEntity item?item.getItem():ItemStack.EMPTY,target,target.getBoundingBox(),false);
        if(block.getType()!=HitResult.Type.BLOCK)return null;
        var pos=block.getBlockPos();var state=level.getBlockState(pos);var stack=new ItemStack(state.getBlock().asItem());
        if(stack.isEmpty()&&!state.getFluidState().isEmpty())stack=new ItemStack(state.getFluidState().getType().getBucket());
        var shape=state.getShape(level,pos);var bounds=shape.isEmpty()?new AABB(pos):shape.bounds().move(pos);
        return new ScanTarget(BLOCK,-1,pos,stack,null,bounds,state.is(thaumcraft.taint.TaintContent.GOO.get()));
    }
    public boolean creature(){return entity!=null&&!(entity instanceof ItemEntity);}
    public String id(){return creature()?BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString():goo()?"thaumcraft:flux_goo":BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();}
    public String identity(){return (creature()?"entity:":"item:")+(creature()?id():goo()?"thaumcraft:flux_goo":ResearchCatalog.proof(stack));}
    public String nameKey(){return creature()?entity.getType().getDescriptionId():goo()?"block.thaumcraft.flux_goo":stack.getItem().getDescriptionId();}
    private boolean goo(){return fluxGoo;}
    public Map<String,Integer> aspects(ServerLevel level){
        if(kind==BLOCK&&level.getBlockState(blockPos).is(thaumcraft.taint.TaintContent.GOO.get()))return Map.of("vitium",10);
        return creature()?AspectCatalog.entity(entity):thaumcraft.alchemy.AspectResolver.item(level,stack);
    }
}
