# FTB Ultimine

## Tags

### Item Tags

* `ftbultimine:excluded_tools` - items in this tag can't be used for ultimining (applies to main hand slot)
* `ftbultimine:excluded_tools/strict` - items in this tag can't be used for ultimining (applies to main _and_ offhand slots)
* `ftbultimine:included_tools` - if `require_tool` is true in server config, by default only "tool" items can be used (tiered items with durability); this can be used to allow extra items

### Block Tags

* `ftbultimine:excluded_blocks` - blocks in this tag may never be ultimined
* `ftbultimine:block_whitelist` - if this tag is non-empty, then _only_ blocks in this tag may be ultimined
* `ftbultimine:farmland_tillable` - blocks in this tag can be ultimine-tilled with a hoe tool; includes grass & dirt blocks by default
* `ftbultimine:shovel_flattenable` - blocks in this tag can be ultimine-flattened (turned to dirt path) with a shovel tool; includes grass & dirt blocks by default

## FTB Ranks Integration

Following nodes can be configured via [FTB Ranks](https://www.curseforge.com/minecraft/mc-mods/ftb-ranks-forge):

* `ftbultimine.max_blocks` - if present in a player's rank, overrides the server `max_blocks` config setting
* `ftbultimine.ultimine_cooldown` - if present in a player's rank, overrides the server `ultimine_cooldown` setting
