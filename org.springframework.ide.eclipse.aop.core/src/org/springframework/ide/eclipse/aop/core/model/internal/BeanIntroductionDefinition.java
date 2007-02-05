/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ide.eclipse.aop.core.model.internal;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.aspectj.TypePatternClassFilter;
import org.springframework.aop.support.ClassFilters;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;

public class BeanIntroductionDefinition
        extends BeanAspectDefinition implements IIntroductionDefinition {

    private String introducedInterfaceName;

    private ClassFilter typePatternClassFilter;

    private String defaultImplName;

    private String typePattern;

    public BeanIntroductionDefinition() {
        setType(ADVICE_TYPES.DECLARE_PARENTS);
    }

    public ClassFilter getTypeMatcher() {
        if (this.typePatternClassFilter == null) {
            ClassFilter typePatternFilter = new TypePatternClassFilter(typePattern);

            // Excludes methods implemented.
            ClassFilter exclusion = new ClassFilter() {
                @SuppressWarnings("unchecked")
                public boolean matches(Class clazz) {
                    try {
                        Class<?> implInterfaceClass = Thread.currentThread()
                                .getContextClassLoader().loadClass(introducedInterfaceName);
                        return !(implInterfaceClass.isAssignableFrom(clazz));
                    }
                    catch (ClassNotFoundException e) {
                        return false;
                    }
                }
            };
            this.typePatternClassFilter = ClassFilters.intersection(typePatternFilter, exclusion);
        }
        return this.typePatternClassFilter;
    }

    public String getTypePattern() {
        return this.typePattern;
    }

    public String getAdviceMethodName() {
        throw new IllegalArgumentException();
    }

    public ADVICE_TYPES getType() {
        return ADVICE_TYPES.DECLARE_PARENTS;
    }

    public String getDefaultImplName() {
        return this.defaultImplName;
    }

    public String getImplInterfaceName() {
        return this.introducedInterfaceName;
    }

    public void setDefaultImplName(String defaultImplName) {
        this.defaultImplName = defaultImplName;
    }

    public void setIntroducedInterfaceName(String introducedInterfaceName) {
        this.introducedInterfaceName = introducedInterfaceName;
    }

    public void setTypePattern(String typePattern) {
        this.typePattern = typePattern;
    }

}
