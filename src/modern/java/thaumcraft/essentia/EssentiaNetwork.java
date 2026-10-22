package thaumcraft.essentia;

import net.minecraft.core.*;
import net.minecraft.world.level.Level;

public final class EssentiaNetwork {
    private EssentiaNetwork(){}
    /** Never loads an absent chunk. Both ends must expose their touching face. */
    public static EssentiaTransport neighbour(Level level,BlockPos pos,EssentiaTransport self,Direction face){
        var target=pos.relative(face);
        return level!=null&&level.hasChunkAt(target)&&self.connects(face)&&level.getBlockEntity(target) instanceof EssentiaTransport other&&other.connects(face.getOpposite())?other:null;
    }
    /** One unit, preflight both ends before committing on the server thread. */
    public static boolean pull(Level level,BlockPos pos,EssentiaTransport target,Direction face){
        if(level==null||level.isClientSide()||!target.canInput(face))return false;
        var source=neighbour(level,pos,target,face);var opposite=face.getOpposite();
        if(source==null||!source.canOutput(opposite)||target.suction(face)<=source.suction(opposite)||target.suction(face)<source.minimumSuction())return false;
        String wanted=target.suctionType(face);
        for(var aspect:source.contents(opposite).keySet().stream().sorted().toList()){
            if(!wanted.isEmpty()&&!wanted.equals(aspect))continue;
            if(target.insert(aspect,face,true)&&source.extractFor(aspect,opposite,target,true)){
                if(!source.extractFor(aspect,opposite,target,false))return false;
                if(!target.insert(aspect,face,false))throw new IllegalStateException("Essentia target changed during server-thread transfer");
                return true;
            }
        }
        return false;
    }
}
