package com.edu.smartshop.mapper;

import com.edu.smartshop.dto.request.ClientCreateDTO;
import com.edu.smartshop.dto.response.ClientResponseDTO;
import com.edu.smartshop.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(source = "username", target = "user.username")
    @Mapping(source = "password", target = "user.password")
    Client toEntity(ClientCreateDTO dto);

    @Mapping(source = "user.username", target = "username")
    ClientResponseDTO toDto(Client entity);
}
