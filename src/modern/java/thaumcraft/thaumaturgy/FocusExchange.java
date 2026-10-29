package thaumcraft.thaumaturgy;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.util.BlockSnapshot;
import thaumcraft.aura.AuraManager;

/** Single-position exchange: stage quietly, authorize both events, then pay and publish. */
public final class FocusExchange {
    private static final int QUIET=Block.UPDATE_SKIP_ALL_SIDEEFFECTS|Block.UPDATE_INVISIBLE;
    private static final Set<ServerLevel> ACTIVE=Collections.newSetFromMap(new IdentityHashMap<>());
    private FocusExchange() {}
    public static float cost(FocusProgram.Effect effect){return .25F+(effect.silk()?.25F:0)+effect.fortune()*.1F;}
    private static boolean permitted(ServerPlayer p,BlockPos q,Direction face) {
        var l=p.level();return p.isAlive()&&!p.isRemoved()&&!p.isSpectator()&&p.getAbilities().mayBuild&&!p.blockActionRestricted(l,q,p.gameMode.getGameModeForPlayer())
            &&!l.isOutsideBuildHeight(q)&&l.getWorldBorder().isWithinBounds(q)&&l.hasChunkAt(q)&&l.mayInteract(p,q)&&p.mayUseItemAt(q,face,p.getMainHandItem());
    }
    private static ItemStack material(ServerPlayer p,ItemStack sample,boolean typeOnly) {
        boolean container=typeOnly&&sample.getComponentsPatch().isEmpty()&&sample.getItem() instanceof BlockItem block&&block.getBlock().defaultBlockState().hasBlockEntity();
        for(var stack:p.getInventory().getNonEquipmentItems())if(matches(stack,sample,container))return stack;
        var off=p.getOffhandItem();return matches(off,sample,container)?off:ItemStack.EMPTY;
    }
    private static boolean matches(ItemStack stack,ItemStack sample,boolean container) {
        return !stack.isEmpty()&&(container?stack.is(sample.getItem()):ItemStack.isSameItemSameComponents(stack,sample));
    }
    /** Restore the same live entity, including inventories held by an open menu. */
    private static void restore(ServerLevel l,BlockPos q,BlockState state,BlockEntity original) {
        quietly(l,q,state);
        if(original!=null&&l.getBlockEntity(q)!=original){
            l.removeBlockEntity(q);original.setBlockState(state);original.reviveCaps();l.setBlockEntity(original);
        }
    }
    private static boolean prepareEntity(ServerLevel l,ServerPlayer p,BlockPos q,BlockState state,ItemStack item) {
        var entity=l.getBlockEntity(q);
        if(!state.hasBlockEntity())return entity==null&&!item.has(DataComponents.BLOCK_ENTITY_DATA)&&!item.has(DataComponents.CONTAINER);
        if(entity==null)return false;
        if(item.has(DataComponents.BLOCK_ENTITY_DATA)&&!BlockItem.updateCustomBlockEntityTag(l,p,q,item))return false;
        entity.applyComponentsFromItemStack(item);entity.setChanged();
        return !(entity instanceof thaumcraft.devices.AuraDeviceEntity device)||device.supported();
    }
    /** Capture suppresses onPlace, client packets and neighbour/shape callbacks until commit. */
    private static boolean quietly(ServerLevel l,BlockPos q,BlockState state) {
        int before=l.capturedBlockSnapshots.size();l.captureBlockSnapshots=true;
        try{return l.setBlock(q,state,QUIET);}
        finally{l.captureBlockSnapshots=false;l.capturedBlockSnapshots.subList(before,l.capturedBlockSnapshots.size()).clear();}
    }
    public static boolean exchange(ServerPlayer p,BlockHitResult hit,FocusProgram.Effect effect) {
        var l=p.level();if(!l.getServer().isSameThread())throw new IllegalStateException("Exchange outside server thread");
        var caster=p.getMainHandItem();var program=ExchangeSelection.program(caster);var q=hit.getBlockPos();
        if(!effect.valid()||!effect.kind().equals("exchange")||program==null||!ExchangeSelection.supports(caster)||!program.knows(p)||!permitted(p,q,hit.getDirection())||l.captureBlockSnapshots||l.restoringBlockSnapshots||!ACTIVE.add(l))return false;
        try{return replace(p,caster,q,hit,effect);}finally{ACTIVE.remove(l);}
    }
    private static boolean replace(ServerPlayer p,ItemStack caster,BlockPos q,BlockHitResult hit,FocusProgram.Effect effect) {
        var l=p.level();var source=l.getBlockState(q);
        var original=l.getBlockEntity(q);
        if(!ExchangeSelection.singleBlock(source)||source.hasBlockEntity()&&original==null||!Float.isFinite(source.getDestroySpeed(l,q))||source.getDestroySpeed(l,q)<0)return false;
        if(original instanceof thaumcraft.devices.AuraDeviceEntity device&&!device.supported())return false;
        var sample=ExchangeSelection.selected(caster);
        // Previously crafted foci used the off hand; keep that path until a sample is selected.
        if(sample.isEmpty()&&!caster.has(ThaumaturgyContent.PICKED_BLOCK.get()))sample=p.getOffhandItem();
        if(!(sample.getItem() instanceof BlockItem block))return false;
        boolean creative=p.getAbilities().instabuild;
        var payment=material(p,sample,caster.has(ThaumaturgyContent.PICKED_BLOCK.get()));
        if(!creative&&payment.isEmpty())return false;
        // Consume and place the actual inventory object, retaining its name and contents.
        sample=(!payment.isEmpty()?payment:sample).copyWithCount(1);
        var context=new BlockPlaceContext(l,p,InteractionHand.MAIN_HAND,sample,hit){{replaceClicked=true;}@Override public BlockPos getClickedPos(){return q;}@Override public boolean replacingClickedOnBlock(){return true;}};
        var intended=block.getBlock().getStateForPlacement(context);
        if(intended==null)return false;
        intended=sample.getOrDefault(DataComponents.BLOCK_STATE,BlockItemStateProperties.EMPTY).apply(intended);
        if(!ExchangeSelection.singleBlock(intended)||!intended.getBlock().isEnabled(l.enabledFeatures())||intended.equals(source)||original!=null&&intended.is(source.getBlock())||!intended.canSurvive(l,q)||!l.isUnobstructed(intended,q,CollisionContext.placementContext(p)))return false;
        float cost=creative?0:cost(effect);
        if(AuraManager.drainVis(l,q,cost,true)<cost)return false;
        var tool=FocusHarvest.tool(p,effect);int experience;
        p.setItemInHand(InteractionHand.MAIN_HAND,tool);
        try{experience=net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(l,p.gameMode.getGameModeForPlayer(),p,q);}
        finally{p.setItemInHand(InteractionHand.MAIN_HAND,caster);}
        if(experience<0||!l.getBlockState(q).equals(source)||l.getBlockEntity(q)!=original||!permitted(p,q,hit.getDirection()))return false;
        var snapshot=BlockSnapshot.create(l.dimension(),l,q);
        if(!l.getBlockState(q).equals(source)||!quietly(l,q,intended))return false;
        boolean committed=false;float paid=0;
        try {
            if(original!=null&&l.getBlockEntity(q)==original||!prepareEntity(l,p,q,intended,sample))return false;
            var destination=l.getBlockEntity(q);
            if(net.minecraftforge.event.ForgeEventFactory.onBlockPlace(p,snapshot,hit.getDirection())||!l.getBlockState(q).equals(intended)||l.getBlockEntity(q)!=destination||!permitted(p,q,hit.getDirection())||p.getMainHandItem()!=caster)return false;
            if(!creative&&(payment.isEmpty()||!ItemStack.isSameItemSameComponents(payment,sample)||material(p,sample,false)!=payment))return false;
            if(AuraManager.drainVis(l,q,cost,true)<cost)return false;
            paid=AuraManager.drainVis(l,q,cost,false);if(paid<cost)return false;
            if(!creative)payment.shrink(1);
            committed=true;
        } finally {
            if(!committed){restore(l,q,source,original);if(paid>0)AuraManager.addVis(l,q,paid);}
        }
        // Match native removal order: spill ordinary inventories first; portable containers
        // override this hook and keep their contents for the block loot table instead.
        if(original!=null)original.preRemoveSideEffects(q,source);
        var drops=creative||!l.getGameRules().get(GameRules.BLOCK_DROPS)?List.<ItemStack>of():Block.getDrops(source,l,q,original,p,tool);
        if(creative&&original instanceof ShulkerBoxBlockEntity box&&!box.isEmpty()){
            var portable=new ItemStack(source.getBlock());portable.applyComponents(original.collectComponents());
            var drop=new net.minecraft.world.entity.item.ItemEntity(l,q.getX()+.5,q.getY()+.5,q.getZ()+.5,portable);drop.setDefaultPickUpDelay();l.addFreshEntity(drop);
        }
        source.affectNeighborsAfterRemoval(l,q,false);intended.onPlace(l,q,source,false);
        intended.getBlock().setPlacedBy(l,q,intended,p,sample);
        l.markAndNotifyBlock(q,l.getChunkAt(q),source,intended,Block.UPDATE_ALL,Block.UPDATE_LIMIT);
        if(!creative){for(var drop:drops){var rest=drop.copy();p.getInventory().add(rest);if(!rest.isEmpty())Block.popResource(l,q,rest);}if(experience>0&&l.getGameRules().get(GameRules.BLOCK_DROPS))source.getBlock().popExperience(l,q,experience);p.awardStat(net.minecraft.stats.Stats.BLOCK_MINED.get(source.getBlock()));}
        l.levelEvent(2001,q,Block.getId(source));var sound=intended.getSoundType(l,q,p);l.playSound(null,q,sound.getPlaceSound(),net.minecraft.sounds.SoundSource.BLOCKS,.7F,1);
        l.gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_PLACE,q,net.minecraft.world.level.gameevent.GameEvent.Context.of(p,intended));
        p.inventoryMenu.broadcastChanges();return true;
    }
}
