package dev.exodus.teleporter.domain;

public final class StartCommitPolicy {
    private StartCommitPolicy() {}
    public static boolean mayClearInventories(boolean allocationComplete,boolean structuresPlaced,boolean spawnsValidated){
        return allocationComplete&&structuresPlaced&&spawnsValidated;
    }
}
