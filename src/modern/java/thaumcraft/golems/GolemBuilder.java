package thaumcraft.golems;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import thaumcraft.devices.AuraDeviceEntity;
import thaumcraft.essentia.*;
import thaumcraft.research.ResearchService;
public final class GolemBuilder extends AuraDeviceEntity implements MenuProvider,EssentiaTransport {
    public final SimpleContainer output=new SimpleContainer(1){public void setChanged(){super.setChanged();GolemBuilder.this.setChanged();}};
    public GolemDesign draft=GolemDesign.BASIC;
    private GolemDesign pending;private int remaining,total;private UUID owner;
    public GolemBuilder(BlockPos p,BlockState s){super(GolemContent.BUILDER_TYPE.get(),p,s);}
    public boolean working(){return pending!=null;}public int remaining(){return remaining;}public int total(){return total;}
    public boolean valid(Player p){return level!=null&&level==p.level()&&level.getBlockEntity(worldPosition)==this&&p.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition))<=64;}
    private boolean pressReady(){return !getBlockState().getValue(GolemBuilderBlock.ASSEMBLED)||level instanceof ServerLevel l&&GolemPress.loaded(l,worldPosition)&&GolemPress.intact(l,worldPosition);}
    public boolean start(Player p){if(!(level instanceof ServerLevel)||!valid(p)||working()||!output.isEmpty()||!pressReady())return false;if(!draft.knows(p)){p.sendOverlayMessage(Component.translatable("golem.thaumcraft.missing_research"));return false;}
        // Exact reservation from the player's inventory, including component-bearing items. No partial payment.
        var missing=new LinkedHashMap<>(draft.components());record Payment(int slot,int amount){}var payments=new ArrayList<Payment>();
        for(int i=0;i<36;i++){var s=p.getInventory().getItem(i);String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()).toString();int n=Math.min(s.getCount(),missing.getOrDefault(id,0));if(n>0){payments.add(new Payment(i,n));missing.put(id,missing.get(id)-n);}}
        if(missing.values().stream().anyMatch(n->n>0)){p.sendOverlayMessage(Component.translatable("golem.thaumcraft.missing_components"));return false;}
        for(var pay:payments)p.getInventory().removeItem(pay.slot,pay.amount);p.getInventory().setChanged();pending=draft;owner=p.getUUID();remaining=total=draft.cost();sync();return true;
    }
    public boolean edit(Player p,int button){if(!valid(p)||working())return false;if(button==4)return start(p);if(button<0||button>5)return false;draft=draft.cycle(button);sync();return true;}
    private void complete(){if(pending!=null&&remaining==0&&output.isEmpty()){var result=GolemContent.item(pending);output.setItem(0,result);if(level instanceof ServerLevel l){var p=owner==null?null:l.getServer().getPlayerList().getPlayer(owner);if(p!=null)ResearchService.recordCraft(p,result);}pending=null;sync();}}
    public void serverTick(){if(level instanceof ServerLevel l&&getBlockState().getValue(GolemBuilderBlock.ASSEMBLED)&&!GolemPress.loaded(l,worldPosition))return;if(level instanceof ServerLevel server&&getBlockState().getValue(GolemBuilderBlock.ASSEMBLED)&&!GolemPress.intact(server,worldPosition)){GolemPress.dismantle(server,worldPosition,null);return;}if(pending==null)return;if(remaining<=0){complete();return;}if(level.getGameTime()%5==0)for(var face:Direction.values())if(EssentiaNetwork.pull(level,worldPosition,this,face))break;}
    public boolean connects(Direction d){return d!=Direction.UP;}public boolean canInput(Direction d){return connects(d)&&working()&&remaining>0&&pressReady();}public boolean canOutput(Direction d){return false;}public Map<String,Integer> contents(Direction d){return Map.of();}public String suctionType(Direction d){return "machina";}public int suction(Direction d){return canInput(d)?128:0;}public int minimumSuction(){return 0;}
    public boolean insert(String a,Direction d,boolean simulate){if(!canInput(d)||!a.equals("machina"))return false;if(!simulate){remaining--;sync();complete();}return true;}public boolean extract(String a,Direction d,boolean simulate){return false;}
    public Component getDisplayName(){return Component.translatable("block.thaumcraft.golem_builder");}public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new GolemBuilderMenu(id,inv,this);}
    public void preRemoveSideEffects(BlockPos p,BlockState s){super.preRemoveSideEffects(p,s);if(level instanceof ServerLevel server){if(getBlockState().hasProperty(GolemBuilderBlock.ASSEMBLED)&&getBlockState().getValue(GolemBuilderBlock.ASSEMBLED))GolemPress.dismantle(server,p,p);Block.popResource(level,p,output.getItem(0));output.clearContent();}}
    protected void saveAdditional(ValueOutput o){super.saveAdditional(o);o.store("draft",GolemDesign.CODEC,draft);if(pending!=null)o.store("pending",GolemDesign.CODEC,pending);o.putInt("remaining",remaining);o.putInt("total",total);if(owner!=null)o.putString("owner",owner.toString());o.store("output",ItemStack.OPTIONAL_CODEC,output.getItem(0));}
    protected void loadAdditional(ValueInput i){super.loadAdditional(i);draft=i.read("draft",GolemDesign.CODEC).orElse(GolemDesign.BASIC);pending=i.read("pending",GolemDesign.CODEC).orElse(null);total=pending==null?0:pending.cost();remaining=Math.clamp(i.getIntOr("remaining",0),0,total);try{owner=UUID.fromString(i.getStringOr("owner",""));}catch(IllegalArgumentException e){owner=null;}output.setItem(0,i.read("output",ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));}
}
