package thaumcraft.auxiliary;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.*;
import thaumcraft.Registrar;
import thaumcraft.content.SimpleContent;
public final class AuxiliaryContent {
 private static final DeferredRegister<BlockEntityType<?>> TYPES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,"thaumcraft");
 public static final RegistryObject<Block> TRAVEL=block("paving_stone_travel",p->new PavingStone(false,p));
 public static final RegistryObject<Block> BARRIER=block("paving_stone_barrier",p->new PavingStone(true,p));
 public static final RegistryObject<Block> EAR=block("arcane_ear",p->new ArcaneEarBlock(false,p));
 public static final RegistryObject<Block> TOGGLE=block("arcane_ear_toggle",p->new ArcaneEarBlock(true,p));
 public static final RegistryObject<Block> RELAY=block("redstone_relay",RelayBlock::new);
 public static final RegistryObject<Block> FIELD=Registrar.BLOCKS.register("barrier",()->new BarrierField(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id("barrier"))).noOcclusion().noLootTable().replaceable().strength(-1,3600000).pushReaction(net.minecraft.world.level.material.PushReaction.POPPED)));
 public static final RegistryObject<BlockEntityType<ArcaneEarEntity>> EAR_TYPE=TYPES.register("arcane_ear",()->new BlockEntityType<>(ArcaneEarEntity::new,Set.of(EAR.get(),TOGGLE.get())));
 public static final RegistryObject<BlockEntityType<RelayEntity>> RELAY_TYPE=TYPES.register("redstone_relay",()->new BlockEntityType<>(RelayEntity::new,Set.of(RELAY.get())));
 private static RegistryObject<Block> block(String n,java.util.function.Function<BlockBehaviour.Properties,Block>b){var r=Registrar.BLOCKS.register(n,()->b.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,SimpleContent.id(n))).strength(n.startsWith("arcane_ear")?1:2.5F).sound(n.startsWith("arcane_ear")?SoundType.WOOD:SoundType.STONE).noOcclusion()));SimpleContent.BLOCKS.put(n,r);SimpleContent.item(n,p->new net.minecraft.world.item.BlockItem(r.get(),p));return r;}
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){TYPES.register(bus);net.minecraftforge.event.level.NoteBlockEvent.Play.BUS.addListener((event,cancelled)->{if(!cancelled&&event.getLevel() instanceof net.minecraft.server.level.ServerLevel l)ArcaneEarEntity.hear(l,event.getPos(),event.getVanillaNoteId(),event.getInstrument());});}
}
