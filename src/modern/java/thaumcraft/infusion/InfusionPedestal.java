package thaumcraft.infusion;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import com.mojang.serialization.*;
public final class InfusionPedestal extends thaumcraft.devices.AuraDeviceEntity{
    private ItemStack item=ItemStack.EMPTY;
    private Tag unsupported;
    private final IItemHandler handler=new IItemHandler(){
        public int getSlots(){return 1;}public int getSlotLimit(int s){return 1;}public ItemStack getStackInSlot(int s){return s==0?item.copy():ItemStack.EMPTY;}public boolean isItemValid(int s,ItemStack stack){return s==0&&supported();}
        public ItemStack insertItem(int s,ItemStack stack,boolean simulate){if(s!=0||!supported()||!item.isEmpty()||stack.isEmpty())return stack;var rest=stack.copy();rest.shrink(1);if(!simulate)setItem(stack.copyWithCount(1));return rest;}
        public ItemStack extractItem(int s,int n,boolean simulate){if(s!=0||n<1||!supported())return ItemStack.EMPTY;var result=item.copy();if(!simulate)setItem(ItemStack.EMPTY);return result;}
    };
    private LazyOptional<IItemHandler> capability=LazyOptional.of(()->handler);
    public InfusionPedestal(BlockPos p,BlockState s){super(InfusionContent.PEDESTAL_TYPE.get(),p,s);}
    public boolean supported(){return unsupported==null;}
    public ItemStack item(){return item.copy();}
    public boolean setItem(ItemStack value){if(!supported()||!value.isEmpty()&&value.getCount()!=1)return false;item=value.copy();sync();if(level!=null)level.updateNeighbourForOutputSignal(worldPosition,getBlockState().getBlock());return true;}
    @Override public void serverTick(){}
    public InteractionResult interact(Player p,InteractionHand hand){var held=p.getItemInHand(hand);if(held.is(thaumcraft.research.ResearchContent.METER.get()))return InteractionResult.PASS;if(level.isClientSide())return InteractionResult.SUCCESS;if(!supported()||p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)>64)return InteractionResult.FAIL;if(item.isEmpty()){if(held.isEmpty())return InteractionResult.PASS;setItem(held.split(1));}else{var out=item.copy();setItem(ItemStack.EMPTY);if(!p.getInventory().add(out))Block.popResource(level,worldPosition,out);}p.getInventory().setChanged();p.inventoryMenu.broadcastChanges();return InteractionResult.SUCCESS;}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);if(unsupported!=null)out.store("offering",Codec.PASSTHROUGH,new Dynamic<>(NbtOps.INSTANCE,unsupported));else if(!item.isEmpty())out.store("offering",ItemStack.CODEC,item);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);item=ItemStack.EMPTY;unsupported=null;var raw=in.read("offering",Codec.PASSTHROUGH);if(raw.isPresent()){Tag t=raw.get().convert(NbtOps.INSTANCE).getValue();var parsed=ItemStack.CODEC.parse(NbtOps.INSTANCE,t).result();if(parsed.isPresent()&&parsed.get().getCount()==1)item=parsed.get();else unsupported=t.copy();}}
    @Override public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(level instanceof ServerLevel&&supported()){var drop=item;item=ItemStack.EMPTY;Block.popResource(level,p,drop);}}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction side){return !isRemoved()&&cap==ForgeCapabilities.ITEM_HANDLER?capability.cast():super.getCapability(cap,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();capability.invalidate();}
    @Override public void reviveCaps(){super.reviveCaps();capability=LazyOptional.of(()->handler);}
}
