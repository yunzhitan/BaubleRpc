package top.yunzhitan.Util;


import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * 对ServiceLoader的封装 能根据String查找加载的服务
 * @param <S>
 */
public final class BaubleServiceLoader<S> implements Iterable<S> {

    private static final String PREFIX = "META-INF/services/";

    // the class or interface representing the serviceConfig being loaded
    private final Class<S> service;

    // the class loader used to locate, load, and instantiate providers
    private final ClassLoader loader;

    // cached providers, in instantiation order
    private LinkedHashMap<String, S> providers = new LinkedHashMap<>();

    // the current lazy-lookup iterator
    private LazyIterator lookupIterator;

    public static <S> BaubleServiceLoader<S> load(Class<S> service) {
        return BaubleServiceLoader.load(service, Thread.currentThread().getContextClassLoader());
    }

    public static <S> BaubleServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        return new BaubleServiceLoader<>(service, loader);
    }

    public List<S> sort() {
        List<S> sortList = Lists.newArrayList(iterator());

        if (sortList.size() <= 1) {
            return sortList;
        }

        sortList.sort((o1, o2) -> {
            SPI o1_spi = o1.getClass().getAnnotation(SPI.class);
            SPI o2_spi = o2.getClass().getAnnotation(SPI.class);

            int o1_priority = o1_spi == null ? 0 : o1_spi.priority();
            int o2_priority = o2_spi == null ? 0 : o2_spi.priority();

            // 优先级高的排前边
            return o2_priority - o1_priority;
        });

        return sortList;
    }

    public S first() {
        return sort().get(0);
    }

    public S find(String implName) {
        for (S s : providers.values()) {
            SPI spi = s.getClass().getAnnotation(SPI.class);
            if (spi != null && spi.name().equalsIgnoreCase(implName)) {
                return s;
            }
        }
        while (lookupIterator.hasNext()) {
            Pair<String, Class<S>> e = lookupIterator.next();
            String name = e.getFirst();
            Class<S> cls = e.getSecond();
            SPI spi = cls.getAnnotation(SPI.class);
            if (spi != null && spi.name().equalsIgnoreCase(implName)) {
                try {
                    S provider = service.cast(cls.newInstance());
                    providers.put(name, provider);
                    return provider;
                } catch (Throwable x) {
                    throw fail(service, "provider " + name + " could not be instantiated", x);
                }
            }
        }
        throw fail(service, "provider " + implName + " could not be found");
    }

    public void reload() {
        providers.clear();
        lookupIterator = new LazyIterator(service, loader);
    }

    private BaubleServiceLoader(Class<S> service, ClassLoader loader) {
        this.service = checkNotNull(service, "serviceConfig interface cannot be null");
        this.loader = (loader == null) ? ClassLoader.getSystemClassLoader() : loader;
        reload();
    }

    private static ServiceConfigurationError fail(Class<?> service, String msg, Throwable cause) {
        return new ServiceConfigurationError(service.getName() + ": " + msg, cause);
    }

    private static ServiceConfigurationError fail(Class<?> service, String msg) {
        return new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static ServiceConfigurationError fail(Class<?> service, URL url, int line, String msg) {
        return fail(service, url + ":" + line + ": " + msg);
    }

    // parse a single line from the given configuration file, adding the name
    // on the line to the names list.
    private int parseLine(Class<?> service, URL u, BufferedReader r, int lc, List<String> names)
            throws IOException, ServiceConfigurationError {

        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) {
            ln = ln.substring(0, ci);
        }
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
                throw fail(service, u, lc, "illegal configuration-file syntax");
            }
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                throw fail(service, u, lc, "illegal provider-class name: " + ln);
            }
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                    throw fail(service, u, lc, "Illegal provider-class name: " + ln);
                }
            }
            if (!providers.containsKey(ln) && !names.contains(ln)) {
                names.add(ln);
            }
        }
        return lc + 1;
    }

    @SuppressWarnings("all")
    private Iterator<String> parse(Class<?> service, URL url) {
        InputStream in = null;
        BufferedReader r = null;
        ArrayList<String> names = Lists.newArrayList();
        try {
            in = url.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            while ((lc = parseLine(service, url, r, lc, names)) >= 0) ;
        } catch (IOException x) {
            throw fail(service, "error reading configuration file", x);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException y) {
                throw fail(service, "error closing configuration file", y);
            }
        }
        return names.iterator();
    }

    @Override
    public Iterator<S> iterator() {
        return new Iterator<S>() {

            Iterator<Map.Entry<String, S>> knownProviders = providers.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return knownProviders.hasNext() || lookupIterator.hasNext();
            }

            @Override
            public S next() {
                if (knownProviders.hasNext()) {
                    return knownProviders.next().getValue();
                }
                Pair<String, Class<S>> pair = lookupIterator.next();
                String name = pair.getFirst();
                Class<S> cls = pair.getSecond();
                try {
                    S provider = service.cast(cls.newInstance());
                    providers.put(name, provider);
                    return provider;
                } catch (Throwable x) {
                    throw fail(service, "provider " + name + " could not be instantiated", x);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private class LazyIterator implements Iterator<Pair<String, Class<S>>> {
        Class<S> service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        String nextName = null;

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        @Override
        public boolean hasNext() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null) {
                        configs = ClassLoader.getSystemResources(fullName);
                    } else {
                        configs = loader.getResources(fullName);
                    }
                } catch (IOException x) {
                    throw fail(service, "error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Pair<String, Class<S>> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            String name = nextName;
            nextName = null;
            Class<?> cls;
            try {
                cls = Class.forName(name, false, loader);
            } catch (ClassNotFoundException x) {
                throw fail(service, "provider " + name + " not found");
            }
            if (!service.isAssignableFrom(cls)) {
                throw fail(service, "provider " + name + " not a subtype");
            }
            return Pair.of(name, (Class<S>) cls);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns a string describing this serviceConfig.
     */
    @Override
    public String toString() {
        return "top.common.util.BaubleServiceLoader[" + service.getName() + "]";
    }
}


