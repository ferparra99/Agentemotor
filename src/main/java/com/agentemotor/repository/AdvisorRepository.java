package com.agentemotor.repository;

import com.agentemotor.model.Advisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {
}
