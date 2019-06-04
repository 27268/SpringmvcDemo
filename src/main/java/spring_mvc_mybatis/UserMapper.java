package spring_mvc_mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("select * from USER_info where id = #{id}")
    public User getUserByID(@Param("id") String id);
}
