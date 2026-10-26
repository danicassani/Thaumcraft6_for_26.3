package thaumcraft.thaumaturgy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;

/** Persistent sample on the gauntlet; the native use-on packet supplies only a block hit. */
public final class ExchangeSelection {
    private ExchangeSelection() {}
    public static FocusProgram program(ItemStack caster) {
        if(!(caster.getItem() instanceof CasterItem))return null;
        var focus=caster.get(ThaumaturgyContent.FOCUS.get());
        if(focus==null||!(focus.getItem() instanceof FocusItem item))return null;
        var program=focus.get(ThaumaturgyContent.PROGRAM.get());
        return program!=null&&program.valid()&&program.complexity()<=item.capacity?program:null;
    }
    public static boolean supports(ItemStack caster) {
        var program=program(caster);if(program==null)return false;
        return program.graphNodes().stream().anyMatch(n->n.effects().stream().anyMatch(e->e.kind().equals("exchange"))||n.branches().stream().anyMatch(b->b.effects().stream().anyMatch(e->e.kind().equals("exchange"))));
    }
    public static boolean singleBlock(BlockState state) {
        var block=state.getBlock();return !state.isAir()&&!state.hasBlockEntity()&&!(block instanceof DoorBlock)&&!(block instanceof DoublePlantBlock)&&!(block instanceof BedBlock)&&!(block instanceof GameMasterBlock);
    }
    public static ItemStack selected(ItemStack caster) {
        var sample=caster.get(ThaumaturgyContent.PICKED_BLOCK.get());
        return sample!=null&&sample.getItem() instanceof BlockItem block&&singleBlock(block.getBlock().defaultBlockState())?sample:ItemStack.EMPTY;
    }
    public static boolean pick(ServerPlayer p,ItemStack caster,BlockPos pos) {
        var l=p.level();var program=program(caster);
        if(!l.getServer().isSameThread()||!p.isAlive()||p.isSpectator()||p.getMainHandItem()!=caster||!p.isShiftKeyDown()||!supports(caster)||!program.knows(p)||!l.hasChunkAt(pos)||!l.getWorldBorder().isWithinBounds(pos)||p.getEyePosition().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))>36)return false;
        var state=l.getBlockState(pos);if(!singleBlock(state)||state.getDestroySpeed(l,pos)<0||l.getBlockEntity(pos)!=null)return false;
        var sample=state.getCloneItemStack(l,pos,false);
        if(sample.isEmpty()||!(sample.getItem() instanceof BlockItem block)||!singleBlock(block.getBlock().defaultBlockState()))return false;
        caster.set(ThaumaturgyContent.PICKED_BLOCK.get(),sample.copyWithCount(1));p.inventoryMenu.broadcastChanges();
        p.sendOverlayMessage(Component.translatable("caster.thaumcraft.exchange_selected",sample.getHoverName()));return true;
    }
}
