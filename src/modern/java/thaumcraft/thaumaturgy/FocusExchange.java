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
    private static ItemStack material(ServerPlayer p,ItemStack sample) {
        for(var stack:p.getInventory().getNonEquipmentItems())if(!stack.isEmpty()&&ItemStack.isSameItemSameComponents(stack,sample))return stack;
        var off=p.getOffhandItem();return !off.isEmpty()&&ItemStack.isSameItemSameComponents(off,sample)?off:ItemStack.EMPTY;
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
        if(!ExchangeSelection.singleBlock(source)||l.getBlockEntity(q)!=null||!Float.isFinite(source.getDestroySpeed(l,q))||source.getDestroySpeed(l,q)<0)return false;
        var sample=ExchangeSelection.selected(caster);
        // Previously crafted foci used the off hand; keep that path until a sample is selected.
        if(sample.isEmpty()&&!caster.has(ThaumaturgyContent.PICKED_BLOCK.get()))sample=p.getOffhandItem();
        if(!(sample.getItem() instanceof BlockItem block))return false;
        sample=sample.copyWithCount(1);
        boolean creative=p.getAbilities().instabuild;
        if(!creative&&material(p,sample).isEmpty())return false;
        var context=new BlockPlaceContext(l,p,InteractionHand.MAIN_HAND,sample,hit){@Override public BlockPos getClickedPos(){return q;}@Override public boolean replacingClickedOnBlock(){return true;}};
        var intended=block.getBlock().getStateForPlacement(context);
        if(intended==null)return false;
        intended=sample.getOrDefault(DataComponents.BLOCK_STATE,BlockItemStateProperties.EMPTY).apply(intended);
        if(!ExchangeSelection.singleBlock(intended)||!intended.getBlock().isEnabled(l.enabledFeatures())||intended.equals(source)||!intended.canSurvive(l,q)||!l.isUnobstructed(intended,q,CollisionContext.placementContext(p)))return false;
        float cost=creative?0:cost(effect);
        if(AuraManager.drainVis(l,q,cost,true)<cost)return false;
        var tool=FocusHarvest.tool(p,effect);int experience;
        p.setItemInHand(InteractionHand.MAIN_HAND,tool);
        try{experience=net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(l,p.gameMode.getGameModeForPlayer(),p,q);}
        finally{p.setItemInHand(InteractionHand.MAIN_HAND,caster);}
        if(experience<0||!l.getBlockState(q).equals(source)||!permitted(p,q,hit.getDirection()))return false;
        var snapshot=BlockSnapshot.create(l.dimension(),l,q);
        var drops=creative||!l.getGameRules().get(GameRules.BLOCK_DROPS)?List.<ItemStack>of():Block.getDrops(source,l,q,null,p,tool);
        if(!l.getBlockState(q).equals(source)||!quietly(l,q,intended))return false;
        boolean committed=false;float paid=0;
        try {
            if(net.minecraftforge.event.ForgeEventFactory.onBlockPlace(p,snapshot,hit.getDirection())||!l.getBlockState(q).equals(intended)||!permitted(p,q,hit.getDirection())||p.getMainHandItem()!=caster)return false;
            var payment=creative?ItemStack.EMPTY:material(p,sample);
            if(!creative&&payment.isEmpty())return false;
            if(AuraManager.drainVis(l,q,cost,true)<cost)return false;
            paid=AuraManager.drainVis(l,q,cost,false);if(paid<cost)return false;
            if(!creative)payment.shrink(1);
            committed=true;
        } finally {
            if(!committed){quietly(l,q,source);if(paid>0)AuraManager.addVis(l,q,paid);}
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
