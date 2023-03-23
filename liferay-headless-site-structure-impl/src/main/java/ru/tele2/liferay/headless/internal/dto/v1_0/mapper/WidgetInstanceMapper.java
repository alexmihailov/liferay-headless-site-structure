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

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.layout.exporter.PortletPreferencesPortletConfigurationExporter;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.portlet.PortletIdCodec;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.service.PortletLocalService;
import com.liferay.portal.kernel.util.Validator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import ru.tele2.liferay.headless.dto.v1_0.WidgetInstance;
import ru.tele2.liferay.headless.internal.dto.v1_0.util.ContentFieldUtil;

import java.util.Locale;
import java.util.Map;

import static com.liferay.portal.kernel.search.Field.ARTICLE_ID;

/**
 * @author atyutin
 */
@Component(service = WidgetInstanceMapper.class)
public class WidgetInstanceMapper {

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private PortletLocalService _portletLocalService;

	@Reference
	private PortletPreferencesPortletConfigurationExporter _portletPreferencesPortletConfigurationExporter;

	@Reference
	private JournalArticleLocalService _journalArticleLocalService;

	public WidgetInstance getWidgetInstance(long groupId, Locale locale, FragmentEntryLink fragmentEntryLink, String portletId) {

		if (Validator.isNull(portletId)) return null;

		String _articleId = _getArticleId(fragmentEntryLink.getPlid(), portletId);

		if (Validator.isNull(_articleId)) return null;

		JournalArticle journalArticle = _getJournalArticle(groupId, _articleId);

		if (journalArticle == null) return null;

		return new WidgetInstance() {{
			articleId = _articleId;
			name = journalArticle.getTitle(locale);
			contentFields = ContentFieldUtil._toContentFields(journalArticle, _journalArticleLocalService, locale);
		}};
	}

	private JournalArticle _getJournalArticle(long groupId, String _articleId) {

		JournalArticle journalArticle = null;

		try {
			journalArticle = _journalArticleLocalService.getArticle(groupId, _articleId);
		} catch (PortalException e) {
			_log.error("Error get article " + _articleId, e);
		}

		return journalArticle;
	}


	private String _getArticleId(long plid, String portletId) {

		Layout layout = _layoutLocalService.fetchLayout(plid);

		if (layout == null) return null;

		String portletName = PortletIdCodec.decodePortletName(portletId);

		Portlet portlet = _portletLocalService.getPortletById(portletName);

		if (portlet == null) return null;

		Map<String, Object> portletConfiguration = _portletPreferencesPortletConfigurationExporter.getPortletConfiguration(plid, portletId);

		Object value = portletConfiguration != null ? portletConfiguration.get(ARTICLE_ID) : null;

		return value != null ?  value.toString() : null;
	}
	private static final Log _log = LogFactoryUtil.getLog(WidgetInstanceMapper.class);

}