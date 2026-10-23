package com.connectapp.core.uitest

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// @RunWith, @Config and @GraphicsMode are inherited, so every subclass in commonTest runs under
// Robolectric.
// - SDK 35: Robolectric's SDK 36 image needs JVM internals that JDK 21 no longer opens.
// - A landscape tablet (D12: tablets only); a phone-sized default window would leave the
//   bottom of the forms, and their buttons, outside the screen where clicks cannot reach.
// - Native graphics: real bitmaps, so the signature PNG is actually rendered and decoded.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w1280dp-h800dp-xlarge-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
actual abstract class UiTest actual constructor()
