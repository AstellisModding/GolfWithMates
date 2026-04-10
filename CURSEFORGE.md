# ⛳ Golf With Mates

> Build courses, grab your clubs, and play a round with your mates — all inside Minecraft.

Golf With Mates adds a fully functional golf experience to Minecraft. Simulate real shots with physics-based ball flight, build custom courses anywhere in your world, and compete across holes with named cups and par tracking.

---

## 📸 Screenshots

<!-- Replace the lines below with actual image embeds once you have screenshots -->

| | |
|---|---|
| *Course overview* | *Mid-flight ball* |
| *Cup config screen* | *Disguised cup* |

---

## ✨ Features

### 🏌️ Four Club Types
Each club produces a different shot shape, giving you real choices on the course:

| Club | Behaviour |
|------|-----------|
| **Putter** | Flat ground shot — roll the ball along the surface |
| **Iron** | Shallow parabola — reliable mid-range approach |
| **Wedge** | Steep loft — short distance, great from rough |
| **Driver** | Long flat arc — maximum distance off the tee |

### 🎯 Physics Simulation
- Full server-side trajectory simulation before the ball moves a single block
- Ball flight, bouncing, and rolling all calculated in one pass
- Realistic friction per surface — ice is slippery, soul sand is sticky, greenzones are smooth
- Bounce behaviour varies by block type (slime bounces high, sand kills momentum)

### 🌀 Smooth Ball Animation
- The ball physically travels its entire calculated path after a swing
- Animation speed scales with shot distance — short putts are quick, long drives take time
- Seamless transition: ball disappears from your hand, flies the path, and lands as a placed block

### 🏆 Golf Cup
- Place a cup anywhere to mark a hole
- Right-click to open the **Cup Settings** screen:
  - Set a **Course Name** for the hole
  - Set the **Par** value
  - **Disguise** the cup as any block so it blends into your course design
- The cup renders a glowing interior gradient so you can always spot it

### 🚩 Course Building Blocks

| Block | Purpose |
|-------|---------|
| **Golf Cup** | Marks the hole — configurable name, par, and disguise |
| **Golf Flag** | Visual marker to show where the cup is |
| **Golf Flag Pole** | Pairs with the flag for a proper flagstick |
| **Golf Greenzone** | Smooth putting surface with low friction |
| **Golf Ball** | Place it to tee up; swing a club to launch |

### 📏 Shot Power Control
- Scroll the mouse wheel (while holding Shift) to adjust shot power
- Fine-tune using the dedicated **Increase/Decrease Power** keybinds
- Power percentage shown on your HUD as you adjust

### 🔭 Trajectory Beam (Optional)
- After a shot lands, a coloured beam traces the full ball path through the air
- **Off by default** — toggle on/off any time with `/golf togglebeam`
- Useful for understanding how the physics behaved on a tricky shot

---

## 🛠️ Crafting Recipes

All recipes use common vanilla materials — no progression gate, just build and play.

> *Screenshots or recipe viewer (e.g. JEI) recommended here*

| Item | Key Materials |
|------|--------------|
| Putter | Sticks + Iron Nugget |
| Iron | Sticks + Iron Nuggets |
| Wedge | Sticks + Iron Nuggets |
| Driver | Sticks + Iron Ingot |
| Golf Ball | Clay + Bone Meal |
| Golf Cup | Sticks + Flower Pot |
| Golf Flag | Iron Ingot + Paper |
| Golf Flag Pole | Iron Ingots |
| Golf Greenzone | Grass Block |

---

## 🎮 How to Play

1. **Craft** a set of clubs and a golf ball
2. **Place** the golf ball where you want to tee off
3. **Hold** a club and **right-click** the ball to swing
4. Adjust power with **Shift + Scroll** before swinging
5. Walk to where the ball landed and swing again
6. Keep going until the ball rolls into a **Golf Cup**

**Building a course:**
- Place **Golf Cups** at your holes and configure each with a name and par via right-click
- Use **Golf Greenzones** around the cup for a proper putting surface
- **Disguise** the cups by right-clicking them with any block item to blend them into the terrain
- Add **Flags** to mark each hole from a distance

---

## ⌨️ Key Bindings

| Binding | Default | Action |
|---------|---------|--------|
| Golf Increase Power | Unbound | Raise shot power |
| Golf Decrease Power | Unbound | Lower shot power |
| Mouse Scroll (+ Shift) | — | Adjust shot power |

**Commands:**

| Command | Description |
|---------|-------------|
| `/golf togglebeam` | Toggle trajectory beam visibility (client-side) |

---

## 🔧 Compatibility

- **Loader:** NeoForge
- **Minecraft:** 1.21.x
- **Side:** Server required (physics run server-side)
- **Multiplayer:** Fully supported — host a server and play courses with friends

---

## 🗺️ Planned / Coming Soon

- Hole detection and scoring
- Differentiated club physics (distinct feel per club type)
- Stroke counter and scoreboard
- More course decoration blocks

---

## 💬 Feedback & Issues

Found a bug or have a suggestion? Open an issue on the [GitHub repository](#).

