package thaumcraft.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import thaumcraft.equipment.*;
public final class AccessoryScreen extends AbstractContainerScreen<AccessoryMenu> {
 public AccessoryScreen(AccessoryMenu m,Inventory i,Component t){super(m,i,t,176,156);inventoryLabelY=62;}
 public void extractBackground(GuiGraphicsExtractor g,int x,int y,float partial){super.extractBackground(g,x,y,partial);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xffb9afcc);for(var slot:menu.slots){g.fill(leftPos+slot.x-1,topPos+slot.y-1,leftPos+slot.x+17,topPos+slot.y+17,0xff17141f);g.fill(leftPos+slot.x,topPos+slot.y,leftPos+slot.x+16,topPos+slot.y+16,0xff524661);}for(int i=0;i<7;i++)g.text(font,Integer.toString(i+1),leftPos+31+i*18,topPos+17,0xff292036,false);g.text(font,Component.translatable("equipment.thaumcraft.slot_help"),leftPos+8,topPos+50,0xff292036,false);for(int i=0;i<7;i++)if(x>=leftPos+26+i*18&&x<leftPos+42+i*18&&y>=topPos+28&&y<topPos+44)g.setTooltipForNextFrame(font,java.util.List.of(Component.translatable("equipment.thaumcraft.slot_"+AccessorySlots.TYPES.get(i))),java.util.Optional.empty(),x,y);}
}
