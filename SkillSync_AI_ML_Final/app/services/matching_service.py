from app.core import config
from app.services.nlp_service import normalize_skill, similarity, shared_items

def complementary(teacher,learner):
    t={normalize_skill(x["name"]):x["name"] for x in teacher["teach"]}
    l={normalize_skill(x["name"]):x["name"] for x in learner["learn"]}
    return [t[k] for k in sorted(set(t)&set(l))]

def compare(a,b):
    a_to_b=complementary(a,b)
    b_to_a=complementary(b,a)
    requests=len(a["learn"])+len(b["learn"])
    skill_score=(len(a_to_b)+len(b_to_a))/requests if requests else 0.0
    shared=shared_items(a["interests"],b["interests"])
    interest_score=similarity(a["interests"],b["interests"])
    weights=[config.WEIGHT_SKILLS,config.WEIGHT_INTERESTS]
    score=(config.WEIGHT_SKILLS*skill_score+
           config.WEIGHT_INTERESTS*interest_score)/sum(weights)
    reasons=[]
    if a_to_b: reasons.append("You can teach "+", ".join(a_to_b))
    if b_to_a: reasons.append("This user can teach you "+", ".join(b_to_a))
    if shared: reasons.append("You share interests in "+", ".join(shared))
    if not reasons: reasons.append("Your interests show textual similarity")
    return {"matchScore":round(max(0,min(1,score)),4),
            "matchedSkills":a_to_b,"learningOpportunities":b_to_a,
            "sharedInterests":shared,"explanation":"; ".join(reasons)+".",
            "scoreBreakdown":{"skillComplementarity":round(skill_score,4),
              "interestSimilarity":round(interest_score,4)}}

def rank(user,profiles,existing,blocked,previous_recommendations=None):
    previous_recommendations=previous_recommendations or set()
    results=[]
    for other in profiles:
        if other["id"]==user["id"]: continue
        pair=tuple(sorted((user["id"],other["id"])))
        if pair in existing or pair in blocked or (user["id"],other["id"]) in previous_recommendations: continue
        m=compare(user,other)
        if m["matchScore"]<config.MIN_MATCH_SCORE: continue
        results.append({"recommendedUserId":other["id"],**m})
    results.sort(key=lambda x:(-x["matchScore"],x["recommendedUserId"]))
    return results[:config.MAX_RECOMMENDATIONS]
