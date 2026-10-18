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
import thaumcraft.crafting.*;
import thaumcraft.crystals.CrystalAspects;

@Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT)
public final class ArcaneScreen extends AbstractContainerScreen<ArcaneMenu> {
    private Button craft;
    public ArcaneScreen(ArcaneMenu menu,Inventory inventory,Component title) {super(menu,inventory,title,184,228);inventoryLabelY=134;}
    @SubscribeEvent public static void screens(FMLClientSetupEvent event) {event.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(ArcaneContent.MENU.get(),ArcaneScreen::new));}
    @Override protected void init() {super.init();craft=addRenderableWidget(Button.builder(Component.translatable("menu.thaumcraft.craft"),button->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0)).bounds(leftPos+96,topPos+78,76,20).build());}
    @Override public void containerTick() {super.containerTick();craft.active=menu.ready();}
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial) {
        super.extractBackground(graphics,mouseX,mouseY,partial);
        graphics.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFFB9AFCC);
        graphics.fill(leftPos+4,topPos+18,leftPos+180,topPos+134,0xFF302840);
        for(var slot:menu.slots) {if(slot.index==16)graphics.fill(leftPos+slot.x-2,topPos+slot.y-2,leftPos+slot.x+18,topPos+slot.y+18,0xFFAA88CC);graphics.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF17141F);}
        for(int i=0;i<6;i++) {
            int color=0xFF000000|CrystalAspects.COLORS.get(ArcaneRecipe.PRIMALS.get(i));
            graphics.fill(leftPos+16+i*24,topPos+121,leftPos+32+i*24,topPos+123,color);
            if(menu.crystalCost(i)>0)graphics.text(font,Integer.toString(menu.crystalCost(i)),leftPos+20+i*24,topPos+124,color,false);
        }
        graphics.text(font,Component.translatable("menu.thaumcraft.vis",menu.available(),menu.cost()),leftPos+82,topPos+20,0xFFFFFFFF,false);
        graphics.text(font,Component.translatable("menu.thaumcraft.chunks",menu.chunks()),leftPos+82,topPos+42,0xFFDDD0EE,false);
        if(menu.researchLocked())graphics.text(font,Component.translatable("menu.thaumcraft.research_locked"),leftPos+82,topPos+32,0xFFFF8888,false);
    }
}
