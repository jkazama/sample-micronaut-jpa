package sample.controller;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.Valid;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import lombok.Value;
import sample.ActionStatusType;
import sample.context.Dto;
import sample.model.asset.CashInOut;
import sample.model.asset.CashInOut.RegCashOut;
import sample.usecase.AssetService;

/**
 * API controller of the asset domain.
 */
@Controller("/api/asset")
@Validated
public class AssetController {

    private final AssetService service;

    public AssetController(AssetService service) {
        this.service = service;
    }
    
    @Get("/cio/unprocessedOut")
    public List<CashOutUI> findUnprocessedCashOut() {
        return service.findUnprocessedCashOut().stream().map((cio) -> CashOutUI.of(cio)).collect(Collectors.toList());
    }
    
    @Post("/cio/withdraw")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public Map<String, Long> withdraw(@Body @Valid RegCashOut p) {
        HashMap<String, Long> result = new HashMap<>();
        result.put("id", service.withdraw(p));
        return result;
    }

    @Value
    public static class CashOutUI implements Dto {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String currency;
        private BigDecimal absAmount;
        private LocalDate requestDay;
        private LocalDateTime requestDate;
        private LocalDate eventDay;
        private LocalDate valueDay;
        private ActionStatusType statusType;
        private LocalDateTime updateDate;
        private Long cashflowId;

        public static CashOutUI of(final CashInOut cio) {
            return new CashOutUI(cio.getId(), cio.getCurrency(), cio.getAbsAmount(), cio.getRequestDay(),
                    cio.getRequestDate(), cio.getEventDay(), cio.getValueDay(), cio.getStatusType(),
                    cio.getUpdateDate(), cio.getCashflowId());
        }
    }

}
