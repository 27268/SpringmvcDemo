package spring_security;


import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import javax.sql.DataSource;
import java.util.LinkedList;
import java.util.List;

/**
 * Spring Security通过过滤器实现权限验证：授权 & 认证
 *
 * 在java代码中配置Spring Security：
 * 1. 创建过滤器：@EnableWebSecurity注解可以创建一个名为 springSecurityFilterChain 的Filter。
 *
 *    1.1 springSecurityFilterChain在哪里创建？
 *      1.1.1 @EnableWebSecurity注解
 *           @Import({ WebSecurityConfiguration.class, SpringWebMvcImportSelector.class, OAuth2ImportSelector.class })
 *           @EnableGlobalAuthentication
 *           @Configuration
 *           public @interface EnableWebSecurity{ boolean debug() default false; }
 *       1.1.2 WebSecurityConfiguration.class中
 *           @Bean(name = AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME) 即 springSecurityFilterChain
 * 	         public Filter springSecurityFilterChain() throws Exception{
 * 	             //create the FilterChainProxy that performs the web based security for Spring Security
 * 	             return webSecurity.build();
 * 	         }
 * 	     1.1.3 webSecurity.build() -> FilterChainProxy的创建
 * 	          AbstractSecurityBuilder<O> implements SecurityBuilder<O>{
 * 	              public final O build() throws Exception{
 * 	                  this.object = doBuild(); // protected abstract O doBuild() throws Exception; 子类中实现：AbstractConfiguredSecurityBuilder
 * 			          return this.object;
 * 	              }
 * 	          }
 * 	      1.1.4 AbstractConfiguredSecurityBuilder#doBuild()
 *                  O result = performBuild(); // protected abstract O performBuild() throws Exception;
 *                  return result;
 *            WebSecurity # protected Filter performBuild() throws Exception
 *
 *    1.2 springSecurityFilterChain的实现类是什么？
 *        实现类：FilterChainProxy
 *
 *    本例中，在一个添加了@EnableWebSecurity注解的类中，注入AbstractConfiguredSecurityBuilder（HttpSecurity是其子类）
 *
 * 2. 注册过滤器
 *    本例中@Configuration, @SpringBootApplication（其中@EnableAutoConfiguration）
 *
 * 3. 授权配置 Authorize
 *    HttpSecurity的配置：默认所有的url访问都需要进行验证，
 *    要自定义需要权限验证的URL，只需重写 configure(HttpSecurity http)即可
 *
 * 4. 认证配置 Authentication
 *    在Spring Security中，认证过程称之为Authentication(验证)，指的是建立 系统使用者 信息( principal )的过程。
 *    - 基于内存的认证： AuthenticationManagerBuilder#inMemoryAuthentication()
 *    - 基于JDBC的认证： AuthenticationManagerBuilder#jdbcAuthentication()
 *    - 基于LDAP的认证： AuthenticationManagerBuilder#ldapAuthentication()
 *    - 基于UserDetailsService的认证
 *    - AuthenticationProvider？？？
 *
 *    基于内存、JDBC、LDAP的认证，都是通过 AuthenticationManagerBuilder 对象完成的，该对象用于构建 AuthenticationManager。
 *    Builder会创建 AuthenticationManager的子类实例：ProviderManager，用于管理 AuthenticationProvider;
 *        关于Provider，上述三种认证方式，每种都对应了一个provider，ProviderManager对这些provider进行管理
 *         - 内存、JDBC：DaoAuthenticationProvider
 *         - LDAP：LdapAuthenticationProvider
 *
 *
 */

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter(){
        UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
        List<AuthenticationProvider> list = new LinkedList<>();
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService());
        list.add(provider);
        AuthenticationManager manager = new ProviderManager(list);
        filter.setAuthenticationManager(manager);
        return filter;

    }

    @Bean
    public UserService userService(){
        return new UserService();
    }

    /**
     * 以and()方法作为切分，可以划分为3个部分，你可以认为每个部分实际上都是配置了一个过滤器。
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.authorizeRequests()
                .antMatchers("/", "/home").permitAll().anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login").permitAll()
                .and()
                .logout().permitAll();

    }


    @Bean
    DataSource dataSource(){
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource(
                "jdbc:postgresql:blogDB",
                "postgres",
                "123456"
        );
        driverManagerDataSource.setDriverClassName("org.postgresql.Driver");
        return driverManagerDataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception{
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.addMapper(UserDetailMapper.class);
        factoryBean.setConfiguration(configuration);
        return factoryBean.getObject();
    }

    @Bean
    public UserDetailMapper userDetailMapper() throws Exception {
        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory());
        return sqlSessionTemplate.getMapper(UserDetailMapper.class);
    }


//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        // ensure the passwords are encoded properly
//        User.UserBuilder users = User.withDefaultPasswordEncoder();
//        auth
//                .jdbcAuthentication()
//                .dataSource(dataSource())
//                .withDefaultSchema()
//                .withUser(users.username("user").password("password").roles("USER"))
//                .withUser(users.username("admin").password("password").roles("USER","ADMIN"));
//
//    }


//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .jdbcAuthentication()
//                .dataSource(dataSource())
//                .withDefaultSchema()
//                .withUser(User.withDefaultPasswordEncoder().username("user").password("password").roles("USER"));
//    }
//

//    @Bean
//    @Override
//    public UserDetailsService userDetailsService(){
//        UserDetails user  = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//        /**
//         * Non-persistent implementation of {@code UserDetailsManager} which is backed by an
//         * in-memory map.
//         *
//         * Mainly intended for testing and demonstration purposes, where a full blown persistent
//         * system isn't required.
//         */
//        //return new InMemoryUserDetailsManager(user);
//    }
}
