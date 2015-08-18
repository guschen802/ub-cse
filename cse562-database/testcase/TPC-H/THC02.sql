CREATE TABLE LINEITEM (
    orderkey INT , 
    partkey INT , 
    suppkey INT , 
    linenumber INT, 
    quantity DECIMAL , 
    extendedprice DECIMAL , 
    discount DECIMAL , 
    tax DECIMAL , 
    returnflag CHAR (1) , 
    linestatus CHAR (1) , 
    shipdate DATE , 
    commitdate DATE , 
    receiptdate DATE , 
    shipinstruct CHAR (25) , 
    shipmode CHAR (10) , 
    comment VARCHAR (44) 
    );
    SELECT *
    FROM lineitem;
    
    
    SELECT sum(extendedprice * discount) AS revenue 
    FROM lineitem 
    WHERE shipdate >= DATE('1994-01-01') AND shipdate < date('1995-01-01') AND discount > 0.05 AND discount < 0.07 AND quantity < 24;
  