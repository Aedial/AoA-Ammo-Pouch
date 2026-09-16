# Ammo Pouch

An Advent of Ascension/DivineRPG 1.12 addon providing an Ammo Pouch. This mod is primarily intended for streamlining ammo management and handling large quantities of ammo without having to play Tetris.
The pouch ONLY works for AoA and DivineRPG, any other mod's guns will have to integrate with the pouch themselves, or use a different ammo management solution.

The pouch can be opened by right-clicking while holding it, and it will display a GUI to manage the ammo stored inside. It can also be filled by crafting it with ammo items.

The mod ships with a default pouch that has 5 slots and a maximum stack size of 1024 for each slot. See the configuration section below for details on how to customize these values, or add additional pouch tiers with their own slot counts and maximum stack sizes.
Any crafting recipe for upgrading the pouch to a higher tier should just change the metadata of the pouch item, without touching the NBT data, so that the contents of the pouch are preserved.


## Configuration

The mod includes a server-side configuration file with an in-game GUI editor:

### DivineRPG toggle
The integration with DivineRPG can be toggled off. Disabling means the pouch WILL NOT work with DivineRPG guns whatsover, the pouch will not accept items from the DivineRPG ammo list (see config below).

### AoA toggle
The integration with Advent of Ascension can be toggled off. Disabling means the pouch WILL NOT work with AoA guns whatsover, the pouch will not accept items from the AoA ammo list (see config below).

### Number of Slots
The number of slots in the pouch can be configured. It is a list of integers, with the first entry being the number of slots for the first pouch tier. Any subsequent entries will be used for higher tiers (with metadata values 1, 2, etc.). Tiers beside 0 come with no texture or recipe, so you will need to add your own if you want to use them.

### Carrying Capacity
The carrying capacity of the pouch can be configured. It follows the same rules as the number of slots, with a list of integers where the first entry is the capacity for the first pouch tier, and subsequent entries for higher tiers.

### Allowed Ammo Items (AoA)
The allowed ammo items can be configured. It is a list of registry names of every ammo item that the pouch accepts. The default list includes most AoA ammo items, but you can add or remove items as needed.

### Allowed Ammo Items (DivineRPG)
The allowed ammo items can be configured. It is a list of registry names of every ammo item that the pouch accepts. The default list includes most DivineRPG ammo items, but you can add or remove items as needed.

## Credits
- Chinese translation: @ZHAY10086
- Pouch GUI texture: @Foreck1
- Pouch item texture: @NerdySpider
