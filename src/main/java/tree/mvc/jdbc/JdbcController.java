package tree.mvc.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tree.mvc.jdbc.Jdbc;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class JdbcController {


    @Autowired
    private ApplicationContext appContext;

    Jdbc jdbc;

    private void createJdbcNode(){
        jdbc=(Jdbc)appContext.getBean("studentJDBCTemplate");
    }

    @RequestMapping(value = "/jdbc/getRoot", method = RequestMethod.GET)
    public String getRootsJSON()
    {
        createJdbcNode();
        return jdbc.getRootNodes();
    }

    @RequestMapping(value = "/jdbc/getChildrenJson/", method = RequestMethod.GET)
    public String getChildrenJSON(HttpServletRequest request)
    {
        return jdbc.getChildrenNodes(request.getParameter("id"));
    }

    @RequestMapping(value = "/jdbc/getChildrenJson/{nodeId:.+}", method = RequestMethod.GET)
    public String getChildrenJSON(@PathVariable(value="nodeId")String nodeId)
    {
        return jdbc.getChildrenNodes(nodeId);
    }

    @RequestMapping(value = "/jdbc/actionItem/", method = RequestMethod.GET)
    public String actionItemJSON(HttpServletRequest request)
    {
        System.out.println(request.getParameter("action"));
        switch (request.getParameter("action")) {
            case "Create":
                return jdbc.createNode(request.getParameter("id"),request.getParameter("name"));
            case "Edit":
                if(request.getParameter("id").equals("0")){
                    return "false";
                }
                return jdbc.editNode(request.getParameter("id"),request.getParameter("name"));
            case "Delete":
                if(request.getParameter("id").equals("0")){
                    return "false";
                }
                return jdbc.deleteNode(request.getParameter("id"));
            case "Dnd":
                if(request.getParameter("newParent").equals("#")){
                    return "false";
                }
                return jdbc.moveNode(request.getParameter("id"),request.getParameter("newParent"),request.getParameter("oldParent"));
            default:
                System.out.println("shock");
                break;
        }
        return "false";
    }

}