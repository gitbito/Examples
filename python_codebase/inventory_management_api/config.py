import os


def configure_app(app):
    app.config["REDIS_HOST"] = "localhost"
    app.config["REDIS_PORT"] = 6379
    app.config["DB_HOST"] = "localhost"
    app.config["DB_USER"] = "user"
    app.config["DB_PASSWORD"] = os.getenv("DB_PASSWORD", "default")
    app.config["DB_NAME_1"] = "inventory_db1"
    app.config["DB_NAME_2"] = "inventory_db2"
