package dev.exodus.event.domain;

/** Datapack metadata; objective handlers own interactions with Minecraft. */
public record EventDefinition(String id, String title, Scope scope, Objective objective,
                              int minDay, int maxDay, int weight, boolean oneTime,
                              int cooldownDays, int milestoneDay, String lootTable, boolean core,
                              int durationSeconds,int targetCount,String targetEntity,
                              int rewardEmeralds,int hunterEmeralds,int preyEmeralds,ParticipantSelector participantSelector,
                              int chancePercent,int chanceIncreasePercent,int priority) {
    public enum Scope { GLOBAL, TARGETED }
    public enum Objective { KILL_ENTITY, KILL_PLAYER, WORLD_DROP }
    public enum ParticipantSelector { ALL_ACTIVE, RANDOM_PAIR, SINGLE_ACTIVE }
    public EventDefinition(String id,String title,Scope scope,Objective objective,int minDay,int maxDay,int weight,
                           boolean oneTime,int cooldownDays,int milestoneDay,String lootTable,boolean core){
        this(id,title,scope,objective,minDay,maxDay,weight,oneTime,cooldownDays,milestoneDay,lootTable,core,
                -1,-1,"minecraft:zombie",-1,-1,-1,scope==Scope.TARGETED?ParticipantSelector.RANDOM_PAIR:ParticipantSelector.ALL_ACTIVE,30,30,100);
    }
    public EventDefinition {
        if (id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || title == null
                || title.isBlank() || title.length() > 64 || scope == null || objective == null
                || minDay < 1 || maxDay < 0 || maxDay != 0 && maxDay < minDay
                || weight < 0 || weight > 10000 || cooldownDays < 0 || milestoneDay < 0
                || objective == Objective.KILL_PLAYER && scope != Scope.TARGETED
                || objective == Objective.WORLD_DROP && scope != Scope.GLOBAL
                || lootTable == null || objective == Objective.WORLD_DROP && !lootTable.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")
                || core && (objective != Objective.WORLD_DROP || !oneTime)
                || durationSeconds != -1 && (durationSeconds < 1 || durationSeconds > 86400)
                || targetCount != -1 && (targetCount < 1 || targetCount > 10000)
                || targetEntity == null || !targetEntity.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")
                || rewardEmeralds < -1 || rewardEmeralds > 4096 || hunterEmeralds < -1 || hunterEmeralds > 4096 || preyEmeralds < -1 || preyEmeralds > 4096
                || chancePercent < 0 || chancePercent > 100 || chanceIncreasePercent < 0 || chanceIncreasePercent > 100 || priority < 0)
            throw new IllegalArgumentException("Invalid event definition: " + id);
        if(participantSelector==null
                || (participantSelector==ParticipantSelector.RANDOM_PAIR)!=(objective==Objective.KILL_PLAYER)
                || (participantSelector==ParticipantSelector.SINGLE_ACTIVE)!=(scope==Scope.TARGETED&&objective==Objective.KILL_ENTITY))
            throw new IllegalArgumentException("Unsupported participant selector: "+id);
    }
}
