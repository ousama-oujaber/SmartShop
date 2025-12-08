package com.edu.smartshop.service;

import com.edu.smartshop.dto.request.ProductCreateDTO;
import com.edu.smartshop.dto.response.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {
    
    ProductDTO createProduct(ProductCreateDTO createDTO);
    
    ProductDTO getProductById(Long id);
    
    List<ProductDTO> getAllProducts();
    
    List<ProductDTO> getAllActiveProducts();
    
    Page<ProductDTO> getAllProductsPaginated(Pageable pageable, boolean includeDeleted);
    
    ProductDTO updateProduct(Long id, ProductCreateDTO updateDTO);
    
    void deleteProduct(Long id);
    
    void restoreProduct(Long id);
}

