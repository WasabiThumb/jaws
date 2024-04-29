package xyz.wasabicodes.jaws.struct.facet;

import xyz.wasabicodes.jaws.packet.Packet;
import xyz.wasabicodes.jaws.struct.User;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

public class FacetContextImpl<C extends FacetContext<C>> implements FacetContext<C> {

    private Map<Class<? extends Facet<C>>, Facet<C>> map = new HashMap<>();
    private boolean locked = false;

    public synchronized void lock(C context) {
        if (this.locked) return;
        this.map = Collections.unmodifiableMap(map);
        this.locked = true;
        for (Facet<C> f : this.map.values()) f.onInit(context);
    }

    public void fireStart(C context) {
        this.forEach((Facet<C> f) -> f.onStart(context));
    }

    public void fireMessage(C context, User user, Packet message) {
        this.forEach((Facet<C> f) -> f.onMessage(context, user, message));
    }

    public void fireConnect(C context, User user) {
        this.forEach((Facet<C> f) -> f.onConnect(context, user));
    }

    public void fireDisconnect(C context, User user) {
        this.forEach((Facet<C> f) -> f.onDisconnect(context, user));
    }

    public void fireEnd(C context) {
        this.forEach((Facet<C> f) -> f.onEnd(context));
    }

    private void forEach(Consumer<Facet<C>> con) {
        Set<Class<?>> seenImpls = new HashSet<>();
        for (Facet<C> f : this.map.values()) {
            if (!seenImpls.add(f.getClass())) continue;
            con.accept(f);
        }
    }

    @SuppressWarnings("unchecked")
    public void forceRegisterFacet(Facet<C> facet) {
        if (facet == null) return;
        Class<? extends Facet<C>> clazz = (Class<? extends Facet<C>>) facet.getClass();
        Class<?> parent;
        while (true) {
            this.map.put(clazz, facet);
            parent = clazz.getSuperclass();
            if (!Facet.class.isAssignableFrom(parent)) break;
            if (Objects.equals(Facet.class, parent)) break;
            clazz = (Class<? extends Facet<C>>) parent.asSubclass(Facet.class);
        }
    }

    private boolean checkDependenciesMet(Facet<C> facet) {
        Class<? extends Facet<C>>[] deps = facet.getDependencies();
        for (Class<? extends Facet<C>> clazz : deps) {
            if (this.getFacet(clazz) == null) return false;
        }
        return true;
    }

    @SafeVarargs
    @Override
    public final void registerFacets(Facet<C>... facets) {
        int head = 0;
        int lastHead = -1;
        final int len = facets.length;

        while (head < len) {
            if (head == lastHead) throw new IllegalStateException("Failed to resolve facet dependency tree");
            lastHead = head;

            int firstInvalid = -1;
            Facet<C> cur;
            for (int i = head; i < len; i++) {
                cur = facets[i];
                if (checkDependenciesMet(cur)) {
                    forceRegisterFacet(cur);
                    if (firstInvalid == -1) {
                        head = i + 1;
                    } else {
                        facets[i] = facets[firstInvalid];
                        facets[firstInvalid] = cur;
                        head = firstInvalid + 1;
                        break;
                    }
                } else if (firstInvalid == -1) {
                    firstInvalid = i;
                }
            }
        }
    }

    private Facet<C> constructFacet(Class<? extends Facet<C>> clazz) {
        Constructor<?> choice = null;
        Object[] choiceParams = new Object[0];
        int choiceFlags = 0;
        for (Constructor<?> candidate : clazz.getDeclaredConstructors()) {
            Class<?>[] types = candidate.getParameterTypes();
            Object[] candidateParams;
            int candidateFlags = 1;

            if (types.length == 0) {
                candidateFlags |= 4;
                candidateParams = new Object[0];
            } else {
                candidateParams = new Object[types.length];
                Class<?> pt;
                boolean valid = true;
                for (int i=0; i < types.length; i++) {
                    pt = types[i];
                    if (FacetContext.class.isAssignableFrom(pt)) {
                        candidateParams[i] = this;
                    } else {
                        valid = false;
                        break;
                    }
                }
                if (!valid) continue;
            }
            if (Modifier.isPublic(candidate.getModifiers())) candidateFlags |= 2;
            if (candidateFlags > choiceFlags) {
                choice = candidate;
                choiceParams = candidateParams;
                choiceFlags = candidateFlags;
            }
        }
        if (choiceFlags == 0) throw new IllegalStateException("Cannot resolve ideal constructor for facet " + clazz);

        Object out;
        try {
            out = choice.newInstance(choiceParams);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return clazz.cast(out);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    @Override
    public final void registerFacets(Class<? extends Facet<C>>... facetClasses) {
        final int len = facetClasses.length;

        Facet<?>[] arr = new Facet<?>[len];
        for (int i = 0; i < len; i++) arr[i] = this.constructFacet(facetClasses[i]);
        this.registerFacets((Facet<C>[]) arr);
    }

    @Override
    public <T extends Facet<C>> T getFacet(Class<T> clazz) {
        return clazz.cast(this.map.get(clazz));
    }

    @Override
    public Collection<Facet<C>> getAllFacets() {
        return Set.copyOf(this.map.values());
    }

}
