package com.nexora.payment.service;

import com.nexora.payment.dto.request.CreatePaymentRequest;
import com.nexora.payment.dto.request.PaymentStatusUpdateRequest;
import com.nexora.payment.dto.response.PaymentResponse;
import com.nexora.payment.dto.response.PaymentStatusResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse getPaymentById(String paymentId);

    PaymentResponse getPaymentByReferenceId(String referenceId);

    List<PaymentResponse> getPaymentsByUserId(String userId);

    List<PaymentResponse> getPaymentsByWalletId(String walletId);

    PaymentStatusResponse updatePaymentStatus(String paymentId, PaymentStatusUpdateRequest request);

    PaymentResponse cancelPayment(String paymentId);

    PaymentResponse processPayment(String paymentId);
}