"""Optional ESCO occupation context. Failures never affect peer ranking."""
from __future__ import annotations

import logging
import re
import threading
import time
from typing import Any

import requests

from app.core import config

logger = logging.getLogger(__name__)
_CACHE: dict[str, tuple[float, list[dict[str, str]]]] = {}
_LOCK = threading.Lock()
_CACHE_TTL_SECONDS = 24 * 60 * 60
_FAILURE_TTL_SECONDS = 60


def _normalize(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip().casefold()


def _localized(value: Any) -> list[str]:
    if isinstance(value, str):
        return [value.strip()] if value.strip() else []
    if isinstance(value, dict):
        selected = value.get(config.ESCO_LANGUAGE) or value.get("en")
        if selected is None and config.ESCO_LANGUAGE.startswith("en"):
            selected = value.get("en-us") or value.get("en-gb")
        if selected is not None:
            return _localized(selected)
        # Some ESCO records publish an English synonym under en-us only.
        return [text for item in value.values() for text in _localized(item)]
    if isinstance(value, list):
        return [text for item in value for text in _localized(item)]
    return []


def _skill_matches(item: dict[str, Any], query: str) -> bool:
    labels = [
        *_localized(item.get("title")),
        *_localized(item.get("preferredLabel")),
        *_localized(item.get("alternativeLabel")),
    ]
    expected = _normalize(query)
    return any(_normalize(label) == expected or _normalize(label).startswith(expected + " (") for label in labels)


def _related_occupations(skill_concept: dict[str, Any], limit: int = 3) -> list[dict[str, str]]:
    """Collect ESCO's declared essential/optional occupation relations for a skill."""
    links = skill_concept.get("_links", {})
    results: list[dict[str, str]] = []
    seen: set[str] = set()
    for relation in ("isEssentialForOccupation", "isOptionalForOccupation"):
        entries = links.get(relation, []) if isinstance(links, dict) else []
        if isinstance(entries, dict):
            entries = [entries]
        for entry in entries if isinstance(entries, list) else []:
            if not isinstance(entry, dict):
                continue
            title = str(entry.get("title") or "").strip()
            uri = str(entry.get("uri") or "").strip()
            key = _normalize(title)
            if title and key not in seen:
                results.append({"title": title, "uri": uri})
                seen.add(key)
                if len(results) >= limit:
                    return results
    return results


def occupations_for_skill(skill: str) -> list[dict[str, str]]:
    """Map an exact ESCO skill label/alias to its declared occupation relations."""
    normalized = _normalize(skill)
    if not normalized:
        return []
    now = time.monotonic()
    with _LOCK:
        cached = _CACHE.get(normalized)
        if cached and cached[0] > now:
            return list(cached[1])
    timeout = max(0.5, min(float(config.ESCO_TIMEOUT_SECONDS), 5.0))
    params: dict[str, str | int] = {
        "text": skill,
        "language": config.ESCO_LANGUAGE,
        "type": "skill",
        "full": "true",
        "limit": 10,
    }
    if config.ESCO_SELECTED_VERSION:
        params["selectedVersion"] = config.ESCO_SELECTED_VERSION
    try:
        response = requests.get(
            f"{config.ESCO_API_BASE_URL}/search",
            params=params,
            headers={"Accept": "application/json,application/json;charset=UTF-8"},
            timeout=timeout,
        )
        response.raise_for_status()
        payload = response.json()
        candidates = payload.get("_embedded", {}).get("results", [])
        concept = next(
            (item for item in candidates if isinstance(item, dict) and _skill_matches(item, skill)),
            None,
        )
        results = _related_occupations(concept) if concept else []
        expiry = now + _CACHE_TTL_SECONDS
    except Exception as exc:
        # Never log the query URL, response body, or user skill text.
        logger.warning("ESCO lookup unavailable (%s).", type(exc).__name__)
        results = []
        expiry = now + _FAILURE_TTL_SECONDS
    with _LOCK:
        _CACHE[normalized] = (expiry, results)
    return list(results)