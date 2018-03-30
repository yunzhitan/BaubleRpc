package top.yunzhitan.rpc.provider;

import com.google.common.collect.Lists;
import top.yunzhitan.rpc.model.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultServiceContainer implements ServiceContainer {

    private final ConcurrentMap<String,ServiceProvider> serviceProviders = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceContainer.class);

    @Override
    public void registerService(String uniqueKey, ServiceProvider serviceProvider) {
        serviceProviders.put(uniqueKey,serviceProvider);
        logger.debug("Service Provider[{},{}] is registered",uniqueKey,serviceProvider);
    }

    @Override
    public ServiceProvider findService(String uniqueKey) {
        return serviceProviders.get(uniqueKey);
    }

    @Override
    public ServiceProvider removeService(String uniqueKey) {
        ServiceProvider provider = serviceProviders.remove(uniqueKey);
         if(provider == null) {
             logger.warn("Service Provider[{}] not found",uniqueKey);
         }
         else {
             logger.debug("Service Provider[{},{}] is removed",uniqueKey,provider.getServiceProvider());
         }
         return provider;
    }

    @Override
    public List<ServiceProvider> getAllService() {
        return Lists.newArrayList(serviceProviders.values());
    }
}
