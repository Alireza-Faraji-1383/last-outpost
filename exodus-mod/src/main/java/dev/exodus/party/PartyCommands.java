package dev.exodus.party;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class PartyCommands {
    private PartyCommands(){}
    @FunctionalInterface private interface Action{void run(ServerPlayer player) throws CommandSyntaxException;}
    private static int execute(CommandContext<CommandSourceStack> context,Action action) throws CommandSyntaxException {
        try{action.run(context.getSource().getPlayerOrException());return 1;}catch(IllegalStateException e){context.getSource().sendFailure(Component.literal(e.getMessage()));return 0;}
    }
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("exodus").then(Commands.literal("party")
            .executes(c->execute(c,PartyService::status))
            .then(Commands.literal("create").executes(c->execute(c,PartyService::create)))
            .then(Commands.literal("invite").then(Commands.argument("player",EntityArgument.player()).executes(c->execute(c,p->PartyService.invite(p,EntityArgument.getPlayer(c,"player"))))))
            .then(Commands.literal("accept").executes(c->execute(c,p->PartyService.accept(p,singleInvite(p))))
                .then(Commands.argument("player",EntityArgument.player()).executes(c->execute(c,p->PartyService.accept(p,EntityArgument.getPlayer(c,"player").getUUID())))))
            .then(Commands.literal("decline").executes(c->execute(c,p->PartyService.decline(p,singleInvite(p))))
                .then(Commands.argument("player",EntityArgument.player()).executes(c->execute(c,p->PartyService.decline(p,EntityArgument.getPlayer(c,"player").getUUID())))))
            .then(Commands.literal("leave").executes(c->execute(c,p->PartyService.leave(p,false))))
            .then(Commands.literal("disband").executes(c->execute(c,p->PartyService.leave(p,true))))
            .then(Commands.literal("status").executes(c->execute(c,PartyService::status)))));
    }
    private static java.util.UUID singleInvite(ServerPlayer player){var invites=PartyService.invitations(player);if(invites.size()!=1)throw new IllegalStateException(invites.isEmpty()?"You have no pending invitation.":"Specify the inviting player: /exodus party accept <player>.");return invites.iterator().next();}
}
