# FTB Ultimine

public static final TagKey<Item> DENY_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "excluded_tools"));
public static final TagKey<Item> STRICT_DENY_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "excluded_tools/strict"));
public static final TagKey<Item> ALLOW_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "included_tools"));

	public static final TagKey<Block> EXCLUDED_BLOCKS = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "excluded_blocks"));
	public static final TagKey<Block> BLOCK_WHITELIST = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "block_whitelist"));
	public static final TagKey<Block> TILLABLE_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "farmland_tillable"));
	public static final TagKey<Block> FLATTENABLE_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "shovel_flattenable"));

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