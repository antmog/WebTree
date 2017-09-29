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

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public String queryAllNodes() {
        final String QUERY_SQL = "SELECT * FROM treenodes ORDER BY id";
        return Selector(QUERY_SQL, jdbcTemplate).toString();
    }

    public String getRootNodes() {
        String result;
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            final String QUERY_SQL = "SELECT * FROM treenodes WHERE parent ='#' ORDER BY id;";
            result = Selector(QUERY_SQL, jdbcTemplate).toString();
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            System.out.println("Error in creating record, rolling back");
            transactionManager.rollback(status);
            throw e;
        }
        return result;
    }

    public String getChildrenNodes(String nodeId) {
        final String QUERY_SQL = "SELECT * FROM treenodes WHERE parent ='" + nodeId + "' ORDER BY id;";
        ExecutorService exec = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(1);
        exec.execute(() -> {
            try {
                Thread.sleep(2);
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        String result = Selector(QUERY_SQL, jdbcTemplate).toString();
        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        exec.shutdown();
        return result;
    }



    public String createNode(String parentId, String nodeName) {
        String nodeId = null;
        int result = 0;
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            final String QUERY_PRESQL = "SELECT * FROM treenodes WHERE parent ='" + parentId + "' ORDER BY id;";
            List<CustomTreeNode> children = Selector(QUERY_PRESQL, jdbcTemplate);
            if (children.isEmpty()) {
                nodeId = parentId + ".0";
                final String QUERY_SETCHILDREN = "UPDATE treenodes SET children = 'TRUE' WHERE id = '" + parentId + "';";
                if (jdbcTemplate.update(QUERY_SETCHILDREN) <= 0) {
                    return "false";
                }
            } else {
                String postfix = children.get(children.size() - 1).getId().substring(parentId.length() + 1);
                System.out.println("pfix: " + postfix);
                System.out.println(children);
                nodeId = parentId + "." + (Integer.parseInt(postfix) + 1);
            }
            System.out.println(nodeId);
            final String QUERY_SQL =
                    "INSERT INTO treenodes (id , parent , text , children)\n" +
                            "VALUES ('" + nodeId + "' , '" + parentId + "' , '" + nodeName + "' , 'FALSE');";
            jdbcTemplate.update(QUERY_SQL);
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            System.out.println("Error in creating record, rolling back");
            transactionManager.rollback(status);
            return "false";
        } catch (Exception e) {
            throw e;
        }
        return "true";
    }

    public String editNode(String nodeId, String nodeName) {
        final String QUERY_SQL = "UPDATE treenodes SET text = '"+nodeName+"' WHERE id = '" + nodeId + "';";
        if(jdbcTemplate.update(QUERY_SQL)>0){
            return "true";
        }
        return "false";
    }
    public String moveNode(String nodeId, String newParent, String oldParent){
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            final String QUERY_CHECKOLDPARENT = "SELECT * FROM treenodes WHERE parent = '" + oldParent + "';";
            List<CustomTreeNode> nodes = Selector(QUERY_CHECKOLDPARENT, jdbcTemplate);
            System.out.println(QUERY_CHECKOLDPARENT);
            if (nodes.size() < 1) {
                final String QUERY_OLDPARENT = "UPDATE treenodes SET children = 'FALSE' WHERE id = '" + oldParent + "';";
                jdbcTemplate.update(QUERY_OLDPARENT);
                System.out.println(QUERY_OLDPARENT);
            }
            final String QUERY_NEWPARENT = "UPDATE treenodes SET children = 'TRUE' WHERE id = '" + newParent + "';";
            jdbcTemplate.update(QUERY_NEWPARENT);
            System.out.println(QUERY_NEWPARENT);
            final String QUERY_SQL = "UPDATE treenodes SET parent ='" + newParent + "' WHERE id='"+nodeId+"';";
            jdbcTemplate.update(QUERY_SQL);
            System.out.println(QUERY_SQL);
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            System.out.println("Error in creating record, rolling back");
            transactionManager.rollback(status);
            return "false";
        } catch (Exception e) {
            throw e;
        }
        return "true";
    }

    public String deleteNode(String nodeId) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            final String QUERY_GETPARENT = "SELECT parent FROM treenodes WHERE id = ?";
            String parentId = jdbcTemplate.queryForObject(QUERY_GETPARENT, new Object[]{nodeId}, String.class);
            System.out.println("parent ID: " + parentId);
            final String QUERY_PRESQL = "SELECT * FROM treenodes WHERE parent = '" + parentId + "';";
            List<CustomTreeNode> nodes = Selector(QUERY_PRESQL, jdbcTemplate);
            if (nodes.size() < 2) {
                final String QUERY_SETCHILDREN = "UPDATE treenodes SET children = 'FALSE' WHERE id = '" + parentId + "';";
                jdbcTemplate.update(QUERY_SETCHILDREN);
            }
            final String QUERY_SQL = "DELETE FROM treenodes WHERE id ='" + nodeId + "';";
            jdbcTemplate.update(QUERY_SQL);
            transactionManager.commit(status);
        } catch (DataAccessException e) {
            System.out.println("Error in creating record, rolling back");
            transactionManager.rollback(status);
            return "false";
        } catch (Exception e) {
            throw e;
        }
        return "true";
    }


    private List<CustomTreeNode> Selector(String query, JdbcTemplate jdbc) {
        List<CustomTreeNode> treeNodes = jdbcTemplate.query(query, (resultSet, i) -> {
            CustomTreeNode treeNode = new CustomTreeNode();
            treeNode.setId(resultSet.getString("id"));
            treeNode.setText(resultSet.getString("text"));
            treeNode.setParent(resultSet.getString("parent"));
            treeNode.setChildren(resultSet.getBoolean("children"));
            return treeNode;
        });
        return treeNodes;
    }

}