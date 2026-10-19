package thaumcraft.thaumaturgy;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.nio.file.Files;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.*;
import net.minecraft.world.level.storage.LevelResource;

/** Shared two-second cloud immunity per dimension, persisted across restarts and chunk unloads. */
public final class FocusCooldowns extends SavedData {
    private static final Codec<String> KEY=Codec.STRING.validate(s->s.length()<=64&&(s.startsWith("entity:")||s.startsWith("block:"))?DataResult.success(s):DataResult.error(()->"Invalid focus cooldown key"));
    private static final Codec<Map<String,Long>> ENTRIES=Codec.unboundedMap(KEY,Codec.LONG).validate(m->m.size()<=4096?DataResult.success(m):DataResult.error(()->"Excess focus cooldowns"));
    private static final Codec<FocusCooldowns> FORMAT=RecordCodecBuilder.create(i->i.group(Codec.intRange(1,1).fieldOf("version").forGetter(d->1),ENTRIES.fieldOf("cooldowns").forGetter(d->d.cooldowns)).apply(i,(v,m)->new FocusCooldowns(m)));
    static final Codec<FocusCooldowns> CODEC=Codec.of(FORMAT,new Decoder<>(){public <T> DataResult<com.mojang.datafixers.util.Pair<FocusCooldowns,T>> decode(DynamicOps<T> ops,T input){var result=FORMAT.decode(ops,input);return result.result().map(DataResult::success).orElseGet(()->DataResult.error(()->result.error().orElseThrow().message()));}});
    public static final SavedDataType<FocusCooldowns> TYPE=new SavedDataType<>(Identifier.parse("thaumcraft:focus_cooldowns"),FocusCooldowns::new,CODEC,null);
    private final Map<String,Long> cooldowns;
    private long lastCleanup=Long.MIN_VALUE;
    public FocusCooldowns(){this(Map.of());}
    private FocusCooldowns(Map<String,Long> entries){cooldowns=new HashMap<>(entries);}
    public boolean acquire(String key,long now){if(lastCleanup!=now){if(cooldowns.entrySet().removeIf(e->e.getValue()<=now))setDirty();lastCleanup=now;}if(cooldowns.getOrDefault(key,Long.MIN_VALUE)>now||cooldowns.size()>=4096)return false;cooldowns.put(key,now+40);setDirty();return true;}
    public static FocusCooldowns get(ServerLevel level){var data=level.getDataStorage().get(TYPE);if(data!=null)return data;var path=DimensionType.getStorageFolder(level.dimension(),level.getServer().getWorldPath(LevelResource.ROOT)).resolve("data/thaumcraft/focus_cooldowns.dat");if(Files.exists(path))throw new IllegalStateException("Cannot decode focus cooldown data; preserving "+path);return level.getDataStorage().computeIfAbsent(TYPE);}
}
