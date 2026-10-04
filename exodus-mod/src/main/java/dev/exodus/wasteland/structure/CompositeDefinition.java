package dev.exodus.wasteland.structure;

import java.util.List;

public record CompositeDefinition(String name, List<Part> parts, Size total) {
    public record Size(int x, int y, int z) {
        @Override
        public String toString() {
            return x + "x" + y + "x" + z;
        }
    }

    public record Part(String id, Size size, int offsetX, int offsetY, int offsetZ) {}

    public static CompositeDefinition faction(String nation) {
        return new CompositeDefinition(nation + "_base", List.of(
                new Part(nation + "_base_1", new Size(29, 21, 30), 0, 0, 0),
                new Part(nation + "_base_2", new Size(28, 21, 30), 29, 0, 0),
                new Part(nation + "_base_3", new Size(29, 21, 30), 0, 0, 30),
                new Part(nation + "_base_4", new Size(28, 21, 30), 29, 0, 30)
        ), new Size(57, 21, 60));
    }
}
