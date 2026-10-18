package thaumcraft.aura;

import com.mojang.brigadier.arguments.FloatArgumentType;
import java.util.Locale;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

/** Operator diagnostics until the thaumometer and research HUD are ported. */
public final class AuraCommands {
    private AuraCommands() {}
    public static void register() {
        RegisterCommandsEvent.BUS.addListener(event -> event.getDispatcher().register(
            Commands.literal("thaumcraft").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("aura").executes(ctx -> {
                    var source = ctx.getSource(); var pos = net.minecraft.core.BlockPos.containing(source.getPosition());
                    var cell = AuraManager.get(source.getLevel(), pos);
                    source.sendSuccess(() -> cell == null ? Component.translatable("command.thaumcraft.aura.unloaded") :
                        Component.translatable("command.thaumcraft.aura.read", pos.getX() >> 4, pos.getZ() >> 4, cell.base(), number(cell.vis()), number(cell.flux())), false);
                    return cell == null ? 0 : cell.base();
                }).then(Commands.literal("addvis").then(Commands.argument("amount", FloatArgumentType.floatArg(0, AuraCell.MAX)).executes(ctx -> mutate(ctx.getSource(), FloatArgumentType.getFloat(ctx, "amount"), false))))
                  .then(Commands.literal("addflux").then(Commands.argument("amount", FloatArgumentType.floatArg(0, AuraCell.MAX)).executes(ctx -> mutate(ctx.getSource(), FloatArgumentType.getFloat(ctx, "amount"), true)))))));
    }
    private static String number(float value) { return String.format(Locale.ROOT, "%.2f", value); }
    private static int mutate(net.minecraft.commands.CommandSourceStack source, float amount, boolean flux) {
        var pos = net.minecraft.core.BlockPos.containing(source.getPosition());
        float moved = flux ? AuraManager.addFlux(source.getLevel(), pos, amount) : AuraManager.addVis(source.getLevel(), pos, amount);
        source.sendSuccess(() -> Component.translatable("command.thaumcraft.aura.add", number(moved), flux ? "flux" : "vis"), true);
        return (int)moved;
    }
}
