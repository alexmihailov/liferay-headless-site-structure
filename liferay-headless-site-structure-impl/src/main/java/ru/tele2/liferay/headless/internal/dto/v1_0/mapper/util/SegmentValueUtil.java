package ru.tele2.liferay.headless.internal.dto.v1_0.mapper.util;

import com.liferay.asset.list.service.AssetListEntryAssetEntryRelLocalService;
import com.liferay.info.list.provider.item.selector.criterion.InfoListProviderItemSelectorReturnType;
import com.liferay.item.selector.criteria.InfoListItemSelectorReturnType;
import com.liferay.layout.util.structure.CollectionStyledLayoutStructureItem;
import com.liferay.layout.util.structure.LayoutStructure;
import com.liferay.layout.util.structure.LayoutStructureItem;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.service.SegmentsEntryLocalService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.liferay.portal.kernel.search.Field.CLASS_PK;
import static com.liferay.portal.kernel.search.Field.TYPE;
import static com.liferay.segments.constants.SegmentsEntryConstants.ID_DEFAULT;

public class SegmentValueUtil {
    private static final Log _log = LogFactoryUtil.getLog(SegmentValueUtil.class);

    /**
     * Getting segments with parent item, if it CollectionStyledLayoutStructureItem.
     */
    public static Set<String> getSegments(Locale locale, Set<String> parentSegments, LayoutStructure layoutStructure, LayoutStructureItem collectionItem,
                                          AssetListEntryAssetEntryRelLocalService assetListEntryAssetEntryRelLocalService,
                                          SegmentsEntryLocalService segmentsEntryLocalService) {
        Set<String> segments = parentSegments;

        String parentItemId = collectionItem != null ? collectionItem.getParentItemId() : null;

        if (parentItemId != null) {

            LayoutStructureItem parentLayoutStructureItem = layoutStructure.getLayoutStructureItem(parentItemId);

            if (parentLayoutStructureItem != null && parentLayoutStructureItem.getClass().equals(CollectionStyledLayoutStructureItem.class)) {

                CollectionStyledLayoutStructureItem collectionStyledLayoutStructureItem = (CollectionStyledLayoutStructureItem) parentLayoutStructureItem;

                segments = getSegments(locale, collectionStyledLayoutStructureItem,
                    assetListEntryAssetEntryRelLocalService, segmentsEntryLocalService);

            } else {
                segments = getSegments(locale, parentSegments, layoutStructure, parentLayoutStructureItem, assetListEntryAssetEntryRelLocalService, segmentsEntryLocalService);
            }
        }
        return segments;
    }

    /**
     * Getting segments with CollectionStyledLayoutStructureItem.
     */
    public static Set<String> getSegments(Locale locale, CollectionStyledLayoutStructureItem collectionStyledLayoutStructureItem,
                                          AssetListEntryAssetEntryRelLocalService assetListEntryAssetEntryRelLocalService,
                                          SegmentsEntryLocalService segmentsEntryLocalService) {

        JSONObject jsonObject = collectionStyledLayoutStructureItem.getCollectionJSONObject();

        Set<String> segments = new HashSet<>();

        if (jsonObject == null) return segments;

        String type = jsonObject.getString(TYPE);

        if (Validator.isNull(type)) return segments;

        if (Objects.equals(type, InfoListItemSelectorReturnType.class.getName())) {

            long assetListEntryId = jsonObject.getLong(CLASS_PK);

            segments = assetListEntryAssetEntryRelLocalService.getAssetListEntryAssetEntryRels(assetListEntryId, 0, Integer.MAX_VALUE)
                .stream()
                .map(assetListRel -> {
                        String segmentName = null;
                        if (assetListRel.getSegmentsEntryId() != ID_DEFAULT) {
                            try {
                                SegmentsEntry segment = segmentsEntryLocalService.getSegmentsEntry(assetListRel.getSegmentsEntryId());
                                segmentName = segment.getName(locale);
                            } catch (PortalException ex) {
                                _log.error("Error getting segment by id: " + assetListRel.getSegmentsEntryId(), ex);
                            }
                        }
                        return segmentName;
                    }
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        } else if (Objects.equals(type, InfoListProviderItemSelectorReturnType.class.getName())) {
            //TODO: Don't use, need adding analysis.
        }

        return segments;
    }
}
