package dev.exodus.teleporter.domain;

public final class ActivationPolicy {
    public enum Result { INCOMPLETE, ACTIVATE, REJECT_SECOND_DEVICE }
    private ActivationPolicy() {}
    public static Result evaluate(int installedCount,boolean anotherActive){
        if(installedCount<9)return Result.INCOMPLETE;
        return anotherActive?Result.REJECT_SECOND_DEVICE:Result.ACTIVATE;
    }
}
