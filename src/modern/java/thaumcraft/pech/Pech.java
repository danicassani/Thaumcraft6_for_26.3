package thaumcraft.pech;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.*;
import thaumcraft.config.ThaumcraftConfig;
import thaumcraft.content.SimpleContent;

public final class Pech extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Integer> TYPE=SynchedEntityData.defineId(Pech.class,EntityDataSerializers.INT),ANGER=SynchedEntityData.defineId(Pech.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FRIEND=SynchedEntityData.defineId(Pech.class,EntityDataSerializers.BOOLEAN);
    public static final TagKey<Biome> HABITAT=TagKey.create(net.minecraft.core.registries.Registries.BIOME,SimpleContent.id("pech_habitat"));
    public static final String RETURNED="thaumcraft_pech_returned";
    public final SimpleContainer pack=new SimpleContainer(9),escrow=new SimpleContainer(5);
    private UUID trader,escrowOwner;private ServerPlayer customer;private boolean initialized;private int chargeTicks;public float mumble;
    public Pech(EntityType<? extends Pech> type,Level level){super(type,level);xpReward=8;setCanPickUpLoot(false);}
    public static AttributeSupplier attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.ATTACK_DAMAGE,6).add(Attributes.MOVEMENT_SPEED,.5).add(Attributes.FOLLOW_RANGE,16).add(Attributes.ARMOR,2).build();}
    protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(TYPE,0);b.define(ANGER,0);b.define(FRIEND,false);}
    public int variant(){return entityData.get(TYPE);}public void variant(int value){if(value<0||value>2)throw new IllegalArgumentException("Invalid pech variant");entityData.set(TYPE,value);initialized=true;}
    public int anger(){return entityData.get(ANGER);}public boolean friendly(){return entityData.get(FRIEND);}
    public void friendly(boolean value){entityData.set(FRIEND,value);}
    public Component getName(){return hasCustomName()?super.getName():Component.translatable("entity.thaumcraft.pech."+variant());}
    public static boolean enabled(LevelAccessor l){return ThaumcraftConfig.allowSpawnPech.get()&&!ThaumcraftConfig.wussMode.get()&&l.getDifficulty()!=Difficulty.PEACEFUL;}
    private static boolean habitat(ServerLevel l,BlockPos p){var biome=l.getBiome(p);return biome.is(HABITAT)&&(l.dimension()==Level.OVERWORLD||biome.is(thaumcraft.worldgen.MagicalForest.KEY)||biome.is(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,SimpleContent.id("eerie"))));}
    public static boolean placement(EntityType<Pech> type,ServerLevelAccessor l,EntitySpawnReason reason,BlockPos p,RandomSource r){return enabled(l)&&habitat(l.getLevel(),p)&&thaumcraft.rifts.WispSpawning.light(l,p,r)&&Mob.checkMobSpawnRules(type,l,reason,p,r);}
    public boolean checkSpawnRules(LevelAccessor l,EntitySpawnReason reason){return enabled(l)&&level() instanceof ServerLevel s&&habitat(s,blockPosition())&&s.getEntitiesOfClass(Pech.class,getBoundingBox().inflate(16),Pech::isAlive).size()<4;}
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor l,DifficultyInstance difficulty,EntitySpawnReason reason,SpawnGroupData group){
        var result=super.finalizeSpawn(l,difficulty,reason,group);
        if(!initialized){int roll=random.nextInt(20);ItemStack hand=switch(roll){case 0,12->new ItemStack(SimpleContent.ITEMS.get("pech_wand").get());case 1->new ItemStack(Items.STONE_SWORD);case 3->new ItemStack(Items.STONE_AXE);case 5->new ItemStack(Items.IRON_SWORD);case 6->new ItemStack(Items.IRON_AXE);case 7->new ItemStack(Items.FISHING_ROD);case 8->new ItemStack(Items.STONE_PICKAXE);case 9->new ItemStack(Items.IRON_PICKAXE);case 2,4,10,11,13->new ItemStack(Items.BOW);default->ItemStack.EMPTY;};setItemSlot(EquipmentSlot.MAINHAND,hand);variant(roll==0||roll==12?1:hand.is(Items.BOW)?2:0);setDropChance(EquipmentSlot.MAINHAND,variant()==1?.1F:.2F);setDropChance(EquipmentSlot.OFFHAND,.2F);if(variant()!=1)populateDefaultEquipmentEnchantments(l,random,difficulty);setCanPickUpLoot(random.nextFloat()<.75F*difficulty.getSpecialMultiplier());}
        return result;
    }
    private boolean ranged(){return getMainHandItem().is(Items.BOW)||getMainHandItem().is(SimpleContent.ITEMS.get("pech_wand").get());}
    protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK,Flag.JUMP));}public boolean canUse(){return trader!=null&&friendly()&&anger()==0;}public void tick(){getNavigation().stop();}});
        goalSelector.addGoal(2,new MeleeAttackGoal(this,.6,false){public boolean canUse(){return !ranged()&&super.canUse();}public boolean canContinueToUse(){return !ranged()&&super.canContinueToUse();}});
        goalSelector.addGoal(2,new RangedAttackGoal(this,.6,20,50,15){public boolean canUse(){return ranged()&&super.canUse();}public boolean canContinueToUse(){return ranged()&&super.canContinueToUse();}});
        goalSelector.addGoal(3,new PechPickupGoal(this));
        goalSelector.addGoal(4,new AvoidEntityGoal<Player>(this,Player.class,8,.5,.6){public boolean canUse(){return !friendly()&&anger()==0&&super.canUse();}});
        goalSelector.addGoal(5,new OpenDoorGoal(this,true));
        goalSelector.addGoal(6,new MoveTowardsRestrictionGoal(this,.5));
        goalSelector.addGoal(9,new WaterAvoidingRandomStrollGoal(this,.6));goalSelector.addGoal(9,new LookAtPlayerGoal(this,Player.class,3));goalSelector.addGoal(10,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<Player>(this,Player.class,true){public boolean canUse(){return anger()>0&&super.canUse();}public boolean canContinueToUse(){return anger()>0&&super.canContinueToUse();}});
    }
    public boolean considersEntityAsAlly(Entity e){return e instanceof Pech||super.considersEntityAsAlly(e);}
    public boolean doHurtTarget(ServerLevel l,Entity target){return !ThaumcraftConfig.wussMode.get()&&!(target instanceof Pech)&&super.doHurtTarget(l,target);}
    public void die(DamageSource source){if(customer!=null)endTrade(customer);super.die(source);}
    public boolean hurtServer(ServerLevel l,DamageSource source,float amount){boolean hit=super.hurtServer(l,source,amount);if(hit&&source.getEntity() instanceof Player p&&!p.isCreative()&&!p.isSpectator()&&!ThaumcraftConfig.wussMode.get()){for(var pech:l.getEntitiesOfClass(Pech.class,getBoundingBox().inflate(32,16,32),Pech::isAlive))pech.enrage(p);}return hit;}
    private void enrage(Player p){if(anger()==0){level().broadcastEntityEvent(this,(byte)19);playSound(PechContent.CHARGE.get(),.4F,1);}entityData.set(ANGER,400+random.nextInt(400));friendly(false);setTarget(p);getNavigation().stop();}
    public boolean removeWhenFarAway(double distance){int occupied=0;for(int i=0;i<9;i++)if(!pack.getItem(i).isEmpty())occupied++;return occupied<5&&escrow.isEmpty()&&trader==null;}
    protected boolean shouldDespawnInPeaceful(){return escrow.isEmpty();}
    public void tick(){super.tick();if(mumble>0)mumble*=.75F;if(!(level() instanceof ServerLevel l))return;
        if(anger()>0)entityData.set(ANGER,anger()-1);
        if(ThaumcraftConfig.wussMode.get()||l.getDifficulty()==Difficulty.PEACEFUL){entityData.set(ANGER,0);setTarget(null);}
        else if(anger()==0&&getTarget() instanceof Player)setTarget(null);
        if(trader!=null){var p=customer;if(p==null||p.isRemoved()||!p.isAlive()||p.level()!=level()||!(p.containerMenu instanceof PechMenu menu)||menu.pech()!=this){trader=null;customer=null;}}
        if(anger()>0&&getTarget()!=null){if(--chargeTicks<=0){chargeTicks=100;playSound(PechContent.CHARGE.get(),.4F,1);}if(tickCount%15==0)l.sendParticles(ParticleTypes.ANGRY_VILLAGER,getX(),getY()+1,getZ(),1,.3,.3,.3,0);}
        if(tickCount%40==0&&!isNoAi())heal(1);
    }
    public int value(ItemStack s){if(s.isEmpty())return 0;if(s.is(Items.ENDER_PEARL))return 15;return level() instanceof ServerLevel l?Math.min(32,thaumcraft.alchemy.AspectResolver.item(l,s).getOrDefault("desiderium",0)/2):0;}
    public boolean valued(ItemStack s){return value(s)>0;}
    protected void pickUpItem(ServerLevel l,ItemEntity item){if(item.getPersistentData().getBooleanOr(RETURNED,false))return;if(!friendly()&&valued(item.getItem())){collect(item);return;}super.pickUpItem(l,item);}
    public boolean canCollect(ItemStack s){if(s.isEmpty())return false;if(!friendly()&&valued(s))return true;for(int i=0;i<9;i++){var old=pack.getItem(i);if(old.isEmpty()||ItemStack.isSameItemSameComponents(old,s)&&old.getCount()<old.getMaxStackSize())return true;}return false;}
    public boolean collect(ItemEntity entity){
        if(!(level() instanceof ServerLevel l)||!entity.isAlive()||entity.hasPickUpDelay()||entity.getPersistentData().getBooleanOr(RETURNED,false)||distanceToSqr(entity)>1.5||!canCollect(entity.getItem())||!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(l,this))return false;
        var stack=entity.getItem().copy();int before=stack.getCount();
        if(!friendly()&&valued(stack)){if(random.nextInt(10)<value(stack)){friendly(true);l.broadcastEntityEvent(this,(byte)18);}stack.shrink(1);}
        else{for(int i=0;i<9&&!stack.isEmpty();i++){var old=pack.getItem(i);if(!old.isEmpty()&&ItemStack.isSameItemSameComponents(old,stack)){int n=Math.min(stack.getCount(),old.getMaxStackSize()-old.getCount());if(n>0){old.grow(n);stack.shrink(n);}}}for(int i=0;i<9&&!stack.isEmpty();i++)if(pack.getItem(i).isEmpty()){int n=Math.min(stack.getCount(),stack.getMaxStackSize());pack.setItem(i,stack.copyWithCount(n));stack.shrink(n);}}
        if(stack.getCount()==before)return false;if(stack.isEmpty())entity.discard();else entity.setItem(stack);playSound(SoundEvents.ITEM_PICKUP,.2F,1.5F);return true;
    }
    public boolean beginTrade(ServerPlayer p){if(!isAlive()||!friendly()||anger()>0||p.isSpectator()||p.level()!=level()||distanceToSqr(p)>64||trader!=null&&!trader.equals(p.getUUID())||!escrow.isEmpty()&&escrowOwner!=null&&!escrowOwner.equals(p.getUUID()))return false;trader=p.getUUID();customer=p;escrowOwner=trader;return true;}
    public boolean tradingWith(Player p){return p.getUUID().equals(trader)&&p.level()==level()&&isAlive()&&friendly()&&anger()==0&&distanceToSqr(p)<=64;}
    public void endTrade(ServerPlayer p){if(!p.getUUID().equals(trader))return;trader=null;customer=null;if(p.isAlive()&&p.level()==level()){for(int i=0;i<5;i++){var left=escrow.getItem(i).copy();if(left.isEmpty())continue;p.getInventory().add(left);escrow.setItem(i,left);if(!left.isEmpty()){var drop=new ItemEntity(level(),getX(),getY()+.5,getZ(),left.copy());drop.getPersistentData().putBoolean(RETURNED,true);drop.setTarget(p.getUUID());drop.setDefaultPickUpDelay();if(level().addFreshEntity(drop))escrow.setItem(i,ItemStack.EMPTY);}}}if(escrow.isEmpty())escrowOwner=null;}
    protected InteractionResult mobInteract(Player p,InteractionHand hand){if(p.isShiftKeyDown()||p.getItemInHand(hand).is(Items.NAME_TAG))return super.mobInteract(p,hand);if(!friendly())return InteractionResult.PASS;if(p instanceof ServerPlayer s){if(s.containerMenu instanceof PechMenu menu&&menu.pech()==this)return InteractionResult.SUCCESS;if(!beginTrade(s))return InteractionResult.FAIL;s.openMenu(new SimpleMenuProvider((id,inv,q)->new PechMenu(id,inv,this),getDisplayName()));}return InteractionResult.SUCCESS;}
    public void performRangedAttack(LivingEntity target,float power){if(!(level() instanceof ServerLevel l)||ThaumcraftConfig.wussMode.get()||target instanceof Pech||target instanceof Player p&&(p.isCreative()||p.isSpectator())||!target.isAlive()||!hasLineOfSight(target)||!thaumcraft.thaumaturgy.SpellEngine.loadedLine(l,getEyePosition(),target.getEyePosition()))return;
        if(getMainHandItem().is(Items.BOW)){var arrow=new Arrow(l,this,new ItemStack(Items.ARROW),getMainHandItem());if(random.nextFloat()<.2F)arrow.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,40));var d=target.getEyePosition().subtract(arrow.position());arrow.shoot(d.x,d.y+Math.sqrt(d.x*d.x+d.z*d.z)*.2,d.z,1.6F,rangedAttackUncertainty(l));arrow.setBaseDamage(power*2+random.nextGaussian()*.25+l.getDifficulty().getId()*.11);net.minecraft.world.entity.projectile.Projectile.spawnProjectile(arrow,l,new ItemStack(Items.ARROW));playSound(SoundEvents.SKELETON_SHOOT,1,1);}
        else if(getMainHandItem().is(SimpleContent.ITEMS.get("pech_wand").get())){var bolt=new PechBolt(PechContent.BOLT.get(),l);bolt.setOwner(this);bolt.effect(random.nextBoolean()?0:random.nextBoolean()?1:random.nextBoolean()?2:random.nextBoolean()?3:4);bolt.setPos(getEyePosition());var d=target.getEyePosition().subtract(getEyePosition());bolt.shoot(d.x,d.y,d.z,.75F,(float)(distanceTo(target)/6));l.addFreshEntity(bolt);swingForAttack(InteractionHand.MAIN_HAND);playSound(SoundEvents.EVOKER_CAST_SPELL,.4F,1);}
    }
    public void handleEntityEvent(byte event){if(event==16)mumble=(float)Math.PI;else if(event==17||event==19)mumble=(float)Math.PI*2;else super.handleEntityEvent(event);if(event==18||event==19)for(int i=0;i<5;i++)level().addParticle(event==18?ParticleTypes.HAPPY_VILLAGER:ParticleTypes.ANGRY_VILLAGER,getRandomX(1),getY()+.5+random.nextFloat(),getRandomZ(1),0,.02,0);}
    protected SoundEvent getAmbientSound(){if(!level().isClientSide()){if(random.nextInt(3)==0&&!level().getEntitiesOfClass(Pech.class,getBoundingBox().inflate(4,2,4),p->p!=this&&p.isAlive()).isEmpty()){level().broadcastEntityEvent(this,(byte)17);return PechContent.TRADE.get();}level().broadcastEntityEvent(this,(byte)16);}return PechContent.IDLE.get();}protected SoundEvent getHurtSound(DamageSource s){return PechContent.HIT.get();}protected SoundEvent getDeathSound(){return PechContent.DEATH.get();}protected float getSoundVolume(){return .4F;}public int getAmbientSoundInterval(){return 120;}
    protected void dropCustomDeathLoot(ServerLevel l,DamageSource source,boolean player){super.dropCustomDeathLoot(l,source,player);for(int i=0;i<9;i++){var s=pack.removeItemNoUpdate(i);if(!s.isEmpty()&&random.nextFloat()<.33F)spawnAtLocation(l,s);}for(int i=0;i<5;i++){var s=escrow.removeItemNoUpdate(i);if(!s.isEmpty())spawnAtLocation(l,s);}escrowOwner=null;trader=null;}
    protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);o.putInt("pech_variant",variant());o.putInt("pech_anger",anger());o.putBoolean("pech_friend",friendly());for(int i=0;i<9;i++)if(!pack.getItem(i).isEmpty())o.store("pech_pack_"+i,ItemStack.CODEC,pack.getItem(i));for(int i=0;i<5;i++)if(!escrow.getItem(i).isEmpty())o.store("pech_escrow_"+i,ItemStack.CODEC,escrow.getItem(i));if(escrowOwner!=null)o.putString("pech_escrow_owner",escrowOwner.toString());}
    protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);if(i.getInt("pech_variant").isPresent())variant(Math.clamp(i.getIntOr("pech_variant",0),0,2));entityData.set(ANGER,Math.clamp(i.getIntOr("pech_anger",0),0,799));friendly(i.getBooleanOr("pech_friend",false));for(int n=0;n<9;n++)pack.setItem(n,i.read("pech_pack_"+n,ItemStack.CODEC).orElse(ItemStack.EMPTY));for(int n=0;n<5;n++)escrow.setItem(n,i.read("pech_escrow_"+n,ItemStack.CODEC).orElse(ItemStack.EMPTY));try{escrowOwner=UUID.fromString(i.getStringOr("pech_escrow_owner",""));}catch(IllegalArgumentException bad){escrowOwner=null;}trader=null;}
}
