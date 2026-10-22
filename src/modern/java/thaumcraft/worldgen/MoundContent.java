package thaumcraft.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;

public final class MoundContent {
    private static final DeferredRegister<StructureType<?>> TYPES=DeferredRegister.create(Registries.STRUCTURE_TYPE,"thaumcraft");
    private static final DeferredRegister<StructurePieceType> PIECES=DeferredRegister.create(Registries.STRUCTURE_PIECE,"thaumcraft");
    public static final RegistryObject<StructureType<MoundStructure>> MOUND=TYPES.register("mound",()->()->MoundStructure.CODEC);
    public static final RegistryObject<StructurePieceType> PIECE=PIECES.register("mound",()->(context,tag)->new MoundPiece(tag));
    private MoundContent() {}
    public static void register(BusGroup bus) {TYPES.register(bus);PIECES.register(bus);}
}
