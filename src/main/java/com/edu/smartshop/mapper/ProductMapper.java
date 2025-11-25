package com.edu.smartshop.mapper;

import com.edu.smartshop.dto.request.ProductCreateDTO;
import com.edu.smartshop.dto.response.ProductDTO;
import com.edu.smartshop.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductCreateDTO dto);

    ProductDTO toDto(Product entity);
}
