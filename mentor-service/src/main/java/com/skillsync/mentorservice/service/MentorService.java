package com.skillsync.mentorservice.service;

import com.skillsync.mentorservice.dto.MentorDtos;
import com.skillsync.mentorservice.entity.Mentor;
import com.skillsync.mentorservice.entity.Skill;
import com.skillsync.mentorservice.repository.MentorRepository;
import com.skillsync.mentorservice.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorRepository mentorRepository;
    private final SkillRepository skillRepository;

    // Learner applies as mentor - identity from JWT via Authentication
    public Mentor applyAsMentor(String email, String fullName, MentorDtos.MentorRequest request) {
        Mentor mentor = mentorRepository.findByEmail(email).orElse(new Mentor());
        mentor.setEmail(email);
        mentor.setFullName(fullName);
        mentor.setBio(request.getBio());
        mentor.setExperienceYears(request.getExperienceYears());
        mentor.setPricePerHour(request.getPricePerHour());
        mentor.setAvailability(request.getAvailability());
        if (request.getSkillIds() != null) {
            mentor.setSkills(skillRepository.findAllById(request.getSkillIds()));
        }
        mentor.setStatus(Mentor.MentorStatus.PENDING);
        return mentorRepository.save(mentor);
    }

    @CacheEvict(value = {"approvedMentors", "mentor"}, allEntries = true)
    public Mentor approveMentor(Long id) {
        Mentor mentor = getMentorById(id);
        mentor.setStatus(Mentor.MentorStatus.APPROVED);
        return mentorRepository.save(mentor);
    }

    @CacheEvict(value = {"approvedMentors", "mentor"}, allEntries = true)
    public Mentor rejectMentor(Long id) {
        Mentor mentor = getMentorById(id);
        mentor.setStatus(Mentor.MentorStatus.REJECTED);
        return mentorRepository.save(mentor);
    }

    @Cacheable(value = "approvedMentors")
    public List<Mentor> getAllApprovedMentors() {
        return mentorRepository.findByStatus(Mentor.MentorStatus.APPROVED);
    }

    public List<Mentor> getPendingMentors() {
        return mentorRepository.findByStatus(Mentor.MentorStatus.PENDING);
    }

    @Cacheable(value = "mentor", key = "#id")
    public Mentor getMentorById(Long id) {
        return mentorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mentor not found: " + id));
    }

//    @Cacheable(value = "mentorSearch", key = "#skill")
    public List<Mentor> searchBySkill(String skill) {
    	  System.out.println("DB HIT");
        return mentorRepository.findBySkillName(skill);
    }

    public List<Mentor> filterMentors(Double minRating, Integer minExp, Double maxPrice) {
        return mentorRepository.findByFilters(
                minRating != null ? minRating : 0.0,
                minExp != null ? minExp : 0,
                maxPrice != null ? maxPrice : Double.MAX_VALUE);
    }

    // Mentor updates own availability
    @CacheEvict(value = {"mentor", "approvedMentors"}, allEntries = true)
    public Mentor updateAvailability(String email, String availability) {
        Mentor mentor = mentorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Mentor not found for: " + email));
        mentor.setAvailability(availability);
        return mentorRepository.save(mentor);
    }

    @CacheEvict(value = {"mentor", "approvedMentors"}, allEntries = true)
    public void updateMentorRating(Long mentorId, Double newRating) {
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));
        int total = mentor.getTotalReviews();
        double avg = mentor.getAverageRating();
        mentor.setAverageRating(((avg * total) + newRating) / (total + 1));
        mentor.setTotalReviews(total + 1);
        mentorRepository.save(mentor);
    }

    // Skills
    @CacheEvict(value = "skills", allEntries = true)
    public Skill createSkill(MentorDtos.SkillRequest request) {
        if (skillRepository.existsByName(request.getName())) {
            throw new RuntimeException("Skill already exists");
        }
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription());
        return skillRepository.save(skill);
    }

    @Cacheable(value = "skills")
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
}
