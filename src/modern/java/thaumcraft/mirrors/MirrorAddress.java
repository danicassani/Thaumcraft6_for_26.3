package thaumcraft.mirrors;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
/** Resource-key dimensions and persistent identities reject stale links to replacement blocks. */
public record MirrorAddress(GlobalPos location,String identity) {
 public static final Codec<String> ID=Codec.STRING.validate(s->s.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")?DataResult.success(s):DataResult.error(()->"Invalid mirror identity"));
 public static final Codec<MirrorAddress> CODEC=RecordCodecBuilder.create(i->i.group(GlobalPos.CODEC.fieldOf("location").forGetter(MirrorAddress::location),ID.fieldOf("identity").forGetter(MirrorAddress::identity)).apply(i,MirrorAddress::new));
}
