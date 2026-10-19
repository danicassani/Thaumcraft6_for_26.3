package thaumcraft.golems;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import thaumcraft.devices.AuraDeviceEntity;
public final class SealEntity extends AuraDeviceEntity implements MenuProvider {
    private static final Map<ServerLevel,Map<BlockPos,SealEntity>> INDEX=new WeakHashMap<>();
    private UUID owner,worker;private long lease;public int channel,priority,radius=3;public boolean redstone=true,blacklist;public ItemStack filter=ItemStack.EMPTY;
    public SealEntity(BlockPos p,BlockState s){super(GolemContent.SEAL_TYPE.get(),p,s);}
    public SealKind kind(){return ((SealBlock)getBlockState().getBlock()).kind;}public UUID owner(){return owner;}public void claimOwner(Player p){if(owner==null){owner=p.getUUID();sync();}}
    public boolean owned(Player p){return owner!=null&&owner.equals(p.getUUID());}public boolean valid(Player p){return owned(p)&&level==p.level()&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))<=64;}
    public boolean active(){return level!=null&&!isRemoved()&&level.hasChunkAt(worldPosition)&&level.getBlockEntity(worldPosition)==this&&(!redstone||!level.hasNeighborSignal(worldPosition))&&getBlockState().canSurvive(level,worldPosition);}
    public boolean matches(ItemStack s){if(s.isEmpty())return false;boolean match=filter.isEmpty()||ItemStack.isSameItemSameComponents(s.copyWithCount(1),filter);return blacklist?!match:match;}
    public boolean claim(WorkerGolem g){long now=level.getGameTime();if(!active()||!Objects.equals(owner,g.ownerId())||g.channel()!=channel||!kind().accepts(g.design())||worker!=null&&!worker.equals(g.getUUID())&&lease>now)return false;worker=g.getUUID();lease=now+100;return true;}
    public void release(WorkerGolem g){if(g.getUUID().equals(worker)){worker=null;lease=0;}}
    public BlockPos support(){return worldPosition.relative(getBlockState().getValue(SealBlock.FACING).getOpposite());}
    public IItemHandler handler(){var p=support();if(level==null||!level.hasChunkAt(p))return null;var be=level.getBlockEntity(p);return be==null?null:be.getCapability(ForgeCapabilities.ITEM_HANDLER,getBlockState().getValue(SealBlock.FACING)).orElse(null);}
    public static List<SealEntity> nearby(ServerLevel l,WorkerGolem g){var map=INDEX.get(l);if(map==null)return List.of();return map.values().stream().filter(s->s.active()&&Objects.equals(s.owner,g.ownerId())&&s.channel==g.channel()&&s.worldPosition.distSqr(g.home())<=32*32&&s.kind().accepts(g.design())).sorted(Comparator.<SealEntity>comparingInt(s->-s.priority).thenComparingDouble(s->s.worldPosition.distSqr(g.blockPosition()))).toList();}
    public void serverTick(){if(level instanceof ServerLevel l){INDEX.computeIfAbsent(l,k->new HashMap<>()).put(worldPosition,this);if(!getBlockState().canSurvive(l,worldPosition))l.destroyBlock(worldPosition,true);}}
    public static void unload(net.minecraft.world.level.Level l){if(l instanceof ServerLevel s)INDEX.remove(s);}
    public void setRemoved(){if(level instanceof ServerLevel l){var map=INDEX.get(l);if(map!=null)map.remove(worldPosition,this);}super.setRemoved();}
    public boolean edit(Player p,int button){if(!valid(p))return false;switch(button){case 0->channel=(channel+1)%16;case 1->priority=priority==5?-5:priority+1;case 2->radius=radius==8?1:radius+1;case 3->redstone=!redstone;case 4->{filter=p.getMainHandItem().copyWithCount(1);if(filter.getItem() instanceof GolemBell)filter=p.getOffhandItem().copyWithCount(1);}case 5->filter=ItemStack.EMPTY;case 6->blacklist=!blacklist;default->{return false;}}worker=null;sync();return true;}
    public Component getDisplayName(){return Component.translatable("item.thaumcraft.seal_"+kind().id());}public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new SealMenu(id,inv,this);}
    protected void saveAdditional(ValueOutput o){super.saveAdditional(o);if(owner!=null)o.putString("owner",owner.toString());o.putInt("channel",channel);o.putInt("priority",priority);o.putInt("radius",radius);o.putBoolean("redstone",redstone);o.putBoolean("blacklist",blacklist);o.store("filter",ItemStack.OPTIONAL_CODEC,filter);}
    protected void loadAdditional(ValueInput i){super.loadAdditional(i);try{owner=UUID.fromString(i.getStringOr("owner",""));}catch(IllegalArgumentException e){owner=null;}channel=Math.clamp(i.getIntOr("channel",0),0,15);priority=Math.clamp(i.getIntOr("priority",0),-5,5);radius=Math.clamp(i.getIntOr("radius",3),1,8);redstone=i.getBooleanOr("redstone",true);blacklist=i.getBooleanOr("blacklist",false);filter=i.read("filter",ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY).copyWithCount(1);worker=null;lease=0;}
}
