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
    @SubscribeEvent public static void renderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(GolemContent.GOLEM.get(),GolemRenderer::new);e.registerEntityRenderer(GolemContent.DART.get(),net.minecraft.client.renderer.entity.TippableArrowRenderer::new);}
    public static final class Builder extends AbstractContainerScreen<GolemBuilderMenu> {
        private final Button[] buttons=new Button[6];public Builder(GolemBuilderMenu m,Inventory i,Component t){super(m,i,t,176,238);inventoryLabelY=144;}
        protected void init(){super.init();for(int j=0;j<6;j++){final int action=j;buttons[j]=addRenderableWidget(Button.builder(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action)).bounds(leftPos+8,topPos+18+(j==4?5:j==5?4:j)*18,j==4?126:160,18).build());}containerTick();}
        public void containerTick(){super.containerTick();var d=menu.design();String[] parts={GolemDesign.MATERIALS.get(d.material()),GolemDesign.HEADS.get(d.head()),GolemDesign.ARMS.get(d.arms()),GolemDesign.ADDONS.get(d.addon()),"",GolemDesign.LEGS.get(d.legs())};for(int j=0;j<6;j++)if(buttons[j]!=null){buttons[j].active=menu.value(4)==0;buttons[j].setMessage(Component.translatable("golem.thaumcraft."+(j==4?"build":"part_"+j)).append(j==4?"":" : ").append(j==4?Component.empty():Component.translatable("golem.thaumcraft."+parts[j])));}if(buttons[4]!=null){var tip=Component.empty();for(var e:d.components().entrySet()){var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(e.getKey()));tip.append(new net.minecraft.world.item.ItemStack(item).getHoverName()).append(" × "+e.getValue()+"\n");}boolean first=true;for(var trait:d.traits()){if(!first)tip.append(", ");tip.append(Component.translatable("golem.thaumcraft."+trait));first=false;}buttons[4].setTooltip(net.minecraft.client.gui.components.Tooltip.create(tip));}}
        public void extractBackground(GuiGraphicsExtractor g,int x,int y,float t){super.extractBackground(g,x,y,t);g.fill(leftPos,topPos,leftPos+176,topPos+238,0xFFBFB5A0);background(g,leftPos,topPos,menu.slots);g.text(font,Component.literal("Machina: "+(menu.value(4)==1?menu.value(6)-menu.value(5)+" / "+menu.value(6):menu.design().cost())),leftPos+8,topPos+132,0xFF222222,false);}
    }
    public static final class Seal extends AbstractContainerScreen<SealMenu> {
        private final Button[] buttons=new Button[17];private int page;
        public Seal(SealMenu m,Inventory i,Component t){super(m,i,t,176,226);inventoryLabelY=132;titleLabelY=-100;}
        protected void init(){super.init();for(int j=0;j<17;j++){final int action=j;int n=j%6;buttons[j]=addRenderableWidget(Button.builder(Component.empty(),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action)).bounds(leftPos+8+(n%2)*82,topPos+20+(n/2)*22,78,20).build());}addRenderableWidget(Button.builder(Component.translatable("focal.thaumcraft.page"),b->{page=(page+1)%(menu.kind()==SealKind.USE?3:2);containerTick();}).bounds(leftPos+122,topPos+2,46,14).build());containerTick();}
        public void containerTick(){super.containerTick();String[] keys={"channel_button","priority","radius","redstone","set_filter","clear_filter","blacklist","exact","more","less","guard_targets","silk","use_left","use_air","use_empty","use_sneak","use_provision"};var k=menu.kind();for(int j=0;j<17;j++)if(buttons[j]!=null){buttons[j].visible=j/6==page;buttons[j].active=switch(j){case 4,5->k.hasFilters();case 6->k!=SealKind.STOCK&&k.hasFilters();case 7->k.hasFilters()&&(k.advanced()||k==SealKind.STOCK||k==SealKind.PROVIDER||k==SealKind.USE);case 8,9->k==SealKind.STOCK;case 10->k==SealKind.GUARD_ADVANCED;case 11->k==SealKind.BREAKER_ADVANCED;case 12,13,14,15,16->k==SealKind.USE;default->true;};var c=Component.translatable("golem.thaumcraft."+keys[j]);if(j<3)c.append(" "+menu.value(j));if(j==3||j==6||j==7||j==11)c.append(" "+(menu.value(j==3?3:j==6?4:j==7?16:18)==1?"✓":"×"));if(j>=12)c.append(" "+(menu.value(j+9)==1?"✓":"×"));if(j==10)c=Component.translatable("golem.thaumcraft.guard_"+menu.value(17));buttons[j].setMessage(c);}}
        public void extractBackground(GuiGraphicsExtractor g,int x,int y,float t){super.extractBackground(g,x,y,t);background(g,leftPos,topPos,menu.slots);g.text(font,menu.kind().base()==SealKind.GUARD?Component.translatable("golem.thaumcraft.guard_"+menu.value(17)):menu.kind()==SealKind.STOCK?Component.translatable("golem.thaumcraft.filter_selected",menu.value(15)+1,menu.value(19),menu.value(20)):Component.translatable("golem.thaumcraft.filter_index",menu.value(15)+1,menu.value(19)),leftPos+8,topPos+88,0xFF222222,false);int sx=leftPos+8+menu.value(15)*18;g.fill(sx-1,topPos+107,sx+17,topPos+108,0xFFFFDD66);g.text(font,Component.literal(font.plainSubstrByWidth(title.getString(),110)),leftPos+8,topPos+6,0xFF222222,false);}
    }
    private static void background(GuiGraphicsExtractor g,int x,int y,java.util.List<net.minecraft.world.inventory.Slot> slots){g.fill(x,y,x+176,y+226,0xFFBFB5A0);for(var s:slots){g.fill(x+s.x-1,y+s.y-1,x+s.x+17,y+s.y+17,0xFF38302A);g.fill(x+s.x,y+s.y,x+s.x+16,y+s.y+16,0xFF817665);}}
}
