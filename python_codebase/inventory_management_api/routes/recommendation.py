from collections import defaultdict
from db import execute_query, get_shards, get_shard


class RecommendationEngine:
    def __init__(self):
        self.shards = get_shards()

    def get_recommendations(self, user_id, num_recommendations=5):
        try:
            product_counts = defaultdict(int)

            # Fetch past orders of the user
            for shard in self.shards:
                query = """
                    SELECT 
                        oi.product_id, COUNT(*) AS count
                    FROM 
                        orders o
                    JOIN 
                        order_items oi ON o.id = oi.order_id
                    WHERE 
                        o.user_id = %s
                    GROUP BY 
                        oi.product_id
                """
                cursor = execute_query(shard, query, (user_id,))
                results = cursor.fetchall()

                for product_id, count in results:
                    product_counts[product_id] += count

            # Fetch recent search history of the user
            for shard in self.shards:
                query = """
                    SELECT 
                        product_id
                    FROM 
                        search_history
                    WHERE 
                        user_id = %s
                    ORDER BY 
                        search_time DESC
                    LIMIT %s
                """
                cursor = execute_query(shard, query, (user_id, num_recommendations))
                results = cursor.fetchall()

                for (product_id,) in results:
                    product_counts[
                        product_id
                    ] += 1  # Increment count for searched products

            # Aggregate recommendations from past orders and recent searches
            recommendations = sorted(
                product_counts.items(), key=lambda x: x[1], reverse=True
            )[:num_recommendations]
            return [product_id for product_id, _ in recommendations]
        except Exception as e:
            print(f"Error occurred while fetching recommendations: {str(e)}")
            return []


recommendation_engine = RecommendationEngine()
