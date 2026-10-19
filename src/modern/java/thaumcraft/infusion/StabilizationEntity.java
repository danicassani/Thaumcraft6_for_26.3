package thaumcraft.infusion;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.AABB;
import thaumcraft.aura.AuraManager;
import thaumcraft.rifts.FluxRiftEntity;

/** Bounded horizontal paths through inlays/pedestals. Derived charge never acts as stored energy. */
public final class StabilizationEntity extends thaumcraft.devices.AuraDeviceEntity {
    private int energy,ticks;
    public StabilizationEntity(BlockPos p,BlockState s){super(InfusionContent.STABILIZER_TYPE.get(),p,s);}
    public int energy(){return source(getBlockState())?energy:getBlockState().getValue(InfusionBlock.CHARGE);}
    private static boolean source(BlockState s){return s.is(InfusionContent.STABILIZER.get());}
    private static boolean conductor(BlockState s){return s.getBlock() instanceof InfusionBlock b&&(b.kind==InfusionBlock.Kind.PEDESTAL||b.kind==InfusionBlock.Kind.INLAY);}
    private record Path(BlockPos pos,int distance){}
    private record Supply(StabilizationEntity entity,int charge){}
    private static List<Supply> supplies(Level level,BlockPos origin){
        var out=new ArrayList<Supply>();var seen=new HashSet<BlockPos>();var queue=new ArrayDeque<Path>();queue.add(new Path(origin,0));seen.add(origin);
        while(!queue.isEmpty()){
            var path=queue.remove();for(var d:Direction.Plane.HORIZONTAL){var p=path.pos.relative(d);if(!level.hasChunkAt(p)||!seen.add(p))continue;var s=level.getBlockState(p);
                if(source(s)&&level.getBlockEntity(p) instanceof StabilizationEntity t&&t.energy>path.distance)out.add(new Supply(t,t.energy-path.distance));
                else if(path.distance<14&&conductor(s)&&(!s.is(InfusionContent.INLAY.get())||s.canSurvive(level,p)))queue.add(new Path(p,path.distance+1));
            }
        }
        out.sort(Comparator.<Supply>comparingInt(Supply::charge).reversed().thenComparingLong(v->v.entity.getBlockPos().asLong()));return out;
    }
    public static void updateCharge(Level level,BlockPos pos){
        if(level.isClientSide()||!level.hasChunkAt(pos))return;var state=level.getBlockState(pos);if(!conductor(state))return;
        var all=supplies(level,pos);int charge=all.isEmpty()?0:all.getFirst().charge;
        var updated=state.setValue(InfusionBlock.CHARGE,charge);
        if(state.is(InfusionContent.INLAY.get()))for(var d:Direction.Plane.HORIZONTAL){var p=pos.relative(d);boolean connected=level.hasChunkAt(p)&&(conductor(level.getBlockState(p))||source(level.getBlockState(p)));var property=switch(d){case NORTH->InfusionBlock.NORTH;case SOUTH->InfusionBlock.SOUTH;case EAST->InfusionBlock.EAST;default->InfusionBlock.WEST;};updated=updated.setValue(property,connected);}
        if(state!=updated){level.setBlock(pos,updated,Block.UPDATE_CLIENTS);level.updateNeighbourForOutputSignal(pos,updated.getBlock());}
    }
    public static boolean mitigate(ServerLevel level,BlockPos pedestal,int cost){
        if(cost<1||cost>15||!level.hasChunkAt(pedestal)||!conductor(level.getBlockState(pedestal)))return false;
        for(var s:supplies(level,pedestal))if(s.charge>0&&s.entity.spend(cost)){updateCharge(level,pedestal);return true;}return false;
    }
    public boolean spend(int amount){if(amount<1||energy<amount)return false;energy-=amount;changed();return true;}
    private void changed(){if(level==null)return;var s=getBlockState().setValue(InfusionBlock.CHARGE,energy);if(s!=getBlockState())level.setBlock(worldPosition,s,Block.UPDATE_CLIENTS);sync();level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());}
    @Override public void serverTick(){
        if(!(level instanceof ServerLevel server)||!server.tickRateManager().runsNormally())return;
        if(!source(getBlockState())){if(!getBlockState().canSurvive(level,worldPosition)){server.destroyBlock(worldPosition,true);return;}if(++ticks%5==0)updateCharge(level,worldPosition);return;}
        ++ticks;if(ticks%20==0&&energy<15){energy++;AuraManager.addFlux(server,worldPosition,.25F);changed();}
        if(ticks%5==0&&energy>0)for(var r:server.getEntitiesOfClass(FluxRiftEntity.class,new AABB(worldPosition).inflate(8)).stream().sorted(Comparator.comparingInt(FluxRiftEntity::getId)).toList())if(!r.isRemoved()&&!r.collapsing()&&r.stability()<=50&&spend(1))r.setStability(r.stability()+.125F);
    }
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("energy",energy);out.putInt("charge_ticks",ticks%20);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);energy=Math.clamp(in.getIntOr("energy",0),0,15);ticks=Math.clamp(in.getIntOr("charge_ticks",0),0,19);}
}
