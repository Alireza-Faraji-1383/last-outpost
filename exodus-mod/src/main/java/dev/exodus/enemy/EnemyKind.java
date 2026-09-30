package dev.exodus.enemy;

public enum EnemyKind {
    ZOMBIE("exodus:zombie"), RUSSIAN("simpleenemymod:ruunit"), AMERICAN("simpleenemymod:usunit");
    private final String entityId;
    EnemyKind(String entityId) { this.entityId = entityId; }
    public String entityId() { return entityId; }
}
