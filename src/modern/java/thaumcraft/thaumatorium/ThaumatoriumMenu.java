package thaumcraft.thaumatorium;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import thaumcraft.alchemy.*;
public final class ThaumatoriumMenu extends AbstractContainerMenu{
 public static final int ROWS=5;private final Thaumatorium machine;private final Player user;private final SimpleContainer offers=new SimpleContainer(ROWS);private final ContainerData data=new SimpleContainerData(16+ROWS*AlchemyMenu.ASPECTS.size());private List<RecipeHolder<?>> catalog=List.of();private int page;
 public ThaumatoriumMenu(int id,Inventory inv){this(id,inv,null);}
 public ThaumatoriumMenu(int id,Inventory inv,Thaumatorium m){super(ThaumatoriumContent.MENU.get(),id);machine=m;user=inv.player;addSlot(new Slot(m==null?new SimpleContainer(1):m.inventory,0,222,22){public boolean mayPlace(ItemStack s){return machine==null||machine.supported();}});for(int j=0;j<ROWS;j++)addSlot(new Slot(offers,j,12,30+j*22){public boolean mayPlace(ItemStack s){return false;}public boolean mayPickup(Player p){return false;}});addStandardInventorySlots(inv,48,156);addDataSlots(data);refresh();}
 private void refresh(){if(machine==null)return;catalog=machine.recipes().stream().filter(h->{var r=(AlchemyRecipe)h.value();return machine.selections().stream().anyMatch(s->s.id().equals(h.id().identifier().toString()))||thaumcraft.research.ResearchService.knows(user,r.research());}).toList();page=Math.clamp(page,0,Math.max(0,(catalog.size()-1)/ROWS));data.set(0,page+1);data.set(1,Math.max(1,(catalog.size()+ROWS-1)/ROWS));data.set(2,machine.capacity());data.set(3,machine.selections().size());data.set(4,machine.heated()?1:0);data.set(5,machine.powered()?1:0);data.set(6,machine.paid());data.set(7,machine.total());data.set(8,machine.supported()?1:0);for(int j=0;j<ROWS;j++){int at=page*ROWS+j;RecipeHolder<?> h=at<catalog.size()?catalog.get(at):null;var r=h==null?null:(AlchemyRecipe)h.value();offers.setItem(j,r==null?ItemStack.EMPTY:r.result().create());data.set(9+j,h!=null&&machine.selections().stream().anyMatch(s->s.id().equals(h.id().identifier().toString()))?1:0);for(int a=0;a<AlchemyMenu.ASPECTS.size();a++)data.set(16+j*AlchemyMenu.ASPECTS.size()+a,r==null?0:r.aspects().getOrDefault(AlchemyMenu.ASPECTS.get(a),0));}}
 public int value(int i){return data.get(i);}public ItemStack offer(int row){return offers.getItem(row);}public Map<String,Integer> cost(int row){var m=new LinkedHashMap<String,Integer>();for(int a=0;a<AlchemyMenu.ASPECTS.size();a++){int n=value(16+row*AlchemyMenu.ASPECTS.size()+a);if(n>0)m.put(AlchemyMenu.ASPECTS.get(a),n);}return m;}
 public void broadcastChanges(){refresh();super.broadcastChanges();}
 public boolean clickMenuButton(Player p,int b){if(machine==null||!stillValid(p)||p!=user)return false;if(b==5||b==6){page+=b==5?-1:1;refresh();return true;}if(b<0||b>=ROWS)return false;int at=page*ROWS+b;if(at>=catalog.size())return false;boolean ok=machine.toggle(p,catalog.get(at));refresh();return ok;}
 public boolean stillValid(Player p){return machine==null||machine.valid(p);}
 public ItemStack quickMoveStack(Player p,int index){if(index<0||index>=slots.size()||index>0&&index<=ROWS)return ItemStack.EMPTY;var slot=slots.get(index);var s=slot.getItem();var old=s.copy();if(s.isEmpty()||!moveItemStackTo(s,index==0?ROWS+1:0,index==0?slots.size():1,index==0))return ItemStack.EMPTY;if(s.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return old;}
}
