from app.services.matching_service import compare, rank


def profile(user_id, teach=(), learn=(), interests=()):
    return {
        "id": user_id,
        "teach": [{"name": skill} for skill in teach],
        "learn": [{"name": skill} for skill in learn],
        "interests": list(interests),
    }


def test_compare_scores_complementary_skills_and_shared_interests():
    learner = profile(1, teach=["Python"], learn=["React"], interests=["AI"])
    peer = profile(2, teach=["React"], learn=["Python"], interests=["AI"])

    result = compare(learner, peer)

    assert result["matchedSkills"] == ["Python"]
    assert result["learningOpportunities"] == ["React"]
    assert result["sharedInterests"] == ["AI"]
    assert result["matchScore"] == 1.0


def test_compare_handles_empty_profiles_without_division_errors():
    result = compare(profile(1), profile(2))

    assert result["matchScore"] == 0.0
    assert "explanation" in result


def test_rank_excludes_self_connections_blocks_and_prior_recommendations():
    learner = profile(1, learn=["Python"])
    peer = profile(2, teach=["Python"])
    other = profile(3, teach=["Python"])

    results = rank(
        learner,
        [learner, peer, other],
        existing={(1, 3)},
        blocked=set(),
        previous_recommendations={(1, 2)},
    )

    assert results == []
