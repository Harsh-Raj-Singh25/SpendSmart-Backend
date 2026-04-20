package com.spendsmart.payment;

import com.spendsmart.payment.dto.CreateOrderRequest;
import com.spendsmart.payment.dto.CreateOrderResponse;
import com.spendsmart.payment.dto.VerifyPaymentRequest;
import com.spendsmart.payment.entity.Payment;
import com.spendsmart.payment.resource.PaymentResource;
import com.spendsmart.payment.service.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentResourceTest {

    @Mock
    private PaymentServiceImpl paymentService;

    @InjectMocks
    private PaymentResource paymentResource;

    @Test
    void endpointsDelegateToService() {
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(5);

        CreateOrderResponse createOrderResponse = new CreateOrderResponse("order_1", 10000, "INR", "rzp_test_x");
        when(paymentService.createOrder(createOrderRequest)).thenReturn(createOrderResponse);

        ResponseEntity<CreateOrderResponse> createOrderResult = paymentResource.createOrder(createOrderRequest);
        assertEquals(200, createOrderResult.getStatusCode().value());
        assertEquals("order_1", createOrderResult.getBody().getOrderId());

        VerifyPaymentRequest verifyPaymentRequest = new VerifyPaymentRequest();
        verifyPaymentRequest.setRazorpayOrderId("order_1");
        when(paymentService.verifyPayment(verifyPaymentRequest)).thenReturn("ok");

        ResponseEntity<Map<String, String>> verifyResult = paymentResource.verifyPayment(verifyPaymentRequest);
        assertEquals(200, verifyResult.getStatusCode().value());
        assertEquals("ok", verifyResult.getBody().get("message"));

        when(paymentService.getPaymentsByUser(5)).thenReturn(List.of(Payment.builder().userId(5).build()));
        ResponseEntity<List<Payment>> historyResult = paymentResource.getPaymentHistory(5);
        assertEquals(200, historyResult.getStatusCode().value());
        assertEquals(1, historyResult.getBody().size());

        verify(paymentService).createOrder(createOrderRequest);
        verify(paymentService).verifyPayment(verifyPaymentRequest);
        verify(paymentService).getPaymentsByUser(5);
    }
}
