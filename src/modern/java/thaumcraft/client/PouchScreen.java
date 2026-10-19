package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.thaumaturgy.PouchMenu;
public final class PouchScreen extends AbstractContainerScreen<PouchMenu> {
    public PouchScreen(PouchMenu m,Inventory i,Component t){super(m,i,t,176,150);inventoryLabelY=56;}
    public void extractBackground(GuiGraphicsExtractor g,int x,int y,float partial){super.extractBackground(g,x,y,partial);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xffb9afcc);for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff17141f);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xff524661);}}
}
