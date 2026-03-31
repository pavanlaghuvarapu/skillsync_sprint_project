package com.skillsync.groupservice.service;

import com.skillsync.groupservice.dto.GroupDtos;
import com.skillsync.groupservice.entity.GroupMessage;
import com.skillsync.groupservice.entity.LearningGroup;
import com.skillsync.groupservice.repository.GroupMessageRepository;
import com.skillsync.groupservice.repository.LearningGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final LearningGroupRepository groupRepository;
    private final GroupMessageRepository messageRepository;

    public LearningGroup createGroup(String createdByEmail, GroupDtos.GroupRequest request) {
        LearningGroup group = new LearningGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setTopic(request.getTopic());
        group.setCreatedByEmail(createdByEmail);
        group.setMaxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 50);
        group.getMemberEmails().add(createdByEmail);
        return groupRepository.save(group);
    }

    public LearningGroup joinGroup(Long groupId, String email) {
        LearningGroup group = getById(groupId);
        if (group.getMemberEmails().contains(email))
            throw new RuntimeException("Already a member of this group");
        if (group.getMemberEmails().size() >= group.getMaxMembers())
            throw new RuntimeException("Group is full");
        group.getMemberEmails().add(email);
        return groupRepository.save(group);
    }

    public LearningGroup leaveGroup(Long groupId, String email) {
        LearningGroup group = getById(groupId);
        if (!group.getMemberEmails().contains(email))
            throw new RuntimeException("Not a member of this group");
        group.getMemberEmails().remove(email);
        return groupRepository.save(group);
    }

    public LearningGroup getById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));
    }

    public List<LearningGroup> getAll() {
        return groupRepository.findAll();
    }

    public List<LearningGroup> getMyGroups(String email) {
        return groupRepository.findByMemberEmail(email);
    }

    public List<LearningGroup> searchByTopic(String topic) {
        return groupRepository.findByTopicContainingIgnoreCase(topic);
    }

    public GroupMessage sendMessage(Long groupId, String senderEmail, String message) {
        LearningGroup group = getById(groupId);
        if (!group.getMemberEmails().contains(senderEmail))
            throw new RuntimeException("Only members can send messages");
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setSenderEmail(senderEmail);
        msg.setMessage(message);
        return messageRepository.save(msg);
    }

    public List<GroupMessage> getMessages(Long groupId) {
        return messageRepository.findByGroupIdOrderBySentAtAsc(groupId);
    }

    public void deleteGroup(Long groupId) {
        groupRepository.deleteById(groupId);
    }
}
