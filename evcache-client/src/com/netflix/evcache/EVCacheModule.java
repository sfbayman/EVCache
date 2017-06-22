package com.netflix.evcache;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.netflix.archaius.api.annotations.ConfigurationSource;
import com.netflix.evcache.connection.DIConnectionModule;
import com.netflix.evcache.connection.IConnectionBuilder;
import com.netflix.evcache.event.hotkey.HotKeyListener;
import com.netflix.evcache.event.throttle.ThrottleListener;
import com.netflix.evcache.pool.EVCacheClientPoolManager;
import com.netflix.evcache.pool.EVCacheNodeList;
import com.netflix.evcache.pool.eureka.DIEVCacheNodeListProvider;

@Singleton
@SuppressWarnings("deprecation")
public class EVCacheModule extends AbstractModule {

    public EVCacheModule() {
    }

    @Singleton
    @ConfigurationSource("evcache")
    public static class EVCacheModuleConfigLoader {

        @Inject
        public EVCacheModuleConfigLoader(Injector injector, EVCacheModule module) {
            if(injector.getExistingBinding(Key.get(IConnectionBuilder.class)) == null) {
                module.install(new DIConnectionModule());
            }
        }
    }

    
    @Override
    protected void configure() {
        // Make sure connection factory provider Module is initialized in your Module when you init EVCacheModule
        bind(EVCacheModuleConfigLoader.class).asEagerSingleton();
        bind(EVCacheNodeList.class).toProvider(DIEVCacheNodeListProvider.class);
        bind(EVCacheClientPoolManager.class).asEagerSingleton();
        
        bind(HotKeyListener.class).asEagerSingleton();
        bind(ThrottleListener.class).asEagerSingleton();
    }

    @Inject
    EVCacheClientPoolManager manager;

    @PostConstruct
    public void init() {
        if(manager != null) {
            manager.initAtStartup();
        } else {
            EVCacheClientPoolManager.getInstance().initAtStartup();
        }
    }

    @PreDestroy
    public void shutdown() {
        if(manager != null) {
            manager.shutdown();
        } else {
            EVCacheClientPoolManager.getInstance().shutdown();
        }
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
 
    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj.getClass() == getClass());
    }

}