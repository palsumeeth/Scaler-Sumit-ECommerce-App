### Product Read Policy v 1.0

1. DB is authoritative source of 
    - product state, and  
    - price
   for writes and validation.

2. Elasticsearch is a 
   - derived
   - eventually consistent
   read model used for search and listing queries. 
   
3. Search results are stale up to 10 seconds due to index lag.

4. Checkout must revalidate product price and `product.state` from DB for order creation. 

5. Products are never hard deleted immediately; they transition through defined states. 

6. Only products in `ACTIVE` state with available stock are eligible for purchase.

7. If search is unavailable, search endpoints return 503. 

8. Order service must validate product price and state against DB before order persistence.