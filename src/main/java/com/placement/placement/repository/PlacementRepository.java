package com.placement.placement.repository;
import com.placement.placement.entity.Placement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PlacementRepository extends JpaRepository<Placement, Long> {

    // FIX: added so a student/company's placement records can be cleaned up
    // automatically when that student/company is deleted. Without this,
    // deleting a student/company that already has a placement failed with a
    // database foreign-key error, because the placement row still pointed
    // to the now-deleted student/company.
    @Transactional
    void deleteByStudentId(Long studentId);

    @Transactional
    void deleteByCompanyId(Long companyId);
}