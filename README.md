# Easy Painter

**Easy painter** is a server-side mod for improving paintings!

## Features
* Easy painting selection GUI opens when you place/shift click a painting, showing you all the paintings that could go in that position.
* Add custom paintings to your server, no resource packs required. Just create a simple datapack, [see here](/example).
* Painting locking, add the `locked:true` tag to a painting entity to lock it. This will make it unbreakable, unchangeable, ect.
* Painting block/entity tags, customise the blocks which can support paintings/sit informant of paintings without breaking them.
    * `easy_painter/tags/blocks/cannot_support_paiting.json` - Blocks which cannot be behind paintings without them breaking
    * `easy_painter/tags/blocks/paiting_ignored.json` - Blocks which can sit in-front of paintings without them breaking
    * `easy_painter/tags/entity_types/painting_interact.json` - Entities which cannot share a space with paintings

## Libraries Used
* [Image2Map](https://github.com/TheEssem/Image2Map) - TheEssem
* [sgui](https://github.com/Patbox/sgui) - Patbox
* [Server Translations](https://github.com/arthurbambou/Server-Translations) - Arthurbambou
* [FabricAPI](https://github.com/FabricMC/fabric) - FabricMC
