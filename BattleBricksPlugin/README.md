# BattleBricks

BattleBricks is a small plugin that allows you to get Battle Bricks, which are similar to pet rocks. They are named and you can battle against other players and their bricks to level up your brick. You can also apply lapis or lapis blocks to your brick to level it up.

## Usage

To fight a player (the player must be within 16 blocks), do `/bb fight <name>`. They will be sent a fight request, and they will have to do `/bb fight <yourName>` to accept it. Once the request has been accepted, there's a 5 second countdown, and then you must twerk as much as possible.

## Commands

All commands can start with either `/bb` or `/battlebricks`:
- `/bb` or `/bb help` or `/bb ?`: Show help message.
- `/bb fight <player name>`: Fight the specified player.
- `/bb config <reload|save>`: Reload or save the config. Requires `BattleBricks.admin` permission.

## Math

The calculations for winning is `(# of twerks) + (brick level * 10)`. The winner gets `opponent's brick level * 15` XP for winning.

The XP value of lapis is 10 XP, with a lapis block being worth 90 XP. To level up your brick via lapis, simply put your brick and one lapis or lapis block in a crafting grid of any kind.

Enjoy! :)
