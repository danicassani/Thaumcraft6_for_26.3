package thaumcraft.utility;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
public abstract class InventoryDevice extends thaumcraft.devices.AuraDeviceEntity implements MenuProvider {
 protected boolean loading;public abstract boolean supported();public abstract boolean accepts(ItemStack stack);public String kind(){return ((UtilityBlock)getBlockState().getBlock()).kind;}
 public final SimpleContainer inventory=new SimpleContainer(1){public void setItem(int slot,ItemStack stack){var old=getItem(slot).copy();super.setItem(slot,stack);if(!loading&&!ItemStack.isSameItemSameComponents(old,stack))patternChanged();}public void setChanged(){super.setChanged();if(!loading)sync();}};
 protected void patternChanged(){}
 protected InventoryDevice(BlockEntityType<?> t,BlockPos p,BlockState s){super(t,p,s);}
 public boolean valid(Player p){return supported()&&!isRemoved()&&level!=null&&level.getBlockEntity(worldPosition)==this&&p.level()==level&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64;}
 public Component getDisplayName(){return Component.translatable("block.thaumcraft."+kind());}public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new UtilityMenu(id,inv,this);}
 private final IItemHandler items=new IItemHandler(){public int getSlots(){return 1;}public ItemStack getStackInSlot(int slot){return slot==0&&!isRemoved()&&supported()?inventory.getItem(0):ItemStack.EMPTY;}public int getSlotLimit(int slot){return kind().equals("spa")?64:1;}public boolean isItemValid(int slot,ItemStack stack){return slot==0&&!isRemoved()&&supported()&&accepts(stack);}public ItemStack insertItem(int slot,ItemStack offered,boolean simulate){if(!isItemValid(slot,offered))return offered;var old=inventory.getItem(0);if(!old.isEmpty()&&!ItemStack.isSameItemSameComponents(old,offered))return offered;int n=Math.min(offered.getCount(),getSlotLimit(slot)-old.getCount());if(n<=0)return offered;if(!simulate)inventory.setItem(0,offered.copyWithCount(old.getCount()+n));return offered.copyWithCount(offered.getCount()-n);}public ItemStack extractItem(int slot,int n,boolean simulate){if(slot!=0||isRemoved()||!supported()||n<=0)return ItemStack.EMPTY;var stack=inventory.getItem(0);var out=stack.copyWithCount(Math.min(n,stack.getCount()));if(!simulate)inventory.setItem(0,stack.copyWithCount(stack.getCount()-out.getCount()));return out;}};
 private LazyOptional<IItemHandler> itemCap=LazyOptional.of(()->items);
 public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction side){return cap==ForgeCapabilities.ITEM_HANDLER&&!(kind().equals("spa")&&side==Direction.UP)?itemCap.cast():super.getCapability(cap,side);}
 public void invalidateCaps(){super.invalidateCaps();itemCap.invalidate();}public void reviveCaps(){super.reviveCaps();itemCap=LazyOptional.of(()->items);}
 public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(level!=null&&!level.isClientSide()&&supported()){var out=inventory.getItem(0);inventory.setItem(0,ItemStack.EMPTY);if(!out.isEmpty())net.minecraft.world.level.block.Block.popResource(level,p,out);}}
}
