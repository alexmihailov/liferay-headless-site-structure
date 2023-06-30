package ru.tele2.liferay.headless.internal.resource.v1_0;

import com.liferay.friendly.url.model.FriendlyURLEntryLocalization;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.change.tracking.CTCollectionThreadLocal;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.segments.model.SegmentsExperience;
import com.liferay.segments.service.SegmentsExperienceLocalService;
import com.liferay.segments.service.SegmentsExperienceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import ru.tele2.liferay.headless.dto.v1_0.SiteStructure;
import ru.tele2.liferay.headless.dto.v1_0.SiteStructurePage;
import ru.tele2.liferay.headless.dto.v1_0.SiteStructurePageItem;
import ru.tele2.liferay.headless.internal.dto.v1_0.converter.SiteStructurePageItemDTOConverter;
import ru.tele2.liferay.headless.internal.resource.v1_0.util.SearchUtil;
import ru.tele2.liferay.headless.resource.v1_0.SiteStructureResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.liferay.portal.kernel.util.GetterUtil.DEFAULT_LONG;
import static ru.tele2.liferay.headless.internal.dto.v1_0.converter.SiteStructurePageItemDTOConverter.SEGMENTS_EXPERIENCE;

/**
 * @author atyutin
 */
@Component(
    properties = "OSGI-INF/liferay/rest/v1_0/site-structure.properties",
    scope = ServiceScope.PROTOTYPE, service = SiteStructureResource.class
)
public class SiteStructureResourceImpl extends BaseSiteStructureResourceImpl {

    @Reference
    private DTOConverterRegistry _dtoConverterRegistry;

    @Reference
    private SiteStructurePageItemDTOConverter _siteStructurePageItemDTOConverter;

    @Reference
    private SegmentsExperienceLocalService _segmentsExperienceLocalService;

    @Reference
    private FriendlyURLEntryLocalService _friendlyURLEntryLocalService;

    @Reference
    private LayoutLocalService _layoutLocalService;

    @Reference
    private SegmentsExperienceService _segmentsExperienceService;

    @Reference
    private Portal _portal;

    @Override
    public SiteStructure getSiteStructure(Long siteId, Long publicationId) throws Exception {
        try (SafeCloseable safeCloseable = CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(publicationId)) {
            List<SiteStructurePage> pages = _layoutLocalService.getLayouts(siteId, false).stream().map(layout -> {
                try {
                    return _getSiteStructurePage(layout);
                } catch (PortalException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            SiteStructure result = new SiteStructure();
            result.setSiteId(siteId);
            result.setPages(pages.toArray(new SiteStructurePage[0]));
            return result;

//            return SearchUtil.search(
//                    siteId,
//                    searchContext -> {
//                        searchContext.setCompanyId(contextCompany.getCompanyId());
//                        searchContext.setGroupIds(new long[]{siteId});
//                    },
//                    document -> {
//                        long plid = GetterUtil.getLong(document.get(Field.ENTRY_CLASS_PK));
//                        Layout layout = _layoutLocalService.getLayout(plid);
//                        return _getSiteStructurePage(layout);
//                    });
        }
    }

    @Override
    public SiteStructurePage getSiteStructurePage(Long siteId, String friendlyUrlPath, Long publicationId) throws Exception {
        try (SafeCloseable safeCloseable = CTCollectionThreadLocal.setCTCollectionIdWithSafeCloseable(publicationId)) {
            Layout layout = _getLayout(siteId, friendlyUrlPath);
            return _getSiteStructurePage(layout);
        }
    }

    private SiteStructurePage _getSiteStructurePage(Layout layout) throws PortalException {
        Layout parentLayout = layout.getParentPlid() != DEFAULT_LONG ? _layoutLocalService.getLayout(layout.getParentPlid()) : null;
        return new SiteStructurePage() {{
            setPage(layout.getHTMLTitle(contextAcceptLanguage.getPreferredLocale()));
            setUrl(layout.getFriendlyURL());
            setSiteId(layout.getGroupId());
            setParentUrl(parentLayout != null ? parentLayout.getFriendlyURL() : null);
            setItems(() -> {
                List<SiteStructurePageItem> siteStructurePageItems = _getSiteStructurePageItems(layout);
                return siteStructurePageItems.toArray(new SiteStructurePageItem[0]);
            });
        }};
    }

    private List<SiteStructurePageItem> _getSiteStructurePageItems(Layout layout) throws Exception {
        List<SegmentsExperience> segmentsExperiences = _getSegmentsExperiences(layout);
        List<SiteStructurePageItem> items = new ArrayList<>();
        for (SegmentsExperience experience : segmentsExperiences) {
            items.add(_toSiteStructurePageItem(layout, experience.getSegmentsExperienceKey()));
        }
        return items;
    }

    private Layout _getLayout(long groupId, String friendlyUrlPath)
        throws Exception {

        String resourceName = ResourceActionsUtil.getCompositeModelName(Layout.class.getName(), "false");
        FriendlyURLEntryLocalization friendlyURLEntryLocalization =
            _friendlyURLEntryLocalService.getFriendlyURLEntryLocalization(
                groupId, _portal.getClassNameId(resourceName),
                StringPool.FORWARD_SLASH + friendlyUrlPath);
        return _layoutLocalService.getLayout(friendlyURLEntryLocalization.getClassPK());
    }

    private List<SegmentsExperience> _getSegmentsExperiences(Layout layout)
        throws Exception {

        if (!layout.isTypeContent()) {
            return Collections.emptyList();
        }

        return _segmentsExperienceLocalService.getSegmentsExperiences(
            layout.getGroupId(), layout.getPlid(), true);
    }

    private SiteStructurePageItem _toSiteStructurePageItem(Layout layout, String segmentsExperienceKey) throws Exception {

        DefaultDTOConverterContext dtoConverterContext =
            new DefaultDTOConverterContext(
                contextAcceptLanguage.isAcceptAllLanguages(), null,
                _dtoConverterRegistry, contextHttpServletRequest,
                layout.getPlid(), contextAcceptLanguage.getPreferredLocale(),
                contextUriInfo, contextUser);

        dtoConverterContext.setAttribute(SEGMENTS_EXPERIENCE, _getSegmentsExperience(layout, segmentsExperienceKey));

        return _siteStructurePageItemDTOConverter.toDTO(dtoConverterContext, layout);
    }

    private SegmentsExperience _getSegmentsExperience(
        Layout layout, String segmentsExperienceKey)
        throws Exception {

        return _segmentsExperienceService.fetchSegmentsExperience(
            layout.getGroupId(), segmentsExperienceKey, layout.getPlid());
    }
}
