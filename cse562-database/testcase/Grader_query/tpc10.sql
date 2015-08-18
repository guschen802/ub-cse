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
CREATE TABLE CUSTOMER (
        custkey      INT,
        name         VARCHAR(25),
        address      VARCHAR(40),
        nationkey    INT,
        phone        CHAR(15),
        acctbal      DECIMAL,
        mktsegment   CHAR(10),
        comment      VARCHAR(117)
    );
CREATE TABLE NATION (
        nationkey    INT,
        name         CHAR(25),
        regionkey    INT,
        comment      VARCHAR(152)
    );
SELECT 
    customer.custkey, 
    sum(lineitem.extendedprice * (1 - lineitem.discount)) AS revenue, 
    customer.acctbal, 
    nation.name, 
    customer.address, 
    customer.phone, 
    customer.comment 

FROM customer, orders, lineitem, nation 

WHERE 
    customer.custkey = orders.custkey 
    AND lineitem.orderkey = orders.orderkey 
    AND orders.orderdate >= date('1993-01-01') 
    AND orders.orderdate < date('1993-04-01') 
    AND lineitem.returnflag = 'R' 
    AND customer.nationkey = nation.nationkey 

GROUP BY customer.custkey, customer.acctbal, customer.phone, nation.name, customer.address, customer.comment 

ORDER BY revenue 

LIMIT 20;