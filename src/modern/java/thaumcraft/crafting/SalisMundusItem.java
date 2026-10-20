package thaumcraft.crafting;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

public final class SalisMundusItem extends Item {
    public SalisMundusItem(Properties properties) { super(properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level=context.getLevel();var pos=context.getClickedPos();
        if(level.getBlockEntity(pos) instanceof thaumcraft.infusion.InfusionMatrix matrix){if(level.isClientSide())return InteractionResult.SUCCESS;return context.getPlayer()!=null&&matrix.assemble(context.getPlayer(),context.getItemInHand())?InteractionResult.SUCCESS:InteractionResult.FAIL;}
        if(level.getBlockState(pos).is(Blocks.PISTON)){if(level.isClientSide())return InteractionResult.SUCCESS;return context.getPlayer()!=null&&thaumcraft.golems.GolemPress.assemble((ServerLevel)level,context.getPlayer(),pos,context.getItemInHand())?InteractionResult.SUCCESS:InteractionResult.FAIL;}
        if(level.getBlockState(pos).is(Blocks.OBSIDIAN)||level.getBlockState(pos).is(Blocks.NETHER_BRICKS)||level.getBlockState(pos).is(Blocks.IRON_BARS)){if(level.isClientSide())return InteractionResult.SUCCESS;return context.getPlayer()!=null&&thaumcraft.infernal.InfernalStructure.assemble((ServerLevel)level,context.getPlayer(),pos,context.getItemInHand())?InteractionResult.SUCCESS:InteractionResult.FAIL;}
        boolean book=level.getBlockState(pos).is(Blocks.BOOKSHELF);
        boolean crucible=level.getBlockState(pos).is(Blocks.CAULDRON);
        if(!book&&!crucible&&!level.getBlockState(pos).is(Blocks.CRAFTING_TABLE))return InteractionResult.PASS;
        if(level instanceof ServerLevel server) {
            var player=context.getPlayer();
            if(player==null||!thaumcraft.research.ResearchService.knows(player,book?"!gotdream":crucible?"UNLOCKALCHEMY@2":"FIRSTSTEPS@1")){
                if(player!=null)player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable(book?"research.thaumcraft.need_dream":"research.thaumcraft.need_book"));return InteractionResult.FAIL;
            }
            if(!server.setBlockAndUpdate(pos,book?Blocks.AIR.defaultBlockState():crucible?thaumcraft.alchemy.AlchemyContent.CRUCIBLE.get().defaultBlockState():ArcaneContent.WORKBENCH.get().defaultBlockState()))return InteractionResult.FAIL;
            var result=new net.minecraft.world.item.ItemStack(book?thaumcraft.research.ResearchContent.BOOK.get():crucible?thaumcraft.alchemy.AlchemyContent.CRUCIBLE.get().asItem():ArcaneContent.WORKBENCH.get().asItem());
            thaumcraft.research.ResearchService.recordCraft(player,result);
            if(book){if(!player.getInventory().add(result))net.minecraft.world.level.block.Block.popResource(server,pos,result);thaumcraft.research.ResearchService.flag(player,"!gotthaumonomicon");}
            if(context.getPlayer()==null || !context.getPlayer().getAbilities().instabuild)context.getItemInHand().shrink(1);
            server.sendParticles(ParticleTypes.ENCHANT,pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,40,.5,.5,.5,.1);
        }
        return InteractionResult.SUCCESS;
    }
}
