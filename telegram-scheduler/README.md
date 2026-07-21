# Telegram Userbot Message Scheduler

Sends messages **from your own Telegram account** on a schedule. Two modes,
usable per job (you can run many jobs at once):

- **interval** — send every X hours (or minutes) from the moment you start it
- **times** — send at exact clock times every day, e.g. `09:00` and `9:30 pm`

## Setup (one time)

1. **Get API credentials** (needed to log in as yourself):
   - Go to https://my.telegram.org → *API development tools*
   - Create an app (any name), copy the **api_id** and **api_hash**

2. **Install:**
   ```
   pip install -r requirements.txt
   ```

3. **Configure:**
   - Copy `config.example.json` to `config.json`
   - Put in your `api_id` / `api_hash`
   - Edit the `jobs` list (see below)

4. **Run:**
   ```
   python scheduler.py
   ```
   First run asks for your phone number and the login code Telegram sends
   you. After that it saves `userbot.session` and never asks again.
   Leave the script running; Ctrl+C stops it.

## Job options

| Field | Meaning |
|---|---|
| `name` | Label used in the log output |
| `target` | Where to send: `@username`, phone number, exact chat title, or numeric chat id. Use `"me"` for Saved Messages (good for testing) |
| `mode` | `"interval"` or `"times"` |
| `every_hours` / `every_minutes` | Interval mode: how often to send |
| `send_on_start` | Interval mode: send immediately on launch (default `true`) or wait one interval first |
| `times` | Times mode: list like `["09:00", "21:30"]` — also accepts `"9:30 pm"` |
| `messages` | List of one or more message texts |
| `send_style` | `"all"` = send every message each time, `"random"` = pick one at random, `"rotate"` = cycle through them in order |

Times use your PC's local clock.

## Notes / warnings

- This is a **userbot** — it acts as you. Telegram tolerates personal
  automation, but spamming people or groups that didn't ask for it can get
  your **account** limited or banned. Keep intervals reasonable and only
  message chats where it's welcome.
- Keep `config.json` and `userbot.session` private — the session file is a
  full login to your account.
- To run it 24/7, host it on a machine that stays on (e.g. your Proxmox VM)
  and run it under `tmux`/`screen` or a systemd service.
