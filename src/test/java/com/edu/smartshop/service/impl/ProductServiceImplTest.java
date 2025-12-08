package com.edu.smartshop.service.impl;

import com.edu.smartshop.dto.request.ProductCreateDTO;
import com.edu.smartshop.dto.response.ProductDTO;
import com.edu.smartshop.entity.Product;
import com.edu.smartshop.exception.BusinessRuleException;
import com.edu.smartshop.exception.ResourceNotFoundException;
import com.edu.smartshop.mapper.ProductMapper;
import com.edu.smartshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;
    private ProductCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stock(100)
                .deleted(false)
                .build();

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setName("Test Product");
        productDTO.setPrice(new BigDecimal("99.99"));
        productDTO.setStock(100);

        createDTO = new ProductCreateDTO();
        createDTO.setName("Test Product");
        createDTO.setPrice(new BigDecimal("99.99"));
        createDTO.setStock(100);
    }

    // ==================== CREATE PRODUCT TESTS ====================

    @Test
    @DisplayName("createProduct - Should create product successfully")
    void createProduct_ShouldCreateProductSuccessfully() {
        when(productRepository.existsByName(createDTO.getName())).thenReturn(false);
        when(productMapper.toEntity(createDTO)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDTO);

        ProductDTO result = productService.createProduct(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct - Should throw exception when product name already exists")
    void createProduct_ShouldThrowExceptionWhenProductNameExists() {
        when(productRepository.existsByName(createDTO.getName())).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(createDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    @DisplayName("getProductById - Should return product when found")
    void getProductById_ShouldReturnProductWhenFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productDTO);

        ProductDTO result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getProductById - Should throw exception when product not found")
    void getProductById_ShouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    // ==================== GET ALL PRODUCTS TESTS ====================

    @Test
    @DisplayName("getAllProducts - Should return all products")
    void getAllProducts_ShouldReturnAllProducts() {
        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .price(new BigDecimal("199.99"))
                .stock(50)
                .deleted(true)
                .build();

        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setId(2L);
        productDTO2.setName("Product 2");

        when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));
        when(productMapper.toDto(product)).thenReturn(productDTO);
        when(productMapper.toDto(product2)).thenReturn(productDTO2);

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
    }

    // ==================== GET ALL ACTIVE PRODUCTS TESTS ====================

    @Test
    @DisplayName("getAllActiveProducts - Should return only active products")
    void getAllActiveProducts_ShouldReturnOnlyActiveProducts() {
        Product deletedProduct = Product.builder()
                .id(2L)
                .name("Deleted Product")
                .deleted(true)
                .build();

        when(productRepository.findAll()).thenReturn(Arrays.asList(product, deletedProduct));
        when(productMapper.toDto(product)).thenReturn(productDTO);

        List<ProductDTO> result = productService.getAllActiveProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Product");
    }

    // ==================== UPDATE PRODUCT TESTS ====================

    @Test
    @DisplayName("updateProduct - Should update product successfully")
    void updateProduct_ShouldUpdateProductSuccessfully() {
        ProductCreateDTO updateDTO = new ProductCreateDTO();
        updateDTO.setName("Updated Product");
        updateDTO.setPrice(new BigDecimal("149.99"));
        updateDTO.setStock(200);

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Updated Product")
                .price(new BigDecimal("149.99"))
                .stock(200)
                .build();

        ProductDTO updatedDTO = new ProductDTO();
        updatedDTO.setId(1L);
        updatedDTO.setName("Updated Product");
        updatedDTO.setPrice(new BigDecimal("149.99"));
        updatedDTO.setStock(200);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findByName("Updated Product")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toDto(updatedProduct)).thenReturn(updatedDTO);

        ProductDTO result = productService.updateProduct(1L, updateDTO);

        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));
    }

    @Test
    @DisplayName("updateProduct - Should throw exception when updating to existing name")
    void updateProduct_ShouldThrowExceptionWhenUpdatingToExistingName() {
        Product existingProduct = Product.builder()
                .id(2L)
                .name("Existing Product")
                .build();

        ProductCreateDTO updateDTO = new ProductCreateDTO();
        updateDTO.setName("Existing Product");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findByName("Existing Product")).thenReturn(Optional.of(existingProduct));

        assertThatThrownBy(() -> productService.updateProduct(1L, updateDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");
    }

    // ==================== DELETE PRODUCT TESTS ====================

    @Test
    @DisplayName("deleteProduct - Should soft delete product successfully")
    void deleteProduct_ShouldSoftDeleteProductSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).save(argThat(p -> p.isDeleted()));
    }

    @Test
    @DisplayName("deleteProduct - Should throw exception when product already deleted")
    void deleteProduct_ShouldThrowExceptionWhenProductAlreadyDeleted() {
        product.setDeleted(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already deleted");
    }

    // ==================== RESTORE PRODUCT TESTS ====================

    @Test
    @DisplayName("restoreProduct - Should restore deleted product successfully")
    void restoreProduct_ShouldRestoreDeletedProductSuccessfully() {
        product.setDeleted(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.restoreProduct(1L);

        verify(productRepository).save(argThat(p -> !p.isDeleted()));
    }

    @Test
    @DisplayName("restoreProduct - Should throw exception when product is not deleted")
    void restoreProduct_ShouldThrowExceptionWhenProductNotDeleted() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.restoreProduct(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not deleted");
    }
}
