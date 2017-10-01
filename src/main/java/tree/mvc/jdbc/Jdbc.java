package tree.mvc.jdbc;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import tree.mvc.model.CustomTreeNode;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Repository
public class Jdbc {
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private PlatformTransactionManager transactionManager;

    private int delay = 2000; // on load of node

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
    /**
     * Select all nodes. (unused)
     * @return
     */
    public String queryAllNodes() {
        final String QUERY_SQL = "SELECT * FROM treenodes ORDER BY id";
        return Selector(QUERY_SQL).toString();
    }

    /**
     * getRootNodes
     * @return String (json watch CustomTreeNode.toString())
     */
    public String getRootNodes() {
        String result;
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            final String QUERY_SQL = "SELECT * FROM treenodes WHERE parent ='#' ORDER BY id;";
            // CustomTreeNode.toString() - string looks like json
            result = Selector(QUERY_SQL).toString();
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            //System.out.println("Error in creating record, rolling back");
            transactionManager.rollback(status);
            throw e;
        }
        return result;
    }

    /**
     * getChildrenNodes
     * @param nodeId
     * @return String (json watch CustomTreeNode.toString())
     */
    public String getChildrenNodes(String nodeId) {
        final String QUERY_SQL = "SELECT * FROM treenodes WHERE parent ='" + nodeId + "' ORDER BY id;";
        // Hardcoded 2 sec delay for loading child nodes.
        ExecutorService exec = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(1);
        // Start timer
        if(nodeId.equals("0")){
            latch.countDown();
        }else {
            exec.execute(() -> {
                try {
                    Thread.sleep(delay);
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        // Getting data meanwhile.
        // CustomTreeNode.toString() - string looks like json
        String result = Selector(QUERY_SQL).toString();
        // Wait when timer ends.
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Continue (shutdown exec).
        exec.shutdown();
        return result;
    }

    /**
     * createNode
     * @param parentId
     * @param nodeName
     * @return
     */
    public synchronized String createNode(String parentId, String nodeName) {
        String nodeId = null;
        int result = 0;
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            // Generate ID for new node.
            final String QUERY_PRESQL = "SELECT * FROM treenodes WHERE parent ='" + parentId + "' ORDER BY id;";
            List<CustomTreeNode> children = Selector(QUERY_PRESQL);
            if (children.isEmpty()) {
                nodeId = parentId + ".0";
                final String QUERY_SETCHILDREN = "UPDATE treenodes SET children = 'TRUE' WHERE id = '" + parentId + "';";
                if (jdbcTemplate.update(QUERY_SETCHILDREN) <= 0) {
                    return "false";
                }
            } else {
                String postfix = children.get(children.size() - 1).getId().substring(parentId.length() + 1);
                nodeId = parentId + "." + (Integer.parseInt(postfix) + 1);
            }
            // Add new node with new id.
            final String QUERY_SQL =
                    "INSERT INTO treenodes (id , parent , text , children)\n" +
                            "VALUES ('" + nodeId + "' , '" + parentId + "' , '" + nodeName + "' , 'FALSE');";
            jdbcTemplate.update(QUERY_SQL);
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            //System.out.println("Error in creating record, rolling back");
            transactionManager.rollback(status);
            return "false";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "true";
    }

    /**
     * editNode
     * @param nodeId
     * @param nodeName
     * @return
     */
    public String editNode(String nodeId, String nodeName) {
        final String QUERY_SQL = "UPDATE treenodes SET text = '"+nodeName+"' WHERE id = '" + nodeId + "';";
        if(jdbcTemplate.update(QUERY_SQL)>0){
            return "true";
        }
        return "false";
    }

    /**
     * moveNode (drag&drop)
     * @param nodeId
     * @param newParent
     * @param oldParent
     * @return
     */
    public String moveNode(String nodeId, String newParent, String oldParent){
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            // Set children (haschildren) field TRUE for new parent.
            final String QUERY_NEWPARENT = "UPDATE treenodes SET children = 'TRUE' WHERE id = '" + newParent + "';";
            jdbcTemplate.update(QUERY_NEWPARENT);
            // Set new parent for the node.
            final String QUERY_SQL = "UPDATE treenodes SET parent ='" + newParent + "' WHERE id='"+nodeId+"';";
            jdbcTemplate.update(QUERY_SQL);
            // Check old node if it has no other children (except moved one).
            final String QUERY_CHECKOLDPARENT = "SELECT * FROM treenodes WHERE parent = '" + oldParent + "';";
            List<CustomTreeNode> nodes = Selector(QUERY_CHECKOLDPARENT);
            if (nodes.size() < 1) {
                // Set children(haschildren) = false if that was last child :(
                final String QUERY_OLDPARENT = "UPDATE treenodes SET children = 'FALSE' WHERE id = '" + oldParent + "';";
                jdbcTemplate.update(QUERY_OLDPARENT);
            }

            transactionManager.commit(status);
        } catch (DataAccessException e) {
            //System.out.println("Error in creating record, rolling back");
            transactionManager.rollback(status);
            return "false";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "true";
    }

    /**
     * deleteNode
     * @param nodeId
     * @return
     */
    public String deleteNode(String nodeId) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            // Getting parent ID of target node.
            final String QUERY_GETPARENT = "SELECT parent FROM treenodes WHERE id = ?";
            String parentId = jdbcTemplate.queryForObject(QUERY_GETPARENT, new Object[]{nodeId}, String.class);
            // Check if parent of target node has any children except target node.
            final String QUERY_PRESQL = "SELECT * FROM treenodes WHERE parent = '" + parentId + "';";
            List<CustomTreeNode> nodes = Selector(QUERY_PRESQL);
            if (nodes.size() < 2) {
                // Set children(haschildren) FALSE if it dont.
                final String QUERY_SETCHILDREN = "UPDATE treenodes SET children = 'FALSE' WHERE id = '" + parentId + "';";
                jdbcTemplate.update(QUERY_SETCHILDREN);
            }
            // Deleting target node.
            final String QUERY_SQL = "DELETE FROM treenodes WHERE id ='" + nodeId + "';";
            jdbcTemplate.update(QUERY_SQL);
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            //System.out.println("Error in creating record, rolling back");
            transactionManager.rollback(status);
            return "false";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "true";
    }

    /**
     * Selector. Makes treenode list from query result.
     * Carefull, CustomTreeNode.toString() returns String in json format.
     * @return treeNode as list
     */
    private List<CustomTreeNode> Selector(String query) {
        return jdbcTemplate.query(query, (resultSet, i) -> {
            CustomTreeNode treeNode = new CustomTreeNode();
            treeNode.setId(resultSet.getString("id"));
            treeNode.setText(resultSet.getString("text"));
            treeNode.setParent(resultSet.getString("parent"));
            treeNode.setChildren(resultSet.getBoolean("children"));
            return treeNode;
        });
    }
}