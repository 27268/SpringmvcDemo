package spring_security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserService implements UserDetailsService {

    @Autowired
    private UserDetailMapper userDetailMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetail userDetail = userDetailMapper.getUserDetailByName(username);
        if(userDetail==null){
            System.out.println("User not found");
            throw new UsernameNotFoundException("Username not found");
        }
        //User user = new User(userDetail.getUserName(), userDetail.getPassword(), AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder().username(userDetail.getUserName()).password(userDetail.getPassword()).roles("USER");
        return userBuilder.build();
    }
}
