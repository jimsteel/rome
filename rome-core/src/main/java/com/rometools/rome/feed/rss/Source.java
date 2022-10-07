/*
 * Copyright 2004 Sun Microsystems, Inc.
 * Copyright 2011 The ROME Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.rometools.rome.feed.rss;

import java.io.Serializable;
import java.util.Collections;

import com.rometools.rome.feed.impl.CloneableBean;
import com.rometools.rome.feed.impl.EqualsBean;
import com.rometools.rome.feed.impl.ToStringBean;

/**
 * Bean for item sources of RSS feeds.
 */
public class Source implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    private String url;
    private String value;

    public Source() { }

    /**
     * Creates a deep 'bean' clone of the object.
     * <p>
     *
     * @return a clone of the object.
     * @throws CloneNotSupportedException thrown if an element of the object cannot be cloned.
     *
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return CloneableBean.beanClone(this, Collections.<String>emptySet());
    }

    /**
     * Indicates whether some other object is "equal to" this one as defined by the Object equals()
     * method.
     * <p>
     *
     * @param other The reference object with which to compare.
     * @return <b>true</b> if 'this' object is equal to the 'other' object.
     *
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Source)) {
            return false;
        }
        return EqualsBean.beanEquals(this.getClass(), this, other);
    }

    /**
     * Returns a hashcode value for the object.
     * <p>
     * It follows the contract defined by the Object hashCode() method.
     * <p>
     *
     * @return the hashcode of the bean object.
     *
     */
    @Override
    public int hashCode() {
        return EqualsBean.beanHashCode(this);
    }

    /**
     * Returns the String representation for the object.
     * <p>
     *
     * @return String representation for the object.
     *
     */
    @Override
    public String toString() {
        return ToStringBean.toString(this.getClass(), this);
    }

    /**
     * Returns the source URL.
     * <p>
     *
     * @return the source URL, <b>null</b> if none.
     *
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the source URL.
     * <p>
     *
     * @param url the source URL to set, <b>null</b> if none.
     *
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Returns the source value.
     * <p>
     *
     * @return the source value, <b>null</b> if none.
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the source value.
     * <p>
     *
     * @param value the source value to set, <b>null</b> if none.
     *
     */
    public void setValue(final String value) {
        this.value = value;
    }
}
