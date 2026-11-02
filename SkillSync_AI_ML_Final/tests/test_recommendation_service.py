from app.services.recommendation_service import ingestion_payload


def test_ingestion_payload_matches_spring_dto():
    result = {
        "userId": 10,
        "sourceVersion": "unit-test",
        "recommendations": [
            {
                "recommendedUserId": 20,
                "matchScore": 0.875,
                "explanation": "Complementary skills.",
                "matchedSkills": ["Python"],
                "learningOpportunities": ["React"],
                "sharedInterests": ["AI"],
            }
        ],
    }

    assert ingestion_payload(result) == {
        "userId": 10,
        "sourceVersion": "unit-test",
        "results": [
            {
                "recommendedUserId": 20,
                "score": 0.875,
                "explanation": "Complementary skills.",
            }
        ],
    }


def test_empty_matches_skip_backend_ingestion():
    assert ingestion_payload({"userId": 10, "sourceVersion": "unit-test", "recommendations": []}) is None
