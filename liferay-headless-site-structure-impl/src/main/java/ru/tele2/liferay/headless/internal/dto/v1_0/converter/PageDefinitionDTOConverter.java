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

package ru.tele2.liferay.headless.internal.dto.v1_0.converter;

import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import ru.tele2.liferay.headless.dto.v1_0.PageDefinition;
import ru.tele2.liferay.headless.internal.dto.v1_0.mapper.LayoutStructureItemMapperRegistry;

import java.util.Set;

import static ru.tele2.liferay.headless.internal.dto.v1_0.converter.SiteStructurePageItemDTOConverter.LAYOUT;
import static ru.tele2.liferay.headless.internal.dto.v1_0.converter.SiteStructurePageItemDTOConverter.SEGMENTS;
import static ru.tele2.liferay.headless.internal.dto.v1_0.util.PageElementUtil.toPageElement;

/**
 * @author atyutin
 */
@Component(
	property = "dto.class.name=com.liferay.layout.util.structure.LayoutStructure",
	service = {DTOConverter.class, PageDefinitionDTOConverter.class}
)
public class PageDefinitionDTOConverter implements DTOConverter<LayoutStructure, PageDefinition> {

	@Reference
	private LayoutStructureItemMapperRegistry _layoutStructureItemMapperRegistry;

	@Override
	public String getContentType() {
		return PageDefinition.class.getSimpleName();
	}

	@Override
	public PageDefinition toDTO(DTOConverterContext dtoConverterContext, LayoutStructure layoutStructure) {

		Layout layout = (Layout) dtoConverterContext.getAttribute(LAYOUT);

		if (layout == null) {
			throw new IllegalArgumentException("Layout is not defined for layout structure item " + layoutStructure.getMainItemId());
		}

		LayoutStructureItem mainLayoutStructureItem = layoutStructure.getMainLayoutStructureItem();

		return new PageDefinition() {
			{
				Set<String> segments = (Set<String>) dtoConverterContext.getAttribute(SEGMENTS);

				pageElement = toPageElement(layoutStructure, mainLayoutStructureItem, _layoutStructureItemMapperRegistry,
					layout.getGroupId(), dtoConverterContext.getLocale(), segments
				);
			}
		};
	}
}