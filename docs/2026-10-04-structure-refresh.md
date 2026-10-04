# Structure refresh (0.7.1)

Imported all eleven corrected templates from `saves/New World (1)/generated/exodus/structures` without modifying their contents. Updated the KubeJS starter-base override as well as the packaged mod resource.

Faction pieces now measure 21 blocks high; composite definitions, preparation heights and asset contracts follow the submitted dimensions. Horizontal sizes and composite offsets remain unchanged.

Player-base terrain clearing now uses zero outer margin and its additional clearing loop stays inside the template footprint. Previously the shared POI margin cleared two surrounding columns on every side, and the additional loop cleared one. Other POIs retain their configured margin.

Validation: full Gradle clean/test/build; imported-file equality, NBT block bounds and palette references; installed JAR equality. Visual placement and two-client multiplayer acceptance remain manual. Restart Minecraft and test a newly placed base; existing terrain holes are not retroactively repaired.
