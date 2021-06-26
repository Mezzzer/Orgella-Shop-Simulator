CREATE KEYSPACE IF NOT EXISTS Test
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 };

USE Test;

CREATE TABLE Products (
  id int PRIMARY KEY,
  quantity counter,
  storage_quantity counter
);

CREATE TABLE PendingOrders(
  pk uuid,
  product_id int,
  client_id int,
  delivery_window int,
  PRIMARY KEY(delivery_window, pk)
);

CREATE TABLE OrdersHistory (
    product_id int,
    client_id int,
    sent_at timestamp PRIMARY KEY
);

CREATE TABLE WaitingOrders (
    pk uuid PRIMARY KEY,
    product_id int,
    client_id int
);