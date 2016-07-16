#NBTEdit

A mod for Minecraft that allows you to modify the NBT-data of entities and TileEntities while you are playing. The original version for MC 1.7 and lower can be found [here](https://github.com/DavidGoldman/NBTEdit). While the idea is taken from the original mod, no code has been taken from it.

***This is intended to be used by developers, not normal players!*** While it is not forbidden to use this on a server that people actually play on, it is discouraged since misusing this mod can lead to potentially irreparable damage to the world.

####Warning
No mod author expects people to modify the NBT-data of his (Tile-)entities or items. If something breaks because you modified its NBT-data, it is neither my fault nor that of the developers of the mod that broke.

###Features

- View&edit NBT-data of Entities and TileEntities without leaving the world
- NBT-data is shown in external windows
- It is possible to view multiple NBT-tags at once
- NBT-Clipboard
  - Store tags for future use
  - Unlimited amount of named clipboards
  - Export clipboard contents to sharable files
- Integration with Immersive Engineering: view the NBT-data of the master block of a multiblock
- Has an API that makes it possible to register special actions for specific (Tile-)entities


####API
It is possible to add special actions for the NBT-data of specific (Tile)-entities. This is done by calling `malte0811.nbtedit.api.API.registerTileHandler(Class<? extends TileEntity>, IEditHandler)` or `malte0811.nbtedit.api.API.registerEntityHandler(Class<? extends Entity>, IEditHandler)`. `malte0811.nbtedit.api.IEditHandler` is a functional interface containing a method that is called when an NBT-tag for the specified (Tile-)entity type is loaded. The parameters are the `NBTTagCompound` being edited, the `JMenu` to which any special actions should be added and the `NBTFrame`, the window in which the NBT-data is displayed and edited.

A use case for this feature are multiblocks where only one block stores useful information. An example implementation for [ImmersiveEngineering](https://github.com/BluSunrize/ImmersiveEngineering) multiblocks can be found [here](src/main/java/malte0811/nbtedit/Compat.java).