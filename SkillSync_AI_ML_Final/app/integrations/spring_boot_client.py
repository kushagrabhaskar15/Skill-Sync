import requests
from app.core import config

class SpringBootClient:
    def headers(self):
        if not config.SPRING_BOOT_API_KEY:
            raise ValueError("SPRING_BOOT_API_KEY must match the backend INTERNAL_API_KEY.")
        return {
            "Content-Type": "application/json",
            "X-API-Key": config.SPRING_BOOT_API_KEY,
        }

    def post(self,path,payload):
        response=requests.post(
            config.SPRING_BOOT_BASE_URL + path,
            json=payload,
            headers=self.headers(),
            timeout=config.HTTP_TIMEOUT_SECONDS,
        )
        response.raise_for_status()
        try: body=response.json()
        except ValueError: body={"message":response.text}
        return {"statusCode":response.status_code,"body":body}
