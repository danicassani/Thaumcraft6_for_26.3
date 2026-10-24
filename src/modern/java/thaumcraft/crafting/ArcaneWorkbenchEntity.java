package thaumcraft.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import thaumcraft.aura.AuraManager;
import thaumcraft.crystals.CrystalContent;
import thaumcraft.devices.AuraDeviceEntity;

public final class ArcaneWorkbenchEntity extends AuraDeviceEntity implements MenuProvider {
    // Nine ingredients, six dedicated primal crystal slots, one actual crafted output.
    public final SimpleContainer inventory = new SimpleContainer(16) { @Override public void setChanged() { super.setChanged(); ArcaneWorkbenchEntity.this.setChanged(); } };
    public ArcaneWorkbenchEntity(BlockPos pos, BlockState state) { super(ArcaneContent.WORKBENCH_TYPE.get(), pos, state); }
    @Override public void serverTick() {}
    @Override public Component getDisplayName() { return Component.translatable("block.thaumcraft.arcane_workbench"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new ArcaneMenu(id, inv, this); }
    public boolean charged() { return level != null && level.getBlockState(worldPosition.above()).is(ArcaneContent.CHARGER.get()); }
    public List<BlockPos> sources() {
        var result = new ArrayList<BlockPos>(); int radius = charged() ? 1 : 0;
        for (int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) result.add(worldPosition.offset(x*16,0,z*16));
        return result;
    }
    public int availableVis() {
        if (!(level instanceof ServerLevel server)) return 0;
        int total=0; for (var pos:sources()) { var cell=AuraManager.get(server,pos); if(cell!=null) total+=(int)cell.vis(); }
        return total;
    }
    /** Main-thread preflight then debit. Whole units preserve each chunk's fractional vis. */
    public boolean spendVis(int amount) {
        if (amount < 0 || !(level instanceof ServerLevel server) || availableVis() < amount) return false;
        var sources=sources(); int remaining=amount;
        while (remaining>0) for(var pos:sources) {
            var cell=AuraManager.get(server,pos); int take=cell==null?0:Math.min(remaining,Math.min((int)cell.vis(),Math.max(1,amount/sources.size())));
            if(take>0) { AuraManager.drainVis(server,pos,take,false); remaining-=take; }
        }
        return true;
    }
    public CraftingInput.Positioned input() {
        var stacks=new ArrayList<ItemStack>();for(int i=0;i<9;i++)stacks.add(inventory.getItem(i).copy());
        return CraftingInput.ofPositioned(3,3,stacks);
    }
    public record Match(ItemStack result, int vis, java.util.Map<String,Integer> crystals, NonNullList<ItemStack> remaining, CraftingInput.Positioned input, String research) {}
    public Match match() {
        if (!(level instanceof ServerLevel server)) return null;
        var positioned=input();var input=positioned.input();
        var candidates=server.recipeAccess().getRecipes().stream()
            .filter(h->h.value() instanceof ArcaneRecipe r&&r.matches(input,server))
            .sorted(java.util.Comparator.comparing(h->h.id().toString())).toList();
        if(!candidates.isEmpty()) {
            // Several arcane recipes can share a grid and differ only in their crystals.
            var selected=candidates.stream().filter(h->hasCrystals(((ArcaneRecipe)h.value()).crystals())).findFirst().orElse(candidates.getFirst());
            var recipe=(ArcaneRecipe)selected.value();
            return new Match(recipe.assemble(input),recipe.vis(),recipe.crystals(),recipe.recipe().getRemainingItems(input),positioned,recipe.research());
        }
        var vanilla=server.recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,server);
        if(vanilla.isPresent()) { var recipe=vanilla.get().value();return new Match(recipe.assemble(input),0,java.util.Map.of(),recipe.getRemainingItems(input),positioned,""); }
        return null;
    }
    private boolean hasCrystals(java.util.Map<String,Integer> costs) {
        for(var entry:costs.entrySet()) {
            var stack=inventory.getItem(9+ArcaneRecipe.PRIMALS.indexOf(entry.getKey()));
            if(!stack.is(CrystalContent.ESSENCE.get())||!CrystalContent.aspect(stack).equals(entry.getKey())||stack.getCount()<entry.getValue())return false;
        }
        return true;
    }
    public int visCost(Player p,Match m){return m==null?0:(int)Math.ceil(thaumcraft.equipment.EquipmentContent.visCost(p,m.vis));}
    public boolean canCraft(Match match) {return canCraft(match,match==null?0:match.vis);}
    private boolean canCraft(Match match,int cost) {
        if(match==null || match.result.isEmpty() || availableVis()<cost) return false;
        var output=inventory.getItem(15);
        if(!output.isEmpty() && (!ItemStack.isSameItemSameComponents(output,match.result)||output.getCount()+match.result.getCount()>output.getMaxStackSize()))return false;
        for(var entry:match.crystals.entrySet()) {
            var crystal=inventory.getItem(9+ArcaneRecipe.PRIMALS.indexOf(entry.getKey()));
            if(!crystal.is(CrystalContent.ESSENCE.get()) || !CrystalContent.aspect(crystal).equals(entry.getKey()) || crystal.getCount()<entry.getValue())return false;
        }
        return true;
    }
    public boolean canCraft(Player player,Match match){return canCraft(match,visCost(player,match))&&thaumcraft.research.ResearchService.knows(player,match.research());}
    /** A player-facing reason, evaluated from the same match as the output preview. */
    public int reason(Player player,Match match){if(!inventory.getItem(15).isEmpty())return 0;if(match==null)return 1;if(!thaumcraft.research.ResearchService.knows(player,match.research()))return 2;if(!hasCrystals(match.crystals()))return 3;if(availableVis()<visCost(player,match))return 4;return canCraft(player,match)?0:5;}
    /** Picking up the preview requests this transaction; the server matches and pays again. */
    public boolean craft(Player player) {
        if(!(level instanceof ServerLevel) || !valid(player))return false;
        var match=match();if(!canCraft(player,match)||!spendVis(visCost(player,match)))return false;
        var input=match.input.input();
        for(int y=0;y<input.height();y++)for(int x=0;x<input.width();x++) {
            int i=x+match.input.left()+(y+match.input.top())*3;
            if(!inventory.getItem(i).isEmpty())inventory.removeItem(i,1);
            var remainder=match.remaining.get(x+y*input.width()).copy();if(remainder.isEmpty())continue;
            var existing=inventory.getItem(i);
            if(existing.isEmpty())inventory.setItem(i,remainder);
            else if(ItemStack.isSameItemSameComponents(existing,remainder) && existing.getCount()+remainder.getCount()<=existing.getMaxStackSize())existing.grow(remainder.getCount());
            else if(!player.getInventory().add(remainder))Block.popResource(level,worldPosition,remainder);
        }
        match.crystals.forEach((aspect,count)->inventory.removeItem(9+ArcaneRecipe.PRIMALS.indexOf(aspect),count));
        var output=inventory.getItem(15);if(output.isEmpty())inventory.setItem(15,match.result.copy());else output.grow(match.result.getCount());
        match.result.onCraftedBy(player,match.result.getCount());
        thaumcraft.research.ResearchService.recordCraft(player,match.result);
        inventory.setChanged(); return true;
    }
    public boolean valid(Player player) { return level!=null && player.level()==level && level.getBlockEntity(worldPosition)==this && player.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64; }
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state) { super.preRemoveSideEffects(pos,state);if(level instanceof ServerLevel)for(int i=0;i<16;i++)Block.popResource(level,pos,inventory.removeItemNoUpdate(i)); }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output);for(int i=0;i<16;i++)if(!inventory.getItem(i).isEmpty())output.store("slot"+i,ItemStack.CODEC,inventory.getItem(i)); }
    @Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input);for(int i=0;i<16;i++)inventory.setItem(i,input.read("slot"+i,ItemStack.CODEC).orElse(ItemStack.EMPTY)); }
}
