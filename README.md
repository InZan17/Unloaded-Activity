# Unloaded-Activity-Fabric

When things such as crops get out of simulation distance, normally they wouldn't grow. This mod changes that by doing a calculation once they get within simulation distance and update them so it's like they never left the simulation distance. So now you can explore while waiting for crops to grow or furnaces to cook.

This also works similarly when skipping the night, crops will have grown and furnaces will have cooked as if you were awake the entire time waiting for the day.


## Here is a video showcasing the mod.
[![The video showcasing the mod](https://img.youtube.com/vi/c1hAEfe_zVY/sddefault.jpg)](https://www.youtube.com/watch?v=c1hAEfe_zVY)

## Currently supported blocks/entities (if you use the latest version):
- All crops
- Furnaces
- Saplings
- Bushes
- Cocoa beans
- Sugar canes
- Baby animals
- Copper blocks
- Cactus
- Leaves
- Amethyst
- Kelp
- Glow berries
- Bamboo


## Currently planned:
- Updating blocks in account of each other
- Support for other mods (probably once this mod is out of beta)

# Inspiration
I got this idea from a [video by DigiDigger](https://www.youtube.com/watch?v=YIDbhVPHZbs&t=311s) where he talks about how Terraria can handle all of its tiles.
Even though the video isn't really accurate to how Terraria actually works, he did bring up an optimisation which I feel like could also be implemented to Minecraft.
The timestamp where he talks about this optimisation is at 5:11.

# Simulation accuracy:
Instead of using bruteforce and repeatedly ticking blocks/entities for the amount it was unloaded,
the mod instead has a simulation function for every supported block/entity which will calculate the state of a block/entity after a certain amount of time.
The most complicated things to simulate are blocks which use randomness in their ticks.
To be able to simulate these we randomly sample from a [binomial distribution](https://en.wikipedia.org/wiki/Binomial_distribution)
and sometimes also a [negative binomial distribution](https://en.wikipedia.org/wiki/Negative_binomial_distribution)
to get what state the blocks should be in after a certain amount of time.
By doing this, you will get pretty accurate results without bruteforce.

However, some blocks do stuff that I am not able to find a good way to simulate.
Because of this, certain blocks will not be accurately simulated.\
Here's a list of those blocks:
- **Copper blocks**\
  The copper oxidation speed and max oxidation depends on how many blocks of copper are nearby and the state of those copper blocks.
  Right now my mod simulates one block at a time which means that I won't be able to accurately simulate blocks that are dependent on other blocks.
  This results in copper blocks being very inaccurate. However, when spaced optimally the simulation will be pretty accurate.
  Except that the first oxidation step has a lower chance of happening compared to the rest.
  To not have to deal with that as well I just made all steps have that lower chance.

- **Water freezing**\
  This has pretty much the same problem as copper blocks. Normally ice only generates if one of the surrounding blocks also is not water.
  The way my mod handles it is that it doesn't. I was too lazy to come up with any decent alternative, so I just used the original logic if a position is valid for ice to generate.
  This will definitely be fixed in the future once I come up with a good way to simulate multiple blocks together.

- **Turtle eggs**\
  The turtle eggs doesn't really have any issues regarding accuracy. The biggest issue I had was what age the turtles should have once hatched, but I managed to solve it.
  I just wanted to mention that the accurate simulation can be a slight bit more expensive. So there's an option to turn it off.

Anything that didn't get mentioned here most likely has a pretty accurate simulation function with no issues at all.

# Feedback / Suggestions
If you got any feedback or suggestions for this mod, feel free to join my [discord server](https://discord.gg/aF3sqRN5Ja)!

# Contributing
You are free to contribute so feel free to open issues and pull requests.
When you submit a pull request, you agree to license your contribution under the terms of the LGPL-3.0 license.
