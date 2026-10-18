package thaumcraft.theory;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import thaumcraft.research.*;

public final class CelestialObservations {
    private CelestialObservations(){}
    /** SUN_ANGLE is zero at noon; positive angles carry the sun westward. */
    public static boolean aimed(Vec3 look,float angle){double r=Math.toRadians(angle);return look.dot(new Vec3(-Math.sin(r),Math.cos(r),0))>=Math.cos(Math.toRadians(7));}
    public static boolean scan(Player p){
        if(!(p.level() instanceof ServerLevel level)||!level.dimension().equals(Level.OVERWORLD)||!ResearchService.knows(p,"CELESTIALSCANNING")||p.getXRot()>0||!level.canSeeSky(p.blockPosition().above())||level.isRaining())return false;
        var attrs=level.environmentAttributes();float sun=attrs.getDimensionValue(EnvironmentAttributes.SUN_ANGLE);boolean night=Math.cos(Math.toRadians(sun))<0;int variant;
        if(aimed(p.getLookAngle(),night?attrs.getDimensionValue(EnvironmentAttributes.MOON_ANGLE):sun))variant=night?5+attrs.getDimensionValue(EnvironmentAttributes.MOON_PHASE).index():0;
        else if(night)variant=1+switch(p.getDirection()){case NORTH->0;case SOUTH->1;case WEST->2;default->3;};else return false;
        var state=ResearchService.state(p);if(state==null)return false;
        String prefix="CEL_"+(level.getGameTime()/24000)+"_",key=prefix+variant;
        if(state.flags().contains(key)){p.sendOverlayMessage(Component.translatable("theory.thaumcraft.sky_repeat"));return false;}
        int tool=-1,paper=-1;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(TheoryContent.ink(s,1))tool=i;if(s.is(Items.PAPER))paper=i;}
        if(tool<0||paper<0){p.sendOverlayMessage(Component.translatable("theory.thaumcraft.sky_supplies"));return false;}
        var flags=new HashSet<>(state.flags());flags.removeIf(k->k.startsWith("CEL_")&&!k.startsWith(prefix));flags.add(key);
        var next=new KnowledgeState(state.version(),state.stages(),state.scans(),flags,state.crafts(),state.points());if(!KnowledgeStore.write(p,next))return false;
        p.getInventory().removeItem(paper,1);var ink=p.getInventory().getItem(tool);ink.setDamageValue(ink.getDamageValue()+1);
        var note=TheoryContent.note(variant);if(!p.getInventory().add(note))Block.popResource(level,p.blockPosition(),note);p.getInventory().setChanged();
        p.sendOverlayMessage(Component.translatable("theory.thaumcraft.sky_success",Component.translatable("item.thaumcraft.celestial_notes."+TheoryContent.NOTES.get(variant))));return true;
    }
}
