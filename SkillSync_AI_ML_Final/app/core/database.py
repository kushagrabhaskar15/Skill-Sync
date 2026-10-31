from urllib.parse import quote_plus
from sqlalchemy import create_engine, text
from app.core import config

config.validate()
url=(f"mysql+pymysql://{config.DB_USER}:{quote_plus(config.DB_PASSWORD)}"
     f"@{config.DB_HOST}:{config.DB_PORT}/{config.DB_NAME}?charset=utf8mb4")
engine=create_engine(url,pool_pre_ping=True,pool_recycle=1800,pool_size=5,
                     max_overflow=5,connect_args={"connect_timeout":5})

def ping():
    with engine.connect() as conn:
        conn.execute(text("SELECT 1"))
