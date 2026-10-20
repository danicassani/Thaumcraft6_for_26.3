package thaumcraft.golems;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.*;
import thaumcraft.aura.AuraManager;
import thaumcraft.devices.AuraDeviceEntity;

/** Ten original repeated-ingredient patterns; physical sided inventories and paid aura. */
public final class PatternCrafter extends AuraDeviceEntity {

    private int pattern;private float power;private int crafts;
    public PatternCrafter(BlockPos p,BlockState s){super(GolemContent.PATTERN_TYPE.get(),p,s);}
    public int pattern(){return pattern;}public float power(){return power;}public int crafts(){return crafts;}
    public static int[] slots(int pattern){return GolemPatterns.slots(pattern);}
    public void cycle(){pattern=(pattern+1)%10;sync();}
    private IItemHandler handler(BlockPos p,Direction face){if(!level.hasChunkAt(p))return null;var tile=level.getBlockEntity(p);return tile==null?null:tile.getCapability(ForgeCapabilities.ITEM_HANDLER,face).orElse(null);}
    private static ItemStackHandler simulated(IItemHandler target){var copy=new ItemStackHandler(target.getSlots()){
        @Override public int getSlotLimit(int slot){return target.getSlotLimit(slot);}
        @Override public boolean isItemValid(int slot,ItemStack s){return target.isItemValid(slot,s);}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){if(target.insertItem(slot,stack,true).getCount()==stack.getCount())return stack;return super.insertItem(slot,stack,simulate);}
    };for(int i=0;i<target.getSlots();i++)copy.setStackInSlot(i,target.getStackInSlot(i).copy());return copy;}
    public boolean craft(){if(!(level instanceof ServerLevel l)||l.hasNeighborSignal(worldPosition))return false;var input=handler(worldPosition.above(),Direction.DOWN);var output=handler(worldPosition.below(),Direction.UP);if(input==null||output==null||input==output||input.getSlots()>256||output.getSlots()>256)return false;
        for(int source=0;source<input.getSlots();source++){
            var sample=input.extractItem(source,1,true);if(sample.isEmpty())continue;var grid=new ArrayList<ItemStack>(Collections.nCopies(9,ItemStack.EMPTY));for(int slot:GolemPatterns.slots(pattern))grid.set(slot,sample.copyWithCount(1));var positioned=CraftingInput.ofPositioned(3,3,grid);var matrix=positioned.input();var match=l.recipeAccess().getRecipeFor(RecipeType.CRAFTING,matrix,l);if(match.isEmpty())continue;var recipe=match.get().value();var result=recipe.assemble(matrix);if(result.isEmpty()||!result.isItemEnabled(l.enabledFeatures()))continue;
            var outputs=new ArrayList<ItemStack>();outputs.add(result);for(var remainder:recipe.getRemainingItems(matrix))if(!remainder.isEmpty())outputs.add(remainder.copy());var simulated=simulated(output);boolean fits=true;for(var item:outputs)if(!ItemHandlerHelper.insertItemStacked(simulated,item.copy(),false).isEmpty()){fits=false;break;}if(!fits)continue;
            int need=GolemPatterns.slots(pattern).length;int[] take=new int[input.getSlots()];for(int i=0;i<take.length&&need>0;i++){var item=input.extractItem(i,need,true);if(ItemStack.isSameItemSameComponents(sample,item)){take[i]=Math.min(item.getCount(),need);need-=take[i];}}if(need>0)continue;
            if(power<1){float available=AuraManager.drainVis(l,worldPosition,5-power,true);if(power+available<.9999F)return false;power+=AuraManager.drainVis(l,worldPosition,5-power,false);sync();}if(power<.9999F)return false;
            var removed=new ArrayList<ItemStack>();boolean paid=true;for(int i=0;i<take.length;i++)if(take[i]>0){var actual=input.extractItem(i,take[i],false);if(!actual.isEmpty())removed.add(actual);if(actual.getCount()!=take[i]||!ItemStack.isSameItemSameComponents(actual,sample)){paid=false;break;}}
            if(!paid){for(var actual:removed){var left=ItemHandlerHelper.insertItemStacked(input,actual,false);if(!left.isEmpty())Block.popResource(l,worldPosition.above(),left);}return false;}
            // Every remainder and result is either inserted or dropped physically even for a changing modded handler.
            for(var item:outputs){var left=ItemHandlerHelper.insertItemStacked(output,item.copy(),false);if(!left.isEmpty())Block.popResource(l,worldPosition.below(),left);}power=Math.max(0,power-1);crafts=crafts==Integer.MAX_VALUE?0:crafts+1;sync();l.levelEvent(2001,worldPosition,Block.getId(Blocks.CRAFTING_TABLE.defaultBlockState()));return true;
        }return false;
    }
    public void serverTick(){if(level.getGameTime()%20==0)craft();}
    protected void saveAdditional(ValueOutput o){super.saveAdditional(o);o.putInt("pattern",pattern);o.putFloat("power",power);o.putInt("crafts",crafts);}
    protected void loadAdditional(ValueInput i){super.loadAdditional(i);pattern=Math.clamp(i.getIntOr("pattern",0),0,9);float fuel=i.getFloatOr("power",0);power=Float.isFinite(fuel)?Math.clamp(fuel,0,5):0;crafts=Math.max(0,i.getIntOr("crafts",0));}
}
