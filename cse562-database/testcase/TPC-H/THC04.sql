     SELECT lineitem.shipmode, sum(CASE WHEN orders.orderpriority = '1-URGENT' OR orders.orderpriority = '2-HIGH' THEN 1 ELSE 0 END) AS high_line_count, sum(CASE WHEN orders.orderpriority <> '1-URGENT' AND orders.orderpriority <> '2-HIGH' THEN 1 ELSE 0 END) AS low_line_count 
    FROM orders, lineitem 
    WHERE orders.orderkey = lineitem.orderkey AND (lineitem.shipmode = 'AIR' OR lineitem.shipmode = 'MAIL' OR lineitem.shipmode = 'TRUCK' OR lineitem.shipmode = 'SHIP') AND lineitem.commitdate < lineitem.receiptdate AND lineitem.shipdate < lineitem.commitdate AND lineitem.receiptdate >= date('1995-03-05') AND lineitem.receiptdate < date('1996-03-05') 
    GROUP BY lineitem.shipmode 
    ORDER BY lineitem.shipmode;
