import requests
from app.core import config

class SpringBootClient:
    def headers(self):
        h={"Content-Type":"application/json"}
        if config.SPRING_BOOT_API_TOKEN:
            h["Authorization"]="Bearer "+config.SPRING_BOOT_API_TOKEN
        return h

    def post(self,path,payload):
        response=requests.post(config.SPRING_BOOT_BASE_URL+path,json=payload,
                               headers=self.headers(),timeout=config.HTTP_TIMEOUT_SECONDS)
        response.raise_for_status()
        try: body=response.json()
        except ValueError: body={"message":response.text}
        return {"statusCode":response.status_code,"body":body}
