package thaumcraft.golems;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
/** Shared launch/impact policy; darts never acquire a different victim in flight. */
public final class GolemCombat {
 public static final int RANGE=16;
 public static boolean combat(SealEntity seal){return seal!=null&&(seal.kind().base()==SealKind.GUARD||seal.kind()==SealKind.BUTCHER);}
 public static boolean eligible(ServerLevel l,ServerPlayer owner,WorkerGolem g,SealEntity seal,LivingEntity target){return combat(seal)&&seal.active()&&GolemLogistics.belongs(seal,g)&&GolemTasks.area(seal).contains(target.position())&&(seal.kind()==SealKind.BUTCHER?target instanceof Animal a&&GolemTasks.butcher(l,seal,owner,a):GolemLogistics.guard(seal,owner,target));}
 public static int interval(double distance){return 20+(int)Math.floor(Math.clamp(distance/RANGE,0,1)*5);}
 public static double damage(double attack,double distance,double gaussian){return Math.max(.1,attack/3+Math.clamp(distance/RANGE,.1,1)+gaussian*.25);}
 private GolemCombat(){}
}
