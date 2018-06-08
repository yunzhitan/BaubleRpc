package top.yunzhitan.registry;

import lombok.extern.slf4j.Slf4j;
import top.yunzhitan.Util.BaubleServiceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class RegistryFactory {

    private static ConcurrentMap<RegistryConfig,RegistryService> registryMap = new ConcurrentHashMap<>();

    public static RegistryService getRegistryService(RegistryConfig registryConfig) {
        RegistryService registryService = registryMap.get(registryConfig);
        String protocol = registryConfig.getProtocol();
        if (registryService == null) {
            registryService = BaubleServiceLoader.load(RegistryService.class).find(protocol);

            if(registryService == null) {
                log.error("load RegistryService error! for protocol:{}",protocol);
            }
        }

        return registryService;
    }

    public static List<RegistryService> getAllRegistryServices() {
        return new ArrayList<>(registryMap.values());
    }
}
