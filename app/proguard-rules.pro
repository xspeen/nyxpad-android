# NyxPad release rules. Release builds intentionally keep readable stack traces
# so community builds remain diagnosable. Enable shrinking in a signed release
# pipeline after validating Room, Hilt and Compose reflection rules.
-keepattributes *Annotation*
