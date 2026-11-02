from sqlalchemy import text

from app.core.database import engine


class SkillSyncRepository:
    """Read-only queries aligned with the Spring Boot Flyway schema."""

    def all(self, sql, params=None):
        with engine.connect() as connection:
            return [dict(row._mapping) for row in connection.execute(text(sql), params or {})]

    def get_profiles(self):
        users = self.all(
            "SELECT id, name, bio, location FROM users WHERE status = 'ACTIVE'"
        )
        skills = self.all(
            "SELECT us.user_id, us.skill_type, us.proficiency_level, us.years_experience, "
            "s.id AS skill_id, s.name AS skill_name, s.category AS skill_category "
            "FROM user_skills us JOIN skills s ON s.id = us.skill_id"
        )
        interests = self.all(
            "SELECT user_id, interest_name FROM user_interests"
        )
        links = self.all(
            "SELECT requester_id, receiver_id, status FROM connections "
            "WHERE status IN ('PENDING', 'ACCEPTED', 'BLOCKED')"
        )
        saved_recommendations = self.all(
            "SELECT user_id, recommended_user_id FROM recommendations"
        )

        profiles = {
            int(user["id"]): {
                "id": int(user["id"]),
                "name": user["name"],
                "bio": user.get("bio") or "",
                "location": user.get("location"),
                "teach": [],
                "learn": [],
                "interests": [],
            }
            for user in users
        }

        for row in skills:
            profile = profiles.get(int(row["user_id"]))
            if profile is None:
                continue
            skill = {
                "id": int(row["skill_id"]),
                "name": row["skill_name"],
                "category": row.get("skill_category"),
                "level": row.get("proficiency_level"),
                "yearsExperience": int(row.get("years_experience") or 0),
            }
            if row["skill_type"] == "TEACH":
                profile["teach"].append(skill)
            elif row["skill_type"] == "LEARN":
                profile["learn"].append(skill)

        for row in interests:
            profile = profiles.get(int(row["user_id"]))
            if profile is not None and row.get("interest_name"):
                profile["interests"].append(row["interest_name"])

        existing_connections = set()
        blocked_connections = set()
        for row in links:
            pair = tuple(sorted((int(row["requester_id"]), int(row["receiver_id"]))))
            existing_connections.add(pair)
            if row["status"] == "BLOCKED":
                blocked_connections.add(pair)

        previous_recommendations = {
            (int(row["user_id"]), int(row["recommended_user_id"]))
            for row in saved_recommendations
        }

        return (
            list(profiles.values()),
            existing_connections,
            blocked_connections,
            previous_recommendations,
        )

    def get_profile(self, user_id):
        profiles, _, _, _ = self.get_profiles()
        return next((profile for profile in profiles if profile["id"] == user_id), None)

    def get_saved_recommendations(self, user_id):
        return self.all(
            "SELECT id, user_id, recommended_user_id, score, explanation, status, "
            "source_version, generated_at FROM recommendations "
            "WHERE user_id = :user_id ORDER BY generated_at DESC, score DESC",
            {"user_id": user_id},
        )

    def get_recommendation(self, recommendation_id):
        rows = self.all(
            "SELECT id, user_id, recommended_user_id, score, explanation, status, "
            "source_version, generated_at FROM recommendations WHERE id = :id",
            {"id": recommendation_id},
        )
        return rows[0] if rows else None
