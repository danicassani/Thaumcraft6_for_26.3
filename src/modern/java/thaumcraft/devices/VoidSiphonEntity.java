package thaumcraft.devices;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import thaumcraft.content.SimpleContent;
import thaumcraft.rifts.FluxRiftEntity;

public final class VoidSiphonEntity extends AuraDeviceEntity {
    public static final int REQUIRED=2000;
    private int progress;
    private ItemStack output=ItemStack.EMPTY;
    private final IItemHandler handler=new IItemHandler() {
        public int getSlots(){return 1;}
        private void check(int slot){if(slot!=0)throw new IndexOutOfBoundsException(slot);}
        public ItemStack getStackInSlot(int slot){check(slot);return output.copy();}
        public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){check(slot);return stack;}
        public ItemStack extractItem(int slot,int amount,boolean simulate){check(slot);if(amount<=0)return ItemStack.EMPTY;var result=output.copyWithCount(Math.min(amount,output.getCount()));if(!simulate&&!result.isEmpty()){output.shrink(result.getCount());sync();}return result;}
        public int getSlotLimit(int slot){check(slot);return 64;}
        public boolean isItemValid(int slot,ItemStack stack){check(slot);return false;}
    };
    private LazyOptional<IItemHandler> capability=LazyOptional.of(()->handler);
    public VoidSiphonEntity(BlockPos pos,BlockState state){super(DeviceContent.SIPHON_TYPE.get(),pos,state);}
    public int progress(){return progress;} public ItemStack output(){return output.copy();}
    @Override public void serverTick(){if(level instanceof ServerLevel server&&server.getGameTime()%20==0)harvest();}
    public void harvest(){
        if(!(level instanceof ServerLevel server)||getBlockState().getValue(AuraDeviceBlock.POWERED)||output.getCount()>=64)return;
        boolean changed=false;var origin=Vec3.atCenterOf(worldPosition).add(0,.5,0);
        if(progress<REQUIRED)for(var rift:server.getEntitiesOfClass(FluxRiftEntity.class,new AABB(worldPosition).inflate(8))) {
            if(!rift.isAlive()||rift.collapsing()||rift.size()<2||rift.position().distanceToSqr(Vec3.atCenterOf(worldPosition))>64)continue;
            // Bounds within eight blocks; preflight all ray chunks before any block access.
            var start=origin.add(rift.position().subtract(origin).normalize());var end=rift.position();
            boolean loaded=true;for(int i=0;i<=16;i++)if(!server.hasChunkAt(BlockPos.containing(start.lerp(end,i/16D)))){loaded=false;break;}
            if(!loaded||server.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,rift)).getType()!=HitResult.Type.MISS)continue;
            float amount=(float)Math.sqrt(rift.size());progress+=(int)amount;rift.setStability(rift.stability()-amount/15);if(server.getRandom().nextInt(33)==0)rift.setSize(rift.size()-1);changed=true;
            server.sendParticles(ParticleTypes.REVERSE_PORTAL,origin.x,origin.y,origin.z,4,.1,.1,.1,.03);
            if(progress>=REQUIRED)break;
        }
        if(progress>=REQUIRED){progress-=REQUIRED;if(output.isEmpty())output=new ItemStack(SimpleContent.ITEMS.get("void_seed").get());else output.grow(1);changed=true;}
        if(changed)sync();
    }
    public InteractionResult interact(Player player){if(level instanceof ServerLevel){var stack=handler.extractItem(0,64,false);if(!stack.isEmpty()&&!player.getInventory().add(stack))Block.popResource(level,worldPosition,stack);player.sendOverlayMessage(Component.translatable("block.thaumcraft.void_siphon.progress",progress,REQUIRED));}return InteractionResult.SUCCESS;}
    @Override public <T> LazyOptional<T> getCapability(Capability<T> cap,Direction side){return !isRemoved()&&cap==ForgeCapabilities.ITEM_HANDLER?capability.cast():super.getCapability(cap,side);}
    @Override public void invalidateCaps(){super.invalidateCaps();capability.invalidate();}
    @Override public void reviveCaps(){super.reviveCaps();capability=LazyOptional.of(()->handler);}
    @Override public void preRemoveSideEffects(BlockPos pos,BlockState state){super.preRemoveSideEffects(pos,state);if(level instanceof ServerLevel){Block.popResource(level,pos,output);output=ItemStack.EMPTY;}}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("progress",progress);if(!output.isEmpty())out.store("output",ItemStack.CODEC,output);}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);progress=Math.clamp(in.getIntOr("progress",0),0,REQUIRED+10);var saved=in.read("output",ItemStack.CODEC).orElse(ItemStack.EMPTY);output=saved.is(SimpleContent.ITEMS.get("void_seed").get())?saved.copyWithCount(Math.min(64,saved.getCount())):ItemStack.EMPTY;}
}
