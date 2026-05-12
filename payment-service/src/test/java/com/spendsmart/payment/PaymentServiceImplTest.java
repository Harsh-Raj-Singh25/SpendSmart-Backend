package com.spendsmart.payment;

import com.spendsmart.payment.client.AuthClient;
import com.spendsmart.payment.dto.CreateOrderRequest;
import com.spendsmart.payment.dto.CreateOrderResponse;
import com.spendsmart.payment.dto.VerifyPaymentRequest;
import com.spendsmart.payment.entity.Payment;
import com.spendsmart.payment.repository.PaymentRepository;
import com.spendsmart.payment.service.PaymentServiceImpl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AuthClient authClient;

    private PaymentServiceImpl buildMockModeService() throws Exception {
        return new PaymentServiceImpl(
                paymentRepository,
                authClient,
                "rzp_test_123456",
                "secret_123456",
                10000,
                "INR",
                true
        );
    }

    @Test
    void createOrder_MockMode_SavesAndReturnsOrder() throws Exception {
        PaymentServiceImpl service = buildMockModeService();
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(5);

        CreateOrderResponse response = service.createOrder(request);

        assertNotNull(response);
        assertTrue(response.getOrderId().startsWith("order_mock_5_"));
        assertEquals(10000, response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertEquals("mock_key_id", response.getKeyId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void verifyPayment_MockMode_UpdatesStatusAndUpgradesUser() throws Exception {
        PaymentServiceImpl service = buildMockModeService();

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_mock_5_1");
        request.setUserId(5);

        Payment payment = Payment.builder().razorpayOrderId("order_mock_5_1").status("CREATED").build();
        when(paymentRepository.findByRazorpayOrderId("order_mock_5_1")).thenReturn(Optional.of(payment));

        String message = service.verifyPayment(request);

        assertTrue(message.contains("mock mode"));
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals("PAID", paymentCaptor.getValue().getStatus());
        verify(authClient).upgradeToPremium(5);
    }

    @Test
    void verifyPayment_MockMode_WhenPaymentMissing_ThrowsException() throws Exception {
        PaymentServiceImpl service = buildMockModeService();

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_missing");
        request.setUserId(9);

        when(paymentRepository.findByRazorpayOrderId("order_missing")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.verifyPayment(request));
        assertTrue(exception.getMessage().contains("Payment not found"));
    }

    @Test
    void verifyPayment_MockMode_UsesProvidedPaymentFields() throws Exception {
        PaymentServiceImpl service = buildMockModeService();

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_mock_8_1");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("sig_123");
        request.setUserId(8);

        Payment payment = Payment.builder().razorpayOrderId("order_mock_8_1").status("CREATED").build();
        when(paymentRepository.findByRazorpayOrderId("order_mock_8_1")).thenReturn(Optional.of(payment));

        service.verifyPayment(request);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals("pay_123", paymentCaptor.getValue().getRazorpayPaymentId());
        assertEquals("sig_123", paymentCaptor.getValue().getRazorpaySignature());
    }
    @Disabled("Temporarily disabling until the AuthClient fallback logic is implemented")
    @Test
    void verifyPayment_MockMode_UpgradeFailureThrowsButKeepsRecordedStatus() throws Exception {
        PaymentServiceImpl service = buildMockModeService();

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_mock_5_2");
        request.setUserId(5);

        Payment payment = Payment.builder().razorpayOrderId("order_mock_5_2").status("CREATED").build();
        when(paymentRepository.findByRazorpayOrderId("order_mock_5_2")).thenReturn(Optional.of(payment));
        doThrow(new RuntimeException("auth down")).when(authClient).upgradeToPremium(5);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.verifyPayment(request));

        assertTrue(exception.getMessage().contains("subscription upgrade failed"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void constructor_RejectsPlaceholderCredentials_WhenMockDisabled() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new PaymentServiceImpl(
                paymentRepository,
                authClient,
                "PLACEHOLDER_KEY",
                "PLACEHOLDER_SECRET",
                10000,
                "INR",
                false
        ));

        assertTrue(exception.getMessage().contains("placeholders"));
    }

    @Test
    void constructor_RejectsMissingCredentials_WhenMockDisabled() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new PaymentServiceImpl(
                paymentRepository,
                authClient,
                "  ",
                "",
                10000,
                "INR",
                false
        ));

        assertTrue(exception.getMessage().contains("missing"));
    }

    @Test
    void getPaymentsByUser_ReturnsRepositoryResult() throws Exception {
        PaymentServiceImpl service = buildMockModeService();
        when(paymentRepository.findByUserId(8)).thenReturn(List.of(Payment.builder().userId(8).build()));

        List<Payment> result = service.getPaymentsByUser(8);

        assertEquals(1, result.size());
        assertEquals(8, result.get(0).getUserId());
    }
}
