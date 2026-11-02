import re
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

ALIASES={"ml":"machine learning","machine-learning":"machine learning",
         "js":"javascript","reactjs":"react","react.js":"react",
         "nodejs":"node.js","springboot":"spring boot",
         "ui ux":"ui/ux design","ui/ux":"ui/ux design",
         "postgres":"postgresql","sklearn":"scikit-learn"}

def normalize_skill(value):
    value=re.sub(r"\s+"," ",(value or "").strip().lower())
    return ALIASES.get(value,value)

def normalize_many(values):
    return list(dict.fromkeys(normalize_skill(v) for v in values if v and normalize_skill(v)))

def similarity(left,right):
    a=" ".join(left).strip()
    b=" ".join(right).strip()
    if not a or not b: return 0.0
    try:
        matrix=TfidfVectorizer(ngram_range=(1,2),stop_words="english").fit_transform([a,b])
        return float(cosine_similarity(matrix[0:1],matrix[1:2])[0][0])
    except ValueError:
        return 0.0

def shared_items(left,right):
    keys={normalize_skill(x) for x in right}
    return sorted({x for x in left if normalize_skill(x) in keys},key=str.lower)
