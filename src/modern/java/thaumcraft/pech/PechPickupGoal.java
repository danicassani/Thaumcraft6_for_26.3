package thaumcraft.pech;
import java.util.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
final class PechPickupGoal extends Goal {
    private final Pech pech;private ItemEntity target;private int retryAt;
    PechPickupGoal(Pech pech){this.pech=pech;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    // GoalSelector does not poll every tick: deadlines avoid phase-locking with its cadence.
    public boolean canUse(){if(!(pech.level() instanceof net.minecraft.server.level.ServerLevel level)||pech.tickCount<retryAt||pech.anger()>0||!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level,pech))return false;retryAt=pech.tickCount+10;target=pech.level().getEntitiesOfClass(ItemEntity.class,pech.getBoundingBox().inflate(16),e->e.isAlive()&&!e.hasPickUpDelay()&&!e.getPersistentData().getBooleanOr(Pech.RETURNED,false)&&pech.canCollect(e.getItem())).stream().filter(e->pech.distanceToSqr(e)<=256).min(Comparator.comparingDouble(pech::distanceToSqr)).orElse(null);return target!=null;}
    public boolean canContinueToUse(){return target!=null&&target.isAlive()&&pech.anger()==0&&pech.distanceToSqr(target)<=256&&pech.canCollect(target.getItem())&&(!pech.getNavigation().isDone()||pech.distanceToSqr(target)<=1.5);}
    public void start(){pech.getNavigation().moveTo(target,1.5);}
    public void stop(){target=null;retryAt=pech.tickCount+20;pech.getNavigation().stop();}
    public void tick(){pech.getLookControl().setLookAt(target,30,30);if(pech.tickCount%10==0)pech.getNavigation().moveTo(target,1.5);if(pech.distanceToSqr(target)<=1.5)pech.collect(target);}
}
