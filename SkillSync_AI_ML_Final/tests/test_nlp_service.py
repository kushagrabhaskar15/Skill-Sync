from app.services.nlp_service import normalize_skill, shared_items

def test_aliases():
    assert normalize_skill("ML")=="machine learning"
    assert normalize_skill("React.js")=="react"
    assert normalize_skill("Java")!="javascript"

def test_shared_interests():
    assert shared_items(["AI","Web Development"],["ai"])==["AI"]

def test_normalize_whitespace_and_case():
    assert normalize_skill("  Machine   Learning ")=="machine learning"
