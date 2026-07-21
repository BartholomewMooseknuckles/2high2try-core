#!/usr/bin/env python3
"""
Telegram userbot message scheduler.

Sends one or more messages to chosen chats on a schedule, using YOUR
Telegram account (via Telethon). Two scheduling modes per job:

  - "interval": send every N hours (or minutes), starting from launch
  - "times":    send at exact clock times each day, e.g. 09:00 and 21:30

Configure everything in config.json (see config.example.json / README.md).
First run will ask for your phone number + login code, then it saves a
session file so you never have to log in again.
"""

import asyncio
import json
import random
import sys
from datetime import datetime, timedelta
from pathlib import Path

from telethon import TelegramClient

CONFIG_PATH = Path(__file__).parent / "config.json"
SESSION_PATH = Path(__file__).parent / "userbot"


def log(msg: str) -> None:
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {msg}", flush=True)


def load_config() -> dict:
    if not CONFIG_PATH.exists():
        sys.exit(
            f"No config.json found at {CONFIG_PATH}.\n"
            "Copy config.example.json to config.json and fill it in."
        )
    with open(CONFIG_PATH, encoding="utf-8") as f:
        cfg = json.load(f)

    if not cfg.get("api_id") or not cfg.get("api_hash"):
        sys.exit("config.json needs your api_id and api_hash from https://my.telegram.org")
    if not cfg.get("jobs"):
        sys.exit("config.json has no jobs — add at least one entry to \"jobs\".")

    for i, job in enumerate(cfg["jobs"]):
        mode = job.get("mode")
        if mode not in ("interval", "times"):
            sys.exit(f"Job {i}: \"mode\" must be \"interval\" or \"times\".")
        if mode == "interval" and not (job.get("every_hours") or job.get("every_minutes")):
            sys.exit(f"Job {i}: interval mode needs \"every_hours\" or \"every_minutes\".")
        if mode == "times" and not job.get("times"):
            sys.exit(f"Job {i}: times mode needs a \"times\" list like [\"09:00\", \"21:30\"].")
        if not job.get("target"):
            sys.exit(f"Job {i}: needs a \"target\" (@username, phone, chat title, or chat id).")
        if not job.get("messages"):
            sys.exit(f"Job {i}: needs a \"messages\" list with at least one message.")

    return cfg


def parse_time(s: str) -> tuple[int, int]:
    """Accept '9:00', '09:00', '9:00 pm', '09:00PM', '21:30'."""
    s = s.strip().lower()
    pm = s.endswith("pm")
    am = s.endswith("am")
    if pm or am:
        s = s[:-2].strip()
    hh, mm = s.split(":")
    hour, minute = int(hh), int(mm)
    if pm and hour != 12:
        hour += 12
    if am and hour == 12:
        hour = 0
    if not (0 <= hour <= 23 and 0 <= minute <= 59):
        raise ValueError(f"bad time: {s}")
    return hour, minute


def next_fixed_time(times: list[tuple[int, int]]) -> datetime:
    """Next datetime (today or tomorrow) matching one of the given clock times."""
    now = datetime.now()
    candidates = []
    for hour, minute in times:
        t = now.replace(hour=hour, minute=minute, second=0, microsecond=0)
        if t <= now:
            t += timedelta(days=1)
        candidates.append(t)
    return min(candidates)


def pick_messages(job: dict, counter: int) -> list[str]:
    """Which message(s) to send this round, based on job's send_style."""
    msgs = job["messages"]
    style = job.get("send_style", "all")
    if style == "all":
        return list(msgs)
    if style == "random":
        return [random.choice(msgs)]
    if style == "rotate":
        return [msgs[counter % len(msgs)]]
    sys.exit(f"Unknown send_style \"{style}\" — use \"all\", \"random\", or \"rotate\".")


async def run_job(client: TelegramClient, job: dict, index: int) -> None:
    name = job.get("name", f"job {index}")
    target = job["target"]

    # Resolve the target once up front so typos fail fast.
    try:
        entity = await client.get_entity(target)
    except Exception as e:
        log(f"[{name}] Could not find target {target!r}: {e}")
        return

    counter = 0

    if job["mode"] == "interval":
        minutes = job.get("every_minutes") or job["every_hours"] * 60
        delay = minutes * 60
        if job.get("send_on_start", True):
            pass  # first send happens immediately below
        else:
            log(f"[{name}] First send in {minutes} min.")
            await asyncio.sleep(delay)
        while True:
            for text in pick_messages(job, counter):
                await client.send_message(entity, text)
                log(f"[{name}] Sent to {target}: {text[:60]!r}")
            counter += 1
            log(f"[{name}] Next send in {minutes} min.")
            await asyncio.sleep(delay)

    else:  # mode == "times"
        times = [parse_time(t) for t in job["times"]]
        while True:
            nxt = next_fixed_time(times)
            wait = (nxt - datetime.now()).total_seconds()
            log(f"[{name}] Next send at {nxt.strftime('%Y-%m-%d %H:%M')} ({wait/60:.0f} min).")
            await asyncio.sleep(max(wait, 0))
            for text in pick_messages(job, counter):
                await client.send_message(entity, text)
                log(f"[{name}] Sent to {target}: {text[:60]!r}")
            counter += 1
            await asyncio.sleep(61)  # step past the minute so we don't double-fire


async def main() -> None:
    cfg = load_config()
    client = TelegramClient(str(SESSION_PATH), cfg["api_id"], cfg["api_hash"])
    await client.start()  # first run: prompts for phone + code, then cached
    me = await client.get_me()
    log(f"Logged in as {me.first_name} (@{me.username}). {len(cfg['jobs'])} job(s) loaded.")

    await asyncio.gather(*(run_job(client, job, i) for i, job in enumerate(cfg["jobs"])))


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\nStopped.")
