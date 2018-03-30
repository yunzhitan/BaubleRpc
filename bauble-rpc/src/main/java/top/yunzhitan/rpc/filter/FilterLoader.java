package top.yunzhitan.rpc.filter;

import top.yunzhitan.Util.BaubleServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FilterLoader {
    private static final Logger logger = LoggerFactory.getLogger(FilterLoader.class);

    public static FilterChain loadExtFilters(FilterChain chain, Filter.Type type) {
        try {
            List<Filter> sortedList = BaubleServiceLoader.load(Filter.class).sort();

            for(int i = sortedList.size()-1; i >= 0; --i ) {
                Filter extFilter = sortedList.get(i);
                Filter.Type extType = extFilter.getType();
                if(extType == type || extType == Filter.Type.ALL) {
                    chain = new DefaultFilterChain(extFilter,chain);
                }
            }
        } catch (Throwable t) {
            logger.error("Failed to load extension filter: {}",t);
        }
        return chain;
    }
}
