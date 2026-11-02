import requests
import pytest

from app.core import config
from app.integrations.spring_boot_client import SpringBootClient


def test_client_uses_existing_backend_api_key_header(monkeypatch):
    captured = {}

    class Response:
        status_code = 201
        text = "[]"

        def raise_for_status(self):
            return None

        def json(self):
            return []

    def fake_post(url, **kwargs):
        captured["url"] = url
        captured.update(kwargs)
        return Response()

    monkeypatch.setattr(config, "SPRING_BOOT_BASE_URL", "http://localhost:8080")
    monkeypatch.setattr(config, "SPRING_BOOT_API_KEY", "test-key")
    monkeypatch.setattr(requests, "post", fake_post)

    result = SpringBootClient().post(
        config.SPRING_BOOT_RECOMMENDATION_INGEST_PATH,
        {"userId": 1, "sourceVersion": "test", "results": []},
    )

    assert captured["url"] == "http://localhost:8080/api/v1/internal/recommendations"
    assert captured["headers"]["X-API-Key"] == "test-key"
    assert "Authorization" not in captured["headers"]
    assert result["statusCode"] == 201


def test_client_requires_internal_api_key(monkeypatch):
    monkeypatch.setattr(config, "SPRING_BOOT_API_KEY", "")

    with pytest.raises(ValueError, match="SPRING_BOOT_API_KEY"):
        SpringBootClient().headers()
