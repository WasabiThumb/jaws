package xyz.wasabicodes.jaws.struct.facet;

import java.util.*;

public interface FacetContext<C extends FacetContext<C>> {

    void registerFacets(Facet<C>... facets);

    void registerFacets(Class<? extends Facet<C>>... facetClasses);

    <T extends Facet<C>> T getFacet(Class<T> clazz);

    default <T extends Facet<C>> T getFacetAssert(Class<T> clazz) throws NullPointerException {
        return Objects.requireNonNull(this.getFacet(clazz));
    }

    Collection<Facet<C>> getAllFacets();

}
