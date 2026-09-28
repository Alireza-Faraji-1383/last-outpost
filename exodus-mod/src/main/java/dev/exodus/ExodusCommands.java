package dev.exodus;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import dev.exodus.wasteland.arena.ArenaPreparationService;

public final class ExodusCommands {
    private ExodusCommands() {}
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("exodus").requires(s->s.hasPermission(2))
            .then(Commands.literal("start")
                .executes(c->start(c,null,null,false))
                .then(Commands.literal("random").executes(c->start(c,null,null,true)))
                .then(Commands.argument("x",IntegerArgumentType.integer()).then(Commands.argument("z",IntegerArgumentType.integer())
                    .executes(c->start(c,IntegerArgumentType.getInteger(c,"x"),IntegerArgumentType.getInteger(c,"z"),false)))))
            .then(Commands.literal("stop").executes(c->{int r=MatchManager.stop(c.getSource().getServer(),"Stopped by admin.");c.getSource().sendSuccess(()->Component.literal(r==1?"Exodus match stopped.":"No active Exodus match."),true);return r;}))
            .then(Commands.literal("status").executes(c->lines(c,MatchManager.status(c.getSource().getServer()))))
            .then(Commands.literal("arena")
                .then(Commands.literal("prepare").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();ArenaPreparationService.prepare(c.getSource().getServer(),p.getUUID());return 1;}))
                .then(Commands.literal("status").executes(c->lines(c,ArenaPreparationService.status(c.getSource().getServer()))))
                .then(Commands.literal("cancel").executes(c->{boolean cancelled=ArenaPreparationService.cancel(c.getSource().getServer());c.getSource().sendSuccess(()->Component.literal(cancelled?"Arena preparation cancelled.":"No arena preparation is active."),true);return cancelled?1:0;})))
            .then(Commands.literal("dimension")
                .then(Commands.literal("enter").executes(c->MatchManager.operatorEnter(c.getSource().getPlayerOrException())))
                .then(Commands.literal("leave").executes(c->MatchManager.operatorLeave(c.getSource().getPlayerOrException()))))
            .then(Commands.literal("players").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();return lines(c,MatchManager.playerLines(p));})));
    }
    private static int start(CommandContext<CommandSourceStack> c,Integer x,Integer z,boolean random) throws com.mojang.brigadier.exceptions.CommandSyntaxException{return MatchManager.start(c.getSource().getPlayerOrException(),x,z,random);}
    private static int lines(CommandContext<CommandSourceStack> c,java.util.List<String> lines){for(String line:lines)c.getSource().sendSuccess(()->Component.literal(line),false);return 1;}
}
