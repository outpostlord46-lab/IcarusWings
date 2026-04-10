# 🪽 Icarus Wings

A **Fabric mod for Minecraft 1.21.11** that introduces a tiered progression for exploration: the weaker **Icarus Wings** and the upgraded **Mechanical Wings**.

---

## 🛠 Features

### ✨ Icarus Wings (Early Game)
Lightweight, myth-inspired wings.
* **Risk:** High mobility but extremely fragile construction.
* **Durability:** Low durability and when durability reaches **0**, the wings **shatter and disappear permanently**.
* **Fuel:** Uses Stamina.

### ⚙️ Mechanical Wings (Mid Game)
A heavy-duty steampunk set of wings..
* **Risk:** More durable but less mobility on the ground.
* **Durability:** Becomes "Broken" at 0 durability rather than disappearing.
* **Repairable:** Can be maintained in an Anvil using **Iron Ingots**.
* **Fuel:** Requires **Coal or Charcoal** to perform wing flaps/boosts.


---

## 🎮 Controls & HUD

* **Flap / Boost:** Press the **Jump Key** (Default: `Space`) while gliding to gain altitude and speed.
* **Stamina Bar:** A custom HUD element appears above your hotbar while wearing the wings. This displays your flap cooldown—you cannot flap again until the bar is fully recharged.

---

## 🔧 Mechanics

### 🔋 Fuel System (Mechanical Wings Only)
The Mechanical Wings transition from human power to machinery. To gain altitude or speed:
* **Consumption:** Each flap/boost consumes **1 Coal or Charcoal** directly from your inventory.
* **Empty Tank:** If you run out of fuel, boosting is disabled. Gliding remains possible, but you will slowly lose altitude.

### 🔨 Maintenance & Repair

| Feature | Icarus Wings | Mechanical Wings |
| :--- | :--- | :--- |
| **Repairable** | ✅ Yes | ✅ Yes |
| **Repair Item** | Honeycomb (Anvil) | Iron Ingots (Anvil) |
| **Breaking** | Permanently Destroyed | Becomes Unusable Item |
| **Fuel Type** | Stamina | Coal / Charcoal |

### 💥 Damage Sources
Both wing sets lose durability through:
* **Active Boosting:** The primary source of wear and tear.
* **Environmental Hazards:** Taking damage from fire, the nether.

Icarus wings lose durability through:
* **Environmental Hazards:** Water, rain, flying above Y=150, flying in the desert during the day.

Mechanical wings lose durability through:
* **Environmental Hazards:** Lightning strikes during storms, flying above Y=350.


---

## ⚒️ Crafting Progression
This mod bridges the gap between early-game exploration and the end-game Elytra.

1. **Icarus Wings:** Found or crafted as your first taste of flight.
2. **Mechanical Wings:** An industrial upgrade requiring the Icarus Wings as a base, reinforced with **Iron** and **Leather** for mechanical lift.

---

## 🏆 Advancements
Track your journey from myth to machine:
* **Don't Fly Too Close!** — Obtain your first set of Icarus Wings.
* **Industrial Upgrade** — Craft a set of Mechanical Wings.
* **Struck By Innovation** — Get struck by lighning by fyling with metal wings in a storm.
* **Too Close To The Sun** — Fly too high and melt your icarus wings.

---

## 🚀 Installation
1. Install **[Fabric Loader](https://fabricmc.net/)** for **Minecraft 1.21.x**.
2. Download the latest `.jar` from the **Releases** page.
3. Place the `.jar` file into your `.minecraft/mods` folder.
4. Ensure the **Fabric API** is also in your mods folder.
5. Launch Minecraft using the Fabric profile.

6. Alternativley use your prefered mod laucher application

---

## 📜 License
This project is provided for educational and personal use. See the repository `LICENSE` file for full details.
