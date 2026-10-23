package thaumcraft.infusion;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import thaumcraft.aura.AuraManager;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.research.ResearchService;
import thaumcraft.taint.TaintContent;

/** The 24 weighted draws in TC6's matrix, including all six ingredient accidents. */
public final class InfusionMishaps {
    public enum Kind { EJECT,WARP,ZAP_ONE,ZAP_ALL,EJECT_GOO,EJECT_FLUX,EJECT_GOO_ALT,DESTROY_FLUX,HARM_ONE,EJECT_EXPLOSION,HARM_ALL,EXPLOSION }
    public static Kind kind(int draw){if(draw<0||draw>=24)throw new IllegalArgumentException("Invalid infusion accident");return draw<4?Kind.EJECT:draw<7?Kind.WARP:draw<10?Kind.ZAP_ONE:draw<12?Kind.ZAP_ALL:draw<14?Kind.EJECT_GOO:draw<16?Kind.EJECT_FLUX:draw==16?Kind.EJECT_GOO_ALT:draw==17?Kind.DESTROY_FLUX:draw<20?Kind.HARM_ONE:draw<22?Kind.EJECT_EXPLOSION:draw==22?Kind.HARM_ALL:Kind.EXPLOSION;}
    private InfusionMishaps(){}
    public static boolean apply(ServerLevel level,BlockPos matrix,List<BlockPos> pedestals,int draw){
        var kind=kind(draw);var area=new AABB(matrix).inflate(10);
        var players=level.getEntitiesOfClass(ServerPlayer.class,area,p->p.isAlive()&&!p.isSpectator());
        for(var p:players)ResearchService.flag(p,"!INSTABILITY");
        switch(kind){
            case EJECT,EJECT_GOO,EJECT_FLUX,EJECT_GOO_ALT,DESTROY_FLUX,EJECT_EXPLOSION->{
                var available=pedestals.stream().filter(level::hasChunkAt).filter(p->level.getBlockEntity(p) instanceof InfusionPedestal t&&t.supported()&&!t.item().isEmpty()).toList();
                if(available.isEmpty())return false;
                var pos=available.get(level.getRandom().nextInt(available.size()));
                if(StabilizationEntity.mitigate(level,pos,5+level.getRandom().nextInt(6))){arc(level,matrix,pos.above());return true;}
                var pedestal=(InfusionPedestal)level.getBlockEntity(pos);var item=pedestal.item();
                if(!pedestal.setItem(net.minecraft.world.item.ItemStack.EMPTY))return false;
                if(kind!=Kind.DESTROY_FLUX)Block.popResource(level,pos,item);
                if(kind==Kind.EJECT_GOO||kind==Kind.EJECT_GOO_ALT){var above=pos.above();if(level.hasChunkAt(above)&&level.getBlockState(above).canBeReplaced()&&level.getBlockEntity(above)==null){level.setBlockAndUpdate(above,TaintContent.GOO.get().defaultBlockState());level.playSound(null,pos,SoundEvents.BOTTLE_FILL,SoundSource.BLOCKS,.3F,1);}}
                if(kind==Kind.EJECT_FLUX||kind==Kind.DESTROY_FLUX)AuraManager.addFlux(level,pos,5+level.getRandom().nextInt(5));
                if(kind==Kind.EJECT_EXPLOSION&&!ThaumcraftConfig.wussMode.get())level.explode(null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,1,Level.ExplosionInteraction.NONE);
                arc(level,matrix,pos.above());
            }
            case WARP->{if(!players.isEmpty()){var p=players.get(level.getRandom().nextInt(players.size()));if(level.getRandom().nextFloat()<.25F)CuriosityContent.warp(p,0,1,0);else CuriosityContent.warp(p,0,0,2+level.getRandom().nextInt(4));}}
            case ZAP_ONE,ZAP_ALL,HARM_ONE,HARM_ALL->{
                if(ThaumcraftConfig.wussMode.get())break;
                var targets=level.getEntitiesOfClass(LivingEntity.class,area,e->e.isAlive()&&!(e instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator()))).stream().sorted(Comparator.comparingInt(LivingEntity::getId)).toList();
                for(var target:targets){arc(level,matrix,target.blockPosition());if(kind==Kind.ZAP_ONE||kind==Kind.ZAP_ALL)target.hurtServer(level,level.damageSources().magic(),4+level.getRandom().nextInt(4));else{boolean disease=level.getRandom().nextBoolean();target.addEffect(new MobEffectInstance((disease?TaintContent.DISEASE:TaintContent.EXHAUST).getHolder().orElseThrow(),disease?120:2400,0,!disease,true));}if(kind==Kind.ZAP_ONE||kind==Kind.HARM_ONE)break;}
            }
            case EXPLOSION->{if(!ThaumcraftConfig.wussMode.get())level.explode(null,matrix.getX()+.5,matrix.getY()+.5,matrix.getZ()+.5,1.5F+level.getRandom().nextFloat(),Level.ExplosionInteraction.NONE);}
        }
        return false;
    }
    public static void arc(ServerLevel level,BlockPos from,BlockPos to){double dx=to.getX()-from.getX(),dy=to.getY()-from.getY(),dz=to.getZ()-from.getZ();int steps=Math.max(2,(int)Math.ceil(Math.sqrt(dx*dx+dy*dy+dz*dz)*3));for(int n=0;n<=steps;n++){double f=(double)n/steps;level.sendParticles(ParticleTypes.WITCH,from.getX()+.5+dx*f,from.getY()+.5+dy*f,from.getZ()+.5+dz*f,1,.02,.02,.02,0);}}
}
