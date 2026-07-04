package com.construction.service;

import com.construction.entity.TeamMember;
import com.construction.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<TeamMember> findActive() {
        return teamMemberRepository.findByActiveOrderByDisplayOrderAsc(true);
    }

    @Transactional(readOnly = true)
    public List<TeamMember> findAll() {
        return teamMemberRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    public Optional<TeamMember> findById(Long id) {
        return teamMemberRepository.findById(id);
    }

    public TeamMember save(TeamMember m) {
        if (m.getId() != null && m.getPhoto() == null) {
            teamMemberRepository.findById(m.getId())
                    .ifPresent(existing -> m.setPhoto(existing.getPhoto()));
        }
        return teamMemberRepository.save(m);
    }

    public void delete(Long id) {
        teamMemberRepository.findById(id).ifPresent(member -> {
            if (member.getPhoto() != null && !member.getPhoto().isBlank()) {
                fileStorageService.deleteFile(member.getPhoto());
            }
            teamMemberRepository.deleteById(id);
        });
    }
}
