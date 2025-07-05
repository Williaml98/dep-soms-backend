package com.dep.soms.security;


import com.dep.soms.model.Client;
import com.dep.soms.model.Guard;
import com.dep.soms.model.User;
import com.dep.soms.repository.ClientRepository;
import com.dep.soms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.dep.soms.repository.GuardRepository;


import java.util.Optional;

@Component("userSecurity")
public class UserSecurity {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GuardRepository guardRepository;
    @Autowired
    private ClientRepository clientRepository;

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        return user != null && user.getId().equals(userId);
    }


    public boolean isCurrentGuard(Long guardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return false;
        }

        Optional<Guard> guard = guardRepository.findById(guardId);
        return guard.isPresent() && guard.get().getUser().getId().equals(user.getId());
    }



    public boolean isCurrentClient(Long clientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return false;
        }

        Optional<Client> client = clientRepository.findById(clientId);
        return client.isPresent() && client.get().getUser().getId().equals(user.getId());
    }

}
