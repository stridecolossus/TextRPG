package org.sarge.textrpg.object;

import java.util.Map;

import org.sarge.lib.collection.StrictMap;
import org.sarge.textrpg.common.Contents;
import org.sarge.textrpg.object.TrackedContents.Limit;

/**
 * Object descriptor for an object that has {@link Contents} and {@link Limit}s.
 * @author Sarge
 */
public class ContentsObjectDescriptor extends ObjectDescriptor {
    protected final Map<Limit, String> limits;

    /**
     * Constructor.
     * @param descriptor        Underlying descriptor
     * @param limits            Limit(s) on the contents
     */
    public ContentsObjectDescriptor(ObjectDescriptor descriptor, Map<Limit, String> limits) {
        super(descriptor);
        this.limits = new StrictMap<>(limits);
    }

    /**
     * Copy constructor.
     * @param descriptor Contents descriptor
     */
    protected ContentsObjectDescriptor(ContentsObjectDescriptor descriptor) {
        this(descriptor, descriptor.limits);
    }
}
