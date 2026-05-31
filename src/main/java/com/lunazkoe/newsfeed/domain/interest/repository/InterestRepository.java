package com.lunazkoe.newsfeed.domain.interest.repository;

import com.lunazkoe.newsfeed.domain.interest.entity.Interest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

    @Query("select i.name from Interest i")
    List<String> findAllNames();
}
