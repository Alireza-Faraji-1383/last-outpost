package dev.exodus;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import dev.exodus.wasteland.arena.ArenaPreparationService;

public final class ExodusCommands {
    private ExodusCommands() {}
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dev.exodus.party.PartyCommands.register(dispatcher);
        dispatcher.register(Commands.literal("exodus")
            .then(Commands.literal("start").requires(s->s.hasPermission(2))
                .executes(c->start(c,null,null,false))
                .then(Commands.literal("random").executes(c->start(c,null,null,true)))
                .then(Commands.argument("x",IntegerArgumentType.integer()).then(Commands.argument("z",IntegerArgumentType.integer())
                    .executes(c->start(c,IntegerArgumentType.getInteger(c,"x"),IntegerArgumentType.getInteger(c,"z"),false)))))
            .then(Commands.literal("stop").requires(s->s.hasPermission(2)).executes(c->{int r=MatchManager.stop(c.getSource().getServer(),"Stopped by admin.");c.getSource().sendSuccess(()->Component.literal(r==1?"Exodus match stopped.":"No active Exodus match."),true);return r;}))
            .then(Commands.literal("status").requires(s->s.hasPermission(2)).executes(c->lines(c,MatchManager.status(c.getSource().getServer()))))
            .then(Commands.literal("event").requires(s->s.hasPermission(2))
                .then(Commands.literal("status").requires(s->s.hasPermission(2)).executes(c->lines(c,dev.exodus.event.EventManager.status(c.getSource().getServer()))))
                .then(Commands.literal("stop").requires(s->s.hasPermission(2)).executes(c->{dev.exodus.event.EventManager.stop(c.getSource().getServer());c.getSource().sendSuccess(()->Component.literal("Active events stopped."),true);return 1;}))
                .then(Commands.literal("start").requires(s->s.hasPermission(2)).then(Commands.argument("id",StringArgumentType.word())
                    .suggests((c,b)->SharedSuggestionProvider.suggest(dev.exodus.event.EventCatalog.all().stream().map(dev.exodus.event.domain.EventDefinition::id),b))
                    .executes(c->{boolean started=dev.exodus.event.EventManager.start(c.getSource().getServer(),StringArgumentType.getString(c,"id"));if(started)c.getSource().sendSuccess(()->Component.literal("Event started or drop queued."),true);else c.getSource().sendFailure(Component.literal("Event unavailable: check active match, participants, one-time history and capacity."));return started?1:0;}))))
            .then(Commands.literal("arena").requires(s->s.hasPermission(2))
                .then(Commands.literal("prepare").executes(ExodusCommands::prepareArena))
                .then(Commands.literal("status").requires(s->s.hasPermission(2)).executes(c->lines(c,ArenaPreparationService.status(c.getSource().getServer()))))
                .then(Commands.literal("locations").executes(ExodusCommands::arenaLocations))
                .then(Commands.literal("tp").then(Commands.argument("placementId",StringArgumentType.word())
                    .suggests((c,b)->SharedSuggestionProvider.suggest(ArenaPreparationService.locationIds(c.getSource().getServer()),b))
                    .executes(ExodusCommands::arenaTeleport)))
                .then(Commands.literal("cancel").executes(c->{boolean cancelled=ArenaPreparationService.cancel(c.getSource().getServer());c.getSource().sendSuccess(()->Component.literal(cancelled?"Arena preparation cancelled.":"No arena preparation is active."),true);return cancelled?1:0;})))
            .then(Commands.literal("dimension").requires(s->s.hasPermission(2))
                .then(Commands.literal("enter").executes(c->MatchManager.operatorEnter(c.getSource().getPlayerOrException())))
                .then(Commands.literal("leave").executes(c->MatchManager.operatorLeave(c.getSource().getPlayerOrException()))))
            .then(Commands.literal("players").requires(s->s.hasPermission(2)).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();return lines(c,MatchManager.playerLines(p));})));
    }
    private static int start(CommandContext<CommandSourceStack> c,Integer x,Integer z,boolean random) throws com.mojang.brigadier.exceptions.CommandSyntaxException{return MatchManager.start(c.getSource().getPlayerOrException(),x,z,random);}
    private static int prepareArena(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = c.getSource().getPlayerOrException();
        try {
            ArenaPreparationService.prepare(c.getSource().getServer(),player.getUUID());
            return 1;
        } catch (IllegalStateException exception) {
            c.getSource().sendFailure(Component.literal(exception.getMessage()));
            return 0;
        }
    }
    private static int arenaLocations(CommandContext<CommandSourceStack> c){var locations=ArenaPreparationService.locations(c.getSource().getServer());if(locations.isEmpty()){c.getSource().sendFailure(Component.literal("No finalized Exodus structure locations are available."));return 0;}for(var location:locations){String command="/exodus arena tp "+location.placementId();Component line=Component.literal(location.placementId()+" ["+location.kind()+"] X "+location.x()+" Y "+location.y()+" Z "+location.z()+" ").append(Component.literal("[TP]").withStyle(style->style.withColor(net.minecraft.ChatFormatting.AQUA).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,command)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.literal("Teleport to "+location.placementId())))));c.getSource().sendSuccess(()->line,false);}return locations.size();}
    private static int arenaTeleport(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException{String id=StringArgumentType.getString(c,"placementId");var location=ArenaPreparationService.location(c.getSource().getServer(),id).orElse(null);if(location==null){c.getSource().sendFailure(Component.literal("Unknown or unavailable Exodus structure location: "+id));return 0;}ServerPlayer player=c.getSource().getPlayerOrException();int result=MatchManager.operatorTeleport(player,location.x(),location.teleportY(),location.z());if(result==1)c.getSource().sendSuccess(()->Component.literal("Teleported to "+id+"."),false);return result;}
    private static int lines(CommandContext<CommandSourceStack> c,java.util.List<String> lines){for(String line:lines)c.getSource().sendSuccess(()->Component.literal(line),false);return 1;}
}
