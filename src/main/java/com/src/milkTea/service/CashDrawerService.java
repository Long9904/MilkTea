package com.src.milkTea.service;

import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.entities.CashDrawer;
import com.src.milkTea.entities.Payment;
import com.src.milkTea.enums.TransactionEnum;
import com.src.milkTea.exception.CashDrawerException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.repository.CashDrawerRepository;
import com.src.milkTea.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class CashDrawerService {
    @Autowired
    private CashDrawerRepository cashDrawerRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;

    public CashDrawer openDrawer(double openingBalance) {
        // Kiểm tra xem đã có két nào đang mở không
        if (cashDrawerRepository.existsByDateAndIsOpenTrue(LocalDate.now())) {
            throw new CashDrawerException("Cash drawer is already open for today");
        }

        CashDrawer drawer = new CashDrawer();
        drawer.setOpeningBalance(openingBalance);
        drawer.setCurrentBalance(openingBalance);
        drawer.setOpen(true);
        
        return cashDrawerRepository.save(drawer);
    }

    public CashDrawer closeDrawer(double actualAmount, String note) {
        CashDrawer drawer = getCurrentDrawer();
        
        // Tính chênh lệch
        double difference = actualAmount - drawer.getCurrentBalance();
        
        drawer.setOpen(false);
        drawer.setActualBalance(actualAmount);
        drawer.setClosedAt(LocalDateTime.now());
        drawer.setNote(String.format("Chênh lệch: %,.0f VND. %s", 
            difference, 
            note != null ? note : ""
        ));
        
        return cashDrawerRepository.save(drawer);
    }

    public CashDrawer getCurrentDrawerStatus() {
        return getCurrentDrawer();

    }

    private CashDrawer getCurrentDrawer() {
        return cashDrawerRepository.findByDateAndIsOpenTrue(LocalDate.now())
            .orElseThrow(() -> new NotFoundException("Cash drawer is not open for today"));
    }

    public PagingResponse<CashDrawer> getAllDrawers(Pageable pageable) {

        Page<CashDrawer> drawers = cashDrawerRepository.findAll(pageable);
        List<CashDrawer> drawerList = drawers.getContent();
        // Convert to Page Response
        PagingResponse<CashDrawer> response = new PagingResponse<>();
        response.setData(drawerList);
        response.setPage( drawers.getNumber());
        response.setSize(drawers.getSize());
        response.setTotalElements(drawers.getTotalElements());
        response.setTotalPages(drawers.getTotalPages());
        response.setLast((drawers.isLast()));

        return response;
    }
}
