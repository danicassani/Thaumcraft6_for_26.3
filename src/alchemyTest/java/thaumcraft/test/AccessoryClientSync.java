package thaumcraft.test;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import thaumcraft.Thaumcraft;
import thaumcraft.equipment.*;
@Mod.EventBusSubscriber(modid="thaumcraft",value=Dist.CLIENT)
public final class AccessoryClientSync {
 static boolean title,tuned,shown,menu,out,returned;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent.Post e){if(!AccessoryProbe.mode().equals("accessory-client"))return;var mc=Minecraft.getInstance();if(!tuned){tuned=true;mc.options.renderDistance().set(8);mc.options.simulationDistance().set(8);}
  if(AccessoryProbe.ready&&mc.gui.screen() instanceof TitleScreen&&!title){title=true;try{var raw=net.minecraft.nbt.NbtIo.readCompressed(java.nio.file.Path.of("saves/infusion-client/players/data/"+AccessoryProbe.actor.getUUID()+".dat"),net.minecraft.nbt.NbtAccounter.unlimitedHeap());var forge=raw.getCompoundOrEmpty("ForgeData");var data=forge.get(AccessorySlots.ID);AccessoryProbe.check(data!=null,"real player file missing persisted accessory state");var state=AccessorySlots.CODEC.parse(AccessoryProbe.actor.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),data).getOrThrow();AccessoryProbe.check(state.items().size()==7&&state.items().get(1).getItem() instanceof AccessoryItem&&EquipmentContent.runic(state.items().get(1))==2,"real player file lost slot/runic after native save");Thaumcraft.LOGGER.info("ACCESSORIES_PLAYER_SAVE_OK: actual playerdata file contains seven slots and round-tripped ring components");}catch(Exception ex){throw new AssertionError(ex);}Thaumcraft.LOGGER.info("ACCESSORIES_TITLE_READY: native world saved and title active");}
  if(!AccessoryProbe.ready||mc.level==null||mc.player==null)return;if(!shown){int stands=0;for(var en:mc.level.entitiesForRendering())if(en instanceof net.minecraft.world.entity.decoration.ArmorStand&&en.blockPosition().distSqr(AccessoryProbe.P)<64)stands++;if(stands==3&&mc.player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)==22&&mc.player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS)==12){shown=true;Thaumcraft.LOGGER.info("ACCESSORIES_SCREENSHOT_READY: three cult armor sets and blade synchronized on native armor stands; real player fortress armor 22 and toughness 12");}}
  if(mc.player.containerMenu instanceof AccessoryMenu m){if(!menu&&m.slots.size()==43&&m.slots.get(0).hasItem()&&m.slots.get(1).hasItem()&&m.slots.get(2).hasItem()&&m.slots.get(4).hasItem()&&m.slots.get(6).hasItem()){AccessoryProbe.check(EquipmentContent.runic(m.slots.get(1).getItem())==2,"native menu sync lost item component");menu=true;Thaumcraft.LOGGER.info("ACCESSORIES_MENU_OK: B key opened native seven-slot menu, five actual equipped stacks and runic component synchronized");}if(menu&&!out&&!m.slots.get(1).hasItem()&&m.slots.get(7).hasItem()&&m.getCarried().isEmpty()){out=true;Thaumcraft.LOGGER.info("ACCESSORIES_MOVE_OUT_OK: actual native click moved ring into player inventory");}if(out&&!returned&&m.slots.get(1).hasItem()&&!m.slots.get(7).hasItem()&&m.getCarried().isEmpty()){AccessoryProbe.check(EquipmentContent.runic(m.slots.get(1).getItem())==2,"native shift-click lost ring runic");returned=true;Thaumcraft.LOGGER.info("ACCESSORIES_ROUNDTRIP_OK: actual shift-click returned ring to compatible slot without copying or losing components");}}
 }
}
