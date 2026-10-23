package thaumcraft.infusion;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.level.BlockEvent;
import thaumcraft.content.SimpleContent;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.research.AspectCatalog;
import static thaumcraft.infusion.InfusionEnchantments.Kind.*;

public final class InfusionToolEvents {
    private record Harvest(ServerLevel world,ServerPlayer player,BlockPos pos,BlockState state,ItemStack tool,ItemStack reference,Direction face,long time){}
    private static final List<Harvest> HARVESTS=new ArrayList<>();
    private static final Map<UUID,Direction> FACES=new HashMap<>();
    private static boolean secondary,arcing;
    private static int level(ItemStack s,InfusionEnchantments.Kind k){return InfusionEnchantments.level(s,k);}
    private static boolean ore(BlockState s){return s.is(TagKey.create(Registries.BLOCK,Identifier.parse("c:ores")))||java.util.Set.of(Blocks.COAL_ORE,Blocks.DEEPSLATE_COAL_ORE,Blocks.IRON_ORE,Blocks.DEEPSLATE_IRON_ORE,Blocks.COPPER_ORE,Blocks.DEEPSLATE_COPPER_ORE,Blocks.GOLD_ORE,Blocks.DEEPSLATE_GOLD_ORE,Blocks.NETHER_GOLD_ORE,Blocks.DIAMOND_ORE,Blocks.DEEPSLATE_DIAMOND_ORE,Blocks.EMERALD_ORE,Blocks.DEEPSLATE_EMERALD_ORE,Blocks.LAPIS_ORE,Blocks.DEEPSLATE_LAPIS_ORE,Blocks.REDSTONE_ORE,Blocks.DEEPSLATE_REDSTONE_ORE,Blocks.NETHER_QUARTZ_ORE,thaumcraft.content.BasicContent.ORE_AMBER.get(),thaumcraft.content.BasicContent.ORE_CINNABAR.get()).contains(s.getBlock());}
    public static void register(){
        ItemTooltipEvent.BUS.addListener(e->{int warp=InfusionEnchantments.warp(e.getItemStack());if(warp>0)e.getToolTip().add(Component.translatable("infusion.thaumcraft.warping",warp).withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));for(var k:InfusionEnchantments.Kind.values()){int n=level(e.getItemStack(),k);if(n>0)e.getToolTip().add(Component.translatable("enchantment.infusion."+k.name()).append(k.max>1?" "+n:"").withStyle(net.minecraft.ChatFormatting.GOLD));}});
        PlayerInteractEvent.LeftClickBlock.BUS.addListener(e->{if(e.getEntity() instanceof ServerPlayer p){FACES.put(p.getUUID(),e.getFace());int rank=level(p.getMainHandItem(),SOUNDING);if(rank>0&&p.isShiftKeyDown()&&p.getCooldowns().isOnCooldown(p.getMainHandItem())==false){p.getCooldowns().addCooldown(p.getMainHandItem(),20);int radius=rank*4;for(var pos:BlockPos.betweenClosed(e.getPos().offset(-radius,-radius,-radius),e.getPos().offset(radius,radius,radius)))if(p.level().hasChunkAt(pos)&&ore(p.level().getBlockState(pos)))p.level().sendParticles(p,ParticleTypes.ENCHANT,false,false,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,3,.2,.2,.2,0);}}});
        BlockEvent.BreakEvent.BUS.addListener(e->{if(e.getPlayer() instanceof ServerPlayer p&&!p.getAbilities().instabuild&&!p.isShiftKeyDown()&&!secondary&&!e.getResult().isDenied()&&p.getMainHandItem().has(InfusionEnchantments.ENCHANTMENTS.get()))HARVESTS.add(new Harvest(p.level(),p,e.getPos().immutable(),e.getState(),p.getMainHandItem().copy(),p.getMainHandItem(),FACES.getOrDefault(p.getUUID(),Math.abs(p.getXRot())>60?(p.getXRot()>0?Direction.DOWN:Direction.UP):p.getDirection()),p.level().getGameTime()));});
        EntityJoinLevelEvent.BUS.addListener(e->{if(e.getEntity() instanceof ItemEntity item&&e.getLevel() instanceof ServerLevel l)for(int i=HARVESTS.size()-1;i>=0;i--){var h=HARVESTS.get(i);if(h.world!=l||h.time!=l.getGameTime()||item.distanceToSqr(h.pos.getX()+.5,h.pos.getY()+.5,h.pos.getZ()+.5)>2)continue;refine(l,item,h.tool);if(level(h.tool,COLLECTOR)>0){item.setPos(h.player.getX(),h.player.getY()+.5,h.player.getZ());item.setPickUpDelay(10);}break;}});
        TickEvent.ServerTickEvent.Post.BUS.addListener(e->{var pending=List.copyOf(HARVESTS);HARVESTS.clear();for(var h:pending)if(h.player.level()==h.world&&h.world.hasChunkAt(h.pos)&&!h.world.getBlockState(h.pos).equals(h.state))afterHarvest(h.world,h);});
        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(e->FACES.remove(e.getEntity().getUUID()));
        net.minecraftforge.event.server.ServerStoppedEvent.BUS.addListener(e->{HARVESTS.clear();FACES.clear();});
        LivingDamageEvent.BUS.addListener(e->{if(arcing||e.getAmount()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getSource().getDirectEntity()!=p)return;int rank=level(p.getMainHandItem(),ARCING);if(rank==0)return;arcing=true;try{int count=0;for(var target:p.level().getEntitiesOfClass(Mob.class,e.getEntity().getBoundingBox().inflate(1.5+rank,1+rank/2.,1.5+rank))){if(target==e.getEntity()||!target.isAlive()||target.isAlliedTo(p)||target instanceof TamableAnimal t&&t.isTame())continue;target.hurtServer(p.level(),p.level().damageSources().playerAttack(p),e.getAmount()*.5F);p.level().sendParticles(ParticleTypes.ELECTRIC_SPARK,target.getX(),target.getY()+.5,target.getZ(),8,.2,.3,.2,.1);if(++count>=rank)break;}}finally{arcing=false;}});
        LivingDropsEvent.BUS.addListener(e->{if(!(e.getEntity().level() instanceof ServerLevel l)||!(e.getSource().getEntity() instanceof ServerPlayer p))return;var tool=p.getMainHandItem();int rank=level(tool,ESSENCE);var aspects=new TreeMap<>(AspectCatalog.entity(e.getEntity()));if(rank>0&&!aspects.isEmpty()&&l.getRandom().nextInt(5)<rank)for(int i=0;i<rank&&!aspects.isEmpty();i+=2){var keys=new ArrayList<>(aspects.keySet());var a=keys.get(l.getRandom().nextInt(keys.size()));aspects.computeIfPresent(a,(k,v)->v<=1?null:v-1);e.getDrops().add(new ItemEntity(l,e.getEntity().getX(),e.getEntity().getY()+.5,e.getEntity().getZ(),CrystalContent.essence(a,1)));}if(level(tool,COLLECTOR)>0)for(var d:e.getDrops()){d.setPos(p.getX(),p.getY()+.5,p.getZ());d.setPickUpDelay(10);}});
    }
    private static void refine(ServerLevel l,ItemEntity entity,ItemStack tool){int rank=level(tool,REFINING);if(rank==0)return;var stack=entity.getItem();String name=stack.is(Items.RAW_IRON)?"iron":stack.is(Items.RAW_GOLD)?"gold":stack.is(Items.RAW_COPPER)?"copper":"";if(!name.isEmpty()&&l.getRandom().nextFloat()<(rank+1)*.125F)entity.setItem(new ItemStack(SimpleContent.ITEMS.get("cluster_"+name).get(),stack.getCount()));}
    private static void afterHarvest(ServerLevel l,Harvest h){
        var p=h.player;if(p.isRemoved()||p.getAbilities().instabuild||p.getMainHandItem()!=h.reference)return;
        if(level(h.tool,LAMPLIGHT)>0&&l.getBlockState(h.pos).isAir()&&l.getMaxLocalRawBrightness(h.pos)<10)l.setBlockAndUpdate(h.pos,InfusionContent.GLIMMER.get().defaultBlockState());
        if(level(h.tool,DESTRUCTIVE)==0&&level(h.tool,BURROWING)==0)return;
        var positions=new LinkedHashSet<BlockPos>();
        if(level(h.tool,DESTRUCTIVE)>0)for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++)if(a!=0||b!=0)positions.add(switch(h.face.getAxis()){case Y->h.pos.offset(a,0,b);case Z->h.pos.offset(a,b,0);case X->h.pos.offset(0,a,b);});
        if(level(h.tool,BURROWING)>0&&(ore(h.state)||h.state.is(BlockTags.LOGS))){var queue=new ArrayDeque<BlockPos>();var seen=new HashSet<BlockPos>();queue.add(h.pos);seen.add(h.pos);while(!queue.isEmpty()&&positions.size()<32){var from=queue.remove();for(var d:Direction.values()){var q=from.relative(d);if(!seen.add(q)||!l.hasChunkAt(q)||!l.getBlockState(q).is(h.state.getBlock()))continue;positions.add(q);queue.add(q);if(positions.size()>=32)break;}}}
        secondary=true;try{for(var q:positions){if(p.getMainHandItem()!=h.reference||h.reference.isEmpty())break;if(!l.hasChunkAt(q)||!l.mayInteract(p,q)||!p.mayUseItemAt(q,h.face,h.reference)||p.distanceToSqr(q.getX()+.5,q.getY()+.5,q.getZ()+.5)>100)continue;var state=l.getBlockState(q);if(state.isAir()||l.getBlockEntity(q)!=null||state.getDestroySpeed(l,q)<0||h.reference.getDestroySpeed(state)<=1||!state.canHarvestBlock(l,q,p))continue;var context=new Harvest(p.level(),p,q,state,h.tool,h.reference,h.face,l.getGameTime());HARVESTS.add(context);p.gameMode.destroyBlock(q);HARVESTS.remove(context);}}finally{secondary=false;}
    }
}
