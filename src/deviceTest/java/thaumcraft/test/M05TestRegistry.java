package thaumcraft.test;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thaumcraft.Thaumcraft;
import thaumcraft.api.items.IRechargable;

/** Only compiled with -Pm05Probe; production has no demonstration gear. */
@Mod.EventBusSubscriber(modid = Thaumcraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class M05TestRegistry {
    public static Item rechargeable;
    private M05TestRegistry() {}
    @SubscribeEvent public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            var id = Identifier.parse("thaumcraft:test_rechargeable");
            rechargeable = new Rechargeable(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(1)
                .component(net.minecraft.core.component.DataComponents.ITEM_NAME, Component.literal("Recharge fixture (test only)")));
            helper.register(id, rechargeable);
        });
    }
    private static final class Rechargeable extends Item implements IRechargable {
        Rechargeable(Properties properties) { super(properties); }
        @Override public int getMaxCharge(ItemStack stack, LivingEntity entity) { return 25; }
    }
}
