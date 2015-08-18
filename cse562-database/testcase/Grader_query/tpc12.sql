CREATE TABLE LINEITEM (
        orderkey       INT,
        partkey        INT,
        suppkey        INT,
        linenumber     INT,
        quantity       DECIMAL,
        extendedprice  DECIMAL,
        discount       DECIMAL,
        tax            DECIMAL,
        returnflag     CHAR(1),
        linestatus     CHAR(1),
        shipdate       DATE,
        commitdate     DATE,
        receiptdate    DATE,
        shipinstruct   CHAR(25),
        shipmode       CHAR(10),
        comment        VARCHAR(44)
    );
CREATE TABLE ORDERS (
        orderkey       INT,
        custkey        INT,
        orderstatus    CHAR(1),
        totalprice     DECIMAL,
        orderdate      DATE,
        orderpriority  CHAR(15),
        clerk          CHAR(15),
        shippriority   INT,
        comment        VARCHAR(79)
    );
    
SELECT 
    lineitem.shipmode, 
    sum(CASE WHEN orders.orderpriority = '1-URGENT' OR orders.orderpriority = '2-HIGH' THEN 1 ELSE 0 END) AS high_line_count, 
    sum(CASE WHEN orders.orderpriority <> '1-URGENT' AND orders.orderpriority <> '2-HIGH' THEN 1ELSE 0 END) AS low_line_count 

FROM lineitem, orders 

WHERE 
    orders.orderkey = lineitem.orderkey 
    AND (lineitem.shipmode = 'AIR' OR lineitem.shipmode = 'REG AIR') 
    AND lineitem.commitdate < lineitem.receiptdate 
    AND lineitem.shipdate < lineitem.commitdate 
    AND lineitem.receiptdate >= date('1997-01-01') 
    AND lineitem.receiptdate < date('1998-01-01') 

GROUP BY lineitem.shipmode 

ORDER BY shipmode;