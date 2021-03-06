package tree.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index() {
        return "/index";
    }

    @RequestMapping(value = "tree", method = RequestMethod.GET)
    public ModelAndView tree() {
        ModelAndView mav = new ModelAndView("tree/tree");
        return mav;
    }
}

