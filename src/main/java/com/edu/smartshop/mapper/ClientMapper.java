package com.edu.smartshop.mapper;

import com.edu.smartshop.dto.request.ClientCreateDTO;
import com.edu.smartshop.dto.response.ClientResponseDTO;
import com.edu.smartshop.entity.Client;
import com.edu.smartshop.entity.User;
import com.edu.smartshop.enums.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(createUser(dto))")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(target = "tier", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    @Mapping(target = "totalOrders", ignore = true)
    @Mapping(target = "firstOrderDate", ignore = true)
    @Mapping(target = "lastOrderDate", ignore = true)
    Client toEntity(ClientCreateDTO dto);

    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "tier", target = "tier")
    @Mapping(source = "totalSpent", target = "totalSpent")
    @Mapping(source = "totalOrders", target = "totalOrders")
    @Mapping(source = "firstOrderDate", target = "firstOrderDate")
    @Mapping(source = "lastOrderDate", target = "lastOrderDate")
    ClientResponseDTO toDto(Client client);

    default User createUser(ClientCreateDTO dto) {
        return User.builder()
                .username(dto.getEmail())
                .password(dto.getPassword())
                .role(UserRole.CLIENT)
                .build();
    }
}

