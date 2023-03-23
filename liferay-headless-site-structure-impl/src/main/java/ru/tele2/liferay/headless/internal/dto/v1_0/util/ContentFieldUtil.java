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

import com.liferay.dynamic.data.mapping.form.field.type.constants.DDMFormFieldTypeConstants;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormFieldOptions;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.journal.article.dynamic.data.mapping.form.field.type.constants.JournalArticleDDMFormFieldTypeConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.Validator;
import ru.tele2.liferay.headless.dto.v1_0.ContentField;
import ru.tele2.liferay.headless.dto.v1_0.ContentFieldValue;
import ru.tele2.liferay.headless.dto.v1_0.WidgetInstance;

import javax.ws.rs.BadRequestException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author atyutin
 */
public class ContentFieldUtil {

	public static ContentField _toContentField(
		DDMFormFieldValue ddmFormFieldValue,
		JournalArticleLocalService journalArticleService,
		Locale local) {

		DDMFormField ddmFormField = ddmFormFieldValue.getDDMFormField();

		if (ddmFormField == null) return null;

		LocalizedValue localizedValue = ddmFormField.getLabel();

		ContentFieldValue _contentFieldValue = _toContentFieldValue(
			ddmFormField,
			journalArticleService,
			local,
			ddmFormFieldValue.getValue());

		return _contentFieldValue != null ? new ContentField() {{
			contentFieldValue = _contentFieldValue;
			name = ddmFormField.getName();
			dataType = ddmFormField.getType();
			label = localizedValue.getString(local);
		}} : null;
	}

	public static ContentField[] _toContentFields(
		JournalArticle journalArticle,
		JournalArticleLocalService journalArticleService,
		Locale locale) {

		DDMFormValues ddmFormValues = journalArticle.getDDMFormValues();

		return TransformUtil.transformToArray(
			ddmFormValues.getDDMFormFieldValues(),
			ddmFormFieldValue -> _toContentField(ddmFormFieldValue, journalArticleService, locale),
			ContentField.class);
	}

	/**
	 * Available field type values: DATE, RADIO, SELECT,
	 * CHECKBOX_MULTIPLE, JOURNAL_ARTICLE, COLOR, NUMERIC, TEXT, RICH_TEXT.
	 */
	private static ContentFieldValue _getContentFieldValue(
		DDMFormField ddmFormField,
		JournalArticleLocalService journalArticleService,
		Locale locale, String valueString) {

		ContentFieldValue result = null;

		try {

			if (Objects.equals(DDMFormFieldTypeConstants.DATE, ddmFormField.getType())) {
				result = new ContentFieldValue() {{ data = _toDateString(locale, valueString); }};

			} else if (Objects.equals(DDMFormFieldTypeConstants.RADIO, ddmFormField.getType())) {
				// DDMFormFieldOptions ddmFormFieldOptions = ddmFormField.getDDMFormFieldOptions();
				// LocalizedValue selectedOptionLabelLocalizedValue = ddmFormFieldOptions.getOptionLabels(valueString);
				result = new ContentFieldValue() {{ data = valueString; }};

			} else if (Objects.equals(DDMFormFieldTypeConstants.SELECT, ddmFormField.getType()) ||
				Objects.equals(DDMFormFieldTypeConstants.CHECKBOX_MULTIPLE, ddmFormField.getType())) {
				List<String> list = JSONUtil.toStringList(JSONFactoryUtil.createJSONArray(valueString));
//				DDMFormFieldOptions ddmFormFieldOptions = ddmFormField.getDDMFormFieldOptions();
//				Stream<String> stream = list.stream();
//				List<String> values = stream.map(ddmFormFieldOptions::getOptionLabels)
//					.map(localizedValue -> localizedValue.getString(locale))
//					.collect(Collectors.toList());
				result = new ContentFieldValue() {
					{
						setData(
							() -> {
								if (!ddmFormField.isMultiple() && (list.size() == 1)) { return list.get(0); }
								return String.valueOf(JSONFactoryUtil.createJSONArray(list));
							});
					}
				};
			} else if (Objects.equals(ddmFormField.getType(), JournalArticleDDMFormFieldTypeConstants.JOURNAL_ARTICLE)) {

				JSONObject jsonObject = JSONFactoryUtil.createJSONObject(valueString);

				long classPK = jsonObject.getLong("classPK");

				if (classPK != 0) {
					JournalArticle journalArticle = journalArticleService.getLatestArticle(classPK);
					result = new ContentFieldValue() {{
						widgetInstance = new WidgetInstance() {{
							articleId = journalArticle.getArticleId();
							name = journalArticle.getTitle(locale);
							contentFields = _toContentFields(journalArticle, journalArticleService, locale);
						}};
					}
					};
				}

			} else if (
				Objects.equals(ddmFormField.getType(), DDMFormFieldTypeConstants.COLOR) ||
					Objects.equals(ddmFormField.getType(), DDMFormFieldTypeConstants.NUMERIC) ||
					Objects.equals(ddmFormField.getType(), DDMFormFieldTypeConstants.TEXT) ||
					Objects.equals(ddmFormField.getType(), DDMFormFieldTypeConstants.RICH_TEXT)
			) {
				result = new ContentFieldValue() {{ data = valueString; }};
			}

			return result;
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) { _log.warn(exception); }
			return null;
		}
	}

	private static ContentFieldValue _toContentFieldValue(
		DDMFormField ddmFormField, JournalArticleLocalService journalArticleService,
		Locale locale, Value value
	) {
		if (value == null) return new ContentFieldValue();
		String valueString = String.valueOf(value.getString(locale));
		return _getContentFieldValue(ddmFormField, journalArticleService, locale, valueString);
	}

	private static String _toDateString(Locale locale, String value) {
		if (Validator.isNull(value)) return "";
		try {
			return DateUtil.getDate(
				DateUtil.parseDate("yyyy-MM-dd", value, locale),
				"yyyy-MM-dd'T'HH:mm:ss'Z'",
				locale,
				TimeZone.getTimeZone("UTC")
			);
		}
		catch (ParseException parseException) {
			throw new BadRequestException("Unable to parse date that does not conform to ISO-8601", parseException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(ContentFieldUtil.class);
}
