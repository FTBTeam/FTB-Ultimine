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

## Modder API

Note: anything in the `dev.ftb.mods.ftbultimine.api` package is public API, and we will make every effort to keep this API stable. Any code outside the `api` package is subject to change without notice - please to try to avoid using this code directly. [Contact us](https://go.ftb.team/support-mod-issues) if you want to create an addon mod that the existing API does not cover, and we will try to help.

FTB Ultimine fires a few Architectury events intended to make it easy for modders to write plugins and extension to FTB Ultimine.

### Custom right-click handlers

This provides for custom behaviour when a block is right-clicked and the Ultimine key is currently pressed. Look at code in the `dev.ftb.mods.ftbultimine.rightclick` package for examples.

In your mod constructor, register an instance of a class which implements `RightClickHandler`:
```java
RegisterRightClickHandlerEvent.REGISTER.register(dispatcher -> dispatcher.registerHandler(MyHandler.INSTANCE));
```

Example handler:
```java
public enum MyHandler implements RightClickHandler {
    INSTANCE;

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        // do the work you need here
        return numberOfBlocksAffected;
    }
}
```

### Custom crop types

This allows for detection of custom crops which don't behave like vanilla crops. Builtin support is included for Agricraft (see the `AgriCraftCropLikeHandler` class).

In your mod constructor, register an instance of a class which implements `CropLikeHandler`:

```java
RegisterCropLikeEvent.REGISTER.register(registry -> registry.register(MyHandler.INSTANCE));
```

See `VanillaCropLikeHandler` or `AgriCraftCropLikeHandler` for examples.

### Custom ultimining restrictions

This can be used to restrict players' ability to ultimine based on criteria of your choosing.

```java
RegisterRestrictionHandlerEvent.REGISTER.register(registry -> registry.register(MyHandler.INSTANCE));
```

Example to require player to be holding a specific item:
```java
public enum MyHandler implements RestrictionHandler {
    @Override
    public boolean canUltimine(Player player) {
        return player.getMainHandItem().getItem() instanceof SomeCustomItem;
    }
}
```

### Custom ultimining shapes

This can be used to register custom ultimining shapes.

In your mod constructor, register an instance of the `Shape` interface:

```java
RegisterShapeEvent.REGISTER.register(registry -> registry.register(MyShape.INSTANCE));
```

### Custom block-break handlers

In most cases, the default break strategy (simply to break the block and produce its drops) is fine. However, there may be a need at times when breaking blocks to have a little more control. An example is EnderIO conduit bundles, where you might want to break all the connected item conduits while leaving other parts of the bundle alone. This can be achieved with a custom block-break handler.

To register a custom block-break handler:

```java
RegisterBlockBreakHandlerEvent.REGISTER.register(registry -> registry.register(MyBlockBreakHandler.INSTANCE));
```

```java
enum MyBlockBreakHandler implements BlockBreakHandler {
  INSTANCE;

  @Override
  public Result breakBlock(Player player, BlockPos pos, BlockState state, Shape shape, BlockHitResult hitResult) {
    // check if it's your block, and if not return PASS asap
    if (!isMyBlock(state)) {
       return PASS;
    }
    // do your custom block break logic here, return SUCCESS or FAIL as appopriate
    return SUCCESS;
  }
}
```

### Custom block-selection handlers

Block selection determines which blocks will be included in the next ultimining operation, and is shown by the white outline on blocks when the ultimine key is held. By default, this is controlled by the current ultimining shape (and depending on the shape, the currently focused block). For the `Shapeless` shape, this can be customised; using EnderIO conduits again as an example, you might want to only select blocks which have the same facade as the focused block.

To register a custom block-selection handler:

```java
RegisterBlockSelectionHandlerEvent.REGISTER.register(registry -> registry.register(MySelectionHandler.INSTANCE));
```

```java
public enum MySelectionHandler implements BlockSelectionHandler {
  INSTANCE;

  @Override
  public Result customSelectionCheck(Player player, BlockPos origPos, BlockPos pos, BlockState origState, BlockState state) {
     // Called for every block in the current shape; keep the checks quick and efficient!
      
     // check if it's your block first, and return Result.PASS immediately if it's not

     // then check for block equivalence (e.g. do both blocks have the same facade?)
     // return Result.TRUE or Result.FALSE as appropriate
     return Result.TRUE;
  }
}
```

## Support

- For **Modpack** issues, please go here: https://go.ftb.team/support-modpack
- For **Mod** issues, please go here: https://go.ftb.team/support-mod-issues
- Just got a question? Check out our Discord: https://go.ftb.team/discord

## Licence

All Rights Reserved to Feed The Beast Ltd. Source code is `visible source`, please see our [LICENSE.md](/LICENSE.md) for more information. Any Pull Requests made to this mod must have the CLA (Contributor Licence Agreement) signed and agreed to before the request will be considered.

## Keep up to date

[![](https://cdn.feed-the-beast.com/assets/socials/icons/social-discord.webp)](https://go.ftb.team/discord) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-github.webp)](https://go.ftb.team/github) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitter-x.webp)](https://go.ftb.team/twitter) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-youtube.webp)](https://go.ftb.team/youtube) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-twitch.webp)](https://go.ftb.team/twitch) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-instagram.webp)](https://go.ftb.team/instagram) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-facebook.webp)](https://go.ftb.team/facebook) [![](https://cdn.feed-the-beast.com/assets/socials/icons/social-tiktok.webp)](https://go.ftb.team/tiktok)