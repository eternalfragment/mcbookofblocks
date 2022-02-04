# MC Journey Mode

REQUIREMENTS: Fabric API | MC 18+

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

Initial config will be generated on first boot for a singleplayer world, or server. Files are generated within the world folder, and will
include a config and defaultconfig file. Edit the config file within the world folder to enable/disable items by
adding/removing them from the list, and setting researchable to the correct setting. If adding items from other mods,
include their name with correct spelling (such as "spruce_fence_gate"). The system will automatically detect the mods
items and integrate apropriately.

The default config that generates includes about 150 items and basic building materials and guidelines for unlockable goals. 
By default, the menu_item (what brings up the JourneyMode Screen) 

## Config
The config file is 'currently' setup as a json file with items in the current strucutre: 

* "Name": "game_item_name"  || The name of the item that is to be loaded
* "Researchable": 0-4  || How can the item be researched/unlocked?  (0-Disabled; 1-Pay items to unlock; 2-Meet scoreboard objective; 3-Meet scoreboard objective AND pay amount; 4- Either Scoreboard objective OR pay amount)
* "req_amt": 0-999...  || How many items must the player pay to meet the 'pay' requirement to unlock
* "scb_obj": "scoreboard_objective"  || Name of the objective created to meet the 'scoreboard' requirement to unlock
* "scb_amt": 0-999...  || Amt the above scoreboard objective must reach for the player to unlock the item

## Usage

Install mod by placing into mod folder. First load into a world, will generate default config in the world folder. Any changes to config requires restarting (for clients, just reloading world).


## Extras
There is a small datapack that provides a slight glow to the JourneyMode item when you are holding it, when used in conjunction with optifine. You can find it here.

## License

This template is available under the MIT license.
 
