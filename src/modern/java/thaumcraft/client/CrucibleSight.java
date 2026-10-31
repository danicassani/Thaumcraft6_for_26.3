package thaumcraft.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.alchemy.*;
import thaumcraft.research.ScanTarget;
/** Contents are distinct from the aspects of the crucible block itself. */
public final class CrucibleSight {
 public static AlchemyMachine target(){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gui.screen()!=null||(!ScanTarget.holdingMeter(mc.player)&&!thaumcraft.equipment.FortressMasks.hasGoggles(mc.player))||!(mc.hitResult instanceof BlockHitResult hit)||!mc.level.hasChunkAt(hit.getBlockPos())||mc.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(hit.getBlockPos()))>64)return null;
  return mc.level.getBlockEntity(hit.getBlockPos()) instanceof AlchemyMachine m&&m.kind()==AlchemyBlock.Kind.CRUCIBLE&&m.supported()?m:null;
 }
 public static boolean draw(GuiGraphicsExtractor g){var m=target();if(m==null)return false;var mc=Minecraft.getInstance();var title=Component.translatable("block.thaumcraft.crucible");var values=m.aspects();var row=values.isEmpty()?null:new InventoryAspects.Row(values,Math.max(1,Math.min(6,(mc.getWindow().getGuiScaledWidth()-24)/45)));int width=Math.max(mc.font.width(title)+10,row==null?20:row.getWidth(mc.font)+10),height=18+(row==null?0:row.getHeight(mc.font));int x=Math.max(4,Math.min(mc.getWindow().getGuiScaledWidth()/2+18,mc.getWindow().getGuiScaledWidth()-width-5)),y=Math.max(4,Math.min(mc.getWindow().getGuiScaledHeight()/2+12,mc.getWindow().getGuiScaledHeight()-height-26));g.fill(x,y,x+width,y+height,0xAA20152D);g.text(mc.font,title,x+5,y+4,0xffe9d2fa,false);if(row!=null)row.extractImage(mc.font,x+5,y+16,width,height,g);return true;}
 private CrucibleSight(){}
}
