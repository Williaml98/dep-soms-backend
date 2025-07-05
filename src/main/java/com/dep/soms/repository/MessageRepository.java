package com.dep.soms.repository;

import com.dep.soms.model.Conversation;
import com.dep.soms.model.Message;
import com.dep.soms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = ?1 AND m.readAt IS NULL AND m.sender <> ?2")
    long countUnreadMessagesByConversationAndNotSender(Conversation conversation, User user);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.readAt IS NULL AND m.sender <> ?1")
    long countAllUnreadMessagesNotSentByUser(User user);

    List<Message> findByConversationIdAndReadAtIsNull(Long conversationId);
}
