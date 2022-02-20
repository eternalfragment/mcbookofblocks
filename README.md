# MC Journey Mode

REQUIREMENTS: Fabric API 0.43.1+1.18 | MC 18+

## Introduction

The JourneyMode mod is intended to be used in conjunction with a near-vanilla playstyle, while providing an adjustable
quality of life enhancement to the acquisition of blocks and items in game. This is accomplished by the player "
spending" a certain number of the desired block, to unlock an unlimited supply of that block! No more needing to
endlessly grind basic materials.

This mod is intended to let people have fun building without going into creative, reduce the grind without eliminating
it, and let small servers create a manageable way to provide basic resources without massive expansion or contraptions.

### Popular uses

* Basic materials for mass construction (Woods and stones)
* Rare items that may be complicated to get for multiple people (such as elytra)
* Things that get tedious, but valuable after grinding a few times (eg beacon)

This Mod allows you to customize which items are available to unlock, and how much each item will cost to unlock.
Default values are provided, but you can add your own preferences to the config file. Custom Mods can be added too! Just
include their proper names in the config file, and it will auto-find them

## Setup

Place the mod jar in the mods folder for either server or client. Default configuration will generate.

The default config that generates includes about 150 items and basic building materials, and some examples of various configuration options. 
By default, the menu_item (what brings up the JourneyMode Screen) is free.

## Config
With the JourneyMode screen up, those with permissions (perm lvl 4 required / OP), will see a gear in the top right of the JourneyMode screen.
Clicking on this button will show the config settings for the mod. Within these settings you can add new items to the options, as well as edit existing items.

Each item has 4 methods for researching. 
#####1 - Pay only (default); This requires the player to pay 'x' number of this item to unlock. 
#####2 - Scoreboard Only; This requires the player to reach a certain score on the scoreboard defined for this item
#####3 - Pay -or- Scoreboard; Meet either of the criteria to unlock
#####4 - Pay & Scoreboard; Meet both criteria to unlock
You can also set the pay amount, the name of the scoreboard objective, as well as the goal for the scoreboard objective to meet.
Saving the config will instantly adjust those options in-game, and save it to the config file.

--NOTE: A player will unlock an item when the criteria is met, and they open the JourneyMode menu. Changing the goals to unlock the item will not remove the unlock from players who already unlocked it.

## Usage

Install mod by placing into mod folder. First load into a world, will generate default config in the world folder. Any changes to config requires restarting (for clients, just reloading world).

A player with Permission lvl 4 (OP) can use the command "/mjm give <@p>" to give a player the JourneyMode Item.

Players can also craft the MJM Menu Item with 4 paper, 4 ender pearls, and 1 diamond

<img src="https://github.com/eternalfragment/mcjourneymode/blob/master/src/main/resources/imgs/mjm_recipe.png">

## Extras
There is a small datapack that provides a slight glow to the JourneyMode item when you are holding it, when used in conjunction with optifine. You can find it [here](https://github.com/eternalfragment/mcjourneymode/blob/master/mjm_resource_pack.zip).

## License

This template is available under the MIT license.
 
