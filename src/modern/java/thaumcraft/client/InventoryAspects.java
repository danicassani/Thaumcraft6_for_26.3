package thaumcraft.client;
import java.util.*;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.crystals.CrystalAspects;
import thaumcraft.research.InventoryInspection;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class InventoryAspects {
 private static Object world;private static ItemStack hovered=ItemStack.EMPTY;private static int token,requested=-100,slot=-1,container=-1;private static InventoryInspection.Reply reply,lastUpdate;
 private static void reset(){world=null;hovered=ItemStack.EMPTY;reply=null;lastUpdate=null;slot=-1;container=-1;requested=-100;token++;InventoryInspection.clear();}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){reset();}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(world!=mc.level){reset();world=mc.level;}if(mc.player==null||!(mc.gui.screen() instanceof AbstractContainerScreen<?>)){hovered=ItemStack.EMPTY;reply=null;}}
 @SubscribeEvent public static void factories(RegisterClientTooltipComponentFactoriesEvent e){e.register(Row.class,r->r);}
 @SubscribeEvent public static void gather(RenderTooltipEvent.GatherComponents e){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!(mc.gui.screen() instanceof AbstractContainerScreen<?> screen))return;var under=screen.getSlotUnderMouse();var stack=e.getItemStack();if(under==null||stack.isEmpty()||!ItemStack.isSameItemSameComponents(stack,under.getItem()))return;
  int now=mc.player.tickCount;boolean changed=slot!=under.index||container!=screen.getMenu().containerId||!ItemStack.isSameItemSameComponents(hovered,stack);
  if(changed||now-requested>=20){hovered=stack.copyWithCount(1);slot=under.index;container=screen.getMenu().containerId;requested=now;if(changed)reply=null;InventoryInspection.request(++token,container,slot);}
  var update=InventoryInspection.received();if(update!=null&&update!=lastUpdate&&update.token()==token){lastUpdate=update;if(ItemStack.isSameItemSameComponents(hovered,InventoryInspection.item(update,mc.level.registryAccess())))reply=update;}
  if(reply!=null&&reply.known()&&!reply.aspects().isEmpty())e.getTooltipElements().add(Either.right(new Row(reply.aspects(),Math.max(1,Math.min(6,(e.getScreenWidth()-24)/45)))));
 }
 public record Row(Map<String,Integer> aspects,int columns) implements TooltipComponent,ClientTooltipComponent {
  public Row{aspects=Map.copyOf(aspects);columns=Math.max(1,Math.min(columns,aspects.size()));}
  private int cell(Font f){int w=32;for(int n:aspects.values())w=Math.max(w,18+f.width(Integer.toString(n))+4);return w;}
  public int getWidth(Font f){return columns*cell(f);}public int getHeight(Font f){return ((aspects.size()+columns-1)/columns)*20+2;}
  public void extractImage(Font f,int x,int y,int w,int h,GuiGraphicsExtractor g){int i=0;for(var value:new TreeMap<>(aspects).entrySet()){int xx=x+(i%columns)*cell(f),yy=y+2+(i++/columns)*20;g.blit(RenderPipelines.GUI_TEXTURED,Identifier.parse("thaumcraft:textures/aspects/"+value.getKey()+".png"),xx,yy,0,0,16,16,32,32,32,32,0xff000000|CrystalAspects.COLORS.getOrDefault(value.getKey(),0xffffff));g.text(f,Integer.toString(value.getValue()),xx+18,yy+5,0xffeeeeee,false);}}
 }
}
