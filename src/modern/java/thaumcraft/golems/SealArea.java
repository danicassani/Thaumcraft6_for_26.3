package thaumcraft.golems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/** Original TC6 extents: forward along the mounting face, symmetric on the other axes. */
public record SealArea(int x, int y, int z) {
    public static final Codec<SealArea> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("x").forGetter(SealArea::x),
        Codec.INT.fieldOf("y").forGetter(SealArea::y),
        Codec.INT.fieldOf("z").forGetter(SealArea::z)
    ).apply(i, SealArea::new));
    public SealArea { x=Math.clamp(x,1,8); y=Math.clamp(y,1,8); z=Math.clamp(z,1,8); }
    public static SealArea initial(Direction face) {
        return new SealArea(face.getStepX()==0?3:1,face.getStepY()==0?3:1,face.getStepZ()==0?3:1);
    }
    public AABB bounds(BlockPos support, Direction face) {
        return new AABB(support.relative(face))
            .expandTowards(face.getStepX()*(x-1),face.getStepY()*(y-1),face.getStepZ()*(z-1))
            .inflate(face.getStepX()==0?x-1:0,face.getStepY()==0?y-1:0,face.getStepZ()==0?z-1:0);
    }
    public SealArea change(int axis,int delta) {
        return new SealArea(x+(axis==0?delta:0),y+(axis==1?delta:0),z+(axis==2?delta:0));
    }
}
