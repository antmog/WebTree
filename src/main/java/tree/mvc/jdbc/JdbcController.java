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

import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class JdbcController {
    private final Jdbc jdbc;
    private final ApplicationContext appContext;

    @Autowired
    public JdbcController(ApplicationContext appContext) {
        this.appContext = appContext;
        this.jdbc = (Jdbc)appContext.getBean("jdbcTemplate");
    }


    @RequestMapping(value = "/jdbc/getRoot", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public String getRootsJSON()
    {
        // Creating JDBC node here, when initialising tree.
        //createJdbcNode();
        return jdbc.getRootNodes();
    }

    @RequestMapping(value = "/jdbc/getChildrenJson/", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public String getChildrenJSON(HttpServletRequest request)
    {
        return jdbc.getChildrenNodes(request.getParameter("id"));
    }



    @RequestMapping(value = "/jdbc/actionItem/", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public String actionItemJSON(HttpServletRequest request)
    {
        switch (request.getParameter("action")) {
            case "Create":
                return Json.createObjectBuilder().add("result",jdbc.createNode(request.getParameter("id"),
                        request.getParameter("name"))).build().toString();
            case "Edit":
                if(request.getParameter("id").equals("0")){
                    return Json.createObjectBuilder().add("result","false").build().toString();
                }
                return Json.createObjectBuilder().add("result",jdbc.editNode(request.getParameter("id"),
                        request.getParameter("name"))).build().toString();
            case "Delete":
                if(request.getParameter("id").equals("0")){
                    return Json.createObjectBuilder().add("result","false").build().toString();
                }
                return Json.createObjectBuilder().add("result",
                        jdbc.deleteNode(request.getParameter("id"))).build().toString();
            case "Dnd":
                if(request.getParameter("newParent").equals("#")){
                    return Json.createObjectBuilder().add("result","false").build().toString();
                }
                return Json.createObjectBuilder().add("result",jdbc.moveNode(request.getParameter("id"),
                        request.getParameter("newParent"),request.getParameter("oldParent"))).build().toString();
            case "Delay":
                jdbc.setDelay(Integer.parseInt(request.getParameter("name")));
                if(jdbc.getDelay() == Integer.parseInt(request.getParameter("name")) ){
                    return Json.createObjectBuilder().add("result","delay").build().toString();
                }
                return Json.createObjectBuilder().add("result","false").build().toString();
            default:
                System.out.println("Wrong command.");
                break;
        }
        return Json.createObjectBuilder().add("result","false").build().toString();
    }

    // unused
    @RequestMapping(value = "/jdbc/getChildrenJson/{nodeId:.+}", method = RequestMethod.GET)
    public String getChildrenJSON(@PathVariable(value="nodeId")String nodeId)
    {
        //return jdbc.getChildrenNodes(nodeId);
        System.out.println(nodeId);
        return null;
    }

}