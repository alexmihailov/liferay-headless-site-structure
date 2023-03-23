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

import com.liferay.asset.list.service.AssetListEntryAssetEntryRelLocalService;
import com.liferay.layout.util.structure.CollectionItemLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.segments.service.SegmentsEntryLocalService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import ru.tele2.liferay.headless.dto.v1_0.PageElement;
import ru.tele2.liferay.headless.internal.dto.v1_0.mapper.util.SegmentValueUtil;

import java.util.Locale;
import java.util.Set;

/**
 * @author atyutin
 */
@Component(service = LayoutStructureItemMapper.class)
public class CollectionItemLayoutStructureItemMapper implements LayoutStructureItemMapper {

	@Reference
	private SegmentsEntryLocalService segmentsEntryLocalService;

	@Reference
	private AssetListEntryAssetEntryRelLocalService assetListEntryAssetEntryRelLocalService;

	@Override
	public String getClassName() {
		return CollectionItemLayoutStructureItem.class.getName();
	}

	@Override
	public PageElement getPageElement(LayoutStructure layoutStructure, LayoutStructureItem layoutStructureItem, long groupId,
									  Locale locale, Set<String> _segments) {

		CollectionItemLayoutStructureItem collectionItemLayoutStructureItem = (CollectionItemLayoutStructureItem)layoutStructureItem;
		return new PageElement() {{
				segments = SegmentValueUtil.getSegments(locale, _segments, layoutStructure, collectionItemLayoutStructureItem,
					assetListEntryAssetEntryRelLocalService, segmentsEntryLocalService).toArray(new String[0]);
				type = Type.COLLECTION_ITEM;
		}};
	}
}