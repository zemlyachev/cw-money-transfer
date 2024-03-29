package com.example.moneytransfer;

import com.example.moneytransfer.objects.requests.ConfirmOperationRequest;
import com.example.moneytransfer.objects.requests.TransferRequest;
import com.example.moneytransfer.objects.responses.GoodTransferResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MoneytransferIntegrationTests {

  private static final String HOST = "http://localhost:";
  private static final int PORT = 5500;

  @Autowired
  TestRestTemplate restTemplate;
  public static GenericContainer<?> transferService = new GenericContainer<>("moneytransfer").withExposedPorts(PORT);


  @BeforeAll
  public static void setUp() {
    transferService.start();
  }

  @Test
  void transferWithConfirmGood() {
    TransferRequest transferRequest = Mockito.mock(TransferRequest.class);
    Mockito.when(transferRequest.getCardFromNumber()).thenReturn("4111111145551142");
    Mockito.when(transferRequest.getCardToNumber()).thenReturn("4988438843884305");
    Mockito.when(transferRequest.getAmount()).thenReturn(TransferRequest.Amount.builder().value(BigInteger.TEN).build());

    ResponseEntity<Object> transferResponse = restTemplate.postForEntity(
            HOST + transferService.getMappedPort(PORT) + "/transfer",
            transferRequest,
            Object.class);
    assertEquals(HttpStatus.OK, transferResponse.getStatusCode());
    System.out.println(transferResponse.getBody());

    Object transferBody = transferResponse.getBody();
    Assertions.assertTrue((transferBody instanceof GoodTransferResponse));
    GoodTransferResponse goodTransferResponse = (GoodTransferResponse) transferBody;
    String operationId = goodTransferResponse.getOperationId();

    ConfirmOperationRequest confirmOperationRequest = Mockito.mock(ConfirmOperationRequest.class);
    Mockito.when(confirmOperationRequest.getOperationId()).thenReturn(operationId);
    ResponseEntity<Object> objectResponseEntity = restTemplate.postForEntity(
            HOST + transferService.getMappedPort(PORT) + "/confirmOperation",
            confirmOperationRequest,
            Object.class);
    Assertions.assertEquals(HttpStatus.OK, objectResponseEntity.getStatusCode());
  }

}
