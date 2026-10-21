package thaumcraft.warp;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.storage.*;
public final class MindSpider extends Spider {
 private static final EntityDataAccessor<String> VIEWER=SynchedEntityData.defineId(MindSpider.class,EntityDataSerializers.STRING);
 private static final EntityDataAccessor<Boolean> HARMLESS=SynchedEntityData.defineId(MindSpider.class,EntityDataSerializers.BOOLEAN);
 private long expires;
 public MindSpider(EntityType<? extends MindSpider> t,Level l){super(t,l);xpReward=1;}
 public static AttributeSupplier attributes(){return Spider.createAttributes().add(Attributes.MAX_HEALTH,1).add(Attributes.ATTACK_DAMAGE,1).build();}
 protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(VIEWER,"");b.define(HARMLESS,false);}
 public void configure(ServerPlayer p,boolean harmless){entityData.set(HARMLESS,harmless);entityData.set(VIEWER,harmless?p.getUUID().toString():"");expires=harmless?p.level().getGameTime()+1200:0;xpReward=harmless?0:1;}
 public String viewer(){return entityData.get(VIEWER);}public boolean harmless(){return entityData.get(HARMLESS);}
 public boolean doHurtTarget(ServerLevel l,Entity e){return !harmless()&&super.doHurtTarget(l,e);}
 protected void dropCustomDeathLoot(ServerLevel l,DamageSource d,boolean p){}
 public void tick(){super.tick();if(!level().isClientSide()&&(thaumcraft.config.ThaumcraftConfig.wussMode.get()||harmless()&&(expires<=level().getGameTime()||expires>level().getGameTime()+1200)))discard();}
 protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putBoolean("harmless",harmless());o.putString("viewer",viewer());o.putLong("mind_expires",expires);}
 protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);boolean h=i.getBooleanOr("harmless",false);entityData.set(HARMLESS,h);expires=i.getLongOr("mind_expires",0);String v=i.getStringOr("viewer","");try{if(h)java.util.UUID.fromString(v);}catch(Exception ex){v="";expires=0;}entityData.set(VIEWER,v);xpReward=h?0:1;}
}
