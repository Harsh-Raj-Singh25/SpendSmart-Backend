package com.spendsmart.payment.repository;

import com.spendsmart.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
	List<Payment> findByUserId(Integer userId);
}
