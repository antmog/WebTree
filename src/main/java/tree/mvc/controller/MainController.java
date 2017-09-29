package tree.mvc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import tree.mvc.jdbc.Jdbc;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.json.Json;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

@Controller
public class MainController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        System.out.println("MAMOCHKI BLET");
        return "/index";
    }

    @RequestMapping(value = "tree", method = RequestMethod.GET)
    public ModelAndView tree() {
        //ModelAndView mav = new ModelAndView("tree/tree", "data", jdbc.getRootNodes());
        ModelAndView mav = new ModelAndView("tree/tree");
        return mav;
    }
}

