# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1902.4.2]

### Added
* Backported cooldowns feature from 1.20+ versions (thanks @Skullians)
  * Added configurable cooldown between ultimining operations (default remains 0 - no cooldown)
    * See `ultimine_cooldown` in server config
    * Overrideable in FTB Ranks with the `ftbultimine.ultimine_cooldown` permission node
  
## [1902.4.1]

### Added
* Right-click ultimine harvesting now also works for Cocoa Beans and Sweet Berries

## [1902.4.0]

### Added
* Mod configs are now editable in-game using `/ftbultimine clientconfig` and `/ftbultimine serverconfig` commands
  * Client config can be edited by anyone; only admins (permission >= 2) can edit server config
* Added client config option to control requirement to hold sneak to scroll through shape selection
  * Default is true, as before; if set to false, it's enough just to hold the Ultimine key to shape-scroll
* When scrolling, 2 lines of context are now shown above & below the current shape
  * Provides a better UX of actually scrolling
  * Can be reverted in client config if you prefer
* Added a new Large Tunnel shape, which is a 3x3 tunnel
  * Still subject to the max blocks per operation limit (64 by default)
* Common config (in the `config` folder) is no longer used; settings in the old common config will be auto-merged into server config 
  * Server config can be found in `saves/<world>/serverconfig` (SSP) or `world/serverconfig` (SMP)
* Added a new `merge_tags_shaped` config setting, which is used when ultimining shaped areas
  * This is distinct from the existing `merge_tags` setting, which is now only used for shapeless mining
  * See https://github.com/FTBTeam/FTB-Mods-Issues/issues/444 for more discussion on why this was done
* Added FTB Ranks integration to allow max ultiminable blocks to by set by player's rank
  * The FTB Ranks permission node to use is `ftbultimine.max_blocks`
  * if a rank doesn't have this node, then the default max (from config) is used
* Ultimining is now supported for right-click functionality of axes and shovels
  * Axes can be used to strip multiple logs or scrape multiple copper blocks
  * Shovels can be used to flatten multiple dirt/grass blocks into paths
  * Can be disabled via server config, if desired (along with crop harvesting and farmland tilling)

### Fixed
* Fixed player reach distance being 0.5 blocks too short
* Fixed item/XP dupe under certain circumstances - intermod compat issue (thanks @Recon88)
