package com.construction.repository;

import com.construction.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByActiveOrderByDisplayOrderAsc(boolean active);

    List<TeamMember> findAllByOrderByDisplayOrderAsc();
}
