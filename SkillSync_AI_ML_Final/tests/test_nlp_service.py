from app.services.nlp_service import normalize_skill,shared_items

def test_aliases():
    assert normalize_skill("ML")=="machine learning"
    assert normalize_skill("React.js")=="react"
    assert normalize_skill("Java")!="javascript"

def test_shared_interests():
    assert shared_items(["AI","Web Development"],["ai"])==["AI"]
