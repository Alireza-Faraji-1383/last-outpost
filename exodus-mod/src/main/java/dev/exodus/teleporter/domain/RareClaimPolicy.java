package dev.exodus.teleporter.domain;

public final class RareClaimPolicy {
    public enum Result { NOT_RARE, CLAIM, ALREADY_BOUND, DELETE_DUPLICATE }
    private RareClaimPolicy() {}
    public static Result claim(boolean alreadyClaimed,boolean template){
        if(!template)return Result.ALREADY_BOUND;
        return alreadyClaimed?Result.DELETE_DUPLICATE:Result.CLAIM;
    }
    public static Result claimOrdinary(){return Result.NOT_RARE;}
}
