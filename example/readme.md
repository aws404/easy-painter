# Adding Custom Painting Motives
You can add your own custom painting motives by simply adding a data pack. Follow along with the example data pack here!

1. In your datapack add a `painting` directory. For example:
```
my_data_pack
├───pack.mcmeta
└───data
    └───my_mod_id    <─ Your mod/datapack id (any valid Identifier)
        ├───painting <─ The painting directory
        └───lang
```
2. Inside the `painting` place your `.png` image and a new `.json` file. The name of your json file will be the motive's identifier.  
By default, the image name should be the same as the `.json` but with `_image` on the end.
```
my_data_pack
├───pack.mcmeta
└───data
    └───my_mod_id    
        ├───painting
        │   ├───my_painting.json
        │   └───my_painting_image.png
        └───lang
```
3. Edit the contents of the `.json` file to match the following template:
```json5
{
  "blockWidth": 2,              // <─ This is the paintings width in blocks
  "blockHeight": 2,             // <─ This is the paintings height in blocks
  "image": "my_painting_image", // <─ OPTIONAL: By default the mod will look for an image of the same name with `_image`.
  "ditherMode": "FLOYD"         // <─ OPTIONAL: By default this will be NONE
}
```
The image dimensions need to have the same aspect ratio as `blockWidth` and `blockHeight` (`imageWidth/imageHeight == blockWidth/blockHeight`), this is to prevent strange resizing issues.

3.5. You can also add a translation key for your paining inside the `lang` directory, if not supplied it will use the motive's identifier.
```
my_data_pack
├───pack.mcmeta
└───data
    └───my_mod_id    
        ├───painting
        │   ├───my_painting.json
        │   └───my_painting_image.png
        └───lang
            └───en_us.json
```
The translation key uses the format `painting.<mod_id>.<painting_identifier>`, for example the contents of `en_us.json` would be:
```json
{
  "painting.my_mod_id.my_painting": "My Custom Painting"
}
```

4. Add the datapack to your world/mod and restart the server/use `/reload` (can cause some small issues).

# Clearing the Cache
When the server detects a new motive, the maps are created and cached. This means the maps do not need to be re-generated each time, saving space and time.
This also means if you change an image you will need to clear this cache for the change to take effect.

## The Automatic Easy Way
This will clear all the entire motive cache and required a server restart.
1. From the console/an operator run `/easy_painter clearcache` then again to confirm. This will save the server and then shutdown, clearing the cache in the process.
2. Restart the server, you should see in the logs that the mod regenerates the motives.

## The Manual Technical Way
The cache is stored in the overworld's `data/custom_motives.dat` file. You can open this file with an nbt editor and manually remove a cached motive.
You can also safely delete the `map_<id>` values contained in the motives `mapIds` lists.
**Do not touch the `currentMapId` value! It will overwrite other motives maps!**