package com.edu.smartshop.service;

import com.edu.smartshop.dto.request.ProductCreateDTO;
import com.edu.smartshop.dto.response.ProductDTO;

import java.util.List;

public interface IProductService {
    
    ProductDTO createProduct(ProductCreateDTO createDTO);
    
    ProductDTO getProductById(Long id);
    
    List<ProductDTO> getAllProducts();
    
    List<ProductDTO> getAllActiveProducts();
    
    ProductDTO updateProduct(Long id, ProductCreateDTO updateDTO);
    
    void deleteProduct(Long id);
    
    void restoreProduct(Long id);
}
