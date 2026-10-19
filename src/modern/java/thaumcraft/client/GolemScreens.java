package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.golems.*;
@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class GolemScreens {
    @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->{net.minecraft.client.gui.screens.MenuScreens.register(GolemContent.BUILDER_MENU.get(),Builder::new);net.minecraft.client.gui.screens.MenuScreens.register(GolemContent.SEAL_MENU.get(),Seal::new);});}
    @SubscribeEvent public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(GolemContent.GOLEM.get(),GolemRenderer::new);}
    public static final class Builder extends AbstractContainerScreen<GolemBuilderMenu> {
        private final Button[] buttons=new Button[5];public Builder(GolemBuilderMenu m,Inventory i,Component t){super(m,i,t,176,226);inventoryLabelY=-100;}
        protected void init(){super.init();for(int j=0;j<5;j++){final int action=j;buttons[j]=addRenderableWidget(Button.builder(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action)).bounds(leftPos+8,topPos+20+j*21,j==4?126:160,20).build());}containerTick();}
        public void containerTick(){super.containerTick();var d=menu.design();String[] parts={GolemDesign.MATERIALS.get(d.material()),GolemDesign.HEADS.get(d.head()),GolemDesign.ARMS.get(d.arms()),GolemDesign.ADDONS.get(d.addon())};for(int j=0;j<5;j++)if(buttons[j]!=null){buttons[j].active=menu.value(4)==0;buttons[j].setMessage(Component.translatable("golem.thaumcraft."+(j==4?"build":"part_"+j)).append(j==4?"":" : ").append(j==4?Component.empty():Component.translatable("golem.thaumcraft."+parts[j])));}if(buttons[4]!=null){var tip=Component.empty();for(var e:d.components().entrySet()){var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(e.getKey()));tip.append(new net.minecraft.world.item.ItemStack(item).getHoverName()).append(" × "+e.getValue()+"\n");}if(d.has("smart"))tip.append(Component.translatable("golem.thaumcraft.smart_pending")).append("\n");boolean first=true;for(var trait:d.traits()){if(!first)tip.append(", ");tip.append(Component.translatable("golem.thaumcraft."+trait));first=false;}buttons[4].setTooltip(net.minecraft.client.gui.components.Tooltip.create(tip));}}
        public void extractBackground(GuiGraphicsExtractor g,int x,int y,float t){super.extractBackground(g,x,y,t);background(g,leftPos,topPos,menu.slots);g.text(font,Component.literal("Machina: "+(menu.value(4)==1?menu.value(6)-menu.value(5)+" / "+menu.value(6):menu.design().cost())),leftPos+8,topPos+128,0xFF222222,false);}
    }
    public static final class Seal extends AbstractContainerScreen<SealMenu> {
        private final Button[] buttons=new Button[7];public Seal(SealMenu m,Inventory i,Component t){super(m,i,t,176,226);inventoryLabelY=132;}
        protected void init(){super.init();for(int j=0;j<7;j++){final int action=j;buttons[j]=addRenderableWidget(Button.builder(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action)).bounds(leftPos+8+(j%2)*82,topPos+20+(j/2)*22,78,20).build());}containerTick();}
        public void containerTick(){super.containerTick();String[] keys={"channel_button","priority","radius","redstone","set_filter","clear_filter","blacklist"};for(int j=0;j<7;j++)if(buttons[j]!=null){var c=Component.translatable("golem.thaumcraft."+keys[j]);if(j<3)c.append(" "+menu.value(j));if(j==3||j==6)c.append(" "+(menu.value(j==3?3:4)==1?"✓":"×"));buttons[j].setMessage(c);}}
        public void extractBackground(GuiGraphicsExtractor g,int x,int y,float t){super.extractBackground(g,x,y,t);background(g,leftPos,topPos,menu.slots);g.text(font,Component.translatable("golem.thaumcraft.filter_hint"),leftPos+8,topPos+114,0xFF222222,false);}
    }
    private static void background(GuiGraphicsExtractor g,int x,int y,java.util.List<net.minecraft.world.inventory.Slot> slots){g.fill(x,y,x+176,y+226,0xFFBFB5A0);for(var s:slots){g.fill(x+s.x-1,y+s.y-1,x+s.x+17,y+s.y+17,0xFF38302A);g.fill(x+s.x,y+s.y,x+s.x+16,y+s.y+16,0xFF817665);}}
}
