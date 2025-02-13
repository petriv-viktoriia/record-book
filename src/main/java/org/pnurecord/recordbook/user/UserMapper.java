package org.pnurecord.recordbook.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    User toUser(UserDto userDto);

    List<User> toUserList(List<UserDto> userDtoList);

    List<UserDto> toUserDtoList(List<User> userList);

    UserUpdateDto toUserUpdateDto(User user);

    @Mapping(target = "id", ignore = true)
    User toUserCreateDto(UserCreateDto userCreateDto);
}
