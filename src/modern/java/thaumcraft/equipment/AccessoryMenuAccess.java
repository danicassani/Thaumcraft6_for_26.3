package thaumcraft.equipment;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.registries.*;
import net.minecraftforge.network.*;
public final class AccessoryMenuAccess {
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,"thaumcraft");
 public static final RegistryObject<MenuType<AccessoryMenu>> MENU=MENUS.register("accessories",()->new MenuType<>(AccessoryMenu::new,FeatureFlags.VANILLA_SET));
 private static final SimpleChannel CHANNEL=ChannelBuilder.named("thaumcraft:accessories").networkProtocolVersion(1).simpleChannel();
 public record Open(){}
 public static void open(ServerPlayer p){if(!p.isAlive()||p.isSpectator()||p.containerMenu instanceof AccessoryMenu)return;long now=p.level().getGameTime();long due=p.getPersistentData().getLongOr("tc_accessory_open",0);if(now<due&&due<=now+10)return;var storage=AccessorySlots.storage(p);if(storage==null){p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("equipment.thaumcraft.invalid_accessories"));return;}p.getPersistentData().putLong("tc_accessory_open",now+10);p.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,q)->new AccessoryMenu(id,inv,storage),net.minecraft.network.chat.Component.translatable("equipment.thaumcraft.accessories")));}
 public static void request(){CHANNEL.send(new Open(),PacketDistributor.SERVER.noArg());}
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){MENUS.register(bus);AccessorySlots.register();CHANNEL.messageBuilder(Open.class,NetworkDirection.PLAY_TO_SERVER).encoder((m,b)->{}).decoder(b->new Open()).consumerMainThread((m,c)->{var p=c.getSender();if(p!=null)open(p);}).add().build();}
}
