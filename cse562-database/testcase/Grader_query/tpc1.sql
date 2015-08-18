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
SELECT 
    returnflag, 
    linestatus, 
    sum(quantity) AS sum_qty, 
    sum(extendedprice) AS sum_base_price, 
    sum(extendedprice * (1 - discount)) AS sum_disc_price, 
    sum(extendedprice * (1 - discount) * (1 + tax)) AS sum_charge, 
    avg(quantity) AS avg_qty, 
    avg(extendedprice) AS avg_price, 
    avg(discount) AS avg_disc, 
    count(*) AS count_order 

FROM
    lineitem 

WHERE 
    shipdate <= Date('1998-08-09') 

GROUP BY 
    returnflag, linestatus 

ORDER BY 
    returnflag, linestatus;