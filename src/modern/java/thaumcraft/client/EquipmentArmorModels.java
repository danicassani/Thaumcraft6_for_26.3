package thaumcraft.client;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import thaumcraft.equipment.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class EquipmentArmorModels {
 private static final List<String> FAMILIES=List.of("fortress","crimson_plate","crimson_robe","crimson_praetor");
 private static final Map<String,Armor> MODELS=new HashMap<>();
 private static ModelLayerLocation layer(String family,boolean inner){return new ModelLayerLocation(Identifier.parse("thaumcraft:armor_"+family+(inner?"_inner":"")),"main");}
 @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){MODELS.clear();for(String f:FAMILIES)for(boolean inner:new boolean[]{false,true})e.registerLayerDefinition(layer(f,inner),()->definition(f,inner));}
 private static LayerDefinition definition(String f,boolean inner){return switch(f){case "fortress"->inner?EquipmentGeometry.fortress_inner():EquipmentGeometry.fortress();case "crimson_plate"->inner?EquipmentGeometry.crimson_plate_inner():EquipmentGeometry.crimson_plate();case "crimson_robe"->inner?EquipmentGeometry.crimson_robe_inner():EquipmentGeometry.crimson_robe();default->inner?EquipmentGeometry.crimson_praetor_inner():EquipmentGeometry.crimson_praetor();};}
 public static IClientItemExtensions extension(){return new IClientItemExtensions(){public Model getGenericArmorModel(HumanoidRenderState state,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){String name=EquipmentContent.name(stack),family=null;for(String f:FAMILIES)if(name.startsWith(f+"_"))family=f;if(name.startsWith("void_robe_"))family="crimson_robe";if(family==null)return original;String f=family;return MODELS.computeIfAbsent(f+":"+slot,k->new Armor(Minecraft.getInstance().getEntityModels().bakeLayer(layer(f,slot==EquipmentSlot.LEGS)),f,slot));}};}
 private static final class Armor extends HumanoidModel<HumanoidRenderState>{
  final String family;final EquipmentSlot slot;
  Armor(ModelPart root,String family,EquipmentSlot slot){super(root);this.family=family;this.slot=slot;}
  void show(ModelPart p,String child,boolean visible){if(p.hasChild(child))p.getChild(child).visible=visible;}
  public void setupAnim(HumanoidRenderState s){super.setupAnim(s);head.visible=hat.visible=body.visible=leftArm.visible=rightArm.visible=leftLeg.visible=rightLeg.visible=false;switch(slot){case HEAD->head.visible=true;case CHEST->{body.visible=true;leftArm.visible=true;rightArm.visible=true;}case LEGS->{body.visible=true;leftLeg.visible=true;rightLeg.visible=true;}case FEET->{leftLeg.visible=true;rightLeg.visible=true;}default->{}}
   EquipmentCloth.animate(body,family,s.walkAnimationPos,s.walkAnimationSpeed);
   if(family.equals("fortress")){int mask=s.headEquipment.getOrDefault(FortressMasks.MASK.get(),-1),set=0;for(var stack:List.of(s.headEquipment,s.chestEquipment,s.legsEquipment))if(EquipmentContent.name(stack).startsWith("fortress_"))set++;for(int i=0;i<3;i++)show(head,"Mask"+i,mask==i);show(head,"Goggles",s.headEquipment.getOrDefault(FortressMasks.GOGGLES.get(),false));for(String n:List.of("OrnamentL","OrnamentL2","OrnamentR","OrnamentR2","Gemornament","Gem"))show(head,n,set>=3);for(String n:List.of("flapL","flapR"))show(head,n,set>=2);show(body,"Book",set>=2);show(body,"Scroll",set>=3);}
  }
 }
}
