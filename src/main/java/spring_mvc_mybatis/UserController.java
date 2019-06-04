package spring_mvc_mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/selectUser") // http://localhost:8080/selectUser?id=0001
    public String selectUser(@RequestParam(name = "id") String id, Model model){
        User user = userMapper.getUserByID(id);
        model.addAttribute("user", user);
        return "selectUser";
    }
}
