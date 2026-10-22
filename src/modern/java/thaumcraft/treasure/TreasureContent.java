package thaumcraft.treasure;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import thaumcraft.Registrar;
import thaumcraft.content.SimpleContent;

public final class TreasureContent {
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,"thaumcraft");
    public static final RegistryObject<SoundEvent> COINS=SOUNDS.register("coins",()->SoundEvent.createVariableRangeEvent(SimpleContent.id("coins")));
    public static final RegistryObject<SoundEvent> URN_BREAK=SOUNDS.register("urnbreak",()->SoundEvent.createVariableRangeEvent(SimpleContent.id("urnbreak")));
    public static final Map<String,RegistryObject<Block>> BLOCKS=new LinkedHashMap<>();
    public static final String[] TIERS={"common","uncommon","rare"};
    static {
        for (int tier=0;tier<3;tier++) {
            int index=tier;
            Rarity rarity=Rarity.values()[tier];
            SimpleContent.item("loot_bag_"+TIERS[tier],p->new TreasureBag(index,p.stacksTo(16).rarity(rarity)));
            for (String kind:new String[]{"crate","urn"}) {
                String name="loot_"+kind+"_"+TIERS[tier];
                RegistryObject<Block> block=Registrar.BLOCKS.register(name,()->new TreasureBlock(kind.equals("urn"),
                    BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(name)))
                        .strength(.15F,0).noOcclusion().sound(kind.equals("urn") ?
                            new ForgeSoundType(.5F,1.5F,URN_BREAK,URN_BREAK,URN_BREAK,URN_BREAK,URN_BREAK) : SoundType.WOOD)));
                BLOCKS.put(name,block);SimpleContent.BLOCKS.put(name,block);
                SimpleContent.item(name,p->new BlockItem(block.get(),p.rarity(rarity)));
            }
        }
    }
    public static void register(BusGroup bus) { SOUNDS.register(bus); }
    private TreasureContent() {}
}
