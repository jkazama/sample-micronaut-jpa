package sample.support;

import java.time.Clock;
import java.util.*;

import sample.context.*;
import sample.context.actor.ActorSession;

public class MockDomainHelperFactory {

    private final Clock mockClock;
    
    public MockDomainHelperFactory() {
        this(Clock.systemDefaultZone());
    }

    public MockDomainHelperFactory(final Clock mockClock) {
        this.mockClock = mockClock;
    }

    public DomainHelper create() {
        return create(new HashMap<>());
    }
    
    public DomainHelper create(Map<String, String> mockSettingMap) {
        return new DomainHelper(
                SimpleProvider.of(new ActorSession()),
                SimpleProvider.of(new Timestamper(mockClock)),
                SimpleProvider.of(new AppSettingHandler(new HashMap<>())));
    }

}
