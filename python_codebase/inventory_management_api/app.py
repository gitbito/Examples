from flask import Flask
from config import configure_app
from rate_limiter import limiter
from routes.inventory import inventory_bp

app = Flask(__name__)
configure_app(app)
limiter.init_app(app)

app.register_blueprint(inventory_bp, url_prefix="/inventory")

if __name__ == "__main__":
    app.run(debug=True)
