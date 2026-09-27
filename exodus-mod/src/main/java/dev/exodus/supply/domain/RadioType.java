package dev.exodus.supply.domain;

public enum RadioType {
    BASIC("basic"), SPECIAL("special");

    private final String id;

    RadioType(String id) { this.id = id; }
    public String id() { return id; }
}
