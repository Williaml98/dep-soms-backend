package com.dep.soms.service;
import com.dep.soms.dto.client.FeedbackSummaryDto;
import com.dep.soms.model.ClientFeedback;
import com.dep.soms.repository.ClientFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class ClientFeedbackSummaryService {

    @Autowired
    private ClientFeedbackRepository clientFeedbackRepository;

    @Transactional(readOnly = true)
    public FeedbackSummaryDto getOverallFeedbackSummary() {
        List<ClientFeedback> allFeedback = clientFeedbackRepository.findAll();
        return calculateSummary(allFeedback);
    }

    @Transactional(readOnly = true)
    public FeedbackSummaryDto getClientFeedbackSummary(Long clientId) {
        List<ClientFeedback> clientFeedback = clientFeedbackRepository.findByClientId(clientId);
        return calculateSummary(clientFeedback);
    }

    @Transactional(readOnly = true)
    public FeedbackSummaryDto getSiteFeedbackSummary(Long siteId) {
        List<ClientFeedback> siteFeedback = clientFeedbackRepository.findBySiteId(siteId);
        return calculateSummary(siteFeedback);
    }

    @Transactional(readOnly = true)
    public FeedbackSummaryDto getGuardFeedbackSummary(Long guardId) {
        List<ClientFeedback> guardFeedback = clientFeedbackRepository.findByGuardId(guardId);
        return calculateSummary(guardFeedback);
    }

    private FeedbackSummaryDto calculateSummary(List<ClientFeedback> feedbackList) {
        if (feedbackList.isEmpty()) {
            return FeedbackSummaryDto.builder()
                    .overallRating(0.0)
                    .serviceQualityRating(0.0)
                    .responseTimeRating(0.0)
                    .professionalismRating(0.0)
                    .communicationRating(0.0)
                    .guardRating(0.0)
                    .totalFeedbackCount(0L)
                    .build();
        }

        // Calculate averages
        double overallRating = calculateAverage(feedbackList.stream()
                .map(ClientFeedback::getRating)
                .filter(r -> r != null)
                .collect(Collectors.toList()));

        double serviceQualityRating = calculateAverage(feedbackList.stream()
                .map(ClientFeedback::getServiceQualityRating)
                .filter(r -> r != null)
                .collect(Collectors.toList()));

        double responseTimeRating = calculateAverage(feedbackList.stream()
                .map(ClientFeedback::getResponseTimeRating)
                .filter(r -> r != null)
                .collect(Collectors.toList()));

        double professionalismRating = calculateAverage(feedbackList.stream()
                .map(ClientFeedback::getProfessionalismRating)
                .filter(r -> r != null)
                .collect(Collectors.toList()));

        double communicationRating = calculateAverage(feedbackList.stream()
                .map(ClientFeedback::getCommunicationRating)
                .filter(r -> r != null)
                .collect(Collectors.toList()));

        double guardRating = calculateAverage(feedbackList.stream()
                .map(ClientFeedback::getGuardRating)
                .filter(r -> r != null)
                .collect(Collectors.toList()));

        return FeedbackSummaryDto.builder()
                .overallRating(overallRating)
                .serviceQualityRating(serviceQualityRating)
                .responseTimeRating(responseTimeRating)
                .professionalismRating(professionalismRating)
                .communicationRating(communicationRating)
                .guardRating(guardRating)
                .totalFeedbackCount((long) feedbackList.size())
                .build();
    }

    private double calculateAverage(List<Integer> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        OptionalDouble average = values.stream()
                .mapToInt(Integer::intValue)
                .average();
        return average.orElse(0.0);
    }
}

