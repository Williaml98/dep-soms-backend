package com.dep.soms.repository;

import com.dep.soms.model.Conversation;
import com.dep.soms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List findByClientOrderByUpdatedAtDesc(User client);
    List findByAdminOrderByUpdatedAtDesc(User admin);
    List findByStatusOrderByUpdatedAtDesc(String status);
    List findAllByOrderByUpdatedAtDesc();
}
