package thaumcraft.essentia;

import java.util.Map;
import net.minecraft.core.Direction;

/** Face-local essentia, separate from fluids. Empty suction type means any aspect. */
public interface EssentiaTransport {
    boolean connects(Direction face);
    boolean canInput(Direction face);
    boolean canOutput(Direction face);
    Map<String,Integer> contents(Direction face);
    String suctionType(Direction face);
    int suction(Direction face);
    int minimumSuction();
    boolean insert(String aspect,Direction face,boolean simulate);
    boolean extract(String aspect,Direction face,boolean simulate);
    /** A remote source must not drain the same physical container that is receiving. */
    default boolean extractFor(String aspect,Direction face,EssentiaTransport receiver,boolean simulate){return extract(aspect,face,simulate); }
}
