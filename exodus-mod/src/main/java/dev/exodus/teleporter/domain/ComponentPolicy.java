package dev.exodus.teleporter.domain;

import java.util.UUID;

public final class ComponentPolicy {
    private ComponentPolicy() {}
    public static boolean accepts(int slot,TeleporterComponent component,ComponentStackState stack,UUID currentMatch){
        return component!=null&&component==TeleporterComponent.forSlot(slot)&&stack!=null&&!stack.template()
                &&currentMatch!=null&&currentMatch.equals(stack.matchId());
    }
}
