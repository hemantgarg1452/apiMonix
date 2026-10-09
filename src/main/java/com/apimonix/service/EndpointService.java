package com.apimonix.service;

import com.apimonix.model.Endpoint;
import com.apimonix.model.User;
import com.apimonix.repository.EndpointRepository;
import com.apimonix.repository.UserRepository;
import com.apimonix.util.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EndpointService {
    private final EndpointRepository endpointRepository;
    private final UserRepository userRepository;

    private static final Map<String, Integer> PLAN_LIMITS = Map.of(
            "FREE", 1,
            "PRO", 10,
            "TEAM", 50
    );

    public List<Endpoint> getEndpoints(UUID userId){
        return endpointRepository.findByUserIdAndActiveTrue(userId);
    }

    public Endpoint addEndpoint(UUID userId, String userPlan, String name, String url){
        Validator.validateEndpointName(name);
        Validator.validateUrl(url);

        User user = userRepository.findById(userId)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        int currentCount = endpointRepository.countByUserIdAndActiveTrue(userId);
        int limit = PLAN_LIMITS.getOrDefault(userPlan, 1);

        if(currentCount >= limit){
            throw new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "Plan limit reached. Upgrade to add more endpoints"
            );
        }

        if(!url.startsWith("https://") && !url.startsWith("http://")){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URL must start with http:// or https://"
            );
        }

        int intervalMins = userPlan.equals("FREE") ? 15 : 5;

        User userRef = new User();
        userRef.setId(userId);

        Endpoint endpoint = Endpoint.builder()
                .user(userRef)
                .name(name)
                .url(url)
                .intervalMins(intervalMins)
                .active(true)
                .build();

        return endpointRepository.save(endpoint);
    }

    public void deleteEndpoint(UUID endpointId, UUID userId){
        Endpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Endpoint not found"));
        if(!endpoint.getUser().getId().equals(userId)){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Not your endpoint dude!");
        }

        endpoint.setActive(false);
        endpointRepository.save(endpoint);
    }

    private Endpoint getEndpointAndVerifyOwnership(UUID endpointId, UUID userId){
        Endpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(()->new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Endpoint not found"));

        if(!endpoint.getUser().getId().equals(userId)){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied");
        }
        return endpoint;
    }

    public void verifyOwnership(UUID endpointId, UUID userId){
        getEndpointAndVerifyOwnership(endpointId, userId);
    }
}
