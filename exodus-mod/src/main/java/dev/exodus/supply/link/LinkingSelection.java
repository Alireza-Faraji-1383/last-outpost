package dev.exodus.supply.link;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record LinkingSelection(String dimension, int x, int y, int z) {
    public CompoundTag encode() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension); tag.putInt("x", x); tag.putInt("y", y); tag.putInt("z", z);
        return tag;
    }

    public static Optional<LinkingSelection> decode(CompoundTag tag) {
        if (tag == null || ResourceLocation.tryParse(tag.getString("dimension")) == null
                || !tag.contains("x") || !tag.contains("y") || !tag.contains("z")) return Optional.empty();
        return Optional.of(new LinkingSelection(tag.getString("dimension"), tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
    }
}
