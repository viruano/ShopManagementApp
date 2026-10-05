package com.autorepair.shop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LineItemRepository extends JpaRepository<LineItem, Long> {

    // 🔍 THE RE-ALIGNED WORK ORDER QUERY HOOK:
    List<LineItem> findByWorkOrderId(Long workOrderId);
}
