package com.abiz.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.abiz.MainApplication;
import com.abiz.controller.dto.FinancialDto;
import com.abiz.controller.dto.PatchDto;
import com.abiz.domain.FinancialEntity;
import com.abiz.domain.enumeration.AccountingStatus;
import com.abiz.repository.FinancialRepository;
import com.abiz.service.IWalletService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(classes = MainApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinancialControllerTest {

    protected final static ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private FinancialRepository repository;
    @Autowired
    private IWalletService walletService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void create() {
        int databaseSizeBeforeCreate = repository.findAll().size();
        FinancialDto dto = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(12.5))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/financial")
                        .content(mapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        List<FinancialEntity> entities = repository.findAll();
        assertThat(entities).hasSize(databaseSizeBeforeCreate + 1);
        FinancialEntity entity = entities.get(entities.size() - 1);
        assert entity.getAmount().compareTo(dto.getAmount()) == 0;
        assertThat(entity.getUser()).isEqualTo(dto.getUser());
        assertThat(entity.getStatus()).isEqualTo(dto.getStatus());
    }

    @Test
    @SneakyThrows
    void update() {
        FinancialEntity entity = FinancialEntity
                .builder()
                .amount(BigDecimal.valueOf(12.5))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();

        String savedId = repository.save(entity).getId();

        FinancialDto updateDto = FinancialDto
                .builder()
                .id(savedId)
                .amount(BigDecimal.valueOf(5432.60))
                .status(AccountingStatus.DEBTOR)
                .user("user2")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/financial")
                        .content(mapper.writeValueAsBytes(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        FinancialEntity updatedEntity = repository.findById(updateDto.getId()).get();

        assertThat(updatedEntity.getAmount().compareTo(updateDto.getAmount())).isZero();
        assertThat(updatedEntity.getUser()).isEqualTo(updateDto.getUser());
        assertThat(updatedEntity.getStatus()).isEqualTo(updateDto.getStatus());
    }

    @Test
    @SneakyThrows
    void updateWithoutId() {
        FinancialEntity entity = FinancialEntity
                .builder()
                .amount(BigDecimal.valueOf(12.5))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();

        repository.save(entity);

        FinancialDto updateDto = FinancialDto
                .builder()
                //.id(savedId) !!!
                .amount(BigDecimal.valueOf(5432.60))
                .status(AccountingStatus.DEBTOR)
                .user("user2")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/financial")
                        .content(mapper.writeValueAsBytes(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    @Test
    @SneakyThrows
    void partialUpdate() {
        FinancialEntity entity = FinancialEntity
                .builder()
                .amount(BigDecimal.valueOf(12.5))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();

        String savedId = repository.save(entity).getId();
        BigDecimal newAmount = new BigDecimal("87.3");

        PatchDto patchDto = PatchDto
                .builder()
                .key("amount")
                .value(newAmount)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/financial/" + savedId)
                        .content(mapper.writeValueAsBytes(patchDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        FinancialEntity updatedEntity = repository.findById(savedId).get();

        assertThat(updatedEntity.getAmount().compareTo(newAmount)).isZero();
        assertThat(updatedEntity.getUser()).isEqualTo(entity.getUser());
        assertThat(updatedEntity.getStatus()).isEqualTo(entity.getStatus());

        String newUser = "some-other";
        patchDto = PatchDto
                .builder()
                .key("user")
                .value(newUser)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/financial/" + savedId)
                        .content(mapper.writeValueAsBytes(patchDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
        AccountingStatus newStatus = AccountingStatus.DEBTOR;
        patchDto = PatchDto
                .builder()
                .key("status")
                .value(newStatus)
                .build();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/financial/" + savedId)
                        .content(mapper.writeValueAsBytes(patchDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        updatedEntity = repository.findById(savedId).get();

        assertThat(updatedEntity.getUser()).isEqualTo(newUser);
        assertThat(updatedEntity.getStatus()).isEqualTo(newStatus);
    }

    @Test
    @SneakyThrows
    void deleteById() {
        FinancialEntity entity = FinancialEntity
                .builder()
                .amount(BigDecimal.valueOf(12.5))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();

        String savedId = repository.save(entity).getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/financial/" + savedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(repository.findById(savedId).isPresent()).isFalse();
    }

    @Test
    @SneakyThrows
    void deleteByWrongId() {
        FinancialEntity entity = FinancialEntity
                .builder()
                .amount(BigDecimal.valueOf(12.5))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();

        repository.save(entity);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/financial/some-wrong-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getByUser() {
        List<FinancialEntity> list = new ArrayList<>();
        String user = "user99";
        for (int i = 0; i < 4; i++) {
            FinancialEntity entity = FinancialEntity
                    .builder()
                    .amount(BigDecimal.valueOf(12.5))
                    .status(AccountingStatus.CREDITOR)
                    .user(user)
                    .build();
            list.add(entity);
        }
        repository.saveAll(list);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/financial/by-user/" + user)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]['status']").value(list.get(0).getStatus().name()))
                .andExpect(jsonPath("$[3]['amount']").value(list.get(3).getAmount()));
    }

    @Test
    @SneakyThrows
    void getById() {
        FinancialEntity entity = FinancialEntity
                .builder()
                .amount(BigDecimal.valueOf(12.5))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();

        String savedId = repository.save(entity).getId();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/financial/" + savedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value(entity.getUser()))
                .andExpect(jsonPath("$.status").value(entity.getStatus().name()))
                .andExpect(jsonPath("$.amount").value(entity.getAmount()));
    }

    @Test
    @SneakyThrows
    void example1() {
        double user1PreviousCredit = walletService.getWallet("user1").getCredit().doubleValue();
        FinancialDto user1Credit = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(1.2))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();
        FinancialDto user1Debit = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(0.2))
                .status(AccountingStatus.DEBTOR)
                .user("user1")
                .build();
        FinancialDto user1Credit2 = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(0.6))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();

        List<FinancialDto> list = List.of(user1Credit, user1Debit, user1Credit2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(list))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['user']").value("user1"))
                .andExpect(jsonPath("$[0]['credit']").value(1.6 + user1PreviousCredit));
    }

    @Test
    @SneakyThrows
    void example2() {
        double user1PreviousCredit = walletService.getWallet("user1").getCredit().doubleValue();
        double user2PreviousCredit = walletService.getWallet("user2").getCredit().doubleValue();
        FinancialDto user1Creditor1 = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(9))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();
        FinancialDto user1Creditor2 = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(101))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();
        FinancialDto user2Creditor = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(5.6))
                .status(AccountingStatus.CREDITOR)
                .user("user2")
                .build();

        List<FinancialDto> list = List.of(user1Creditor1, user1Creditor2, user2Creditor);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(list))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['user']").value("user1"))
                .andExpect(jsonPath("$[0]['credit']").value(9 + user1PreviousCredit))
                .andExpect(jsonPath("$[1]['user']").value("user2"))
                .andExpect(jsonPath("$[1]['credit']").value(5.6 + user2PreviousCredit));
    }

    @Test
    @SneakyThrows
    void longApiTest() {
        double user1PreviousCredit = walletService.getWallet("user1").getCredit().doubleValue();
        double user2PreviousCredit = walletService.getWallet("user2").getCredit().doubleValue();
        List<FinancialDto> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            FinancialDto user1Creditor1 = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(10.2))
                    .status(AccountingStatus.CREDITOR)
                    .user("user1")
                    .build();
            list.add(user1Creditor1);
            FinancialDto user1CreditorFalse = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(500))
                    .status(AccountingStatus.CREDITOR)
                    .user("user1")
                    .build();
            list.add(user1CreditorFalse);
            FinancialDto user1Debtor = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(9.2))
                    .status(AccountingStatus.DEBTOR)
                    .user("user1")
                    .build();
            list.add(user1Debtor);
            FinancialDto user2Creditor1 = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(5.6))
                    .status(AccountingStatus.CREDITOR)
                    .user("user2")
                    .build();
            list.add(user2Creditor1);
            FinancialDto user2debtor = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(4.6))
                    .status(AccountingStatus.DEBTOR)
                    .user("user2")
                    .build();
            list.add(user2debtor);
        }

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(list))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$[0]['user']").value("user1"))
                .andExpect(jsonPath("$[0]['credit']").value(1000 + user1PreviousCredit))
                .andExpect(jsonPath("$[1]['user']").value("user2"))
                .andExpect(jsonPath("$[1]['credit']").value(1000 + user2PreviousCredit));
    }

    @Test
    @SneakyThrows
    void concurrentTest() {
        double user1PreviousCredit = walletService.getWallet("user1").getCredit().doubleValue();
        double user2PreviousCredit = walletService.getWallet("user2").getCredit().doubleValue();
        FinancialDto user1Creditor = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(10.2))
                .status(AccountingStatus.CREDITOR)
                .user("user1")
                .build();
        FinancialDto user1Debtor = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(9.2))
                .status(AccountingStatus.DEBTOR)
                .user("user1")
                .build();
        FinancialDto user2Creditor = FinancialDto
                .builder()
                .amount(BigDecimal.valueOf(1))
                .status(AccountingStatus.CREDITOR)
                .user("user2")
                .build();
        List<FinancialDto> list = List.of(user1Creditor, user1Debtor, user2Creditor);
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch downLatch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(list))
                    ).andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    downLatch.countDown();
                }
            });
        }
        downLatch.await();
        executorService.shutdown();
        double user1CurrentCredit = walletService.getWallet("user1").getCredit().doubleValue();
        double user2CurrentCredit = walletService.getWallet("user2").getCredit().doubleValue();
        Assertions.assertEquals(user1CurrentCredit, user1PreviousCredit + 100);
        Assertions.assertEquals(user2CurrentCredit, user2PreviousCredit + 100);
    }

    @Test
    @SneakyThrows
    void loadTest() {
        double user1PreviousCredit = walletService.getWallet("user1").getCredit().doubleValue();
        double user2PreviousCredit = walletService.getWallet("user2").getCredit().doubleValue();
        List<FinancialDto> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            FinancialDto user1Creditor1 = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(0.02))
                    .status(AccountingStatus.CREDITOR)
                    .user("user1")
                    .build();
            list.add(user1Creditor1);
            FinancialDto user1CreditorFalse = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(100))
                    .status(AccountingStatus.CREDITOR)
                    .user("user1")
                    .build();
            list.add(user1CreditorFalse);
            FinancialDto user1Debtor = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(0.01))
                    .status(AccountingStatus.DEBTOR)
                    .user("user1")
                    .build();
            list.add(user1Debtor);
            FinancialDto user2Creditor1 = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(0.06))
                    .status(AccountingStatus.CREDITOR)
                    .user("user2")
                    .build();
            list.add(user2Creditor1);
            FinancialDto user2debtor = FinancialDto
                    .builder()
                    .amount(BigDecimal.valueOf(0.05))
                    .status(AccountingStatus.DEBTOR)
                    .user("user2")
                    .build();
            list.add(user2debtor);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch downLatch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(list))
                    ).andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    downLatch.countDown();
                }
            });
        }
        downLatch.await();
        executorService.shutdown();
        double user1CurrentCredit = walletService.getWallet("user1").getCredit().doubleValue();
        double user2CurrentCredit = walletService.getWallet("user2").getCredit().doubleValue();
        Assertions.assertEquals(user1CurrentCredit, user1PreviousCredit + 100);
        Assertions.assertEquals(user2CurrentCredit, user2PreviousCredit + 100);
    }

}