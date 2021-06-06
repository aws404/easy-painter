# Easy Painter

**Easy painter** is a server-side mod for improving paintings!

## Features
* The easy painting selection GUI opens when you place/shift click a painting, showing you all the paintings that could go in that position.
* Add custom paintings to your server, no resource packs required. Just place the image and a small JSON file in the config directory ([see here](#Custom Paintings))

## Custom Paintings
Custom paintings require 2 files to be placed in the `config/easy_painter` dirrecory (see the [example here](/example/config)).
1. The JSON file. This simply tells the mod how big the painting should be. For Example:
```json5
{
  "blockWidth": 2,
  "blockHeight": 2,
  "image": "tater" //<- (optional, will assume the same as the file name if not supplied)
}
```
2. The image file. This is the `png` image that the painting should display, it can be either named the same as the JSON file or defined in the `image` field.  
The dimensions of the image need to be in the same ratio as `blockWidth` to `blockHeight` (Note: this is not a technical limitation but rather imposed as stretching images to fit dimensions looks terrible).  
   
### Removing Custom Paintings
To properly remove a custom painting follow the steps bellow:
1. From the console or as an op run `/easy_painter clear <painting_name>`
2. Run the command again to confirm.
3. Once the server has stopped, delete the relevant files from `config/easy_painter`