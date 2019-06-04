package spring_security;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserDetailMapper {
    @Select("SELECT * from User_Detail_Info where userName=#{userName}")
    public UserDetail getUserDetailByName(@Param("userName") String userName);
}
