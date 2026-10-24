package thaumcraft.research;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.*;
import thaumcraft.content.SimpleContent;

/** Cosmetic scan feedback, emitted only for the server-selected target. */
public final class ScanEffects {
    private static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES=DeferredRegister.create(Registries.PARTICLE_TYPE,"thaumcraft");
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,"thaumcraft");
    public static final RegistryObject<SimpleParticleType> RUNE=PARTICLES.register("scan_rune",()->new SimpleParticleType(false));
    public static final RegistryObject<SoundEvent> SOUND=SOUNDS.register("scan",()->SoundEvent.createVariableRangeEvent(SimpleContent.id("scan")));
    private ScanEffects(){}
    public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){PARTICLES.register(bus);SOUNDS.register(bus);}
    public static void start(net.minecraft.world.entity.player.Player player){player.level().playSound(null,player.getX(),player.getY(),player.getZ(),SOUND.get(),SoundSource.PLAYERS,.5F,1F);}
    public static void play(ServerLevel level,AABB bounds){
        // Surface runes surround the actual target, rather than appearing behind an occluding block.
        for(int i=0;i<16;i++){
            double x=bounds.minX+level.getRandom().nextDouble()*bounds.getXsize();
            double y=bounds.minY+level.getRandom().nextDouble()*bounds.getYsize();
            double z=bounds.minZ+level.getRandom().nextDouble()*bounds.getZsize();
            switch(i%4){case 0->x=bounds.minX-.025;case 1->x=bounds.maxX+.025;case 2->z=bounds.minZ-.025;default->z=bounds.maxZ+.025;}
            level.sendParticles(RUNE.get(),x,y,z,1,0,0,0,0);
        }
    }
}
