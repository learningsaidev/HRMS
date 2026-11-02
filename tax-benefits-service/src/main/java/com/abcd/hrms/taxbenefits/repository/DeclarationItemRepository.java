package com.abcd.hrms.taxbenefits.repository;

import com.abcd.hrms.taxbenefits.entity.DeclarationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeclarationItemRepository extends JpaRepository<DeclarationItem, UUID> {
    List<DeclarationItem> findByDeclarationId(UUID declarationId);
    void deleteByDeclarationId(UUID declarationId);
}
