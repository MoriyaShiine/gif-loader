# gif-loader

A Minecraft mod that adds support for rendering .GIF files

This mod uses [Open-Imaging](https://github.com/DhyanB/Open-Imaging) by [DyhanB](https://github.com/DhyanB).

This mod is built for Minecraft 1.19.2, but may work with other versions.

# Installing

If you wish to use this in a mod, add Jitpack to your repositories:

```gradle
repositories {
  maven {
    name "jitpack"
    url "https://jitpack.io"
  }
}
```

And add Gif Loader as a dependency and include it

```gradle
dependencies {
  implementation 'com.github.MoriyaShiine:gif-loader:VERSION'
}
```

where `VERSION` is a version found on [Jitpack](https://jitpack.io/#MoriyaShiine/gif-loader).

# Usage

Calling any methods from this mod isn't necessary, simply using `RenderSystem#setShaderTexture()` will work. There are
two helper methods for more specific usage, `GifLoader#getFrame()` and `GifLoader#loadGif()`.