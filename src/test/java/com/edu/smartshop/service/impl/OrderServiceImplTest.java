package com.edu.smartshop.service.impl;

import com.edu.smartshop.dto.request.OrderCreateDTO;
import com.edu.smartshop.dto.request.OrderItemDTO;
import com.edu.smartshop.dto.response.OrderResponseDTO;
import com.edu.smartshop.dto.response.ProductDTO;
import com.edu.smartshop.entity.Client;
import com.edu.smartshop.entity.Order;
import com.edu.smartshop.entity.OrderItem;
import com.edu.smartshop.entity.Product;
import com.edu.smartshop.entity.User;
import com.edu.smartshop.enums.CustomerTier;
import com.edu.smartshop.enums.OrderStatus;
import com.edu.smartshop.enums.UserRole;
import com.edu.smartshop.exception.BusinessRuleException;
import com.edu.smartshop.exception.ResourceNotFoundException;
import com.edu.smartshop.mapper.OrderMapper;
import com.edu.smartshop.mapper.ProductMapper;
import com.edu.smartshop.repository.ClientRepository;
import com.edu.smartshop.repository.OrderItemRepository;
import com.edu.smartshop.repository.OrderRepository;
import com.edu.smartshop.repository.ProductRepository;
import com.edu.smartshop.service.IClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IClientService clientService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Client client;
    private Product product;
    private Order order;
    private OrderResponseDTO orderResponseDTO;
    private OrderCreateDTO orderCreateDTO;

    @BeforeEach
    void setUp() {
        // Set tax rate via reflection since @Value injection doesn't work in unit tests
        ReflectionTestUtils.setField(orderService, "taxRate", new BigDecimal("0.20"));
        
        User user = User.builder()
                .id(1L)
                .username("client@test.com")
                .password("password")
                .role(UserRole.CLIENT)
                .build();

        client = Client.builder()
                .id(1L)
                .user(user)
                .fullName("Test Client")
                .email("client@test.com")
                .tier(CustomerTier.BASIC)
                .totalSpent(BigDecimal.ZERO)
                .totalOrders(0)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .deleted(false)
                .build();

        order = Order.builder()
                .id(1L)
                .client(client)
                .subTotal(new BigDecimal("100.00"))
                .totalDiscount(BigDecimal.ZERO)
                .tax(new BigDecimal("20.00"))
                .totalAmount(new BigDecimal("120.00"))
                .remainingAmount(new BigDecimal("120.00"))
                .status(OrderStatus.PENDING)
                .build();

        orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setId(1L);
        orderResponseDTO.setStatus(OrderStatus.PENDING);

        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(1);

        orderCreateDTO = new OrderCreateDTO();
        orderCreateDTO.setClientId(1L);
        orderCreateDTO.setItems(Collections.singletonList(itemDTO));
    }

    @Test
    @DisplayName("createOrder - Should create order successfully")
    void createOrder_ShouldCreateOrderSuccessfully() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(clientService.calculateTier(client)).thenReturn(CustomerTier.BASIC);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.createOrder(orderCreateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    @DisplayName("createOrder - Should throw exception when client not found")
    void createOrder_ShouldThrowExceptionWhenClientNotFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(orderCreateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Client not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder - Should throw exception when product not found")
    void createOrder_ShouldThrowExceptionWhenProductNotFound() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(orderCreateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder - Should throw exception when product is deleted")
    void createOrder_ShouldThrowExceptionWhenProductIsDeleted() {
        product.setDeleted(true);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(orderCreateDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not available");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder - Should throw exception when insufficient stock")
    void createOrder_ShouldThrowExceptionWhenInsufficientStock() {
        product.setStock(0);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(orderCreateDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder - Should apply tier discount for SILVER customers")
    void createOrder_ShouldApplyTierDiscountForSilverCustomers() {
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1L);
        itemDTO.setQuantity(10);

        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setClientId(1L);
        createDTO.setItems(Collections.singletonList(itemDTO));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(clientService.calculateTier(client)).thenReturn(CustomerTier.SILVER);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        orderService.createOrder(createDTO);

        verify(orderRepository).save(argThat(o -> o.getSubTotal().compareTo(new BigDecimal("1000.00")) == 0));
    }

    @Test
    @DisplayName("createOrder - Should throw exception for invalid promo code")
    void createOrder_ShouldThrowExceptionForInvalidPromoCode() {
        orderCreateDTO.setPromoCode("INVALID-CODE");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(clientService.calculateTier(client)).thenReturn(CustomerTier.BASIC);

        assertThatThrownBy(() -> orderService.createOrder(orderCreateDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid promo code");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrderById - Should return order when found")
    void getOrderById_ShouldReturnOrderWhenFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getOrderById - Should throw exception when order not found")
    void getOrderById_ShouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("confirmOrder - Should confirm order successfully")
    void confirmOrder_ShouldConfirmOrderSuccessfully() {
        order.setRemainingAmount(BigDecimal.ZERO);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.confirmOrder(1L);

        assertThat(result).isNotNull();
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("confirmOrder - Should throw exception when order already confirmed")
    void confirmOrder_ShouldThrowExceptionWhenOrderAlreadyConfirmed() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.confirmOrder(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already confirmed");
    }

    @Test
    @DisplayName("confirmOrder - Should throw exception when order has remaining amount")
    void confirmOrder_ShouldThrowExceptionWhenOrderHasRemainingAmount() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.confirmOrder(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot confirm unpaid order");
    }

    @Test
    @DisplayName("confirmOrder - Should throw exception when insufficient stock at confirmation")
    void confirmOrder_ShouldThrowExceptionWhenInsufficientStockAtConfirmation() {
        order.setRemainingAmount(BigDecimal.ZERO);
        Product lowStockProduct = Product.builder()
                .id(1L)
                .name("Low Stock Product")
                .stock(0)
                .build();
        OrderItem item = OrderItem.builder()
                .product(lowStockProduct)
                .quantity(5)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Collections.singletonList(item));

        assertThatThrownBy(() -> orderService.confirmOrder(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("cancelOrder - Should cancel pending order successfully")
    void cancelOrder_ShouldCancelPendingOrderSuccessfully() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        OrderResponseDTO result = orderService.cancelOrder(1L);

        assertThat(result).isNotNull();
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CANCELED));
    }

    @Test
    @DisplayName("cancelOrder - Should throw exception when order is not pending")
    void cancelOrder_ShouldThrowExceptionWhenOrderIsNotPending() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot cancel order");
    }

    @Test
    @DisplayName("getAllOrders - Should return all orders when no clientId filter")
    void getAllOrders_ShouldReturnAllOrdersWhenNoClientIdFilter() {
        Order order2 = Order.builder()
                .id(2L)
                .client(client)
                .status(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order, order2));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);
        when(orderMapper.toDto(order2)).thenReturn(new OrderResponseDTO());

        List<OrderResponseDTO> result = orderService.getAllOrders(null);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getAllOrders - Should return client orders when clientId filter provided")
    void getAllOrders_ShouldReturnClientOrdersWhenClientIdFilterProvided() {
        when(orderRepository.findByClientId(1L)).thenReturn(Collections.singletonList(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        List<OrderResponseDTO> result = orderService.getAllOrders(1L);

        assertThat(result).hasSize(1);
        verify(orderRepository).findByClientId(1L);
        verify(orderRepository, never()).findAll();
    }
}
