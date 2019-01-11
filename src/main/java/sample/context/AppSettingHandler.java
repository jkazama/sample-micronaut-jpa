package sample.context;

import java.util.*;

import javax.inject.*;

import org.springframework.transaction.PlatformTransactionManager;

import sample.context.orm.*;

/**
 * Access application setting information.
 */
@Singleton
public class AppSettingHandler {

    private final OrmRepository rep;
    private final PlatformTransactionManager txm;
    /** You do a fixed key / value with a mock mode to return at the time of the setting */
    private final Optional<Map<String, String>> mockMap;
    
    public AppSettingHandler(OrmRepository rep, @Named(SystemRepository.Name) PlatformTransactionManager txm) {
        this.rep = rep;
        this.txm = txm;
        this.mockMap = Optional.empty();
    }

    public AppSettingHandler(Map<String, String> mockMap) {
        this.rep = null;
        this.txm = null;
        this.mockMap = Optional.of(mockMap);
    }

    public AppSetting setting(String id) {
        if (mockMap.isPresent()) {
            return mockSetting(id);
        }
        AppSetting setting = TxTemplate.of(txm).readOnly().tx(
                () -> AppSetting.load(rep, id));
        return setting;
    }

    private AppSetting mockSetting(String id) {
        return new AppSetting(id, "category", "Mock information for the test", mockMap.get().get(id));
    }

    public AppSetting update(String id, String value) {
        return mockMap.isPresent() ? mockSetting(id)
                : TxTemplate.of(txm).tx(() -> AppSetting.load(rep, id).update(rep, value));
    }

}
