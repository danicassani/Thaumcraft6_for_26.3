package thaumcraft.golems;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.items.*;
/** Requests contain no items. Every transfer rechecks physical source, destination and available space. */
public final class GolemLogistics {
    public record Supply(SealEntity source,ItemStack requested){}
    public static int room(IItemHandler handler,ItemStack stack){if(handler==null||stack.isEmpty())return 0;return stack.getCount()-ItemHandlerHelper.insertItemStacked(handler,stack.copy(),true).getCount();}
    public static boolean belongs(SealEntity s,WorkerGolem g){return s!=null&&s.allows(g)&&s.channel==g.channel()&&g.withinWorkArea(s.getBlockPos())&&s.kind().accepts(g.design());}
    public static int deficit(ServerLevel l,WorkerGolem worker,SealEntity stock,ItemStack sample){long reserved=0;for(var g:l.getEntitiesOfClass(WorkerGolem.class,new net.minecraft.world.phys.AABB(stock.getBlockPos()).inflate(96))){if(g==worker||!g.isAlive()||!stock.getBlockPos().equals(g.delivery())||!stock.allows(g)||g.channel()!=stock.channel)continue;for(int i=0;i<2;i++){var s=g.cargo.getItem(i);if(stock.matchesSample(s,sample))reserved+=s.getCount();}}return (int)Math.max(0,stock.wanted(sample)-reserved);}
    public static Supply find(ServerLevel l,WorkerGolem g,SealEntity stock){var destination=stock.handler();if(destination==null&&stock.kind()!=SealKind.USE)return null;
        for(int f=0;f<9;f++){var sample=stock.sample(f);if(sample.isEmpty())continue;int needed=deficit(l,g,stock,sample);if(needed<=0)continue;
            for(var provider:SealEntity.nearby(l,g)){if(provider.kind()!=SealKind.PROVIDER||provider==stock||provider.support().equals(stock.support()))continue;var source=provider.handler();if(source==null)continue;
                for(int i=0;i<Math.min(256,source.getSlots());i++){var actual=source.extractItem(i,Math.min(64,needed),true);if(actual.isEmpty()||!stock.matchesSample(actual,sample)||!provider.matches(actual))continue;int n=Math.min(provider.extractionLimit(actual,needed),Math.min(stock.kind()==SealKind.USE?1:room(destination,actual),actual.getCount()-g.insertCargo(actual,true).getCount()));if(n>0)return new Supply(provider,actual.copyWithCount(n));}
            }
        }return null;
    }
    public static Supply seed(ServerLevel l,WorkerGolem g,SealEntity harvest,ItemStack seed){if(deficit(l,g,harvest,seed)<=0)return null;for(var provider:SealEntity.nearby(l,g)){if(provider.kind()!=SealKind.PROVIDER)continue;var h=provider.handler();if(h==null)continue;for(int i=0;i<Math.min(256,h.getSlots());i++){var actual=h.extractItem(i,1,true);if(actual.is(seed.getItem())&&provider.matches(actual)&&provider.extractionLimit(actual,1)>0&&g.insertCargo(actual,true).isEmpty())return new Supply(provider,actual);}}return null;}
    public static boolean guard(SealEntity s,net.minecraft.server.level.ServerPlayer p,net.minecraft.world.entity.LivingEntity target){if(!target.isAlive()||target.isSpectator()||thaumcraft.thaumaturgy.SpellEngine.protectedTarget(p,target))return false;int category=target instanceof net.minecraft.world.entity.player.Player?4:target instanceof net.minecraft.world.entity.animal.Animal?2:target instanceof net.minecraft.world.entity.monster.Enemy?1:0;return ((s.kind()==SealKind.GUARD_ADVANCED?s.targets():1)&category)!=0;}
    private GolemLogistics(){}
}
