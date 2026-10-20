package thaumcraft.golems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.level.Level;

/** Native collision-aware navigation; coordinates are never applied as teleportation. */
public final class GolemNavigation {
    static void configure(WorkerGolem g){var l=g.level();switch(g.design().legs()){
        case 2->g.useNavigation(new Climber(g,l),new MoveControl<>(g));
        case 3->{var nav=new Hover(g,l);nav.setCanOpenDoors(false);nav.setCanFloat(true);g.useNavigation(nav,new FlyingMoveControl<>(g,20,true));}
        default->g.useNavigation(new Ground(g,l),new MoveControl<>(g));
    }}
    private static void halt(WorkerGolem g){g.getMoveControl().setWantedPosition(g.getX(),g.getY(),g.getZ(),0);if(g.legs()==3)g.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}
    private static boolean loaded(net.minecraft.world.entity.ai.navigation.PathNavigation nav,WorkerGolem g){var path=nav.getPath();return path==null||path.isDone()||g.level().hasChunkAt(path.getNextNodePos());}
    public static final class Ground extends GroundPathNavigation {
        private final WorkerGolem worker;
        Ground(WorkerGolem g,Level l){super(g,l);worker=g;}
        @Override public void tick(){if(!loaded(this,worker)){stop();halt(worker);return;}super.tick();}
    }
    public static final class Hover extends FlyingPathNavigation {
        private final WorkerGolem worker;
        Hover(WorkerGolem g,Level l){super(g,l);worker=g;}
        @Override public void tick(){if(!loaded(this,worker)){stop();return;}super.tick();}
        @Override public void stop(){super.stop();halt(worker);}
    }
    /** Spider fallback is needed when a vertical goal has no ordinary ground path. */
    public static final class Climber extends WallClimberNavigation {
        private boolean moving;private final WorkerGolem worker;private BlockPos goal;
        Climber(WorkerGolem g,Level l){super(g,l);worker=g;}
        @Override public boolean moveTo(double x,double y,double z,double speed){return moveTo(x,y,z,1,speed);}
        @Override public boolean moveTo(double x,double y,double z,int range,double speed){moving=true;goal=BlockPos.containing(x,y,z);speedModifier=speed;var path=createPath(BlockPos.containing(x,y,z),range);if(path!=null)return moveTo(path,speed);return true;}
        @Override public void tick(){if(goal!=null&&!worker.level().hasChunkAt(goal)||!loaded(this,worker)){stop();return;}if(moving)super.tick();}
        @Override public void stop(){moving=false;goal=null;super.stop();halt(worker);}
    }
    private GolemNavigation(){}
}
