package orgellashop.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import java.util.ArrayList;
import java.util.List;

/*
 * For error handling done right see:
 * https://www.datastax.com/dev/blog/cassandra-error-handling-done-right
 *
 * Performing stress tests often results in numerous WriteTimeoutExceptions,
 * ReadTimeoutExceptions (thrown by Cassandra replicas) and
 * OpetationTimedOutExceptions (thrown by the client). Remember to retry
 * failed operations until success (it can be done through the RetryPolicy mechanism:
 * https://stackoverflow.com/questions/30329956/cassandra-datastax-driver-retry-policy )
 */

public class BackendSession {

    private static final Logger logger = LoggerFactory.getLogger(BackendSession.class);

    public static BackendSession instance = null;

    private Session session;

    public BackendSession(String contactPoint, String keyspace) throws BackendException {

        Cluster cluster = Cluster.builder().addContactPoint(contactPoint).build();
        try {
            session = cluster.connect(keyspace);
        } catch (Exception e) {
            throw new BackendException("Could not connect to the cluster. " + e.getMessage() + ".", e);
        }
        prepareStatements();
    }

    private static PreparedStatement SELECT_ALL_FROM_PRODUCTS;
    private static PreparedStatement DELETE_ALL_FROM_PRODUCTS;
    private static PreparedStatement GET_PRODUCT;
    private static PreparedStatement DECREASE_QUANTITY;
    private static PreparedStatement DECREASE_STORAGE_QUANTITY;
    private static PreparedStatement INSERT_INTO_PENDING;
    private static PreparedStatement GET_PENDINGS;
    private static PreparedStatement DELETE_PENDINGS;
    private static PreparedStatement GET_STORAGE_QUANTITY;
    private static PreparedStatement INSERT_INTO_WAITING;
    private static PreparedStatement GET_WAITINGS;
    private static PreparedStatement DELETE_ALL_WAITINGS;
    private static PreparedStatement GET_NEGATIVE_COUNTERS;
    private static PreparedStatement INCREASE_QUANTITY;
    private static PreparedStatement INSERT_INTO_HISTORY;

    private static final String PRODUCT_FORMAT = "%d %d\n";
    private static final String ORDERS_FORMAT = "%d %d\n";
    private static final String QUANTITY_FORMAT = "%d";

    private void prepareStatements() throws BackendException {
        try {
            SELECT_ALL_FROM_PRODUCTS = session.prepare("SELECT * FROM products;");
            DELETE_ALL_FROM_PRODUCTS = session.prepare("TRUNCATE products;");
            GET_PRODUCT = session.prepare("SELECT id, quantity FROM products WHERE id = ?");
            DECREASE_QUANTITY = session.prepare("UPDATE products SET quantity = quantity - 1 WHERE id = ?;");
            DECREASE_STORAGE_QUANTITY = session.prepare("UPDATE products SET storage_quantity = storage_quantity - 1 WHERE id = ?");
            GET_STORAGE_QUANTITY = session.prepare("SELECT storage_quantity FROM products WHERE id = ?;");
            INSERT_INTO_PENDING = session.prepare("INSERT INTO PendingOrders (pk, product_id, client_id, delivery_window) " +
                    "values (uuid(), ?, ?, ?);");
            GET_PENDINGS = session.prepare("SELECT product_id, client_id FROM PendingOrders WHERE delivery_window = ?;");
            DELETE_PENDINGS = session.prepare("DELETE FROM PendingOrders WHERE delivery_window = ?;");
            INSERT_INTO_WAITING = session.prepare("INSERT INTO WaitingOrders (pk, product_id, client_id) " +
                    "values (uuid(), ?, ?);");
            GET_WAITINGS = session.prepare("SELECT * FROM WaitingOrders;");
            DELETE_ALL_WAITINGS = session.prepare("TRUNCATE WaitingOrders;");
            GET_NEGATIVE_COUNTERS = session.prepare("SELECT id, quantity FROM products WHERE quantity < 0 ALLOW FILTERING");
            INCREASE_QUANTITY = session.prepare("UPDATE products SET quantity = quantity + ?, storage_quantity = storage_quantity + ? WHERE id = ?;");
            INSERT_INTO_HISTORY = session.prepare("INSERT INTO OrdersHistory (product_id, client_id, sent_at) " +
                    "values(?, ?, toTimeStamp(now()))");

        } catch (Exception e) {
            throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
        }

        logger.info("Statements prepared");
    }
    //HISTORY
    public void insertIntoHistory(int product_id, int client_id) throws BackendException {
        BoundStatement bs = new BoundStatement(INSERT_INTO_HISTORY);
        bs.bind(product_id, client_id);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("History Order inserted");
    }

