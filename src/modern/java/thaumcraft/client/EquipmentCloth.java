package thaumcraft.client;
import net.minecraft.client.model.geom.ModelPart;
/** Original cloth angles, evaluated afresh for every rendered entity. */
public final class EquipmentCloth {
 private EquipmentCloth(){}
 private static void angle(ModelPart body,String name,float angle){if(body.hasChild(name))body.getChild(name).xRot=angle;}
 public static void animate(ModelPart body,String family,float phase,float speed){float wave=-Math.abs((float)Math.cos(phase*.6662F)*1.4F*speed);
  if(family.equals("crimson_praetor")){float a=(float)Math.cos(phase*.6662F)*1.4F*speed,b=(float)Math.cos(phase*.6662F+(float)Math.PI)*1.4F*speed;angle(body,"LegClothR",a-.1047198F);angle(body,"LegClothL",b-.1047198F);}
  if(family.equals("crimson_plate")||family.equals("crimson_praetor")){angle(body,"Frontcloth1",wave-.1047198F);angle(body,"Frontcloth2",wave-.3316126F);angle(body,"Cloak1",-wave/2+.1396263F);angle(body,"Cloak2",-wave/2+.3069452F);angle(body,"Cloak3",-wave/2+.4465716F);}
  if(family.equals("crimson_robe")){for(String n:java.util.List.of("FrontclothR1","FrontclothL1"))angle(body,n,wave-.1047198F);for(String n:java.util.List.of("FrontclothR2","FrontclothL2"))angle(body,n,wave-.3316126F);for(String n:java.util.List.of("ClothBackR1","ClothBackL1"))angle(body,n,-wave+.1047198F);for(String n:java.util.List.of("ClothBackR2","ClothBackL2","ClothBackR3","ClothBackL3"))angle(body,n,-wave+.2268928F);}
 }
}
