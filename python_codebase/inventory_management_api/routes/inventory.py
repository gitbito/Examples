from flask import Blueprint, request, jsonify
import mysql.connector
from cache import get_cache
from db import get_shards, get_shard, execute_query
from rate_limiter import limiter


class InventoryRoutes:
    def __init__(self):
        self.shards = get_shards()
        self.cache = get_cache()

    def get_inventory(self):
        try:
            product_id = request.args.get("product_id")
            page = int(request.args.get("page", 1))
            per_page = int(request.args.get("per_page", 10))

            offset = (page - 1) * per_page
            cache_key = f"inventory_{product_id}_{page}_{per_page}"
            cached_inventory = self.cache.get(cache_key)

            if cached_inventory:
                return jsonify({"inventory": cached_inventory.decode("utf-8")})

            shard = get_shard(int(product_id), self.shards)
            query = f"""
                SELECT 
                    i.product_id, i.quantity, p.name AS product_name, c.name AS category_name
                FROM 
                    inventory i
                JOIN 
                    products p ON i.product_id = p.id
                JOIN 
                    categories c ON p.category_id = c.id
                WHERE 
                    i.product_id = %s
                LIMIT %s OFFSET %s
            """
            cursor = execute_query(shard, query, (product_id, per_page, offset))
            inventory = cursor.fetchall()

            if inventory:
                self.cache.setex(cache_key, 300, str(inventory))  # Cache for 5 minutes
                return jsonify({"inventory": inventory})

            return jsonify({"error": "Product not found"}), 404
        except mysql.connector.Error as err:
            return jsonify({"error": str(err)}), 500

    def add_inventory(self):
        try:
            data = request.json
            product_id = data.get("product_id")
            quantity = data.get("quantity")

            if not product_id or not quantity:
                return jsonify({"error": "Invalid data"}), 400

            shard = get_shard(int(product_id), self.shards)
            query = "INSERT INTO inventory (product_id, quantity) VALUES (%s, %s)"
            cursor = execute_query(shard, query, (product_id, quantity))
            shard.commit()

            return jsonify({"message": "Inventory added successfully"}), 201
        except mysql.connector.Error as err:
            return jsonify({"error": str(err)}), 500

    def update_inventory(self):
        try:
            data = request.json
            product_id = data.get("product_id")
            quantity = data.get("quantity")

            if not product_id or not quantity:
                return jsonify({"error": "Invalid data"}), 400

            shard = get_shard(int(product_id), self.shards)
            query = "UPDATE inventory SET quantity = %s WHERE product_id = %s"
            cursor = execute_query(shard, query, (quantity, product_id))
            shard.commit()

            # Invalidate cache after update
            self.cache.delete(f"inventory_{product_id}")

            return jsonify({"message": "Inventory updated successfully"}), 200
        except mysql.connector.Error as err:
            return jsonify({"error": str(err)}), 500

    def delete_inventory(self):
        try:
            data = request.json
            product_id = data.get("product_id")

            if not product_id:
                return jsonify({"error": "Invalid data"}), 400

            shard = get_shard(int(product_id), self.shards)
            query = "DELETE FROM inventory WHERE product_id = %s"
            cursor = execute_query(shard, query, (product_id,))
            shard.commit()

            # Invalidate cache after delete
            self.cache.delete(f"inventory_{product_id}")

            # Confirm deletion
            if cursor.rowcount == 0:
                return jsonify({"error": "Product not found"}), 404

            return jsonify({"message": "Inventory deleted successfully"}), 200
        except mysql.connector.Error as err:
            return jsonify({"error": str(err)}), 500


inventory_routes = InventoryRoutes()

inventory_bp = Blueprint("inventory", __name__)

inventory_bp.route("/", methods=["GET"])(inventory_routes.get_inventory)
inventory_bp.route("/add", methods=["POST"])(inventory_routes.add_inventory)
inventory_bp.route("/update", methods=["POST"])(inventory_routes.update_inventory)
inventory_bp.route("/delete", methods=["POST"])(inventory_routes.delete_inventory)
