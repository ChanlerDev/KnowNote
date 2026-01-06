package dev.chanler.knownote.user.domain.mapper;

import dev.chanler.knownote.user.domain.entity.UserDO;
import org.apache.ibatis.annotations.*;

import java.util.Optional;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = #{id}")
    Optional<UserDO> findById(@Param("id") Long id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    Optional<UserDO> findByUsername(@Param("username") String username);

    @Select("SELECT * FROM user WHERE email = #{email}")
    Optional<UserDO> findByEmail(@Param("email") String email);

    @Select("SELECT * FROM user WHERE google_id = #{googleId}")
    Optional<UserDO> findByGoogleId(@Param("googleId") String googleId);

    @Insert("""
            INSERT INTO user (email, username, nickname, password_hash, google_id, avatar, bio, created_at, updated_at)
            VALUES (#{email}, #{username}, #{nickname}, #{passwordHash}, #{googleId}, #{avatar}, #{bio}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserDO user);

    @Update("""
            <script>
            UPDATE user SET updated_at = #{updatedAt}
            <if test="username != null">, username = #{username}</if>
            <if test="nickname != null">, nickname = #{nickname}</if>
            <if test="passwordHash != null">, password_hash = #{passwordHash}</if>
            <if test="googleId != null">, google_id = #{googleId}</if>
            <if test="avatar != null">, avatar = #{avatar}</if>
            <if test="bio != null">, bio = #{bio}</if>
            WHERE id = #{id}
            </script>
            """)
    int update(UserDO user);
}
