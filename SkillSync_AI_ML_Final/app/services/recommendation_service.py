from app.core import config
from app.services.matching_service import rank,compare

class RecommendationService:
    def __init__(self,repo): self.repo=repo

    def generate(self,user_id):
        profiles,existing,blocked=self.repo.get_profiles()
        user=next((p for p in profiles if p["id"]==user_id),None)
        if user is None: raise LookupError("Active user not found.")
        return {"userId":user_id,"recommendations":rank(user,profiles,existing,blocked),
                "sourceVersion":config.SOURCE_VERSION}

    def compare(self,user_id,other_id):
        profiles,_,_=self.repo.get_profiles()
        a=next((p for p in profiles if p["id"]==user_id),None)
        b=next((p for p in profiles if p["id"]==other_id),None)
        if a is None or b is None: raise LookupError("One or both active users were not found.")
        return {"userId":user_id,"otherUserId":other_id,**compare(a,b)}
