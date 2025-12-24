package com.jobsphere.jobsite.repository.payment;

import com.jobsphere.jobsite.model.User;
import com.jobsphere.jobsite.model.payment.Payment;
import com.jobsphere.jobsite.model.payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTxRef(String txRef);

    Optional<Payment> findByChapaTransactionId(String chapaTransactionId);

    Page<Payment> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Payment> findByUserAndStatus(User user, PaymentStatus status);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.user = :user AND p.status = 'SUCCESS'")
    long countSuccessfulPaymentsByUser(User user);

    @Query("SELECT p FROM Payment p WHERE p.user = :user AND p.status = 'SUCCESS' ORDER BY p.paidAt DESC")
    List<Payment> findSuccessfulPaymentsByUser(User user);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status IN ('SUCCESS', 'VERIFIED')")
    BigDecimal sumTotalRevenue();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status IN ('SUCCESS', 'VERIFIED') AND p.createdAt BETWEEN :start AND :end")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
