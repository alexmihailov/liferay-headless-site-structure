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

import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalService;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.segments.constants.SegmentsEntryConstants;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsEntryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import ru.tele2.liferay.headless.dto.v1_0.Experience;
import ru.tele2.liferay.headless.dto.v1_0.SiteStructurePageItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static com.liferay.segments.constants.SegmentsExperienceConstants.KEY_DEFAULT;

/**
 * @author atyutin
 */
@Component(
	property = "dto.class.name=com.liferay.portal.kernel.model.Layout",
	service = {DTOConverter.class, SiteStructurePageItemDTOConverter.class}
)
public class SiteStructurePageItemDTOConverter implements DTOConverter<Layout, SiteStructurePageItem> {

	public final static String SEGMENTS_EXPERIENCE = "segmentsExperience";
	public final static String SEGMENTS = "segments";
	public final static String LAYOUT = "layout";

	@Override
	public String getContentType() {
		return SiteStructurePageItem.class.getSimpleName();
	}

	@Override
	public SiteStructurePageItem toDTO(DTOConverterContext dtoConverterContext, Layout layout) {
		return new SiteStructurePageItem() {
			{
				SegmentsExperience segmentsExperience = (SegmentsExperience) dtoConverterContext.getAttribute(SEGMENTS_EXPERIENCE);

				setExperience(
					() -> {

						SegmentsEntry segmentsEntry = !KEY_DEFAULT.equals(segmentsExperience.getSegmentsExperienceKey()) ?
							_segmentsEntryService.getSegmentsEntry(segmentsExperience.getSegmentsEntryId()) : null;

						Set<String> _segments = segmentsEntry != null ? Collections.singleton(segmentsEntry.getName(dtoConverterContext.getLocale())) : Collections.emptySet();
						dtoConverterContext.setAttribute(SEGMENTS, _segments);
						return new Experience() {{
							key = segmentsExperience.getSegmentsExperienceKey();
							name = segmentsExperience.getName(dtoConverterContext.getLocale());
							segments = _segments.toArray(new String[0]);
						}};
					}
				);
				setPageDefinition(
					() -> {
						dtoConverterContext.setAttribute(LAYOUT, layout);

						LayoutPageTemplateStructure layoutPageTemplateStructure =
							_layoutPageTemplateStructureLocalService.fetchLayoutPageTemplateStructure(layout.getGroupId(), layout.getPlid());

						if (layoutPageTemplateStructure == null) return null;

						String segmentsExperienceKey = segmentsExperience.getSegmentsExperienceKey();

						LayoutStructure layoutStructure = LayoutStructure.of(layoutPageTemplateStructure.getData(segmentsExperienceKey));

						return _pageDefinitionDTOConverter.toDTO(dtoConverterContext, layoutStructure);
					}
				);
			}
		};
	}

	@Reference
	private SegmentsEntryService _segmentsEntryService;

	@Reference
	private PageDefinitionDTOConverter _pageDefinitionDTOConverter;

	@Reference
	private LayoutPageTemplateStructureLocalService _layoutPageTemplateStructureLocalService;

}