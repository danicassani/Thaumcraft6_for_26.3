package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.alchemy.*;
public final class AlchemyScreen extends AbstractContainerScreen<AlchemyMenu>{
    public AlchemyScreen(AlchemyMenu m,Inventory i,Component title){super(m,i,title,320,232);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float a){
        super.extractBackground(g,x,y,a);g.fill(leftPos,topPos,leftPos+320,topPos+232,0xFF34273E);g.text(font,title,leftPos+8,topPos+6,0xFFFFE9BA,false);
        g.text(font,Component.translatable(menu.value(1)==0?"alchemy.thaumcraft.invalid":menu.value(0)==1?"alchemy.thaumcraft.burning":"alchemy.thaumcraft.water",menu.value(menu.value(0)==1?4:2),menu.value(menu.value(0)==1?6:3),menu.value(7)),leftPos+8,topPos+20,0xFFFFE9BA,false);
        if(menu.value(1)==1&&menu.value(0)>1){int total=0;for(int i=0;i<AlchemyMenu.ASPECTS.size();i++)total+=menu.value(8+i);g.fill(leftPos+8,topPos+20,leftPos+310,topPos+30,0xFF34273E);g.text(font,Component.translatable("alchemy.thaumcraft.stored",total,menu.value(0)==2?128:250),leftPos+8,topPos+20,0xFFFFE9BA,false);}
        if(menu.value(0)==1)g.text(font,Component.translatable("alchemy.thaumcraft.upgrades",SmelterRules.efficiency(menu.value(45)),menu.value(46),menu.value(48),menu.value(47)),leftPos+8,topPos+134,0xFFDDC8A0,false);
        for(int i=0;i<AlchemyMenu.ASPECTS.size();i++){int n=menu.value(8+i);if(n==0)continue;String aspect=AlchemyMenu.ASPECTS.get(i);int col=i/10,row=i%10;g.text(font,Component.translatable("tc.aspect."+aspect).append(" "+n),leftPos+8+col*78,topPos+44+row*9,0xFF000000|thaumcraft.crystals.CrystalAspects.COLORS.get(aspect),false);}
        for(var slot:menu.slots)if(slot.isActive())g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xFF17141F);
    }
    @Mod.EventBusSubscriber(modid=Thaumcraft.MODID,value=Dist.CLIENT) public static final class Registration{
        @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(AlchemyContent.MENU.get(),AlchemyScreen::new));}
    }
}
