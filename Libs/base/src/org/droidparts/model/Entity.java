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
package org.droidparts.model;

import static org.droidparts.contract.DB.Column.ID;

import org.droidparts.annotation.sql.Column;

public abstract class Entity extends Model {
	private static final long serialVersionUID = 1L;

	public Entity() {
		// subclasses must have a no-arg constructor
	}

	@Column(name = ID)
	public long id;

	public String getDBId() {
		return String.valueOf(id);
	}

	@Override
	public String toString() {
		return getClass().getName() + ", id: " + id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o != null && getClass() == o.getClass()) {
			return id == ((Entity) o).id;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() + (int) id;
	}

}
