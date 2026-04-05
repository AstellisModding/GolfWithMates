# Decisions Log

Answers to design questions settled in the planning session.
Claude Code should treat these as closed — don't re-open them.

---

## Physics Model

**Q: Full parabolic arc, flat XZ, or top-down?**
A: Both. Putter is flat XZ only. Drivers, irons, wedges use full parabolic arc.
The simulation loop handles both via a single `applyGravity` boolean flag.

**Q: Should terrain height affect the shot path (e.g. ball follows slope)?**
A: No — not for the putter, which is the current focus. Putter is designed for flat grass.
Terrain height matters for parabolic shots only via gravity.

**Q: Full rebound off all blocks?**
A: Yes for the putter. Ball rebounds off solid block faces using the reflection formula.
Bounciness and friction are per-block via lookup tables.

## Animation

**Q: Smooth interpolation, hop between blocks, or real-time tick-by-tick?**
A: Pinned — not decided yet. Animation system is a separate phase after physics are solid.
`animationTick` and `animationDone` fields are stubbed in `GolfBallBlockEntity` but unused.

## Simulation Safety

**Q: How to prevent infinite loops / stack overflows?**
A: Iterative loop (not recursive) with:
- `MAX_ITERATIONS = 2000`
- `MAX_BOUNCES = 16` (forces REST if ball pinballs in a corner)
- `STOP_THRESHOLD = 0.05` (stops when speed drops below this)

## Step Size

**Q: How granular should collision detection be?**
A: `STEP_SIZE = 0.25` blocks. Sub-block accuracy without excessive iteration count.

## Node Recording

**Q: Record every step or thin the path?**
A: Thin it. BOUNCE nodes always recorded. FLIGHT and ROLL recorded every 4 steps.
Keeps path list lean without losing fidelity for the renderer.

## ClubUtils Scope

**Q: Should ClubUtils contain physics logic?**
A: No. The old version had `CalculateRebound`, `GetBlockFaceNormal`, etc. — all deleted.
ClubUtils is stat lookup only: `isClub`, `getShotType`, `getVelocity`, `getBounciness`.

## Hole Detection

**Q: How is reaching the hole handled?**
A: `reachedHole` is a boolean flag on `ShotResult`, not a `NodeType`.
Currently stubbed as `false` in `TrajectoryCalculator`. Wire up when a hole block exists.

## ShotResult Mutability

**Q: Can ShotResult be mutated after creation?**
A: No. `path` is wrapped in `Collections.unmodifiableList`. New shot = new `ShotResult`.

## GolfBallBlockEntity Default State

**Q: What does the BE hold before any shot is hit?**
A: `ShotResult.empty()` — an immutable empty ShotResult. Never null.
Check `currentShot.path.isEmpty()` instead of null-checking.
