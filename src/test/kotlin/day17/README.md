# [Day 17: Clumsy Crucible](https://adventofcode.com/2023/day/17)
## Part One

The lava starts flowing rapidly once the Lava Production Facility is
operational. As you leave, the reindeer offers you a parachute, allowing you
to quickly reach Gear Island.

As you descend, your bird's-eye view of Gear Island reveals why you had
trouble finding anyone on your way up: half of Gear Island is empty, but the
half below you is a giant factory city!

You land near the gradually-filling pool of lava at the base of your new
_lavafall_. Lavaducts will eventually carry the lava throughout the city, but
to make use of it immediately, Elves are loading it into large
[crucibles](https://en.wikipedia.org/wiki/Crucible) on wheels.

The crucibles are top-heavy and pushed by hand. Unfortunately, the crucibles
become very difficult to steer at high speeds, and so it can be hard to go in
a straight line for very long.

To get Desert Island the machine parts it needs as soon as possible, you'll
need to find the best way to get the crucible _from the lava pool to the
machine parts factory_. To do this, you need to minimize _heat loss_ while
choosing a route that doesn't require the crucible to go in a _straight line_
for too long.

Fortunately, the Elves here have a map (your puzzle input) that uses traffic
patterns, ambient temperature, and hundreds of other parameters to calculate
exactly how much heat loss can be expected for a crucible entering any
particular city block.

For example:

    
    
    2413432311323
    3215453535623
    3255245654254
    3446585845452
    4546657867536
    1438598798454
    4457876987766
    3637877979653
    4654967986887
    4564679986453
    1224686865563
    2546548887735
    4322674655533
    

Each city block is marked by a single digit that represents the _amount of
heat loss if the crucible enters that block_. The starting point, the lava
pool, is the top-left city block; the destination, the machine parts factory,
is the bottom-right city block. (Because you already start in the top-left
block, you don't incur that block's heat loss unless you leave that block and
then return to it.)

Because it is difficult to keep the top-heavy crucible going in a straight
line for very long, it can move _at most three blocks_ in a single direction
before it must turn 90 degrees left or right. The crucible also can't reverse
direction; after entering each city block, it may only turn left, continue
straight, or turn right.

One way to _minimize heat loss_ is this path:

    
    
    2 _>_ _>_ 34 _^_ _>_ _>_ _>_ 1323
    32 _v_ _>_ _>_ _>_ 35 _v_ 5623
    32552456 _v_ _>_ _>_ 54
    3446585845 _v_ 52
    4546657867 _v_ _>_ 6
    14385987984 _v_ 4
    44578769877 _v_ 6
    36378779796 _v_ _>_
    465496798688 _v_
    456467998645 _v_
    12246868655 _<_ _v_
    25465488877 _v_ 5
    43226746555 _v_ _>_
    

This path never moves more than three consecutive blocks in the same direction
and incurs a heat loss of only `_102_`.

Directing the crucible from the lava pool to the machine parts factory, but
not moving more than three consecutive blocks in the same direction, _what is
the least heat loss it can incur?_

Your puzzle answer was `767`.

## Part Two

The crucibles of lava simply aren't large enough to provide an adequate supply
of lava to the machine parts factory. Instead, the Elves are going to upgrade
to _ultra crucibles_.

Ultra crucibles are even more difficult to steer than normal crucibles. Not
only do they have trouble going in a straight line, but they also have trouble
turning!

Once an ultra crucible starts moving in a direction, it needs to move _a
minimum of four blocks_ in that direction before it can turn (or even before
it can stop at the end). However, it will eventually start to get wobbly: an
ultra crucible can move a maximum of _ten consecutive blocks_ without turning.

In the above example, an ultra crucible could follow this path to minimize
heat loss:

    
    
    2 _>_ _>_ _>_ _>_ _>_ _>_ _>_ _>_ 1323
    32154535 _v_ 5623
    32552456 _v_ 4254
    34465858 _v_ 5452
    45466578 _v_ _>_ _>_ _>_ _>_
    143859879845 _v_
    445787698776 _v_
    363787797965 _v_
    465496798688 _v_
    456467998645 _v_
    122468686556 _v_
    254654888773 _v_
    432267465553 _v_
    

In the above example, an ultra crucible would incur the minimum possible heat
loss of `_94_`.

Here's another example:

    
    
    111111111111
    999999999991
    999999999991
    999999999991
    999999999991
    

Sadly, an ultra crucible would need to take an unfortunate path like this one:

    
    
    1 _>_ _>_ _>_ _>_ _>_ _>_ _>_ 1111
    9999999 _v_ 9991
    9999999 _v_ 9991
    9999999 _v_ 9991
    9999999 _v_ _>_ _>_ _>_ _>_
    

This route causes the ultra crucible to incur the minimum possible heat loss
of `_71_`.

Directing the _ultra crucible_ from the lava pool to the machine parts
factory, _what is the least heat loss it can incur?_

Your puzzle answer was `904`.

Both parts of this puzzle are complete! They provide two gold stars: **

At this point, you should [return to your Advent calendar](https://adventofcode.com/2023) and try
another puzzle.

If you still want to see it, you can [get your puzzle input](https://adventofcode.com/2023/day/17/input).
