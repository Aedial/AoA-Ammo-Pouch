# AoA Ammo Pouch

An Advent of Ascension 1.12 addon providing an Ammo Pouch for storing AoA ammo. This mod is primarily intended for streamlining ammo management and handling large quantities of AoA ammo without the inventory playing Tetris with you.

The pouch can be opened by right-clicking while holding it, and it will display a GUI to manage the ammo stored inside. It can also be filled by crafting it with ammo items.

The mod ships with a default pouch that has 5 slots and a maximum stack size of 1024 for each slot. See the configuration section below for details on how to customize these values, or add additional pouch tiers with their own slot counts and maximum stack sizes.
Any crafting recipe for upgrading the pouch to a higher tier should just change the metadata of the pouch item, without touching the NBT data, so that the contents of the pouch are preserved.


## Configuration

The mod includes a server-side configuration file with an in-game GUI editor:

### Number of Slots
The number of slots in the pouch can be configured. It is a list of integers, with the first entry being the number of slots for the first pouch tier. Any subsequent entries will be used for higher tiers (with metadata values 1, 2, etc.). Tiers beside 0 come with no texture or recipe, so you will need to add your own if you want to use them.

### Carrying Capacity
The carrying capacity of the pouch can be configured. It follows the same rules as the number of slots, with a list of integers where the first entry is the capacity for the first pouch tier, and subsequent entries for higher tiers.

### Allowed Ammo Items
The allowed ammo items can be configured. It is a list of registry names of every ammo item that the pouch accepts. The default list includes most AoA ammo items, but you can add or remove items as needed. Note that this mod only adds handling for AoA guns. Any other mod's guns will not be able to use the pouch unless they are specifically coded to do so.


## Credits
