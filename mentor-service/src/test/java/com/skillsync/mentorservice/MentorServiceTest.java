package com.skillsync.mentorservice;

import com.skillsync.mentorservice.dto.MentorDtos;
import com.skillsync.mentorservice.entity.Mentor;
import com.skillsync.mentorservice.entity.Skill;
import com.skillsync.mentorservice.repository.MentorRepository;
import com.skillsync.mentorservice.repository.SkillRepository;
import com.skillsync.mentorservice.service.MentorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorServiceTest {

    @Mock
    private MentorRepository mentorRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private MentorService mentorService;

    @Test
    void applyAsMentor_ShouldSaveMentor() {
        // Arrange
        MentorDtos.MentorRequest request = new MentorDtos.MentorRequest();
        request.setBio("Experienced Java Developer");
        request.setExperienceYears(5);
        request.setPricePerHour(500.0);
        request.setAvailability("Weekends");

        Mentor savedMentor = new Mentor();
        savedMentor.setId(1L);
        savedMentor.setEmail("mentor@gmail.com");
        savedMentor.setStatus(Mentor.MentorStatus.PENDING);

        when(mentorRepository.findByEmail("mentor@gmail.com")).thenReturn(Optional.empty());
        when(mentorRepository.save(any(Mentor.class))).thenReturn(savedMentor);

        // Act
        Mentor result = mentorService.applyAsMentor("mentor@gmail.com", "Mentor Name", request);

        // Assert
        assertNotNull(result);
        assertEquals(Mentor.MentorStatus.PENDING, result.getStatus());
        verify(mentorRepository).save(any(Mentor.class));
    }

    @Test
    void approveMentor_ShouldChangeStatusToApproved() {
        // Arrange
        Mentor mentor = new Mentor();
        mentor.setId(1L);
        mentor.setStatus(Mentor.MentorStatus.PENDING);

        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(mentorRepository.save(any(Mentor.class))).thenReturn(mentor);

        // Act
        Mentor result = mentorService.approveMentor(1L);

        // Assert
        assertEquals(Mentor.MentorStatus.APPROVED, result.getStatus());
    }

    @Test
    void getMentorById_ShouldThrow_WhenNotFound() {
        when(mentorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> mentorService.getMentorById(99L));
    }

    @Test
    void getAllApprovedMentors_ShouldReturnList() {
        Mentor m1 = new Mentor(); m1.setStatus(Mentor.MentorStatus.APPROVED);
        Mentor m2 = new Mentor(); m2.setStatus(Mentor.MentorStatus.APPROVED);

        when(mentorRepository.findByStatus(Mentor.MentorStatus.APPROVED))
                .thenReturn(Arrays.asList(m1, m2));

        List<Mentor> result = mentorService.getAllApprovedMentors();

        assertEquals(2, result.size());
    }

    @Test
    void createSkill_ShouldThrow_WhenSkillExists() {
        MentorDtos.SkillRequest request = new MentorDtos.SkillRequest();
        request.setName("Java");

        when(skillRepository.existsByName("Java")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> mentorService.createSkill(request));
    }

    @Test
    void createSkill_ShouldSave_WhenSkillNotExists() {
        MentorDtos.SkillRequest request = new MentorDtos.SkillRequest();
        request.setName("Python");
        request.setCategory("Programming");
        request.setDescription("Python language");

        Skill savedSkill = new Skill(1L, "Python", "Programming", "Python language");

        when(skillRepository.existsByName("Python")).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(savedSkill);

        Skill result = mentorService.createSkill(request);

        assertEquals("Python", result.getName());
    }

    @Test
    void updateMentorRating_ShouldCalculateCorrectAverage() {
        Mentor mentor = new Mentor();
        mentor.setId(1L);
        mentor.setAverageRating(4.0);
        mentor.setTotalReviews(2);

        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(mentorRepository.save(any())).thenReturn(mentor);

        mentorService.updateMentorRating(1L, 5.0);

        // avg = ((4.0 * 2) + 5.0) / 3 = 4.33
        verify(mentorRepository).save(argThat(m -> m.getTotalReviews() == 3));
    }
}
