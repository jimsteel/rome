/*
 * GoogleBaseParser.java
 *
 * Created on November 17, 2005, 11:31 AM
 *
 * This library is provided under dual licenses.
 * You may choose the terms of the Lesser General Public License or the Apache
 * License at your discretion.
 *
 *  Copyright (C) 2005  Robert Cooper, Temple of the Screaming Penguin
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rometools.feed.module.base.io;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.rometools.feed.module.base.GoogleBase;
import org.rometools.feed.module.base.GoogleBaseImpl;
import org.rometools.feed.module.base.types.CurrencyEnumeration;
import org.rometools.feed.module.base.types.DateTimeRange;
import org.rometools.feed.module.base.types.FloatUnit;
import org.rometools.feed.module.base.types.GenderEnumeration;
import org.rometools.feed.module.base.types.IntUnit;
import org.rometools.feed.module.base.types.PaymentTypeEnumeration;
import org.rometools.feed.module.base.types.PriceTypeEnumeration;
import org.rometools.feed.module.base.types.ShippingType;
import org.rometools.feed.module.base.types.Size;
import org.rometools.feed.module.base.types.YearType;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
 * @version $Revision: 1.3 $
 */
public class GoogleBaseParser implements ModuleParser {
    private static final Logger log = Logger.getAnonymousLogger();
    public static final char[] INTEGER_CHARS = "-1234567890".toCharArray();
    public static final char[] FLOAT_CHARS = "-1234567890.".toCharArray();
    public static final SimpleDateFormat SHORT_DT_FMT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat LONG_DT_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    static final Namespace NS = Namespace.getNamespace(GoogleBase.URI);
    static final Properties PROPS2TAGS = new Properties();
    static PropertyDescriptor[] pds = null;

