package thaumcraft.banners;
import net.minecraft.world.item.DyeColor;
/** Stable legacy IDs and cloth motion, independent of registration and render submission. */
public final class BannerDesign {
 public static String id(DyeColor d){return "banner_"+(d==DyeColor.LIGHT_BLUE?"lightblue":d==DyeColor.LIGHT_GRAY?"silver":d.getName());}
 public static float wave(float phase){return .02F-(float)Math.sin(phase/11)*.02F;}
 private BannerDesign(){}
}
