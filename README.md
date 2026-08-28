# Mongol Zangila

A traditional Mongolian script social network for Flutter, written in [ClojureDart](https://github.com/tensegritics/ClojureDart). The UI is built for **vertical Mongolian typography** (left-to-right columns) with a custom IME, and talks to a Phoenix API for feed, chat, and realtime events.

## Features

- **Auth** — phone login, registration, OTP, password reset
- **Feed** — chronological timeline, post detail, quotes, likes, bookmarks, rich-text compose with mentions/topics and media
- **Offline outbox** — drafts and pending mutations stored in Drift (SQLite) and synced when the network returns
- **Search & discovery** — explore, keyword search, topic feeds, QR scanner
- **Chat** — DMs and groups, voice messages, unread badges
- **Profile** — edit profile, followers/following, QR card, blocklist, settings, light/dark theme
- **Notifications** — likes, mentions, and other social events
- **Realtime** — Phoenix WebSockets for inbox updates and notification counts
- **Mongolian IME** — on-device FST dictionary plus next-word prediction on mobile

## Tech stack

| Layer | Stack |
| --- | --- |
| Client | Flutter, ClojureDart, [mongol](https://github.com/suragch/mongol) widgets |
| Local storage | Drift / SQLite |
| HTTP / realtime | `http`, Phoenix Channels (`phoenix_wings`) |
| Backend | Elixir, Phoenix 1.8, Ecto, PostgreSQL, Guardian JWT |
| Script / IME | Traditional Mongolian fonts, FST assets, virtual keyboard |

## Repository layout

```
.
├── src/mongol_zangila/     # ClojureDart app source
│   ├── main.cljd           # App entry
│   ├── router.cljd         # Named routes
│   ├── state.cljd          # In-memory atoms
│   ├── controllers/        # Auth, feed, chat, publish, search, profile
│   ├── infrastructure/     # API, session, sockets, Drift, media
│   └── ui/                 # Screens and widgets
├── backend/                # Phoenix API (port 4000)
├── assets/                 # Fonts, FST dictionaries, env
├── lib/                    # ClojureDart compile output + Dart interop
├── deps.edn                # ClojureDart deps
└── pubspec.yaml            # Flutter deps
```

API base URL lives in `src/mongol_zangila/infrastructure/config.cljd` (default: `http://localhost:4000/api/v1`). Backend details: [`backend/README.md`](backend/README.md).

## Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure) (`clj`)
- [Flutter](https://docs.flutter.dev/get-started/install) (Dart SDK ^3.10.4)
- [Elixir](https://elixir-lang.org/install.html) ~> 1.15 and PostgreSQL (for the backend)

These sibling repos must sit next to this project (paths from `deps.edn` / `pubspec.yaml`):

| Path | Role |
| --- | --- |
| `mgl-components` | Shared Mongolian UI components |
| `mongol-virtual-keyboard` | Mongolian virtual keyboard |
| `mongol-ime` | Desktop IME |
| `mgl-richtext-editor` | Rich-text composer (`mgl_editor_core`) |
| `horizontal-pull-to-refresh` | Horizontal pull-to-refresh |

## Getting started

### 1. Backend

```bash
cd backend
mix setup
mix phx.server
```

The API listens on `http://localhost:4000`. WebSocket endpoint: `ws://localhost:4000/socket/websocket`.

In development, OTP codes are returned as `dev_code` so you can complete registration without an SMS provider.

Demo accounts after seeds:

| Phone | Password |
| --- | --- |
| `99001122` | `password123` |
| `99003344` | `password123` |

### 2. Flutter client

From the repository root:

```bash
clj -M:cljd init
clj -M:cljd flutter
```

`clj -M:cljd flutter` compiles ClojureDart and runs the Flutter app (device, simulator, or desktop).

On macOS, open a simulator first if you want iOS:

```bash
open -a Simulator
```

### 3. Optional IME next-word service

The client warms a next-word API at `http://localhost:3003`. Leave that service running if you use server-side candidate ranking; on-device FST assets still load without it.

## Architecture

```
UI (ClojureDart / Flutter)
        │
        ▼
  controllers  ──►  infrastructure/api  ──►  Phoenix REST  /api/v1
        │                    │
        │                    └──►  Phoenix Channels  /socket/websocket
        ▼
     state atoms
        │
        ▼
  Drift outbox / drafts  (offline-first writes)
```

- **Controllers** own domain logic (auth, feed, chat, publish).
- **Infrastructure** handles HTTP, JWT session, sockets, media, and local DB.
- **State** is a small set of reactive atoms (token, current user, unread counts, network status).
- Failed or offline writes go into the **outbox** and retry when the API is reachable.

## Credits

Traditional Mongolian rendering and encoding:

- [suragch/mongol](https://github.com/suragch/mongol) — vertical Mongolian widgets for Flutter
- [suragch/mongol_code](https://github.com/suragch/mongol_code) — Unicode conversion for traditional Mongolian script

Compile toolchain: [ClojureDart](https://github.com/tensegritics/ClojureDart).

## License

[Eclipse Public License 2.0](LICENSE)
