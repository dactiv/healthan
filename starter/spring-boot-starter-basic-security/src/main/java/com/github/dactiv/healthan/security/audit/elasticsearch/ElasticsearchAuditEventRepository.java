package com.github.dactiv.healthan.security.audit.elasticsearch;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.id.StringIdEntity;
import com.github.dactiv.healthan.commons.page.Page;
import com.github.dactiv.healthan.commons.page.PageRequest;
import com.github.dactiv.healthan.security.AuditIndexProperties;
import com.github.dactiv.healthan.security.audit.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * es 审计事件仓库实现
 *
 * @author maurice.chen
 */
public class ElasticsearchAuditEventRepository extends AbstractExtendAuditEventRepository {

    public static final String MAPPING_FILE_PATH = "elasticsearch/plugin-audit-mapping.json";

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchAuditEventRepository.class);

    private final ElasticsearchOperations elasticsearchOperations;

    private final IndexGenerator indexGenerator;

    public ElasticsearchAuditEventRepository(List<AuditEventRepositoryInterceptor> interceptors,
                                             ElasticsearchOperations elasticsearchOperations,
                                             AuditIndexProperties auditIndexProperties) {
        super(interceptors);
        this.elasticsearchOperations = elasticsearchOperations;

        this.indexGenerator = new DateIndexGenerator(auditIndexProperties);
    }

    @Override
    public void doAdd(AuditEvent event) {

        IdAuditEvent idAuditEvent = new IdAuditEvent(
                event.getPrincipal(),
                event.getType(),
                event.getData()
        );

        if (IdAuditEvent.class.isAssignableFrom(event.getClass())) {
            idAuditEvent = Casts.cast(event);
        }

        try {

            String index = indexGenerator.generateIndex(idAuditEvent).toLowerCase();

            IndexCoordinates indexCoordinates = IndexCoordinates.of(index);
            IndexOperations indexOperations = elasticsearchOperations.indexOps(indexCoordinates);
            createIndexIfNotExists(indexOperations, MAPPING_FILE_PATH);

            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withId(idAuditEvent.getId())
                    .withObject(idAuditEvent)
                    .build();

            elasticsearchOperations.index(indexQuery, indexCoordinates);

        } catch (Exception e) {
            LOGGER.warn("新增 elasticsearch{} 审计事件出现异常", event.getPrincipal(), e);
        }

    }

    public static void createIndexIfNotExists(IndexOperations indexOperations, String mappingFilePath) throws IOException {
        if (indexOperations.exists()) {
            return ;
        }

        indexOperations.create();
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(mappingFilePath)) {
            Map<String, Object> mapping = Casts.readValue(input, Casts.MAP_TYPE_REFERENCE);
            indexOperations.putMapping(Document.from(mapping));
        }
    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {

        Assert.notNull(after, "查询 elasticsearch 审计数据时 after 参数不能为空");

        String index = getIndexName(after).toLowerCase();

        QueryBuilder queryBuilder = createQueryBuilder(after, type, principal);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSorts(SortBuilders.fieldSort(RestResult.DEFAULT_TIMESTAMP_NAME).order(SortOrder.DESC));

        try {
            return findData(builder.build(), index);
        } catch (Exception e) {
            LOGGER.warn("查询 elasticsearch 审计事件出现异常", e);
            return new LinkedList<>();
        }
    }

    @Override
    public Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type) {

        Assert.notNull(after, "查询 elasticsearch 审计数据分页时 after 参数不能为空");

        String index = getIndexName(after).toLowerCase();

        QueryBuilder queryBuilder = createQueryBuilder(after, type, principal);

        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withSorts(SortBuilders.fieldSort(RestResult.DEFAULT_TIMESTAMP_NAME).order(SortOrder.DESC))
                .withPageable(org.springframework.data.domain.PageRequest.of(pageRequest.getNumber() - 1, pageRequest.getSize()));

        try {
            List<AuditEvent> content = findData(builder.build(), index);
            return new Page<>(pageRequest, content);
        } catch (Exception e) {
            LOGGER.warn("查询 elasticsearch 审计事件出现异常", e);
            return new Page<>(pageRequest, new ArrayList<>());
        }
    }

    public List<AuditEvent> findData(NativeSearchQuery query, String index) {
        return elasticsearchOperations
                .search(query, Map.class, IndexCoordinates.of(index))
                .stream()
                .map(SearchHit::getContent)
                .map(c -> createAuditEvent(Casts.cast(c)))
                .collect(Collectors.toList());
    }

    @Override
    public AuditEvent get(StringIdEntity idEntity) {

        String index = indexGenerator.generateIndex(idEntity).toLowerCase();
        try {
            //noinspection unchecked
            Map<String, Object> map = elasticsearchOperations.get(idEntity.getId(), Map.class, IndexCoordinates.of(index));
            if (MapUtils.isEmpty(map)) {
                return null;
            }
            return createAuditEvent(map);
        } catch (Exception e) {
            LOGGER.warn("通过 ID 查询索引 [{}] 出现错误", index, e);
        }

        return null;
    }

    public String getIndexName(Instant instant) {
        StringIdEntity id = new StringIdEntity();
        id.setCreationTime(java.sql.Date.from(instant));
        return indexGenerator.generateIndex(id).toLowerCase();
    }

    /**
     * 创建查询条件
     *
     * @param after     在什么时间之后的
     * @param type      类型
     * @param principal 操作人
     *
     * @return 查询条件
     */
    private QueryBuilder createQueryBuilder(Instant after, String type, String principal) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        if (StringUtils.isNotBlank(type)) {
            queryBuilder = queryBuilder.must(QueryBuilders.termQuery(IdAuditEvent.TYPE_FIELD_NAME, type));
        }

        if (Objects.nonNull(after)) {
            queryBuilder = queryBuilder.must(QueryBuilders.rangeQuery(RestResult.DEFAULT_TIMESTAMP_NAME).gte(after.getEpochSecond()));
        }

        if (StringUtils.isNotBlank(principal)) {
            queryBuilder = queryBuilder.must(QueryBuilders.termQuery(IdAuditEvent.PRINCIPAL_FIELD_NAME, principal));
        }

        return queryBuilder;
    }

    public ElasticsearchOperations getElasticsearchOperations() {
        return elasticsearchOperations;
    }
}
