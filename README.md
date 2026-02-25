# 🇮🇩 NusantaraScript

**Indonesian Language Scripting for Minecraft Servers (Phase 2)**

NusantaraScript is a revolutionary Minecraft plugin that allows server administrators to write custom scripts using **Indonesian language syntax**. Now with **conditional logic**, **variables**, and **custom commands**!

## ✨ Features

### Core Features
- 📝 **Indonesian Syntax** - Write scripts in Bahasa Indonesia
- 🎯 **Event-Based** - Respond to player actions and server events
- 🚀 **Easy to Learn** - Simple syntax that anyone can understand
- ⚡ **Dynamic Loading** - Reload scripts without restarting the server
- 🔧 **Extensible** - Easy to add new commands and events

### Phase 2 Features ⭐ NEW!
- 🔀 **Conditional Logic** - `jika` (if) statements for complex logic
- 💾 **Variable System** - Store global and player-specific data
- ⚙️ **Custom Commands** - Create server commands dynamically
- 🎨 **Advanced Actions** - Heal, feed, give items, and more!

## 🚀 Installation

1. Download `NusantaraScript.jar`
2. Place it in your server's `plugins/` folder
3. Restart your server
4. Create `.ns` files in `plugins/NusantaraScript/scripts/`
5. Use `/nusantara reload` to load scripts

## 📖 Syntax Guide

### Supported Events

| Indonesian Trigger | Bukkit Event | Description |
|-------------------|--------------|-------------|
| `saat pemain masuk:` | PlayerJoinEvent | When a player joins |
| `saat pemain keluar:` | PlayerQuitEvent | When a player leaves |
| `saat blok dihancurkan:` | BlockBreakEvent | When a block is broken |
| `saat pemain chat:` | AsyncChatEvent | When a player chats |

### Basic Actions

| Indonesian Command | Effect | Example |
|-------------------|--------|---------|
| `kirim "text" ke pemain` | Send message to player | `kirim "Halo!" ke pemain` |
| `broadcast "text"` | Broadcast to all players | `broadcast "Server restart!"` |
| `batalkan event` | Cancel the event | `batalkan event` |
| `pulihkan pemain` | Heal player to full health | `pulihkan pemain` |
| `beri makan pemain` | Feed player to full | `beri makan pemain` |

### Conditional Logic (Phase 2) ⭐

| Indonesian Condition | Effect | Example |
|---------------------|--------|---------|
| `jika blok adalah "MATERIAL":` | Check block type | `jika blok adalah "DIAMOND_ORE":` |
| `jika pemain memegang "MATERIAL":` | Check held item | `jika pemain memegang "IRON_PICKAXE":` |
| `jika pemain punya izin "permission":` | Check permission | `jika pemain punya izin "vip.access":` |
| `jika pemain adalah "Name":` | Check player name | `jika pemain adalah "Notch":` |
| `jika pemain sedang terbang` | Check if flying | `jika pemain sedang terbang` |
| `jika pemain sedang menyelinap` | Check if sneaking | `jika pemain sedang menyelinap` |

### Variable Operations (Phase 2) ⭐

| Indonesian Command | Effect | Example |
|-------------------|--------|---------|
| `atur variabel {name} menjadi "value"` | Set variable | `atur variabel {saldo} menjadi "1000"` |
| `tambah NUMBER ke variabel {name}` | Add to variable | `tambah 1 ke variabel {kills.%player%}` |
| `kurangi NUMBER dari variabel {name}` | Subtract from variable | `kurangi 100 dari variabel {coins}` |
| `hapus variabel {name}` | Delete variable | `hapus variabel {temp}` |

### Custom Commands (Phase 2) ⭐

```
perintah /commandname:
    izin: "permission.node"
    aksi:
        [actions here]
```

### Placeholders

- `%player%` - Player name
- `%block%` - Block type
- `{variableName}` - Variable value ⭐ NEW!
- `{variableName.%player%}` - Player-specific variable ⭐ NEW!
- `&` - Color codes (e.g., `&a` for green)

