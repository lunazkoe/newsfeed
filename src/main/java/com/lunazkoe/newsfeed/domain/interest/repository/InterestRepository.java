package com.lunazkoe.newsfeed.domain.interest.repository;

import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

    @Query("select i.name from Interest i")
    List<String> findAllNames();

    @Query("SELECT DISTINCT i FROM Interest i JOIN FETCH i.keywords WHERE i.id = :id")
    Optional<Interest> findByIdWithKeywords(@Param("id") UUID id);
}
