package org.backmeup.plugin.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PluginContext {
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<String, String> readOnlyAttributes = new ConcurrentHashMap<>();

    public PluginContext() {

    }

    /**
     * Return the value of the specified context attribute, if any; otherwise
     * return <code>null</code>.
     *
     * @param name
     *            Name of the context attribute to return
     */
    public Object getAttribute(String name) {
        return (attributes.get(name));
    }
    
    /**
     * Return the value of the specified context attribute, if any; otherwise
     * return <code>null</code>.
     *
     * @param name
     *            Name of the context attribute to return
     * @param type
     *            Cast return value to this type
     */
    public <T> T getAttribute(String name, Class<T> type) {
        return type.cast(attributes.get(name));
    }
    

    /**
     * Return an enumeration of the names of the attributes associated with this
     * context.
     */
    public Enumeration<String> getAttributeNames() {
        Set<String> names = new HashSet<>();
        names.addAll(attributes.keySet());
        return Collections.enumeration(names);
    }

    /**
     * Remove the context attribute with the specified name, if any.
     *
     * @param name
     *            Name of the attribute to be removed
     */
    public void removeAttribute(String name) {

        Object value = null;

        // Remove the specified attribute
        // Check for read only attribute
        if (readOnlyAttributes.containsKey(name)) {
            return;
        }
        value = attributes.remove(name);
        if (value == null) {
            return;
        }
    }

    /**
     * Bind the specified value with the specified attribute name, replacing any
     * existing value for that name.
     *
     * @param name
     *            Attribute name to be bound
     * @param value
     *            New attribute value to be bound
     */
    public void setAttribute(String name, Object value) {
        setAttribute(name, value, false);
    }

    /**
     * Bind the specified value with the specified attribute name, replacing any
     * existing value for that name.
     *
     * @param name
     *            Attribute name to be bound
     * @param value
     *            New attribute value to be bound
     * @param readOnly
     *            Mark attribute as read only.
     */
    public void setAttribute(String name, Object value, boolean readOnly) {

        // Name cannot be null
        if (name == null) {
            throw new IllegalArgumentException("Name must no be null");
        }

        // Null value is the same as removeAttribute()
        if (value == null) {
            removeAttribute(name);
            return;
        }

        // Add or replace the specified attribute
        // Check for read only attribute
        if (readOnlyAttributes.containsKey(name)) {
            return;
        }
        attributes.put(name, value);

        if (readOnly) {
            setAttributeReadOnly(name);
        }
    }

    /**
     * Set an attribute as read only.
     */
    private void setAttributeReadOnly(String name) {
        if (attributes.containsKey(name)) {
            readOnlyAttributes.put(name, name);
        }
    }

    /**
     * Clear all created attributes, but read only attributes will be left in
     * place.
     */
    public void clear() {

        // Create list of attributes to be removed
        ArrayList<String> list = new ArrayList<>();
        Iterator<String> iter = attributes.keySet().iterator();
        while (iter.hasNext()) {
            list.add(iter.next());
        }

        // Remove application originated attributes
        // (read only attributes will be left in place)
        Iterator<String> keys = list.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            removeAttribute(key);
        }
    }
    
    /**
     * Clear all attributes.
     */
    public void clearAll() {
        attributes.clear();
        readOnlyAttributes.clear();
    }
}
