package dev.exodus.teleporter.domain;

import java.util.Arrays;

public enum TeleporterComponent {
    REINFORCED_FRAME(0,false),
    POWER_REGULATOR(1,false),
    PHASE_COIL(2,false),
    FACILITY_ALPHA_KEY(3,true),
    DIMENSIONAL_CORE(4,true),
    FACILITY_BETA_KEY(5,true),
    SIGNAL_PROCESSOR(6,false),
    SPATIAL_LENS(7,false),
    CONTAINMENT_MODULE(8,false);

    private static final TeleporterComponent[] BY_SLOT=new TeleporterComponent[9];
    static {
        Arrays.fill(BY_SLOT,null);
        for(TeleporterComponent component:values()){
            if(component.slot<0||component.slot>=BY_SLOT.length||BY_SLOT[component.slot]!=null)
                throw new IllegalStateException("Invalid teleporter component slot: "+component.slot);
            BY_SLOT[component.slot]=component;
        }
    }

    private final int slot;
    private final boolean rare;
    TeleporterComponent(int slot,boolean rare){this.slot=slot;this.rare=rare;}
    public int slot(){return slot;}
    public boolean rare(){return rare;}
    public static TeleporterComponent forSlot(int slot){return slot>=0&&slot<BY_SLOT.length?BY_SLOT[slot]:null;}
}
