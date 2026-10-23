package thaumcraft.pech;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class PechMenu extends AbstractContainerMenu {
    private final Pech pech;private final Container inventory;private final ContainerData data=new SimpleContainerData(3);private long nextRoll;
    public PechMenu(int id,Inventory inv){this(id,inv,null);}
    public PechMenu(int id,Inventory inv,Pech pech){super(PechContent.MENU.get(),id);this.pech=pech;inventory=pech==null?new SimpleContainer(5):pech.escrow;
        addSlot(new Slot(inventory,0,36,29));for(int i=0;i<4;i++)addSlot(new Slot(inventory,i+1,106+(i%2)*18,20+(i/2)*18){public boolean mayPlace(ItemStack s){return false;}});addStandardInventorySlots(inv,8,84);addDataSlots(data);refresh();
    }
    public Pech pech(){return pech;}public int entityId(){return data.get(0);}public int variant(){return data.get(1);}public boolean ready(){return data.get(2)==1;}
    private void refresh(){if(pech==null)return;data.set(0,pech.getId());data.set(1,pech.variant());boolean clear=true;for(int i=1;i<5;i++)clear&=inventory.getItem(i).isEmpty();data.set(2,pech.friendly()&&pech.anger()==0&&clear&&pech.valued(inventory.getItem(0))?1:0);}
    public void broadcastChanges(){refresh();super.broadcastChanges();}
    public boolean stillValid(Player p){return pech==null||pech.tradingWith(p);}
    public boolean clickMenuButton(Player player,int button){
        if(button!=0||!(player instanceof ServerPlayer p)||pech==null||p.containerMenu!=this||!stillValid(p)||p.level().getGameTime()<nextRoll)return false;refresh();if(!ready())return false;
        int value=pech.value(inventory.getItem(0));var rolled=PechTrades.roll(p.level(),pech,value);if(rolled.isEmpty()){p.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("pech.thaumcraft.no_room"));return false;}
        var result=rolled.get();for(int i=0;i<9;i++)pech.pack.setItem(i,result.pack().get(i));for(int i=0;i<4;i++)inventory.setItem(i+1,result.outputs().get(i));inventory.removeItem(0,1);nextRoll=p.level().getGameTime()+4;
        if(pech.getRandom().nextInt(100)<=value/2){pech.friendly(false);pech.playSound(PechContent.TRADE.get(),.4F,1);}pech.playSound(PechContent.DICE.get(),.5F,1);broadcastChanges();return true;
    }
    public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size()||!stillValid(p))return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();if(index<5){if(!moveItemStackTo(stack,5,41,true))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,0,1,false))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,stack);return before;}
    public void removed(Player p){super.removed(p);if(pech!=null&&p instanceof ServerPlayer s)pech.endTrade(s);}
}
