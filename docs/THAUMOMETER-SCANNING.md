# Thaumometer scanning

Hold Use for 20 game ticks (one second at normal tick rate). Both first-person arms lift the meter toward the face with a small tremor. Releasing Use or switching items early cancels the scan. The server chooses the target on completion, using the existing nine-block ray with block occlusion and loaded-chunk checks. Knowledge is never awarded from a client render or particle callback.

Sneaking when starting selects the item in the opposite hand instead of the world. Releasing sneak during the one-second animation does not change that selection. This works with the meter in either hand. The opposite item is visually hidden during the two-handed animation but remains equipped and is never moved or consumed. Empty hands and a second meter show an instruction instead of scanning the world or the meter itself.

The HUD names the actual held item and uses the player's configured Use and Sneak keys. Scan hints only appear while holding a meter, rather than while wearing goggles or holding a gauntlet.

Successful new observations play the vanilla experience-orb pickup sound. World scan attempts also restore the original scan sound and sixteen original TC6 rune glyphs, extracted into the native particle atlas. Repeated observations do not award knowledge or play the success sound again. Each attempt is gated by its own one-second use duration, without a subsequent item cooldown. This prevents vanilla from falling through to placing or using the opposite-hand item while Use remains held.

The archived original `ItemThaumometer` performs its scan immediately and its `drawFX` method emits runes; the held one-second animation requested here is implemented explicitly using modern item-use timing and Forge's first-person hand render event. No legacy OpenGL renderer is carried over.

`-PbookProbe=scan runServer` enables the opt-in native `ScanProbe` integration fixture. It checks duration, early release, latched sneak selection, both hand arrangements, retained items, empty-hand handling, repeated observations and world scanning. `-PbookProbe=scan-client` enables a real-client scene for first-person animation and input verification. Normal release builds exclude these fixtures.
