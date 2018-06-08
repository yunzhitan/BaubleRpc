package top.yunzhitan.registry.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.registry.NotifyListener;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProviderObserver {

    private static final Logger logger = LoggerFactory.getLogger(ProviderObserver.class);

    private ConcurrentMap<ServiceConfig,List<NotifyListener>> listenerMap = new ConcurrentHashMap<>();

}
