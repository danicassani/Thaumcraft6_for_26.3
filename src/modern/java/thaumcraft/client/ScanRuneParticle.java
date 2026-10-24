package thaumcraft.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.research.ScanEffects;

/** Original TC6 rune glyphs, adapted to the native particle atlas and renderer. */
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class ScanRuneParticle extends SingleQuadParticle {
    private ScanRuneParticle(ClientLevel level,double x,double y,double z,TextureAtlasSprite sprite){
        super(level,x,y,z,sprite);lifetime=45;hasPhysics=false;
        quadSize=.13F+random.nextFloat()*.04F;
        rCol=.3F+random.nextFloat()*.7F;gCol=0;bCol=.3F+random.nextFloat()*.7F;alpha=0;
    }
    @Override public void tick(){
        xo=x;yo=y;zo=z;
        if(age++>=lifetime){remove();return;}
        alpha=Math.min(age/9F,(lifetime-age)/36F)*.8F;
        y+=.0015;
    }
    @Override protected Layer getLayer(){return Layer.TRANSLUCENT;}
    @Override public int getLightCoords(float partialTick){return 0xF000F0;}
    @SubscribeEvent public static void providers(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(ScanEffects.RUNE.get(),sprites->(options,level,x,y,z,dx,dy,dz,random)->new ScanRuneParticle(level,x,y,z,sprites.get(random)));
    }
}
