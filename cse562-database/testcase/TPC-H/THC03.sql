     SELECT lineitem.orderkey, sum(lineitem.extendedprice * (1 - lineitem.discount))AS revenue, orders.orderdate, orders.shippriority 
    FROM customer, orders, lineitem WHERE customer.mktsegment = 'BUILDING' AND customer.custkey = orders.custkey AND lineitem.orderkey = orders.orderkey AND orders.orderdate < DATE('1995-03-15')AND lineitem.shipdate > DATE('1995-03-15') 
    GROUP BY lineitem.orderkey, orders.orderdate, orders.shippriority 
    ORDER BY revenue DESC, orders.orderdate;
