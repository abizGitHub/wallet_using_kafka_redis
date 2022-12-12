package com.abiz.controller;

import com.abiz.controller.dto.FinancialDto;
import com.abiz.controller.mapper.FinancialMapper;
import com.abiz.controller.model.FinancialModel;
import com.abiz.kafka.Producer;
import com.abiz.service.IFinancialService;
import com.abiz.controller.dto.PatchDto;
import com.abiz.controller.dto.WalletDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FinancialController {

    private final FinancialMapper mapper;

    private final IFinancialService service;

    private final Producer producer;

    public FinancialController(FinancialMapper mapper, IFinancialService service, Producer producer) {
        this.mapper = mapper;
        this.service = service;
        this.producer = producer;
    }

    @PostMapping("/v1/financial")
    public ResponseEntity<FinancialDto> create(@Valid @RequestBody FinancialDto dto) {

        if (dto.getId() != null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        FinancialModel model = mapper.toModel(dto);
        dto = service.save(model);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @GetMapping("/v1/financial")
    public ResponseEntity<List<FinancialDto>> findAll() {
        List<FinancialDto> list = service.findAll();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PutMapping("/v1/financial")
    public ResponseEntity<FinancialDto> update(@Valid @RequestBody FinancialDto dto) {

        if (dto.getId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        FinancialModel model = mapper.toModel(dto);
        dto = service.update(model);
        return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
    }

    @PatchMapping("/v1/financial/{id}")
    public ResponseEntity<FinancialDto> partialUpdate(@PathVariable String id, @Valid @RequestBody PatchDto dto) {
        if (id == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        FinancialDto financialDto = service.partialUpdate(id, dto.getKey(), dto.getValue());
        return new ResponseEntity<>(financialDto, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/v1/financial/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/financial/by-user/{user}")
    public ResponseEntity<List<FinancialDto>> getByUser(@NotEmpty @PathVariable String user) {
        List<FinancialDto> list = service.getByUser(user);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/v1/financial/{id}")
    public ResponseEntity<FinancialDto> getById(@PathVariable String id) {
        FinancialDto dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/v1/wallet")
    public ResponseEntity<List<WalletDto>> wallet(@RequestBody List<FinancialDto> list) throws InterruptedException {
        List<WalletDto> result = service.applyToWallet(list);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
