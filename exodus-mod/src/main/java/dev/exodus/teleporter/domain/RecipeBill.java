package dev.exodus.teleporter.domain;

public record RecipeBill(int blazeRods,int enderPearls,int quartz) {
    public static RecipeBill oneSet(){return new RecipeBill(2,5,14);}
}
