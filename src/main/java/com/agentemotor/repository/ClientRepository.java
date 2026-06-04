package com.agentemotor.repository;

import com.agentemotor.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByAdvisorId(Long advisorId);

    Optional<Client> findByAdvisorIdAndNameAndPhone(Long advisorId, String name, String phone);
}
