package com.pv.couseae.services;

import com.pv.couseae.entities.PayinRequest;
import com.pv.couseae.entities.PayinRequestCrypto;
import com.pv.couseae.repos.PayinRepo;
import com.pv.couseae.utill.SearchRequest;
import com.pv.couseae.utill.SearchRequestCrypto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayinService {

    private final PayinRepo payinRepo;
    private final MongoTemplate mongoTemplate;

    // ─────────────────────────────────────────────────────
    // Existing — settlement lookup
    // ─────────────────────────────────────────────────────
    public List<PayinRequest> getSuccessPayinForSettlement(String merchantId, String orderStatus) {

        Query query = new Query();

        query.addCriteria(
                Criteria.where("merchantId").is(merchantId)
                        .and("transactionStatus").is(orderStatus)
                        .and("settled").is(false)
        );

        return mongoTemplate.find(query, PayinRequest.class);
    }

    // ─────────────────────────────────────────────────────
    // Transaction listing over payin_requests
    // ─────────────────────────────────────────────────────

    /**
     * Paged search over payin_requests.
     */
    public Page<PayinRequest> PayinTxnSearch(SearchRequest searchRequest) {
        Query query = buildQuery(searchRequest);

        // count all matches before paging
        long total = mongoTemplate.count(Query.of(query), PayinRequest.class);

        Pageable pageable = PageRequest.of(
                searchRequest.getStart(),
                searchRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdOn"));
        query.with(pageable);

        List<PayinRequest> results = mongoTemplate.find(query, PayinRequest.class);
        return new PageImpl<>(results, pageable, total);
    }

    /**
     * Unpaged search for Excel export — same filters, no paging.
     */
    public List<PayinRequest> PayinTxnSearchForExcel(SearchRequest searchRequest) {
        Query query = buildQuery(searchRequest);
        query.with(Sort.by(Sort.Direction.DESC, "createdOn"));
        query.limit(50_000); // guardrail against unbounded export
        return mongoTemplate.find(query, PayinRequest.class);
    }

    /**
     * Crypto payin search.
     */
    public Page<PayinRequestCrypto> PayinCryptoTxnSearch(SearchRequestCrypto searchRequest) {
        Query query = new Query();

        if (searchRequest.getUserName() != null && !searchRequest.getUserName().isEmpty()) {
            query.addCriteria(Criteria.where("merchantId").is(searchRequest.getUserName()));
        }

        long total = mongoTemplate.count(Query.of(query), PayinRequestCrypto.class);

        Pageable pageable = PageRequest.of(
                searchRequest.getStart(),
                searchRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdOn"));
        query.with(pageable);

        List<PayinRequestCrypto> results = mongoTemplate.find(query, PayinRequestCrypto.class);
        return new PageImpl<>(results, pageable, total);
    }

    // ─────────────────────────────────────────────────────
    // Shared query builder
    // ─────────────────────────────────────────────────────
    private Query buildQuery(SearchRequest searchRequest) {
        Query query = new Query();

        if (searchRequest.getUserName() != null && !searchRequest.getUserName().isEmpty()) {
            query.addCriteria(Criteria.where("merchantId").is(searchRequest.getUserName()));
        }

        if (searchRequest.getStatus() != null
                && !searchRequest.getStatus().isEmpty()
                && !"ALL".equalsIgnoreCase(searchRequest.getStatus())) {
            query.addCriteria(Criteria.where("transactionStatus").is(searchRequest.getStatus()));
        }

        if (searchRequest.getType() != null
                && !searchRequest.getType().isEmpty()
                && !"ALL".equalsIgnoreCase(searchRequest.getType())) {
            query.addCriteria(Criteria.where("transactionType").is(searchRequest.getType()));
        }

        // Date range on createdOn (LocalDateTime).
        // If getDateFrom()/getDateTo() are Strings, parse them before use — see note.
        if (searchRequest.getDateFrom() != null && searchRequest.getDateTo() != null) {
            query.addCriteria(Criteria.where("createdOn")
                    .gte(searchRequest.getDateFrom())
                    .lte(searchRequest.getDateTo()));
        } else if (searchRequest.getDateFrom() != null) {
            query.addCriteria(Criteria.where("createdOn").gte(searchRequest.getDateFrom()));
        } else if (searchRequest.getDateTo() != null) {
            query.addCriteria(Criteria.where("createdOn").lte(searchRequest.getDateTo()));
        }

        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().isEmpty()) {
            String kw = searchRequest.getKeyword();
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("orderId").regex(kw, "i"),
                    Criteria.where("payment_id").regex(kw, "i"),
                    Criteria.where("customerEmail").regex(kw, "i"),
                    Criteria.where("customerMobile").regex(kw, "i")
            ));
        }

        return query;
    }
}