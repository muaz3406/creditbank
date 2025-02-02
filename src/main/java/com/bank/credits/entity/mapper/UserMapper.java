package com.bank.credits.entity.mapper;

import com.bank.credits.dto.model.UserDTO;
import com.bank.credits.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<UserEntity, UserDTO> {
}
