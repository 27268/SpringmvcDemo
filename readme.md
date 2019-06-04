# Spring security

执行原理分析：核心是springSecurityFilterChain

用户 <-请求/响应-> 过滤器链 <-请求/响应-> 目标资源

过滤器链若干个过滤器，常见过滤器包括：

- UsernamePasswordAuthenticationFilter: 基于表单的用户校验（用户名、密码、用户状态）,用户请求中带了用户名和密码才会过这个过滤器
- BasicAuthenticationFilter: 基于httpbasic方式校验
- 。。。
- ExceptionTransactionFilter: 异常处理器，负责处理异常信息，定向到异常页面、权限不足页面等
- FilterSecurityInterceptor: 总拦截器，过滤器链中的最后一个，处理之前过滤器的结果
- 自定义Filter

执行流程：以UsernamePasswordAuthenticationFilter为例，进行debug

关键方法：attemptAuthentication() 从request中获取用户名和密码

```java
public Authentication attemptAuthentication(HttpServletRequest request,
     			HttpServletResponse response) throws AuthenticationException 
```
doFilter()方法：放行动作，定向到资源页面

整体流程：
```
UsernamePasswordAuthenticationFilter#attemptAuthentication(httppRequest, httpResponse){
    username = obtainUsername(httpRequest);
    password = obtainPassword(httpRequest);
    //对用户信息进行封装，交给AuthenticationManager处理
    authRequest = new UsernamePasswordAuthenticationToken(username, password);
    setDetails(request, authRequest)
    return this.getAuthenticationManager().authenticate(authRequest);
}
----> AuthenticationManager#authenticate(authRequest){
        for(AuthenticationProvider provider: getProviders()){
            Authentication result = provider.authenticate(authRequest);
            //若result == null 验证不通过；不为空，验证通过
            if(result != null){
                eventPublisher.publishAuthenticationSuccess(result); 
                //触发AuthenticationSuccessHandler#onAuthenticationSuccess()
                return result;
            }
            prepareException(exception, authRequest){
                eventPublisher.publishAuthenticationFailure(exception, authRequest);
                //触发AuthenticationFailureHandler#onAuthenticationFailure()
            }
        }
    } 
----> AuthenticationProvider#authenticate(authRequest){
        String username = (authRequest.getPrincipal()==null) ? "NONE_PROVIDED" : authRequest.getName();
        user = this.userCache.getUserFromCache(username); //系统从缓存或者数据库中获取的用户信息
        if(user==null){
            user = retriveUser(username, authRequest);//从配置文件中获取用户信息
        }
        Assert.notNull(user, "retriveUser returned null");
        preAuthenticationChecks.check(user);
        additionalAuthenticationChecks.check(user, authRequest);
        postAuthenticationChecks.check(user);
        return createSuccessAuthentication(user.getUsername(), authRequest, user);
    }

----> AuthenticationSuccessHandler / AuthenticationFailureHandler
```



定制登录页面：
1. 自定义登录页面
2. 配置controller @RequestMapping("/userLogin")
3. 在Spring Security中配置登录页面 HttpSecurity.formLogin("/userLogin").permitAll()
4. 自定义登录请求地址(默认login) ？？？？？一般不要改，CSRF攻击

登录请求地址：login-processing-url="/login" 与 action="@{/login}" 是对应的；

@RequestMapping的url与用户输入的url是对应的

```html
<form th:action="@{/login}" method="post">
    <div><label>User Name: <input type="text" name="username"> </label></div>
    <div><label>Password: <input type="password" name="password"> </label></div>
    <div> <input type="submit" name="Sign In"> </div>
</form>
```
```xml
<security:http>
    <security:form-login login-page="/userLogin" login-procession-url="securityLogin"/>
</security:http>
```

登录请求地址(th:action="@{/login}")、请求方式(post)、变量名(username, password)建议与Spring Security默认提供的页面一致；

登录页面单独配置权限: permitAll() 


自定义：
- 用户信息硬编码
- 自定义用户权限配置服务

    - 哪些角色可以访问哪些页面，
    - 自定义权限不足处理页面
