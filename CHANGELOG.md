# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2101.1.5]

### Fixed
* Hotfix: fixed server crash when right-clicking to ultimine-harvest crops

## [2101.1.4]

### Added
* Added API for registering custom block breaking handlers
  * Allows for custom breaking logic for specific blocks, e.g. complex blocks with multiple hitboxes
* Updated ru_ru translation (thanks @kkylern)

### Fixed
* Right-click-harvesting Farmer's Delight crops which are climbing ropes no longer destroys the rope
  * This may also help with any other mods which add custom crops with custom blockstate properties; all properties other than age are now preserved when harvesting

## [2101.1.3]

### Fixed
* Fix for Accelerated Decay workaround in last release

## [2101.1.2]

### Added
* Added integration for the FTB EZ Crystals mod; right-clicking crystals with the ultimine key held can harvest multiple crystals at once
  * NeoForge only
* Added integration for Agricraft crop harvesting via right-click
* Shape cycling keys are now configurable keybinds, no longer hardcoded to cursor up/down
* Added client config "Require Ultimine Key for Shape Cycling", default true
  * If set to false, Ultimine shapes can be cycled at any time with the defined keybinds
* Added ja_jp translation (thanks @twister716)
* Added ru_ru translation (thanks @BazZziliuS)
* Added pt_br translation (thanks @Xlr11)

### Changed
* Now using the new FTB Library 2101.1.10+ config API
  * **IMPORTANT NOTE FOR PACK MAKERS** any customized FTB Ultimine configuration distributed in `defaultconfigs/ftbultimine/` must now be distributed in `config/` !
    * `defaultconfigs/ftbultimine/ftbultimine-client.snbt` -> `config/ftbultimine-client.snbt`
    * `defaultconfigs/ftbultimine/ftbultimine-server.snbt` -> `config/ftbultimine-server.snbt`
  * Server admins may locally copy `config/ftbultimine-server.snbt` to `world/serverconfig/ftbultimine-server.snbt` if they wish to maintain custom settings which will not be reset by modpack updates

### Fixed
* Fixed default ores tag for the `merge_tags` server config setting (was `c:*_ores`, now `c:ores/*`)
* Fixed unwanted mod interaction with Accelerated Decay causing tools to take excessive damage when leaves fast-decay and ultimine key is still held

## [2101.1.1]

### Added
* Added a sidebar button to open the client & server configs for the mod
  * Admin players get a choice of client/server configs, non-admins can only view/edit the client config
* Added FTB Ranks support for a couple more server settings
  * "XP Cost per Block Mined" now has a corresponding ranks node of `ftbultimine.experience_per_block`
  * "Exhaustion per Block Mined" now has a corresponding ranks node of `ftbultimine.exhaustion_per_block`
* Added translations: tr_tr (complete) and fr_fr (not fully up to date)
  
### Changed
* Revamped the overlay panel a bit for a better visual appearance
  * Overlay panel can now be moved via client config settings
* Client and server configs have been reorganised into sections for greater clarity
  * Note: settings which have been previously altered may be reset to their defaults; check your settings

### Fixed
* Fixed missing item and block tags in the `ftbultimine` namespace

## [2100.1.0]

### Changed
* Ported to Minecraft 1.21. Support for Fabric and NeoForge.
  * Forge support may be re-added if/when Architectury adds support for Forge

## [2006.1.0]

### Changed
* Ported to Minecraft 1.20.6. Support for Fabric and NeoForge.
  * Forge support may be re-added if/when Architectury adds support for Forge

## [2004.1.0]

### Added
* `zh_cn` localization thanks to [@dust-shadows](https://github.com/dust-shadows) [#138](https://github.com/FTBTeam/FTB-Ultimine/pull/138)
* `ko_kr` localization thanks to [@smoong951](https://github.com/smoong951) [#133](https://github.com/FTBTeam/FTB-Ultimine/pull/133)

### Changed
* Ported to Minecraft 1.20.4. Supported on Forge, NeoForge and Fabric.

## [2001.1.3]

### Added
* Added new block tag `ftbultimine:block_whitelist`
  * If this tag is non-empty, then _only_ blocks in this tag can be ultimined
  * Complements the existing `ftbultimine:exclude_blocks` tag which can be used to blacklist blocks

## [2001.1.2]

### Added
* Updated to MC 1.20.1, based on 1902.4.1 release

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
