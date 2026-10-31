from sqlalchemy import text
from app.core.database import engine

class SkillSyncRepository:
    # SQL is aligned to the uploaded MySQL schema; this service only performs SELECTs.
    def all(self, sql, params=None):
        with engine.connect() as conn:
            return [dict(r._mapping) for r in conn.execute(text(sql), params or {})]

    def get_profiles(self):
        users=self.all("SELECT id,name,bio,location FROM users WHERE status='ACTIVE'")
        skills=self.all(
            "SELECT us.user_id,us.skill_type,us.proficiency_level,us.years_experience,"
            "s.id AS skill_id,s.name AS skill_name,s.category AS skill_category "
            "FROM user_skills us JOIN skills s ON s.id=us.skill_id"
        )
        interests=self.all("SELECT user_id,interest_name FROM user_interests")
        goals=self.all(
            "SELECT user_id,title,description FROM learning_goals WHERE status='ACTIVE'"
        )
        slots=self.all(
            "SELECT user_id,day_of_week,start_time,end_time,timezone FROM availability_slots"
        )
        links=self.all(
            "SELECT requester_id,receiver_id,status FROM connections "
            "WHERE status IN ('PENDING','ACCEPTED','BLOCKED')"
        )
        profiles={int(u["id"]):{
            "id":int(u["id"]),"name":u["name"],"bio":u.get("bio") or "",
            "location":u.get("location"),"teach":[],"learn":[],"interests":[],
            "goals":[],"availability":[]} for u in users}
        for r in skills:
            p=profiles.get(int(r["user_id"]))
            if not p: continue
            item={"id":int(r["skill_id"]),"name":r["skill_name"],
                  "category":r.get("skill_category"),"level":r.get("proficiency_level"),
                  "yearsExperience":int(r.get("years_experience") or 0)}
            if r["skill_type"]=="TEACH": p["teach"].append(item)
            elif r["skill_type"]=="LEARN": p["learn"].append(item)
        for r in interests:
            p=profiles.get(int(r["user_id"]))
            if p and r.get("interest_name"): p["interests"].append(r["interest_name"])
        for r in goals:
            p=profiles.get(int(r["user_id"]))
            if p:
                value=" ".join(v for v in [r.get("title"),r.get("description")] if v)
                if value.strip(): p["goals"].append(value)
        for r in slots:
            p=profiles.get(int(r["user_id"]))
            if p:
                p["availability"].append({
                    "dayOfWeek":r["day_of_week"],"startTime":str(r["start_time"]),
                    "endTime":str(r["end_time"]),"timezone":r.get("timezone") or "Asia/Kolkata"})
        existing=set()
        blocked=set()
        for r in links:
            pair=tuple(sorted((int(r["requester_id"]),int(r["receiver_id"]))))
            existing.add(pair)
            if r["status"]=="BLOCKED": blocked.add(pair)
        return list(profiles.values()),existing,blocked

    def get_profile(self,user_id):
        profiles,_,_=self.get_profiles()
        return next((p for p in profiles if p["id"]==user_id),None)

    def get_saved_recommendations(self,user_id):
        return self.all(
            "SELECT id,user_id,recommended_user_id,match_score,matched_skills,"
            "learning_opportunities,shared_interests,explanation,status,source_version,generated_at "
            "FROM recommendations WHERE user_id=:uid ORDER BY generated_at DESC,match_score DESC",
            {"uid":user_id})

    def get_recommendation(self,rid):
        rows=self.all(
            "SELECT id,user_id,recommended_user_id,match_score,matched_skills,"
            "learning_opportunities,shared_interests,explanation,status,source_version,generated_at "
            "FROM recommendations WHERE id=:rid",{"rid":rid})
        return rows[0] if rows else None
