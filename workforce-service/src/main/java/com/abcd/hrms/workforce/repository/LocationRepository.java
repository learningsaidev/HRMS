package com.abcd.hrms.workforce.repository;

import com.abcd.hrms.workforce.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findByCompanyId(UUID companyId);

    List<Location> findByLegalEntityId(UUID legalEntityId);

    Optional<Location> findByCode(String code);
}
