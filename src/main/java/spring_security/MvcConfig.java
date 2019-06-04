package spring_security;

import com.sun.deploy.net.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import spring_mvc_mybatis.UserMapper;

@Configuration
@Controller
public class MvcConfig implements WebMvcConfigurer{

//    @Override
//    public void addViewControllers(ViewControllerRegistry viewControllerRegistry){
//        viewControllerRegistry.addViewController("/home").setViewName("home");
//        viewControllerRegistry.addViewController("/").setViewName("home");
//        viewControllerRegistry.addViewController("/hello").setViewName("hello");
//        viewControllerRegistry.addViewController("/login").setViewName("login");
//
//    }

    @Qualifier
    private UserDetailMapper userDetailMapper;

    @RequestMapping(value = "/home")
    public String home(){
        return "/home";
    }
    @GetMapping(value = "/login")
    public String login(){
        return "/login";
    }
    @RequestMapping(value = "/hello")
    public String hello(HttpRequest request){
        return "/hello";
    }

    @RequestMapping(value = "/registry",params = "/register.json")
    public String registry(){
        return "/registry";
    }

}
