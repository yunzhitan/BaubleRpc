package top.yunzhitan.rpc.provider;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultProviderContainer implements ProviderContainer {

    private final ConcurrentMap<String,Provider> serviceProviders = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DefaultProviderContainer.class);

    @Override
    public void addService(String uniqueKey, Provider provider) {
        serviceProviders.put(uniqueKey, provider);
        logger.info("ServiceConfig Provider[{},{}] is registered",uniqueKey, provider);
    }

    @Override
    public Provider findService(String uniqueKey) {
        return serviceProviders.get(uniqueKey);
    }

    @Override
    public Provider removeService(String uniqueKey) {
        Provider provider = serviceProviders.remove(uniqueKey);
         if(provider == null) {
             logger.warn("ServiceConfig Provider[{}] not found",uniqueKey);
         }
         else {
             logger.debug("ServiceConfig Provider[{},{}] is removed",uniqueKey,provider.getServiceProvider());
         }
         return provider;
    }

    @Override
    public List<Provider> getAllService() {
        return Lists.newArrayList(serviceProviders.values());
    }
}