## 📝 Example Scripts

### Example 1: Welcome with Visit Counter
```
saat pemain masuk:
    tambah 1 ke variabel {kunjungan.%player%}
    kirim "&aSelamat datang, %player%!" ke pemain
    kirim "&7Ini kunjungan ke-&e{kunjungan.%player%}&7 kamu!" ke pemain
```

### Example 2: Diamond Ore Detection with Conditions
```
saat blok dihancurkan:
    jika blok adalah "DIAMOND_ORE":
        broadcast "&b✦ %player% menemukan diamond!"
        tambah 1 ke variabel {diamond.%player%}
    jika pemain punya izin "nusantara.vip":
        kirim "&6[VIP] &aBonus XP diberikan!" ke pemain
```

### Example 3: Custom Heal Command
```
perintah /sembuhkan:
    izin: "nusantara.heal"
    aksi:
        pulihkan pemain
        kirim "&aDarah dan lapar telah dipulihkan!" ke pemain
        broadcast "&e%player% telah menggunakan /sembuhkan"
```

### Example 4: VIP-Only Mining Area
```
saat blok dihancurkan:
    jika blok adalah "DIAMOND_ORE":
        jika pemain punya izin "area.vip":
            kirim "&aKamu menemukan diamond di area VIP!" ke pemain
            tambah 1 ke variabel {diamond.%player%}
```

### Example 5: Statistics Tracker
```
saat pemain masuk:
    tambah 1 ke variabel {login.%player%}
    kirim "&eTotal login: &a{login.%player%}" ke pemain

saat blok dihancurkan:
    tambah 1 ke variabel {blocks.%player%}
```

## 🎮 Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/nusantara reload` | `nusantara.admin` | Reload all scripts |
| `/nusantara list` | `nusantara.admin` | List loaded scripts |
| `/nusantara info` | `nusantara.admin` | Show plugin info |

## 📂 File Structure

```
plugins/
└── NusantaraScript/
    ├── scripts/
    │   ├── contoh.ns          # Sample script with Phase 2 examples
    │   ├── welcome.ns         # Your custom scripts
    │   ├── commands.ns        # Custom commands
    │   └── events.ns          # Event handlers
    └── (variables stored in memory)
```

## 🎓 Indentation Rules

NusantaraScript uses **4-space indentation** to understand code structure:

```
saat pemain masuk:              ← Level 0 (event trigger)
    kirim "Hello" ke pemain     ← Level 1 (direct action)
    jika pemain punya izin "vip": ← Level 1 (condition)
        kirim "VIP!" ke pemain  ← Level 2 (action inside condition)
```

**Important:** Use 4 spaces (or 1 tab) per indentation level!

## 🎯 Roadmap

### Completed ✅
- [x] Basic event handling
- [x] Indonesian syntax parser
- [x] Dynamic listener registration
- [x] **Phase 2: Conditional logic (jika)**
- [x] **Phase 2: Variable system (global & player)**
- [x] **Phase 2: Custom commands**
- [x] **Phase 2: Advanced actions (heal, feed, etc.)**

### Coming Soon 🚀
- [ ] More event types (death, respawn, damage, etc.)
- [ ] More conditions (health check, world check, etc.)
- [ ] More actions (teleport, give items, effects, etc.)
- [ ] Variable persistence (save to file/database)
- [ ] Else statements
- [ ] Math operations in variables
- [ ] Functions/procedures
- [ ] Database integration

## 🛠️ Technical Details

- **Java Version:** 17
- **Minecraft Version:** 1.21.4 (API 1.16+)
- **Dependencies:** None
- **Parser:** Custom lexer/parser with indentation-aware tokenization
- **Architecture:** Event-driven with dynamic listener registration

## 📄 License

This plugin is open source and available under the MIT License.

## 👨‍💻 Author

**crow6980**

## 🤝 Contributing

Contributions are welcome! Feel free to submit issues or pull requests.

## 📞 Support

For support, please open an issue on the GitHub repository.

---

**Made with ❤️ for the Indonesian Minecraft community**
