/**
 * Copyright 2013 Alex Yanchenko
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
 */
package org.droidparts.reflect.ann.json;

import org.droidparts.annotation.json.Key;
import org.droidparts.reflect.ann.Ann;

public final class KeyAnn extends Ann<Key> {

	public String name;
	public boolean optional;

	public KeyAnn(Key annotation) {
		this();
		name = annotation.name();
		optional = annotation.optional();
	}

	public KeyAnn() {
		super(Key.class);
	}

	@Override
	public String toString() {
		return super.toString() + ", name: " + name + ", optional: " + optional;
	}
}