    static {
        try {
            pds = Introspector.getBeanInfo(GoogleBaseImpl.class).getPropertyDescriptors();
            PROPS2TAGS.load(GoogleBaseParser.class.getResourceAsStream("/org/rometools/feed/module/base/io/tags.properties"));
        } catch (final IOException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "Unable to read properties file for Google Base tags!", e);
        } catch (final IntrospectionException e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "Unable to get property descriptors for GoogleBaseImpl!", e);
        }
    }

    /**
     * Creates a new instance of GoogleBaseParser
     */
    public GoogleBaseParser() {
        super();
    }

    @Override
    public Module parse(final Element element, final Locale locale) {
        final HashMap tag2pd = new HashMap();
        final GoogleBaseImpl module = new GoogleBaseImpl();

        try {
            for (final PropertyDescriptor pd : pds) {
                final String tagName = GoogleBaseParser.PROPS2TAGS.getProperty(pd.getName());

                if (tagName == null) {
                    log.log(Level.FINE, "Property: " + pd.getName() + " doesn't have a tag mapping. ");
                } else {
                    tag2pd.put(tagName, pd);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("Exception building tag to property mapping. ", e);
        }

        final List children = element.getChildren();
        final Iterator it = children.iterator();

        while (it.hasNext()) {
            final Element child = (Element) it.next();

            if (child.getNamespace().equals(GoogleBaseParser.NS)) {
                final PropertyDescriptor pd = (PropertyDescriptor) tag2pd.get(child.getName());

                if (pd != null) {
                    try {
                        handleTag(child, pd, module);
                    } catch (final Exception e) {
                        log.log(Level.WARNING, "Unable to handle tag: " + child.getName(), e);
                        e.printStackTrace();
                    }
                }
            }
        }

        return module;
    }

    public static String stripNonValidCharacters(final char[] validCharacters, final String input) {
        final StringBuffer newString = new StringBuffer();

        for (int i = 0; i < input.length(); i++) {
            for (final char validCharacter : validCharacters) {
                if (input.charAt(i) == validCharacter) {
                    newString.append(validCharacter);
                }
            }
        }

        return newString.toString();
    }

    @Override
    public String getNamespaceUri() {
        return GoogleBase.URI;
    }

    private void handleTag(final Element tag, final PropertyDescriptor pd, final GoogleBase module) throws Exception {
        Object tagValue = null;

        if (pd.getPropertyType() == Integer.class || pd.getPropertyType().getComponentType() == Integer.class) {
            tagValue = new Integer(GoogleBaseParser.stripNonValidCharacters(GoogleBaseParser.INTEGER_CHARS, tag.getText()));
        } else if (pd.getPropertyType() == Float.class || pd.getPropertyType().getComponentType() == Float.class) {
            tagValue = new Float(GoogleBaseParser.stripNonValidCharacters(GoogleBaseParser.FLOAT_CHARS, tag.getText()));
        } else if (pd.getPropertyType() == String.class || pd.getPropertyType().getComponentType() == String.class) {
            tagValue = tag.getText();
        } else if (pd.getPropertyType() == URL.class || pd.getPropertyType().getComponentType() == URL.class) {
            tagValue = new URL(tag.getText().trim());
        } else if (pd.getPropertyType() == Boolean.class || pd.getPropertyType().getComponentType() == Boolean.class) {
            tagValue = new Boolean(tag.getText().trim());
        } else if (pd.getPropertyType() == Date.class || pd.getPropertyType().getComponentType() == Date.class) {
            final String text = tag.getText().trim();

            if (text.length() > 10) {
                tagValue = GoogleBaseParser.LONG_DT_FMT.parse(text);
            } else {
                tagValue = GoogleBaseParser.SHORT_DT_FMT.parse(text);
            }
        } else if (pd.getPropertyType() == IntUnit.class || pd.getPropertyType().getComponentType() == IntUnit.class) {
            tagValue = new IntUnit(tag.getText());
        } else if (pd.getPropertyType() == FloatUnit.class || pd.getPropertyType().getComponentType() == FloatUnit.class) {
            tagValue = new FloatUnit(tag.getText());
        } else if (pd.getPropertyType() == DateTimeRange.class || pd.getPropertyType().getComponentType() == DateTimeRange.class) {
            tagValue = new DateTimeRange(LONG_DT_FMT.parse(tag.getChild("start", GoogleBaseParser.NS).getText().trim()), LONG_DT_FMT.parse(tag
                    .getChild("end", GoogleBaseParser.NS).getText().trim()));
        } else if (pd.getPropertyType() == ShippingType.class || pd.getPropertyType().getComponentType() == ShippingType.class) {
            final FloatUnit price = new FloatUnit(tag.getChild("price", GoogleBaseParser.NS).getText().trim());
            ShippingType.ServiceEnumeration service = ShippingType.ServiceEnumeration
                    .findByValue(tag.getChild("service", GoogleBaseParser.NS).getText().trim());

            if (service == null) {
                service = ShippingType.ServiceEnumeration.STANDARD;
            }

            final String country = tag.getChild("country", GoogleBaseParser.NS).getText().trim();
            tagValue = new ShippingType(price, service, country);
        } else if (pd.getPropertyType() == PaymentTypeEnumeration.class || pd.getPropertyType().getComponentType() == PaymentTypeEnumeration.class) {
            tagValue = PaymentTypeEnumeration.findByValue(tag.getText().trim());
        } else if (pd.getPropertyType() == PriceTypeEnumeration.class || pd.getPropertyType().getComponentType() == PriceTypeEnumeration.class) {
            tagValue = PriceTypeEnumeration.findByValue(tag.getText().trim());
        } else if (pd.getPropertyType() == CurrencyEnumeration.class || pd.getPropertyType().getComponentType() == CurrencyEnumeration.class) {
            tagValue = CurrencyEnumeration.findByValue(tag.getText().trim());
        } else if (pd.getPropertyType() == GenderEnumeration.class || pd.getPropertyType().getComponentType() == GenderEnumeration.class) {
            tagValue = GenderEnumeration.findByValue(tag.getText().trim());
        } else if (pd.getPropertyType() == YearType.class || pd.getPropertyType().getComponentType() == YearType.class) {
            tagValue = new YearType(tag.getText().trim());
        } else if (pd.getPropertyType() == Size.class || pd.getPropertyType().getComponentType() == Size.class) {
            tagValue = new Size(tag.getText().trim());
        }

        if (!pd.getPropertyType().isArray()) {
            pd.getWriteMethod().invoke(module, new Object[] { tagValue });
        } else {
            final Object[] current = (Object[]) pd.getReadMethod().invoke(module, (Object[]) null);
            final int newSize = current == null ? 1 : current.length + 1;
            final Object setValue = Array.newInstance(pd.getPropertyType().getComponentType(), newSize);

            int i = 0;

            for (; current != null && i < current.length; i++) {
                Array.set(setValue, i, current[i]);
            }

            Array.set(setValue, i, tagValue);
            pd.getWriteMethod().invoke(module, new Object[] { setValue });
        }
    }
}
