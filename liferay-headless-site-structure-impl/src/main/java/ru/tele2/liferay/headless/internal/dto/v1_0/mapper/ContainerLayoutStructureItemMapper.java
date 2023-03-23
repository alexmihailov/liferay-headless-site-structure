/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package ru.tele2.liferay.headless.internal.dto.v1_0.mapper;

import com.liferay.layout.util.structure.ContainerStyledLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import org.osgi.service.component.annotations.Component;
import ru.tele2.liferay.headless.dto.v1_0.PageElement;
import ru.tele2.liferay.headless.dto.v1_0.PageSectionDefinition;

import java.util.Locale;
import java.util.Set;

/**
 * @author atyutin
 */
@Component(service = LayoutStructureItemMapper.class)
public class ContainerLayoutStructureItemMapper implements LayoutStructureItemMapper {

	@Override
	public String getClassName() {
		return ContainerStyledLayoutStructureItem.class.getName();
	}

	@Override
	public PageElement getPageElement(LayoutStructure layoutStructure, LayoutStructureItem layoutStructureItem, long groupId,
									  Locale locale, Set<String> _segments) {

		ContainerStyledLayoutStructureItem containerStyledLayoutStructureItem = (ContainerStyledLayoutStructureItem)layoutStructureItem;

		return new PageElement() {{
			definition = new PageSectionDefinition() {{ setName(containerStyledLayoutStructureItem::getName); }};
			segments = _segments.toArray(new String[0]);
			type = Type.SECTION;
		}};
	}
}
