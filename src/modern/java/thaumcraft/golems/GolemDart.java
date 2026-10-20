package thaumcraft.golems;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import thaumcraft.thaumaturgy.SpellEngine;
/** Non-pickup physical dart, bound to a loaded owner, worker, seal and specific target. */
public final class GolemDart extends Arrow {
 private boolean defense;private UUID player,target;private BlockPos seal;private int revision,channel;private long expires;
 public GolemDart(EntityType<? extends GolemDart> type,Level l){super(type,l);pickup=Pickup.DISALLOWED;}
 public UUID targetId(){return target;}public BlockPos sealPos(){return seal;}public long expires(){return expires;}
 public static boolean fire(ServerLevel l,WorkerGolem g,ServerPlayer owner,SealEntity s,LivingEntity victim){if(!owner.getUUID().equals(g.ownerId())||owner.level()!=l||!owner.isAlive()||owner.isSpectator()||g.level()!=l||!g.isAlive()||g.isRemoved()||!g.design().has("ranged")||(s==null?!g.defenseTarget(owner,victim):g.mode()!=0||!GolemCombat.eligible(l,owner,g,s,victim))||g.distanceToSqr(victim)>256||!SpellEngine.loadedLine(l,g.getEyePosition(),victim.getEyePosition())||l.clip(new net.minecraft.world.level.ClipContext(g.getEyePosition(),victim.getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,g)).getType()!=HitResult.Type.MISS)return false;
  var d=new GolemDart(GolemContent.DART.get(),l);d.setOwner(g);d.player=owner.getUUID();d.target=victim.getUUID();d.defense=s==null;if(s!=null){d.seal=s.getBlockPos().immutable();d.revision=s.revision();}d.channel=g.channel();d.expires=l.getGameTime()+200;d.setPos(g.getEyePosition());double distance=g.distanceTo(victim),fraction=Math.clamp(distance/16,.1,1);var aim=victim.getEyePosition().subtract(d.position());d.shoot(aim.x,aim.y+fraction*fraction,aim.z,1.6F,3);d.setBaseDamage(GolemCombat.damage(g.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE),distance,g.getRandom().nextGaussian()));d.pickup=Pickup.DISALLOWED;if(!l.addFreshEntity(d))return false;g.playSound(SoundEvents.ARROW_SHOOT,1,1/(g.getRandom().nextFloat()*.4F+.8F));l.broadcastEntityEvent(g,(byte)4);return true;
 }
 public static boolean fireDefense(ServerLevel l,WorkerGolem g,ServerPlayer owner,LivingEntity victim){return fire(l,g,owner,null,victim);}
 private boolean defenseSession(Entity victim){if(!defense||!(level() instanceof ServerLevel l)||player==null||target==null||l.getGameTime()>=expires||!l.hasChunkAt(blockPosition())||!(getOwner() instanceof WorkerGolem g)||!g.isAlive()||g.channel()!=channel||!player.equals(g.ownerId())||!g.design().has("ranged"))return false;var p=l.getServer().getPlayerList().getPlayer(player);return p!=null&&p.isAlive()&&!p.isSpectator()&&p.level()==l&&victim instanceof LivingEntity v&&target.equals(v.getUUID())&&g.defenseTarget(p,v);}
 private SealEntity session(){if(!(level() instanceof ServerLevel l)||player==null||target==null||seal==null||l.getGameTime()>=expires||!l.hasChunkAt(blockPosition())||!l.hasChunkAt(seal)||!(getOwner() instanceof WorkerGolem g)||!g.isAlive()||g.isRemoved()||g.mode()!=0||!g.design().has("ranged")||!player.equals(g.ownerId())||g.channel()!=channel)return null;var owner=l.getServer().getPlayerList().getPlayer(player);if(owner==null||owner.level()!=l||!owner.isAlive()||owner.isSpectator())return null;return l.getBlockEntity(seal) instanceof SealEntity s&&s.revision()==revision&&s.active()&&GolemLogistics.belongs(s,g)?s:null;}
 public boolean mayHit(Entity e){if(!(level() instanceof ServerLevel l)||!(e instanceof LivingEntity victim)||!e.getUUID().equals(target))return false;if(defense)return defenseSession(e);var s=session();return s!=null&&GolemCombat.eligible(l,l.getServer().getPlayerList().getPlayer(player),(WorkerGolem)getOwner(),s,victim);}
 @Override protected boolean canHitEntity(Entity e){return mayHit(e)&&super.canHitEntity(e);}
 @Override protected void onHitEntity(EntityHitResult hit){if(mayHit(hit.getEntity())){var victim=(LivingEntity)hit.getEntity();float health=victim.getHealth()+victim.getAbsorptionAmount();var worker=(WorkerGolem)getOwner();super.onHitEntity(hit);if(victim.getHealth()+victim.getAbsorptionAmount()<health)worker.addRankXp(victim.isAlive()?1:9);}discard();}
 @Override protected void onHitBlock(BlockHitResult hit){discard();}
 @Override public void tick(){pickup=Pickup.DISALLOWED;if(!level().isClientSide()){if((defense?!defenseSession(target==null?null:((ServerLevel)level()).getEntity(target)):session()==null)||!SpellEngine.loadedLine(level(),position(),position().add(getDeltaMovement()))){discard();return;}}super.tick();}
 @Override protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putBoolean("dart_defense",defense);if(player!=null)o.putString("dart_player",player.toString());if(target!=null)o.putString("dart_target",target.toString());if(seal!=null)o.putLong("dart_seal",seal.asLong());o.putInt("dart_revision",revision);o.putInt("dart_channel",channel);o.putLong("dart_expires",expires);}
 @Override protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);defense=i.getBooleanOr("dart_defense",false);try{player=UUID.fromString(i.getStringOr("dart_player",""));target=UUID.fromString(i.getStringOr("dart_target",""));}catch(IllegalArgumentException ex){player=null;target=null;}seal=BlockPos.of(i.getLongOr("dart_seal",0));revision=i.getIntOr("dart_revision",0);channel=Math.clamp(i.getIntOr("dart_channel",0),0,15);expires=i.getLongOr("dart_expires",0);pickup=Pickup.DISALLOWED;}
}
