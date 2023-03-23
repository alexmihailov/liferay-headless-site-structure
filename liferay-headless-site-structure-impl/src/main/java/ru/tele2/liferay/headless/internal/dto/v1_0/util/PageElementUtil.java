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

package ru.tele2.liferay.headless.internal.dto.v1_0.util;

import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import ru.tele2.liferay.headless.dto.v1_0.PageElement;
import ru.tele2.liferay.headless.internal.dto.v1_0.mapper.LayoutStructureItemMapper;
import ru.tele2.liferay.headless.internal.dto.v1_0.mapper.LayoutStructureItemMapperRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author atyutin
 */
public class PageElementUtil {

	public static PageElement toPageElement(LayoutStructure layoutStructure, LayoutStructureItem layoutStructureItem,
											LayoutStructureItemMapperRegistry mapperRegistry, long groupId, Locale locale,
											Set<String> segments) {

		PageElement pageElement = _toPageElement(layoutStructure, layoutStructureItem, mapperRegistry, groupId, locale, segments);

		List<String> childrenItemIds = layoutStructureItem.getChildrenItemIds();

		List<PageElement> pageElements = new ArrayList<>();

		for (String childItemId : childrenItemIds) {

			LayoutStructureItem childLayoutStructureItem = layoutStructure.getLayoutStructureItem(childItemId);

			PageElement childPageElement = toPageElement(layoutStructure, childLayoutStructureItem, mapperRegistry, groupId, locale, segments);

			if (childPageElement != null) pageElements.add(childPageElement);
		}

		if ((pageElement != null) && !pageElements.isEmpty()) pageElement.setPageElements(pageElements.toArray(new PageElement[0]));

		return pageElement;
	}

	private static PageElement _toPageElement(LayoutStructure layoutStructure, LayoutStructureItem layoutStructureItem,
											  LayoutStructureItemMapperRegistry layoutStructureItemMapperRegistry,
											  long groupId, Locale locale, Set<String> segments) {

		Class<?> clazz = layoutStructureItem.getClass();

		LayoutStructureItemMapper layoutStructureItemMapper = layoutStructureItemMapperRegistry.getLayoutStructureItemMapper(clazz.getName());

		if (layoutStructureItemMapper == null) return null;

		return layoutStructureItemMapper.getPageElement(layoutStructure, layoutStructureItem, groupId, locale, segments);
	}

}