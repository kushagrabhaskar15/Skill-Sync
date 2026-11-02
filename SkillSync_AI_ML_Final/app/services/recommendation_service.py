import logging

from app.core import config
from app.integrations.esco_client import occupations_for_skill
from app.services.matching_service import rank, compare

logger = logging.getLogger(__name__)


def ingestion_payload(result):
    """Convert AI results to the current Spring Boot ingestion DTO."""
    recommendations = result["recommendations"]
    if not recommendations:
        return None
    return {
        "userId": result["userId"],
        "sourceVersion": result["sourceVersion"],
        "results": [
            {
                "recommendedUserId": item["recommendedUserId"],
                "score": item["matchScore"],
                "explanation": item["explanation"],
            }
            for item in recommendations
        ],
    }


class RecommendationService:
    def __init__(self, repo):
        self.repo = repo

    def _enrich(self, recommendations):
        """Add ESCO context after ranking; API failure leaves matches unchanged."""
        max_skills = max(0, min(config.ESCO_MAX_SKILLS_PER_GENERATION, 3))
        if not max_skills:
            return recommendations
        lookups: dict[str, list[dict[str, str]]] = {}
        for item in recommendations:
            skills = list(dict.fromkeys(
                item.get("matchedSkills", []) + item.get("learningOpportunities", [])
            ))
            contexts: list[dict[str, str]] = []
            for skill in skills:
                key = skill.casefold().strip()
                if not key:
                    continue
                if key not in lookups and len(lookups) < max_skills:
                    try:
                        lookups[key] = occupations_for_skill(skill)
                    except Exception as exc:
                        logger.warning("ESCO enrichment skipped (%s).", type(exc).__name__)
                        lookups[key] = []
                contexts.extend(lookups.get(key, []))
            unique = list({entry["title"].casefold(): entry for entry in contexts}.values())[:3]
            if unique:
                titles = ", ".join(entry["title"] for entry in unique)
                item["escoCareerContext"] = unique
                item["explanation"] += (
                    f" Related ESCO occupation results: {titles}. "
                    "Source: ESCO (https://esco.ec.europa.eu/)."
                )
        return recommendations

    def generate(self, user_id):
        profiles, existing, blocked, previous = self.repo.get_profiles()
        user = next((profile for profile in profiles if profile["id"] == user_id), None)
        if user is None:
            raise LookupError("Active user not found.")
        recommendations = rank(user, profiles, existing, blocked, previous)
        self._enrich(recommendations)
        return {"userId": user_id, "recommendations": recommendations,
                "sourceVersion": config.SOURCE_VERSION}

    def compare(self, user_id, other_id):
        profiles, _, _, _ = self.repo.get_profiles()
        a = next((profile for profile in profiles if profile["id"] == user_id), None)
        b = next((profile for profile in profiles if profile["id"] == other_id), None)
        if a is None or b is None:
            raise LookupError("One or both active users were not found.")
        return {"userId": user_id, "otherUserId": other_id, **compare(a, b)}