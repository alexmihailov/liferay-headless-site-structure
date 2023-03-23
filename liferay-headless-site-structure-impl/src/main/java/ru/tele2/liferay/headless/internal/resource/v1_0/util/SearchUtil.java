package ru.tele2.liferay.headless.internal.resource.v1_0.util;

import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.search.BooleanClause;
import com.liferay.portal.kernel.search.BooleanClauseFactoryUtil;
import com.liferay.portal.kernel.search.BooleanClauseOccur;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.search.filter.TermFilter;
import com.liferay.portal.kernel.search.generic.BooleanQueryImpl;
import com.liferay.portal.kernel.search.generic.MatchAllQuery;
import com.liferay.portal.vulcan.util.SearchUtil.SearchContext;
import ru.tele2.liferay.headless.dto.v1_0.SiteStructure;
import ru.tele2.liferay.headless.dto.v1_0.SiteStructurePage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author atyutin
 */
public class SearchUtil {

    public static SiteStructure search(long _siteId, UnsafeConsumer<SearchContext, Exception> searchContextUnsafeConsumer,
                                       UnsafeFunction<Document, SiteStructurePage, Exception> transformUnsafeFunction) throws Exception {

        Indexer<Layout> indexer = IndexerRegistryUtil.getIndexer(Layout.class.getName());

        UnsafeConsumer<BooleanQuery, Exception> booleanQueryUnsafeConsumer = booleanQuery -> {
            BooleanFilter booleanFilter = booleanQuery.getPreBooleanFilter();
            booleanFilter.add(new TermFilter(Field.GROUP_ID, String.valueOf(_siteId)), BooleanClauseOccur.MUST);
        };

        SearchContext searchContext = createSearchContext(
            searchContextUnsafeConsumer,
            getBooleanClause(booleanQueryUnsafeConsumer),
            queryConfig -> queryConfig.setSelectedFieldNames(Field.ENTRY_CLASS_PK)
        );

        searchContextUnsafeConsumer.accept(searchContext);

        Hits hits = indexer.search(searchContext);

        List<SiteStructurePage> siteStructurePages = new ArrayList<>();

        for (Document document : hits.getDocs()) {

            SiteStructurePage item = transformUnsafeFunction.apply(document);

            if (item != null) siteStructurePages.add(item);
        }
        return !siteStructurePages.isEmpty() ?
            new SiteStructure() {{
                siteId = _siteId;
                pages = siteStructurePages.toArray(new SiteStructurePage[0]); }}:
            new SiteStructure();
    }


    private static SearchContext createSearchContext(UnsafeConsumer<SearchContext, Exception> searchContextUnsafeConsumer,
                                                     BooleanClause<?> booleanClause,
        UnsafeConsumer<QueryConfig, Exception> queryConfigUnsafeConsumer) throws Exception {

        SearchContext searchContext = new com.liferay.portal.vulcan.util.SearchUtil.SearchContext();

        searchContext.setBooleanClauses(new BooleanClause[] { booleanClause });

        searchContext.setSorts(new Sort(Field.ENTRY_CLASS_PK, Sort.LONG_TYPE, false));

        searchContextUnsafeConsumer.accept(searchContext);

        QueryConfig queryConfig = searchContext.getQueryConfig();

        queryConfig.setHighlightEnabled(false);

        queryConfig.setScoreEnabled(false);

        queryConfigUnsafeConsumer.accept(queryConfig);

        return searchContext;
    }

    private static BooleanClause<?> getBooleanClause(UnsafeConsumer<BooleanQuery, Exception> booleanQueryUnsafeConsumer) throws Exception {
        BooleanQuery booleanQuery = new BooleanQueryImpl() {
            {
                add(new MatchAllQuery(), BooleanClauseOccur.MUST);
                BooleanFilter booleanFilter = new BooleanFilter();
                setPreBooleanFilter(booleanFilter);
            }
        };

        booleanQueryUnsafeConsumer.accept(booleanQuery);

        return BooleanClauseFactoryUtil.create(booleanQuery, BooleanClauseOccur.MUST.getName());
    }

}