    //QUANTITIES
    public void decreaseStorage(int id) throws BackendException {
        BoundStatement bs = new BoundStatement(DECREASE_STORAGE_QUANTITY);
        bs.bind(id);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Product " + id + " storage quantity decreased");
    }

    public String getProductQuantity(int id) throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(GET_PRODUCT);
        bs.bind(id);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            int rId = row.getInt("id");
            long rQuantity = row.getLong("quantity");
            builder.append(String.format(PRODUCT_FORMAT, rId, rQuantity));
        }

        return builder.toString();
    }

    public String getNegativeQuantities() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(GET_NEGATIVE_COUNTERS);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            int rId = row.getInt("id");
            long rQuantity = row.getLong("quantity");
            builder.append(String.format(PRODUCT_FORMAT, rId, rQuantity));
        }

        return builder.toString();
    }

    public String getProductStorageQuantity(int id) throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(GET_STORAGE_QUANTITY);
        bs.bind(id);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            long rQuantity = row.getLong("storage_quantity");
            builder.append(String.format(QUANTITY_FORMAT, rQuantity));
        }

        return builder.toString();
    }

    //PENDING ORDERS
    public void addPendingOrder(int product_id, int client_id, int delivery_window) throws BackendException {
        BoundStatement bs = new BoundStatement(INSERT_INTO_PENDING);
        bs.bind(product_id, client_id, delivery_window);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Pending Order inserted");
    }

    public String selectPendingOrders(int delivery_window) throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(GET_PENDINGS);
        bs.bind(delivery_window);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            int rProductId = row.getInt("product_id");
            int rClientId = row.getInt("client_id");

            builder.append(String.format(ORDERS_FORMAT, rProductId, rClientId));
        }

        return builder.toString();
    }

    public void deletePendingOrders(int delivery_window) throws BackendException {
        BoundStatement bs = new BoundStatement(DELETE_PENDINGS);
        bs.bind(delivery_window);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Pending orders with delivery window " + delivery_window + " deleted");
    }

    //WAITING
    public void addWaitingOrder(int product_id, int client_id) throws BackendException {
        BoundStatement bs = new BoundStatement(INSERT_INTO_WAITING);
        bs.bind(product_id, client_id);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Waiting Order inserted");
    }

    public String selectWaitingOrders() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(GET_WAITINGS);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            int rProductId = row.getInt("product_id");
            int rClientId = row.getInt("client_id");

            builder.append(String.format(ORDERS_FORMAT, rProductId, rClientId));
        }

        return builder.toString();
    }

    public void deleteAllWaitings() throws BackendException {
        BoundStatement bs = new BoundStatement(DELETE_ALL_WAITINGS);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a delete operation. " + e.getMessage() + ".", e);
        }

        logger.info("All waiting orders deleted");
    }

    //PRODUCTS
    public void updateProduct(int id) throws BackendException {
        BoundStatement bs = new BoundStatement(DECREASE_QUANTITY);
        bs.bind(id);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Product " + id + " updated");
    }

    public void increaseQuantities(int id, long quantity, long storage_quantity) throws BackendException {
        BoundStatement bs = new BoundStatement(INCREASE_QUANTITY);
        bs.bind(quantity, storage_quantity, id);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Products updated");
    }

    protected void finalize() {
        try {
            if (session != null) {
                session.getCluster().close();
            }
        } catch (Exception e) {
            logger.error("Could not close existing cluster", e);
        }
    }

}
