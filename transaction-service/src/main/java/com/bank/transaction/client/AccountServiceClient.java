package com.bank.transaction.client;

import com.bank.transaction.exception.AccountServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;


@Component
public class AccountServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AccountServiceClient(RestTemplate restTemplate, @Value("${account-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void placeHold(UUID accountId, BigDecimal amount, String refId) {
        post("/accounts/" + accountId + "/hold", new AmountRefRequest(amount, refId));
    }

    public void releaseHold(UUID accountId, String refId) {
        post("/accounts/" + accountId + "/release", new RefRequest(refId));
    }

    public void debit(UUID accountId, BigDecimal amount, String refId) {
        post("/accounts/" + accountId + "/debit", new AmountRefRequest(amount, refId));
    }

    public void credit(UUID accountId, BigDecimal amount, String refId) {
        post("/accounts/" + accountId + "/credit", new AmountRefRequest(amount, refId));
    }

    private void post(String path, Object body) {
        try {
            restTemplate.postForEntity(baseUrl + path, body, Void.class);
        } catch (RestClientResponseException e) {
            throw new AccountServiceException(
                    "Account Service returned HTTP "
                            + e.getStatusCode()
                            + " for "
                            + path
                            + ". Response: "
                            + e.getResponseBodyAsString(),
                    e
            );

        } catch (RestClientException e) {
            throw new AccountServiceException(
                    "Account Service request failed for "
                            + path
                            + ": "
                            + e.getMessage(),
                    e
            );
        }
    }

    private record AmountRefRequest(BigDecimal amount, String refId) {
    }

    private record RefRequest(String refId) {
    }
}
