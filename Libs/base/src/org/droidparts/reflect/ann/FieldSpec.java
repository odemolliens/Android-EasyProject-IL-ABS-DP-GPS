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
package org.droidparts.reflect.ann;

import java.lang.reflect.Field;

public class FieldSpec<AnnType extends Ann<?>> {

	public final Field field;
	public final Class<?> arrCollItemType;

	public final AnnType ann;

	public FieldSpec(Field field, Class<?> arrCollItemType, AnnType ann) {
		this.field = field;
		this.arrCollItemType = arrCollItemType;
		this.ann = ann;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ", fieldName:" + field.getName()
				+ ", fieldType:" + field.getType() + ", arrCollItemType:"
				+ arrCollItemType + ", ann:" + ann;
	}

}