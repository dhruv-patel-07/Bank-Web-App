package com.bank.web.schedule.Repo;

import com.bank.web.schedule.model.LoanPayment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanPaymentRepo extends MongoRepository<LoanPayment,Long> {

    List<LoanPayment> findByScheduleDate(String scheduleDate);
    LoanPayment findByLoanId(Long LoanId);
    LoanPayment findByPaymentId(Long LoanId);

//    @Query("{ 'scheduleDate': ?0, 'isReminderSend': false }")
//    List<LoanPayment> findByDateAndReminderFalse(String scheduleDate);
    List<LoanPayment> findByScheduleDateAndIsReminderSendFalse(String scheduleDate);

}
