package com.edu.smartshop.service.impl;

import com.edu.smartshop.dto.request.OrderItemDTO;
import com.edu.smartshop.dto.request.ProductCreateDTO;
import com.edu.smartshop.dto.response.ProductDTO;
import com.edu.smartshop.entity.Product;
import com.edu.smartshop.exception.BusinessRuleException;
import com.edu.smartshop.exception.ResourceNotFoundException;
import com.edu.smartshop.mapper.ProductMapper;
import com.edu.smartshop.repository.ProductRepository;
import com.edu.smartshop.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDTO createProduct(ProductCreateDTO createDTO) {
        if (productRepository.existsByName(createDTO.getName())) {
            throw new BusinessRuleException("Product with name '" + createDTO.getName() + "' already exists");
        }

        Product product = productMapper.toEntity(createDTO);
        product.setDeleted(false);
        
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllActiveProducts() {
        return productRepository.findAll().stream()
                .filter(product -> !product.isDeleted())
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProductsPaginated(Pageable pageable, boolean includeDeleted) {
        Page<Product> productPage;
        
        if (includeDeleted) {
            productPage = productRepository.findAll(pageable);
        } else {
            productPage = productRepository.findByDeletedFalse(pageable);
        }
        
        return productPage.map(productMapper::toDto);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductCreateDTO updateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.findByName(updateDTO.getName())
                .ifPresent(existingProduct -> {
                    if (!existingProduct.getId().equals(id)) {
                        throw new BusinessRuleException("Product with name '" + updateDTO.getName() + "' already exists");
                    }
                });

        product.setName(updateDTO.getName());
        product.setPrice(updateDTO.getPrice());
        product.setStock(updateDTO.getStock());
        
        Product updatedProduct = productRepository.save(product);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        if (product.isDeleted()) {
            throw new BusinessRuleException("Product is already deleted");
        }
        
        product.setDeleted(true);
        productRepository.save(product);
    }

    @Override
    public void restoreProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        if (!product.isDeleted()) {
            throw new BusinessRuleException("Product is not deleted");
        }
        
        product.setDeleted(false);
        productRepository.save(product);
    }


}
