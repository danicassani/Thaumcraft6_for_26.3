package thaumcraft.client;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import thaumcraft.taint.TaintContent;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="thaumcraft",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class TaintFluids{
 @net.minecraftforge.eventbus.api.listener.SubscribeEvent public static void bake(net.minecraftforge.client.event.ModelEvent.BakeFluidModels e){for(var set:java.util.List.of(TaintContent.DEATH,TaintContent.PURE)){var model=new net.minecraft.client.renderer.block.FluidModel.Unbaked(new net.minecraft.client.resources.model.sprite.Material(Identifier.parse(set.texture),true),new net.minecraft.client.resources.model.sprite.Material(Identifier.parse(set==TaintContent.PURE?"minecraft:block/water_flow":set.texture),true),null,null).bake(e.materials(),()->set.id);e.register(set.source.get(),model);e.register(set.flowing.get(),model);}var goo=new net.minecraft.client.renderer.block.FluidModel.Unbaked(new net.minecraft.client.resources.model.sprite.Material(Identifier.parse("thaumcraft:blocks/flux_goo")),new net.minecraft.client.resources.model.sprite.Material(Identifier.parse("thaumcraft:blocks/flux_goo")),null,null).bake(e.materials(),()->"flux_goo");e.register(TaintContent.GOO_FLUID.get(),goo);}

 public static void goo(java.util.function.Consumer<IClientFluidTypeExtensions> out){out.accept(new IClientFluidTypeExtensions(){public Identifier getStillTexture(){return Identifier.parse("thaumcraft:blocks/flux_goo");}public Identifier getFlowingTexture(){return getStillTexture();}});}
 public static void initialize(TaintContent.FluidSet set,java.util.function.Consumer<IClientFluidTypeExtensions> out){out.accept(new IClientFluidTypeExtensions(){public Identifier getStillTexture(){return Identifier.parse(set.texture);}public Identifier getFlowingTexture(){return Identifier.parse(set==TaintContent.PURE?"minecraft:block/water_flow":set.texture);}public int getTintColor(){return set.color;}});}
}
