package hello;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class GreetingController {

    /**
     * The @GetMapping annotation ensures that HTTP GET requests to /greeting are mapped to the greeting() method.
     *
     * @RequestParam binds the value of the query String parameter name into the name parameter of the greeting() method.
     *
     * @param name  given in http://localhost:8080/greeting?name=liuyi
     * @param model
     * @return
     */
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World")
                                       String name, Model model){
        /**
         * Java-5-specific interface that defines a holder for model attributes.
         * Primarily designed for adding attributes to the model.
         * Allows for accessing the overall model as a java.util.Map.
         *
         * The value of the name parameter is added to a Model object,
         * ultimately making it accessible to the view template.
         *
         * The implementation of the method body relies Thymeleaf,
         * to perform server-side rendering of the HTML.
         */
        model.addAttribute("name", name);
        return "greeting";
    }

}
