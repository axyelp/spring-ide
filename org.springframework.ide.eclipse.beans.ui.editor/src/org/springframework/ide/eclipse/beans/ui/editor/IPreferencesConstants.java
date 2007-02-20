/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.editor;

/**
 * Defines constants which are used to refer to values in the plugin's
 * preference bundle.
 */
public interface IPreferencesConstants {
	String PREFIX = Activator.PLUGIN_ID + ".";

	String OUTLINE_SPRING = PREFIX + "outline.spring";

	String OUTLINE_SORT = PREFIX + "outline.sort";
}
