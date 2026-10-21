package thaumcraft.client;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.thaumaturgy.*;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class PlanPreview {
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.level==null||mc.gui.screen()!=null)return;
        var caster=p.getMainHandItem();if(caster.getItem() instanceof thaumcraft.equipment.ElementalShovelItem&&!p.isShiftKeyDown()){var hit=mc.hitResult;if(hit instanceof net.minecraft.world.phys.BlockHitResult b&&hit.getType()==HitResult.Type.BLOCK&&p.distanceToSqr(b.getLocation())<=36){try(var ignored=mc.collectPerTickGizmos()){for(var q:thaumcraft.equipment.ArchitectPlane.select(b.getBlockPos(),b.getDirection(),p.getDirection(),caster.getOrDefault(thaumcraft.equipment.ElementalShovelItem.ORIENTATION.get(),0)))if(mc.level.getBlockState(q).canBeReplaced())Gizmos.cuboid(q,.003F,GizmoStyle.stroke(0xAA88FF99,1.5F));}}return;}if(!(caster.getItem() instanceof CasterItem))return;
        var focus=caster.get(ThaumaturgyContent.FOCUS.get());var program=focus==null?null:focus.get(ThaumaturgyContent.PROGRAM.get());if(program==null||!program.valid()||!program.medium().equals("plan"))return;
        var from=p.getEyePosition();var to=from.add(p.getLookAngle().scale(16));if(!SpellEngine.loadedLine(mc.level,from,to))return;
        var hit=mc.level.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));if(hit.getType()!=HitResult.Type.BLOCK)return;
        try(var ignored=mc.collectPerTickGizmos()){for(var q:PlanSelection.select(mc.level,hit,PlanControls.effective(caster,program)))Gizmos.cuboid(q,.003F,GizmoStyle.stroke(0xAA99DDFF,1.5F));}
    }
}
