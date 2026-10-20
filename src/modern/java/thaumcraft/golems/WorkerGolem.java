package thaumcraft.golems;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import thaumcraft.thaumaturgy.SpellEngine;
/** Persistent owned worker; cargo remains physical throughout task acquisition, travel and delivery. */
public final class WorkerGolem extends IronGolem implements OwnableEntity {
    private static final EntityDataAccessor<Integer> MATERIAL=SynchedEntityData.defineId(WorkerGolem.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LEGS=SynchedEntityData.defineId(WorkerGolem.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CLIMBING=SynchedEntityData.defineId(WorkerGolem.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> HEAD=SynchedEntityData.defineId(WorkerGolem.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ARMS=SynchedEntityData.defineId(WorkerGolem.class,EntityDataSerializers.INT);
    private long aimingSince=-1;
    private GolemDesign design=GolemDesign.BASIC;private UUID owner;private BlockPos home=BlockPos.ZERO;private int channel,mode;private SealEntity job;private long jobUntil,nextAction;private BlockPos targetBlock;private UUID targetEntity;private SealEntity supply;private BlockPos delivery;private ItemStack requested=ItemStack.EMPTY;private int jobRevision;private final Map<BlockPos,Long> retries=new HashMap<>();
    public final SimpleContainer cargo=new SimpleContainer(2);
    public WorkerGolem(EntityType<? extends IronGolem> t,Level l){super(t,l);setPersistenceRequired();}
    protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(MATERIAL,0);b.define(LEGS,0);b.define(CLIMBING,false);b.define(HEAD,0);b.define(ARMS,0);}
    protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
    public EntityReference<LivingEntity> getOwnerReference(){return owner==null?null:EntityReference.of(owner);}
    public boolean alliedTo(ServerPlayer p){if(owner==null)return false;if(owner.equals(p.getUUID()))return true;var other=p.level().getServer().getPlayerList().getPlayer(owner);return other!=null&&other.isAlliedTo(p);}
    public BlockPos delivery(){return delivery;}public ItemStack requested(){return requested.copy();}
    public UUID ownerId(){return owner;}public int channel(){return channel;}public int mode(){return mode;}public BlockPos home(){return home;}public GolemDesign design(){return design;}public int material(){return entityData.get(MATERIAL);}public int legs(){return entityData.get(LEGS);}public int head(){return entityData.get(HEAD);}public int arms(){return entityData.get(ARMS);}
    public int workRadius(){return design.workRadius();}public boolean withinWorkArea(BlockPos pos){return pos.distSqr(home)<=workRadius()*workRadius();}
    public void setHome(BlockPos p){home=p.immutable();clearJob();}public void setMode(int m){mode=Math.clamp(m,0,2);clearJob();stopMoving();}public void setChannel(int c){channel=Math.clamp(c,0,15);clearJob();}
    public void configure(Player p,GolemDesign d,BlockPos h,int ch){owner=p.getUUID();design=d;home=h.immutable();channel=Math.clamp(ch,0,15);applyDesign();setHealth(getMaxHealth());setPos(Vec3.atBottomCenterOf(h));}
    private void applyDesign(){entityData.set(MATERIAL,design.material());entityData.set(HEAD,design.head());entityData.set(ARMS,design.arms());getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(design.has("scout")?56:40);getAttribute(Attributes.MAX_HEALTH).setBaseValue(design.health());getAttribute(Attributes.ARMOR).setBaseValue(design.armor());getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(design.speed());getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(design.damage());getAttribute(Attributes.FLYING_SPEED).setBaseValue(design.speed());getAttribute(Attributes.STEP_HEIGHT).setBaseValue(design.legs()==1?.5:1);entityData.set(LEGS,design.legs());GolemNavigation.configure(this);}
    public boolean fireImmune(){return design!=null&&design.has("fireproof")||super.fireImmune();}
    public boolean hurtServer(ServerLevel l,DamageSource s,float n){return super.hurtServer(l,s,design.has("blastproof")&&s.is(DamageTypeTags.IS_EXPLOSION)?n*.5F:n);}
    public net.minecraft.world.scores.PlayerTeam getTeam(){if(owner!=null&&level() instanceof ServerLevel l){var p=l.getServer().getPlayerList().getPlayer(owner);if(p!=null)return p.getTeam();}return super.getTeam();}
    protected boolean shouldDropLoot(){return false;}
    public void die(DamageSource s){if(level() instanceof ServerLevel l)dropCargo(l);super.die(s);clearJob();}
    public void dropCargo(ServerLevel l){for(int i=0;i<2;i++){var s=cargo.removeItemNoUpdate(i);if(!s.isEmpty())l.addFreshEntity(new ItemEntity(l,getX(),getY(),getZ(),s));}}
    public boolean pack(Player p){if(!(level() instanceof ServerLevel l)||!p.getUUID().equals(owner)||!isAlive()||isRemoved())return false;var s=GolemContent.item(design);s.set(GolemContent.CHANNEL.get(),channel);s.set(GolemContent.HEALTH.get(),Math.clamp(getHealth()/getMaxHealth(),.001F,1F));if(hasCustomName())s.set(DataComponents.CUSTOM_NAME,getCustomName());dropCargo(l);if(!p.addItem(s))l.addFreshEntity(new ItemEntity(l,getX(),getY(),getZ(),s));clearJob();discard();return true;}
    protected InteractionResult mobInteract(Player p,InteractionHand h){if(!(p.getItemInHand(h).getItem() instanceof GolemBell))return InteractionResult.PASS;if(!p.getUUID().equals(owner))return InteractionResult.FAIL;if(!level().isClientSide()){if(p.isShiftKeyDown())pack(p);else{setChannel(p.getItemInHand(h).getOrDefault(GolemContent.CHANNEL.get(),0));setMode((mode+1)%3);p.sendOverlayMessage(Component.translatable("golem.thaumcraft.mode_"+mode));}}return InteractionResult.SUCCESS;}
    private void clearDelivery(){delivery=null;requested=ItemStack.EMPTY;}
    private void clearJob(){aimingSince=-1;if(job!=null)job.release(this);if(supply!=null)supply.release(this);supply=null;job=null;targetBlock=null;targetEntity=null;}
    public void onRemoval(RemovalReason reason){clearJob();super.onRemoval(reason);}
    public ItemStack insertCargo(ItemStack stack,boolean simulate){var left=stack.copy();for(int i=0;i<design.capacity();i++){var s=cargo.getItem(i);if(!s.isEmpty()&&!ItemStack.isSameItemSameComponents(s,left))continue;int amount=Math.min(left.getCount(),s.isEmpty()?left.getMaxStackSize():s.getMaxStackSize()-s.getCount());if(amount<=0)continue;if(!simulate){if(s.isEmpty())cargo.setItem(i,left.copyWithCount(amount));else{s.grow(amount);cargo.setChanged();}}left.shrink(amount);if(left.isEmpty())break;}return left;}
    void useNavigation(net.minecraft.world.entity.ai.navigation.PathNavigation nav,net.minecraft.world.entity.ai.control.MoveControl control){if(navigation!=null)navigation.stop();navigation=nav;moveControl=control;setNoGravity(design.legs()==3);}
    private void stopMoving(){getNavigation().stop();getMoveControl().setWantedPosition(getX(),getY(),getZ(),0);if(legs()==3){setDeltaMovement(Vec3.ZERO);setYya(0);setZza(0);}}
    @Override public boolean onClimbable(){return legs()==2&&entityData.get(CLIMBING)||super.onClimbable();}
    @Override public void tick(){super.tick();if(!level().isClientSide())entityData.set(CLIMBING,legs()==2&&horizontalCollision);if(legs()==3)setNoGravity(true);}
    @Override public boolean causeFallDamage(double distance,float multiplier,DamageSource source){return legs()!=2&&legs()!=3&&super.causeFallDamage(distance,multiplier,source);}
    private boolean carries(){return !cargo.isEmpty();}
    private boolean line(ServerLevel l,Vec3 destination){return SpellEngine.loadedLine(l,getEyePosition(),destination)&&l.clip(new ClipContext(getEyePosition(),destination,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
    private boolean near(Vec3 p){return position().distanceToSqr(p)<4;}
    private void walk(Vec3 p){if(level().hasChunkAt(BlockPos.containing(p))&&SpellEngine.loadedLine((ServerLevel)level(),position(),p))getNavigation().moveTo(p.x,p.y,p.z,1);}
    protected void customServerAiStep(ServerLevel l){
        if(owner==null)return;var p=l.getServer().getPlayerList().getPlayer(owner);if(p==null||!p.isAlive()||p.isSpectator()||p.level()!=l){clearJob();stopMoving();return;}
        if(design.has("repair")&&tickCount%100==0)heal(1);
        if(mode==2){stopMoving();return;}if(mode==1){clearJob();if(distanceToSqr(p)>9&&distanceToSqr(p)<workRadius()*workRadius())walk(p.position());else stopMoving();return;}
        if(tickCount%10!=0&&!(design.has("ranged")&&job!=null&&GolemCombat.combat(job)))return;if(delivery!=null&&cargo.isEmpty())clearDelivery();long now=l.getGameTime();retries.entrySet().removeIf(e->e.getValue()<=now);if(job!=null&&now>jobUntil)retries.put(job.getBlockPos(),now+100);if(job!=null&&(!job.active()||job.revision()!=jobRevision||now>jobUntil||!job.claim(this)))clearJob();
        if(job==null&&delivery!=null){if(!l.hasChunkAt(delivery)){stopMoving();return;}var dest=l.getBlockEntity(delivery) instanceof SealEntity se?se:null;if(dest!=null&&GolemLogistics.belongs(dest,this)&&(dest.kind()==SealKind.STOCK||dest.kind()==SealKind.USE)&&dest.matches(requested)&&dest.wanted(requested)>0){if(!dest.active()||!dest.claim(this)){stopMoving();return;}job=dest;jobRevision=dest.revision();jobUntil=now+200;targetBlock=dest.kind()==SealKind.USE?dest.useTarget():null;}else clearDelivery();}
        if(job==null){for(var candidate:SealEntity.nearby(l,this)){if(!retries.containsKey(candidate.getBlockPos())&&acquire(l,p,candidate)&&candidate.claim(this)){job=candidate;jobRevision=candidate.revision();jobUntil=now+200;break;}targetBlock=null;targetEntity=null;supply=null;}}
        if(job==null){if(blockPosition().distSqr(home)>9&&l.hasChunkAt(home))walk(Vec3.atBottomCenterOf(home));else stopMoving();return;}
        if(supply!=null&&(!GolemLogistics.belongs(supply,this)||!supply.claim(this))){clearJob();return;}
        boolean placing=job.kind()==SealKind.USE&&job.useAir&&supply==null;Vec3 destination=placing?GolemInteraction.stand(job,this):targetBlock!=null?Vec3.atBottomCenterOf(targetBlock):Vec3.atCenterOf(job.getBlockPos());Entity target=targetEntity==null?null:l.getEntity(targetEntity);if(targetEntity!=null){if(target==null||!target.isAlive()||!new AABB(job.getBlockPos()).inflate(job.radius).contains(target.position())){clearJob();return;}destination=target.position();}
        if(design.has("ranged")&&GolemCombat.combat(job)&&target instanceof LivingEntity living){
            if(!GolemCombat.eligible(l,p,this,job,living)){clearJob();stopMoving();return;}
            if(distanceToSqr(living)>GolemCombat.RANGE*GolemCombat.RANGE||!line(l,living.getEyePosition())){aimingSince=-1;walk(living.position());return;}
            stopMoving();getLookControl().setLookAt(living.getEyePosition());if(aimingSince<0)aimingSince=now;if(now-aimingSince<20||now<nextAction)return;
            if(GolemDart.fire(l,this,p,job,living)){nextAction=now+GolemCombat.interval(distanceTo(living));jobUntil=now+200;}return;
        }
        if(placing?position().distanceToSqr(destination)>.64:!near(destination)){if(placing&&l.hasChunkAt(BlockPos.containing(destination))&&SpellEngine.loadedLine(l,position(),destination))getNavigation().moveTo(destination.x,destination.y,destination.z,0,1);else walk(destination);return;}stopMoving();getLookControl().setLookAt(destination.x,destination.y+.3,destination.z);if(now<nextAction)return;nextAction=now+20;
        if(!perform(l,p,target))clearJob();
    }
    private boolean acquire(ServerLevel l,ServerPlayer owner,SealEntity s){
        if(s.kind().base()==SealKind.FILL){if(!carries()||s.handler()==null)return false;for(int i=0;i<design.capacity();i++){var stack=cargo.getItem(i);if(s.matches(stack)&&ItemHandlerHelper.insertItemStacked(s.handler(),stack,true).getCount()<stack.getCount())return true;}return false;}
        if(s.kind()==SealKind.STOCK){if(carries()){for(int i=0;i<design.capacity();i++){var stack=cargo.getItem(i);if(s.matches(stack)&&s.wanted(stack)>0&&GolemLogistics.room(s.handler(),stack)>0){delivery=s.getBlockPos();requested=stack.copyWithCount(1);return true;}}return false;}var plan=GolemLogistics.find(l,this,s);if(plan==null)return false;supply=plan.source();requested=plan.requested();targetBlock=supply.getBlockPos();return true;}
        if(s.kind()==SealKind.USE){if(!s.useReady())return false;if(s.useEmpty||GolemInteraction.slot(this,s)>=0){targetBlock=s.useTarget();return true;}if(carries())return false;var plan=GolemLogistics.find(l,this,s);if(plan==null)return false;supply=plan.source();requested=plan.requested();targetBlock=supply.getBlockPos();return true;}
        if(carries())return false;
        if(s.kind()==SealKind.LUMBER){var root=GolemTasks.root(l,s,blockPosition());if(root==null||!breakable(l,owner,root))return false;targetBlock=root;return true;}
        if(s.kind()==SealKind.BUTCHER){for(var animal:l.getEntitiesOfClass(net.minecraft.world.entity.animal.Animal.class,GolemTasks.area(s),a->GolemTasks.butcher(l,s,owner,a))){if(line(l,animal.getEyePosition())){targetEntity=animal.getUUID();return true;}}return false;}
        if(s.kind().base()==SealKind.EMPTY){var h=s.handler();if(h==null)return false;for(int i=0;i<Math.min(256,h.getSlots());i++){var extracted=h.extractItem(i,64,true);if(s.matches(extracted)&&insertCargo(extracted,true).getCount()<extracted.getCount())return true;}return false;}
        var area=new AABB(s.getBlockPos()).inflate(s.radius);
        if(s.kind().base()==SealKind.PICKUP){for(var e:l.getEntitiesOfClass(ItemEntity.class,area,x->x.isAlive()&&!x.hasPickUpDelay()&&s.matches(x.getItem()))){if(insertCargo(e.getItem(),true).getCount()<e.getItem().getCount()){targetEntity=e.getUUID();return true;}}return false;}
        if(s.kind().base()==SealKind.GUARD){for(var e:l.getEntitiesOfClass(LivingEntity.class,area,x->GolemLogistics.guard(s,owner,x))){if(line(l,e.getEyePosition())){targetEntity=e.getUUID();return true;}}return false;}
        if(s.kind().base()==SealKind.BREAKER){var q=s.support();if(breakable(l,owner,q)&&s.matches(new ItemStack(l.getBlockState(q).getBlock().asItem()))){targetBlock=q;return true;}return false;}
        if(s.kind()==SealKind.HARVEST){for(var q:BlockPos.betweenClosed(s.getBlockPos().offset(-s.radius,-1,-s.radius),s.getBlockPos().offset(s.radius,1,s.radius))){if(!l.hasChunkAt(q))continue;var state=l.getBlockState(q);if(state.getBlock() instanceof CropBlock crop&&crop.isMaxAge(state)&&s.matches(new ItemStack(crop.asItem()))){targetBlock=q.immutable();return true;}}}
        return false;
    }
    private boolean breakable(ServerLevel l,ServerPlayer p,BlockPos q){if(!l.hasChunkAt(q)||!l.mayInteract(p,q)||!p.mayBuild())return false;var state=l.getBlockState(q);return !state.isAir()&&state.getDestroySpeed(l,q)>=0&&state.getDestroySpeed(l,q)<=5&&l.getBlockEntity(q)==null&&!state.is(BlockTags.WITHER_IMMUNE);}
    private boolean perform(ServerLevel l,ServerPlayer p,Entity target){if(job==null||!job.active()||!job.claim(this))return false;var s=job;
        if(s.kind()==SealKind.STOCK||s.kind()==SealKind.USE){
            if(supply!=null){if(!GolemLogistics.belongs(supply,this)||!supply.active()||!supply.claim(this)||!s.matches(requested)||!supply.matches(requested))return false;var h=supply.handler();if(h==null)return false;for(int i=0;i<Math.min(256,h.getSlots());i++){var candidate=h.extractItem(i,Math.min(requested.getCount(),GolemLogistics.deficit(l,this,s,requested)),true);if(candidate.isEmpty()||!s.matchesSample(candidate,requested)||!supply.matches(candidate))continue;int n=Math.min(GolemLogistics.deficit(l,this,s,candidate),Math.min(candidate.getCount()-insertCargo(candidate,true).getCount(),s.kind()==SealKind.USE?1:GolemLogistics.room(s.handler(),candidate)));if(n<=0)continue;var actual=h.extractItem(i,n,false);var rest=insertCargo(actual,false);if(!rest.isEmpty())l.addFreshEntity(new ItemEntity(l,getX(),getY(),getZ(),rest));if(actual.isEmpty())return false;delivery=s.getBlockPos();requested=actual.copyWithCount(1);supply.release(this);supply=null;targetBlock=s.kind()==SealKind.USE?s.useTarget():null;jobUntil=l.getGameTime()+200;return true;}return false;}
            if(s.kind()==SealKind.USE){if(s.useReady())GolemInteraction.click(l,p,this,s);clearDelivery();return false;}
            var h=s.handler();if(h==null)return false;for(int i=0;i<design.capacity();i++){var stack=cargo.getItem(i);if(!s.matches(stack))continue;int n=Math.min(stack.getCount(),s.wanted(stack));if(n<=0)continue;var left=ItemHandlerHelper.insertItemStacked(h,stack.copyWithCount(n),false);stack.shrink(n-left.getCount());cargo.setItem(i,stack.isEmpty()?ItemStack.EMPTY:stack);}if(cargo.isEmpty()||s.wanted(requested)==0)clearDelivery();return false;
        }
        if(s.kind().base()==SealKind.PICKUP&&target instanceof ItemEntity e&&!e.hasPickUpDelay()&&s.matches(e.getItem())&&line(l,e.position().add(0,.1,0))){var left=insertCargo(e.getItem(),false);if(left.isEmpty())e.discard();else e.setItem(left);return false;}
        if(s.kind().base()==SealKind.FILL){var h=s.handler();if(h==null)return false;for(int i=0;i<design.capacity();i++){var stack=cargo.getItem(i);if(s.matches(stack))cargo.setItem(i,ItemHandlerHelper.insertItemStacked(h,stack,false));}return false;}
        if(s.kind().base()==SealKind.EMPTY){var h=s.handler();if(h==null)return false;for(int i=0;i<Math.min(256,h.getSlots());i++){var wanted=h.extractItem(i,64,true);if(!s.matches(wanted))continue;int n=wanted.getCount()-insertCargo(wanted,true).getCount();if(n<=0)continue;var actual=h.extractItem(i,n,false);var left=insertCargo(actual,false);if(!left.isEmpty())l.addFreshEntity(new ItemEntity(l,getX(),getY(),getZ(),left));break;}return false;}
        if(s.kind().base()==SealKind.GUARD&&target instanceof LivingEntity e&&GolemLogistics.guard(s,p,e)&&line(l,e.getEyePosition())){l.broadcastEntityEvent(this,(byte)4);e.hurtServer(l,l.damageSources().mobAttack(this),design.damage());return false;}
        if(s.kind()==SealKind.BUTCHER&&target instanceof net.minecraft.world.entity.animal.Animal a){if(GolemTasks.butcher(l,s,p,a)&&line(l,a.getEyePosition())){l.broadcastEntityEvent(this,(byte)4);a.hurtServer(l,l.damageSources().mobAttack(this),design.damage());}return false;}
        if(s.kind()==SealKind.LUMBER){if(targetBlock==null||!l.getBlockState(targetBlock).is(BlockTags.LOGS)||!GolemTasks.seesTimber(l,this,targetBlock))return false;var cut=GolemTasks.furthest(l,s,targetBlock);if(cut==null||!breakable(l,p,cut))return false;var state=l.getBlockState(cut);var event=new BlockEvent.BreakEvent(l,cut,state,p,net.minecraftforge.common.util.Result.DEFAULT);if(BlockEvent.BreakEvent.BUS.post(event)||event.getResult().isDenied())return false;var drops=Block.getDrops(state,l,cut,null,this,new ItemStack(Items.IRON_AXE));if(!l.removeBlock(cut,false))return false;for(var stack:drops){var left=insertCargo(stack,false);if(!left.isEmpty())Block.popResource(l,cut,left);}jobUntil=l.getGameTime()+200;return l.getBlockState(targetBlock).is(BlockTags.LOGS);}
        if(targetBlock==null||!breakable(l,p,targetBlock)||!line(l,Vec3.atCenterOf(targetBlock).add(0,.6,0)))return false;
        var state=l.getBlockState(targetBlock);if(s.kind()==SealKind.HARVEST&&(!(state.getBlock() instanceof CropBlock crop)||!crop.isMaxAge(state)))return false;
        if((s.kind().base()==SealKind.BREAKER||s.kind()==SealKind.HARVEST)&&!s.matches(new ItemStack(state.getBlock().asItem())))return false;
        if(s.kind()==SealKind.HARVEST&&(Math.abs(targetBlock.getX()-s.getBlockPos().getX())>s.radius||Math.abs(targetBlock.getZ()-s.getBlockPos().getZ())>s.radius||Math.abs(targetBlock.getY()-s.getBlockPos().getY())>1))return false;
        var event=new BlockEvent.BreakEvent(l,targetBlock,state,p,net.minecraftforge.common.util.Result.DEFAULT);if(BlockEvent.BreakEvent.BUS.post(event)||event.getResult().isDenied())return false;
        var tool=s.kind().base()==SealKind.BREAKER?new ItemStack(Items.IRON_PICKAXE):ItemStack.EMPTY;if(s.kind()==SealKind.BREAKER_ADVANCED&&s.silk)tool.enchant(l.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH),1);var drops=Block.getDrops(state,l,targetBlock,null,this,tool);
        if(s.kind()==SealKind.HARVEST){var crop=(CropBlock)state.getBlock();var seed=crop.asItem();boolean paid=false;for(var stack:drops)if(stack.is(seed)&&!paid){stack.shrink(1);paid=true;}if(!paid)return false;l.setBlock(targetBlock,crop.getStateForAge(0),3);}else l.removeBlock(targetBlock,false);
        for(var stack:drops){var left=insertCargo(stack,false);if(!left.isEmpty())Block.popResource(l,targetBlock,left);}return false;
    }
    protected void addAdditionalSaveData(ValueOutput o){super.addAdditionalSaveData(o);if(delivery!=null&&!cargo.isEmpty()){o.putLong("delivery",delivery.asLong());o.store("requested",ItemStack.OPTIONAL_CODEC,requested);}o.putLong("next_action",nextAction);o.store("design",GolemDesign.CODEC,design);if(owner!=null)o.putString("worker_owner",owner.toString());o.putLong("home",home.asLong());o.putInt("channel",channel);o.putInt("mode",mode);var slots=new ArrayList<ItemStack>();for(int i=0;i<2;i++)slots.add(cargo.getItem(i));o.store("cargo",ItemContainerContents.CODEC,ItemContainerContents.fromItems(slots));}
    protected void readAdditionalSaveData(ValueInput i){super.readAdditionalSaveData(i);design=i.read("design",GolemDesign.CODEC).orElse(GolemDesign.BASIC);try{owner=UUID.fromString(i.getStringOr("worker_owner",""));}catch(IllegalArgumentException e){owner=null;}home=BlockPos.of(i.getLongOr("home",0));channel=Math.clamp(i.getIntOr("channel",0),0,15);mode=Math.clamp(i.getIntOr("mode",0),0,2);var slots=NonNullList.withSize(2,ItemStack.EMPTY);i.read("cargo",ItemContainerContents.CODEC).orElse(ItemContainerContents.EMPTY).copyInto(slots);for(int n=0;n<2;n++)cargo.setItem(n,slots.get(n));nextAction=Math.clamp(i.getLongOr("next_action",0),0,level().getGameTime()+25);applyDesign();clearJob();requested=i.read("requested",ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY).copyWithCount(1);delivery=requested.isEmpty()||cargo.isEmpty()?null:BlockPos.of(i.getLongOr("delivery",0));}
}
