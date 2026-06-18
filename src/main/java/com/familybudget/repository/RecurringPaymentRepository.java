package com.familybudget.repository;

import com.familybudget.entity.RecurringPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {

    Optional<RecurringPayment> findByAccountId(Long accountId);

    Page<RecurringPayment> findByActiveTrue(Pageable pageable);

    Page<RecurringPayment> findByNextDueDateBefore(LocalDate date, Pageable pageable);
}
