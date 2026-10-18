package thaumcraft.research;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class ResearchCommands {
    private ResearchCommands(){}
    public static void register(){RegisterCommandsEvent.BUS.addListener(event->event.getDispatcher().register(
        Commands.literal("thaumcraft").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("research").then(Commands.literal("grant").then(Commands.argument("player",EntityArgument.player())
                .then(Commands.argument("entry",StringArgumentType.word()).suggests((context,builder)->{
                    for(var key:ResearchCatalog.BY_KEY.keySet())if(key.startsWith(builder.getRemaining()))builder.suggest(key);return builder.buildFuture();
                }).executes(context->{var player=EntityArgument.getPlayer(context,"player");var key=StringArgumentType.getString(context,"entry");boolean success=ResearchService.grant(player,key);context.getSource().sendSuccess(()->Component.literal((success?"Granted ":"Unable to grant ")+key+" → "+player.getName().getString()),true);return success?1:0;})))))));
    }
}
