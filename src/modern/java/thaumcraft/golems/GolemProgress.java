package thaumcraft.golems;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Separate from the construction design so old designs and paid jobs remain unchanged. */
public record GolemProgress(int rank, int xp) {
    public static final int MAX_RANK = 10;
    public static final GolemProgress EMPTY = new GolemProgress(0, 0);
    public static final Codec<GolemProgress> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.optionalFieldOf("rank", 0).forGetter(GolemProgress::rank),
        Codec.INT.optionalFieldOf("xp", 0).forGetter(GolemProgress::xp)
    ).apply(i, GolemProgress::new));

    public GolemProgress {
        rank = Math.clamp(rank, 0, MAX_RANK);
        xp = rank == MAX_RANK ? 0 : Math.clamp(xp, 0, required(rank) - 1);
    }

    public static int required(int rank) {
        return rank >= MAX_RANK ? 0 : (Math.clamp(rank, 0, 9) + 1) * (Math.clamp(rank, 0, 9) + 1) * 1000;
    }

    public GolemProgress earn(int amount, boolean smart) {
        if (!smart || amount <= 0 || rank == MAX_RANK) return this;
        long available = (long) xp + amount;
        int next = rank;
        while (next < MAX_RANK && available >= required(next)) available -= required(next++);
        return new GolemProgress(next, next == MAX_RANK ? 0 : (int) available);
    }

    public double health(GolemDesign design) { return design.health() + rank; }
    public double damage(GolemDesign design) { return design.damage() + (design.has("fighter") ? rank * .25 : 0); }
    public double speed(GolemDesign design) { return design.speed() * (1 + rank * .025); }
    public net.minecraft.network.chat.Component description() {
        return rank == MAX_RANK
            ? net.minecraft.network.chat.Component.translatable("golem.thaumcraft.rank_max", rank)
            : net.minecraft.network.chat.Component.translatable("golem.thaumcraft.rank", rank, xp, required(rank));
    }
}
