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
    private static PreparedStatement INSERT_INTO_PRODUCTS;
    private static PreparedStatement DELETE_ALL_FROM_PRODUCTS;
    private static PreparedStatement GET_PRODUCT;
    private static PreparedStatement UPDATE_PRODUCT;
    private static PreparedStatement DELIVER_PRODUCTS;

    private static final String PRODUCT_FORMAT = "- %03d %03d\n";

    private void prepareStatements() throws BackendException {
        try {
            SELECT_ALL_FROM_PRODUCTS = session.prepare("SELECT * FROM products;");
            INSERT_INTO_PRODUCTS = session
                    .prepare("INSERT INTO products (id, quantity) VALUES (?, ?);");
            DELETE_ALL_FROM_PRODUCTS = session.prepare("TRUNCATE products;");
            GET_PRODUCT = session.prepare("SELECT * FROM products WHERE id = ?");
            UPDATE_PRODUCT = session.prepare("UPDATE products SET quantity = quantity - ? WHERE id = ?");
            DELIVER_PRODUCTS = session.prepare("UPDATE products SET quantity = quantity + ? WHERE quantity=0");
        } catch (Exception e) {
            throw new BackendException("Could not prepare statements. " + e.getMessage() + ".", e);
        }

        logger.info("Statements prepared");
    }


    public String getUser(int id) throws BackendException {
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
            int rQuantity = row.getInt("quantity");

            builder.append(String.format(PRODUCT_FORMAT, rId, rQuantity));
        }

        return builder.toString();
    }

    public String selectAll() throws BackendException {
        StringBuilder builder = new StringBuilder();
        BoundStatement bs = new BoundStatement(SELECT_ALL_FROM_PRODUCTS);

        ResultSet rs = null;

        try {
            rs = session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a query. " + e.getMessage() + ".", e);
        }

        for (Row row : rs) {
            int rId = row.getInt("id");
            int rQuantity = row.getInt("quantity");

            builder.append(String.format(PRODUCT_FORMAT, rId, rQuantity));
        }

        return builder.toString();
    }

    public void upsertProduct(int id, int quantity) throws BackendException {
        BoundStatement bs = new BoundStatement(INSERT_INTO_PRODUCTS);
        bs.bind(id, quantity);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Product " + id + " upserted");
    }

    public void updateProduct(int id, int quantity) throws BackendException {
        BoundStatement bs = new BoundStatement(UPDATE_PRODUCT);
        bs.bind(id, quantity);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Product " + id + " updated");
    }

    public void deliverProducts(int quantity) throws BackendException {
        BoundStatement bs = new BoundStatement(DELIVER_PRODUCTS);
        bs.bind(quantity);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform an upsert. " + e.getMessage() + ".", e);
        }

        logger.info("Products updated");
    }

    public void deleteAll() throws BackendException {
        BoundStatement bs = new BoundStatement(DELETE_ALL_FROM_PRODUCTS);

        try {
            session.execute(bs);
        } catch (Exception e) {
            throw new BackendException("Could not perform a delete operation. " + e.getMessage() + ".", e);
        }

        logger.info("All products deleted");
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
