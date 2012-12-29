## 1.3.0, 2012-12-29:

Finally implemented clean-up functions. A call like

    (.addCleanup max/engine (fn [] ....))

will stack a function to be called when the MaxObject proxy is
wound down, or when the engine's `clear()` function is called.
Both `max/engine` and `max/maxObject` are available as bindings
in the callback (although the MaxObject cannot do much if it
is being deleted).

## 1.2.1, 2012-09-10:

Nudged dependency on `net.loadbang.lib` and `net.loadbang.scripting`.
Packaging tools in `pom.xml`. Now depending on Clojure 1.4.

## 1.2.0, 2011-12-25:

Fix-up for Clojure 1.3.0 (explicit annotation of `max/maxObject` as a
dynamic var).

Merry Christmas.

## 1.1.0, 2011-12-25:

First reconstruction for Maven. Fixed up the unit tests.