- 自定义认证信息提供方式：关键是UserDetailService接口
- 自定义登录成功与失败的处理逻辑：默认进行重定向（定向策略？）

    - 登录成功处理AuthenticationSuccessHandler
    - 登录失败处理AuthenticationFailureHandler

自定义用户角色：
```java
UserDetails user  = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
return new InMemoryUserDetailsManager(user);
```
自定义角色访问权限和权限不足处理页面：
```xml
<security:intercept-url pattern="/product/delect" access="hasRole('ROLE_USER')"/>
<!--权限检查失败后，Spring Security重定向到error，由controller处理"/error"请求-->
<security:access-denied-handler errer-page="/error"/>
```

```java
@RequestMapping("/error")
public String error(){ 
    return "error";
}
```

自定义认证信息提供方式：关键点是自定义UserDetailService实现类，
```java
@Bean
public class MyUserDetailsService implements UserDetailsService {	
    UserDetails loadUserByUsername(String username) 
        throws UsernameNotFoundException{
        User user = new User("liuyi", "123456", 
                      AuthrityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
        return user;//user可以从数据库中读取信息
    }
}
public class org.springframework.security.core.userdetails.User 
    implements UserDetails, CredentialsContainer {
    	// ~ Instance fields
    	// ================================================================================================
    	private String password;
    	private final String username;
    	private final Set<GrantedAuthority> authorities;
    	private final boolean accountNonExpired;
    	private final boolean accountNonLocked;
    	private final boolean credentialsNonExpired;
    	private final boolean enabled;
}
```
配置自定义的UserDetailsService
```xml
<!--在Spring配置文件中 或 使用@Bean-->
<Bean id = "myUserDetailsService" class = "MyUserDetailsService"></Bean>

<!--在Spring Security的配置文件中-->
<security:authentication-manager>
    <security:authentication-provider user-service-ref="myUserDetailsService"></security:authentication-provider>
</security:authentication-manager>
```

自定义登录失败处理逻辑：

```java
@Bean
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler{
    void onAuthenticationFailure(HttpServletRequest request,
    		HttpServletResponse response, AuthenticationException exception)
    		throws IOException, ServletException{
        response.getWriter().write("");
        return;
    }
}
```
在Spring Security中引用：
```xml
<security:http>
    <security:form-login login-page="" 
                     login-processing-url="" 
                     default-target-url="" 
                     authentication-success-handler-ref="myAuthenticationSuccessHandler"
                     authentication-failure-handler-ref="myAuthenticationFailureHandler"/>
</security:http>
```
authentication-success-handler-ref中定义的处理方式会覆盖 default-target-url。

#Spring Security source code
```java
public interface SecurityFilterChain {
	boolean matches(HttpServletRequest request);
	List<Filter> getFilters();
}

public final class DefaultSecurityFilterChain implements SecurityFilterChain{}

```

# RBAC模型
role-based access control: 权限 -(n:m)- 角色 -(n:m)- 用户

- 用户表
- 角色表
- 权限表
- 用户角色表
- 角色权限表

# problems

templates/greeting.html文件需要在 templates 目录下，否则找不到文件。应该与thymeleaf有关，待验证。

By default Spring Boot serves static content from resources in the classpath at "/static" (or "/public"). 
The index.html resource is special because it is used as a "welcome page" 
if it exists, which means it will be served up as the root resource, i.e. at http://localhost:8080/ in our example.

# reference

https://docs.spring.io/spring-boot/docs/2.1.5.RELEASE/reference/htmlsingle/

thymeleaf模板：
https://www.thymeleaf.org/doc/tutorials/2.1/thymeleafspring.html

Spring Boot热启动：
https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-hotswapping

Spring Security教程：
http://www.tianshouzhi.com/api/tutorials/spring_security_4/261

http://www.bilibili.com/vedio/av51809660

Spring Security 认证：
https://docs.spring.io/spring-security/site/docs/5.2.0.BUILD-SNAPSHOT/reference/htmlsingle/#jc-authentication

https://www.yiibai.com/spring-security/spring-security-4-hibernate-annotation-example.html



# debug

Springboot启动报错：Reason: Failed to determine a suitable driver class

参考文献：https://www.cnblogs.com/chenpt/p/8777583.html
