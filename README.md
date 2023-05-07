# MC Journey Mode

REQUIREMENTS: Fabric API 0.80.0+1.19.4 | MC 1.19.4

Would you like to donate as a thank you for this mod?

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=Y8VAGKEDWKDGQ)

Need support? Join the discord

![Discord Banner 2](https://discordapp.com/api/guilds/965017631855902810/widget.png?style=banner2)

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
* Things that get tedious, but valuable after grinding a few times (such as beacon)

This Mod allows you to customize which items are available to unlock, and how much each item will cost to unlock.
Default values are provided, but you can add your own preferences via the config. Custom Mod items can be added too! 

## Setup

Place the mod jar in the mods folder for either server or client. Default configuration will generate.

The default config that generates includes about 150 items and basic building materials, and some examples of various configuration options.

## Config
With the JourneyMode screen up, those with permissions (perm lvl 4 required / OP), will see a gear in the top right of the JourneyMode screen.
Clicking on this button will open a screen to add new items. NOTE: If you add an item that already exists in the config, the new options will overwrite the previous options.

You can also right-click any item within the JourneyMode item screen to open the options for a single item.

Each item has 4 methods for unlocking. 

1 - Pay only (default); This requires the player to pay 'x' number of this item to unlock. 

2 - Scoreboard Only; This requires the player to reach a certain score on the scoreboard defined for this item

3 - Pay -or- Scoreboard; Meet either of the criteria to unlock

4 - Pay & Scoreboard; Meet both criteria to unlock

You can also set the pay amount, the name of the scoreboard objective, as well as the goal for the scoreboard objective to meet.
Saving the config will instantly adjust those options in-game, and save it to the config file.

An example for adding a scoreboard objective. In game, do the command "/scoreboard objectives add Zombie_Kill_Tracker minecraft.killed:minecraft.zombie
". This will create a scoreboard objective called 'Zombie_Kill_Tracker', which will track each time players kill zombies. You would enter 'Zombie_Kill_Tracker' into the scoreboard name, and set the goal to be whatever you wanted.

--NOTE: A player will unlock an item when the criteria is met, and they open the JourneyMode menu. Changing the goals to unlock the item will not remove the unlock from players who already unlocked it.

## Usage

On the first load into a world, default config will generate in the world folder. Changes to the config are advised to be done through in-game tools. If modification of the config file is done directly, the server/client needs to be restarted for changes to take effect.

A player with Permission lvl 4 (OP) can use the command "/mjm give <@p>" to give a player the JourneyMode Item.

Players can also craft the MJM Menu Item with 4 paper, 4 ender pearls, and 1 diamond

<img src="https://github.com/eternalfragment/mcjourneymode/blob/master/src/main/resources/imgs/mjm_recipe.png">

Right click while holding the JourneyMode Menu Item, and it will open the menu. The menu will load 4 categories for the items: Unlocked, In inventory, In progress, and Other.

<img src="https://github.com/eternalfragment/mcjourneymode/blob/master/src/main/resources/imgs/mjm_gui.png">

"Unlocked" has items that are unlocked and available to summon. If you Left Click, it will give you 1 of the item. If you Shift+Left Click, you will be given the 'give amount' set in the config for that item. These items may have a yellow icon at the top right if you have them in your inventory. Clicking with the middle mouse button (scroll wheel) will clear your inventory of that item if its unlocked.

"In Inventory" has items that are not unlocked, but are currently in your inventory. If you left click, it will open a screen showing the unlock progress for the item.

"In progress" has items that are not unlocked, and not in your inventory, but have the unlocking process started. 

"Other" has all other items included in the config.

You can use the search bar to search for specific items. The displayed items will be filtered by the text provided.

Once a player completes the requirements to unlock the item, it will be unlocked when they open the MJM Menu.

## Extras
There is a small datapack that provides a slight glow to the JourneyMode item when you are holding it, when used in conjunction with optifine. You can find it [here](https://github.com/eternalfragment/mcjourneymode/blob/master/mjm_resource_pack.zip).

## License

This template is available under the MIT license.

### CURRENT REMARKS
Most blocks have been added to the default config, however items have not been trial balanced as of yet.

This is a hobby project, originally created for myself and some friends, and as such, programming will be wonky, and updates may be slow.