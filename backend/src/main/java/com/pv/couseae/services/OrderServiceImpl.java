//package com.pv.couseae.services;
//
//import com.zenithpay.zenithpay.enums.TransactionStatus;
//import com.zenithpay.zenithpay.payin.Transaction.models.LineGraphModel;
//import com.zenithpay.zenithpay.payin.Transaction.models.Orders;
//import com.zenithpay.zenithpay.payin.Transaction.models.PayinRequest;
//import com.zenithpay.zenithpay.payin.Transaction.models.PayinRequestCrypto;
//import com.zenithpay.zenithpay.payin.Transaction.repositories.OrderRepo;
//import com.zenithpay.zenithpay.user.models.User;
//import com.zenithpay.zenithpay.user.services.UserService;
//import com.zenithpay.zenithpay.utils.SearchRequest;
//import com.zenithpay.zenithpay.utils.SearchRequestCrypto;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.bson.Document;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.aggregation.*;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@AllArgsConstructor
//public class OrderServiceImpl implements OrderService {
//    private OrderRepo orderRepo;
//    private UserService userService;
//    private MongoTemplate mongoTemplate;
//
//    @Override
//    public Orders addOrder(Orders orders) {
//        return this.orderRepo.save(orders);
//    }
//
//    @Override
//    public void updateOrder(Orders orders) {
//        this.orderRepo.save(orders);
//    }
//
//    @Override
//    public Page<Orders> searchAllOrders(SearchRequest searchRequest) {
//        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
//        String currencyCode = searchRequest.getCurrencyCode();
//
//        if (searchRequest.getUserName().isEmpty()) {
//            if (searchRequest.getStatus().equalsIgnoreCase("ALL"))
//                return this.orderRepo.findAllByCurrencyCodeAndCreatedDateBetween(currencyCode, searchRequest.getDateFrom(), searchRequest.getDateTo(), pageable);
//            else if (searchRequest.getStatus().equalsIgnoreCase("SUCCESS"))
//                return this.orderRepo.findAllByCurrencyCodeAndCreatedDateBetweenAndTransactionStatus(currencyCode, searchRequest.getDateFrom(), searchRequest.getDateTo(), TransactionStatus.CAPTURED, pageable);
//            else if (searchRequest.getStatus().equalsIgnoreCase("FAILED")) {
//                List<String> statuses = List.of(TransactionStatus.FAILED.toString(), TransactionStatus.REJECTED.toString(), TransactionStatus.CANCELLED.toString());
//                return this.orderRepo.findAllByCurrencyCodeAndCreatedDateBetweenAndTransactionStatusIn(currencyCode, searchRequest.getDateFrom(), searchRequest.getDateTo(), statuses, pageable);
//            } else {
//                List<String> statuses = List.of(TransactionStatus.FAILED.toString(), TransactionStatus.SUCCESS.toString(), TransactionStatus.REJECTED.toString(), TransactionStatus.CANCELLED.toString());
//                return this.orderRepo.findAllByCurrencyCodeAndCreatedDateBetweenAndTransactionStatusNotIn(currencyCode, searchRequest.getDateFrom(), searchRequest.getDateTo(), TransactionStatus.CAPTURED, pageable);
//            }
//        } else {
//            if (searchRequest.getStatus().equalsIgnoreCase("ALL"))
//                return this.orderRepo.findAllByCurrencyCodeAndMerchantAndCreatedDateBetween(currencyCode, new User(searchRequest.getUserName()), searchRequest.getDateFrom(), searchRequest.getDateTo(), pageable);
//            else if (searchRequest.getStatus().equalsIgnoreCase("SUCCESS"))
//                return this.orderRepo.findAllByCurrencyCodeAndMerchantAndCreatedDateBetweenAndTransactionStatus(currencyCode, new User(searchRequest.getUserName()), searchRequest.getDateFrom(), searchRequest.getDateTo(), TransactionStatus.CAPTURED, pageable);
//            else if (searchRequest.getStatus().equalsIgnoreCase("FAILED")) {
//                List<String> statuses = List.of(TransactionStatus.FAILED.toString(), TransactionStatus.REJECTED.toString(), TransactionStatus.CANCELLED.toString());
//                return this.orderRepo.findAllByCurrencyCodeAndMerchantAndCreatedDateBetweenAndTransactionStatusIn(currencyCode, new User(searchRequest.getUserName()), searchRequest.getDateFrom(), searchRequest.getDateTo(), statuses, pageable);
//            } else {
//                List<String> statuses = List.of(TransactionStatus.FAILED.toString(), TransactionStatus.SUCCESS.toString(), TransactionStatus.REJECTED.toString(), TransactionStatus.CANCELLED.toString());
//                return this.orderRepo.findAllByCurrencyCodeAndMerchantAndCreatedDateBetweenAndTransactionStatusNotIn(currencyCode, new User(searchRequest.getUserName()), searchRequest.getDateFrom(), searchRequest.getDateTo(), TransactionStatus.CAPTURED, pageable);
//            }
//        }
//    }
//
//    @Override
//    public Page<PayinRequest> PayinTxnSearch(SearchRequest searchRequest) {
//        Query query = new Query();
//        Criteria criteria = new Criteria();
//        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
//        query.with(pageable);
//        if (searchRequest != null && searchRequest.getDateFrom()!=null  && searchRequest.getDateTo() != null) {
//            criteria = criteria.and("createdOn").gte(searchRequest.getDateFrom()).lte(searchRequest.getDateTo());
//        }
//        if (searchRequest != null && !searchRequest.getOrderId().isEmpty()) {
//            criteria = criteria.and("_id").is(searchRequest.getOrderId());
//        }
//        if (searchRequest != null && !searchRequest.getTransactionId().isEmpty()) {
//            criteria = criteria.and("payment_id").is(searchRequest.getTransactionId());
//        }
//        if (searchRequest != null && !searchRequest.getUserName().isEmpty()) {
//            criteria = criteria.and("merchantId").is(searchRequest.getUserName());
//        }
//        if (searchRequest != null && !searchRequest.getAcquirerCode().isEmpty()) {
//            criteria = criteria.and("aggregatorCode").is(searchRequest.getAcquirerCode());
//        }
//        if (searchRequest != null && !searchRequest.getStatus().isEmpty() && !searchRequest.getStatus().equalsIgnoreCase("ALL")) {
//            criteria = criteria.and("transactionStatus").is(searchRequest.getStatus());
//        }
////        if (searchRequest != null && !searchRequest.get.isEmpty()) {
////            criteria = criteria.and("settled").is(callbackSent);
////        }
//
//        query.addCriteria(criteria);
//        List<PayinRequest> list = mongoTemplate.find(query, PayinRequest.class);
//        long count = mongoTemplate.count(query.skip(-1).limit(-1), PayinRequest.class);
//
//        return new PageImpl<>(list, pageable, count);
//    }
//    @Override
//    public List<PayinRequest> PayinTxnSearchForExcel(SearchRequest searchRequest) {
//        Query query = new Query();
//        Criteria criteria = new Criteria();
//
//        if (searchRequest != null && searchRequest.getDateFrom()!=null  && searchRequest.getDateTo() != null) {
//            criteria = criteria.and("createdOn").gte(searchRequest.getDateFrom()).lte(searchRequest.getDateTo());
//        }
//        if (searchRequest != null && !searchRequest.getOrderId().isEmpty()) {
//            criteria = criteria.and("_id").is(searchRequest.getOrderId());
//        }
//        if (searchRequest != null && !searchRequest.getTransactionId().isEmpty()) {
//            criteria = criteria.and("payment_id").is(searchRequest.getTransactionId());
//        }
//        if (searchRequest != null && !searchRequest.getUserName().isEmpty()) {
//            criteria = criteria.and("merchantId").is(searchRequest.getUserName());
//        }
//        if (searchRequest != null && !searchRequest.getAcquirerCode().isEmpty()) {
//            criteria = criteria.and("aggregatorCode").is(searchRequest.getAcquirerCode());
//        }
//        if (searchRequest != null && !searchRequest.getStatus().isEmpty() && !searchRequest.getStatus().equalsIgnoreCase("ALL")) {  // ✅ skip filter if "ALL"
//            criteria = criteria.and("transactionStatus").is(searchRequest.getStatus());
//        }
////        if (searchRequest != null && !searchRequest.get.isEmpty()) {
////            criteria = criteria.and("settled").is(callbackSent);
////        }
//        query.addCriteria(criteria);
//       return mongoTemplate.find(query, PayinRequest.class);
//    }
//    @Override
//    public Page<PayinRequestCrypto> PayinCryptoTxnSearch(SearchRequestCrypto searchRequest) {
//        Query query = new Query();
//        Criteria criteria = new Criteria();
//        Pageable pageable = PageRequest.of(searchRequest.getStart(), searchRequest.getSize());
//        query.with(pageable);
//        if (searchRequest != null && searchRequest.getDateFrom()!=null  && searchRequest.getDateTo() != null) {
//            criteria = criteria.and("createdDate").gte(searchRequest.getDateFrom()).lte(searchRequest.getDateTo());
//        }
//        if (searchRequest != null && !searchRequest.getOrderId().isEmpty()) {
//            criteria = criteria.and("orderId").is(searchRequest.getOrderId());
//        }
//        if (searchRequest != null && !searchRequest.getTransactionId().isEmpty()) {
//            criteria = criteria.and("payment_id").is(searchRequest.getTransactionId());
//        }
//        if (searchRequest != null && !searchRequest.getUserName().isEmpty()) {
//            criteria = criteria.and("merchantId").is(searchRequest.getUserName());
//        }
//        if (searchRequest != null && !searchRequest.getCryptoType().isEmpty()) {
//            criteria = criteria.and("cryptoType").is(searchRequest.getCryptoType());
//        }
//        if (searchRequest != null && !searchRequest.getStatus().isEmpty() && !searchRequest.getStatus().equalsIgnoreCase("ALL")) {
//            criteria = criteria.and("status").is(searchRequest.getStatus());
//        }
////        if (searchRequest != null && !searchRequest.get.isEmpty()) {
////            criteria = criteria.and("settled").is(callbackSent);
////        }
//
//        query.addCriteria(criteria);
//        List<PayinRequestCrypto> list = mongoTemplate.find(query, PayinRequestCrypto.class);
//        long count = mongoTemplate.count(query.skip(-1).limit(-1), PayinRequestCrypto.class);
//
//        return new PageImpl<>(list, pageable, count);
//    }
//    @Override
//    public List<Orders> searchAllOrdersList(SearchRequest searchRequest) {
//        Pageable pageable = PageRequest.of(0, 200000);
//        String currencyCode = searchRequest.getCurrencyCode();
//        Page<Orders> orders = null;
//        if (searchRequest.getUserName().isEmpty()) {
//            if (searchRequest.getStatus().equalsIgnoreCase("ALL"))
//                orders = this.orderRepo.findAllByCurrencyCodeAndCreatedDateBetween(currencyCode, searchRequest.getDateFrom(), searchRequest.getDateTo(), pageable);
//            else if (searchRequest.getStatus().equalsIgnoreCase("SUCCESS"))
//                orders = this.orderRepo.findAllByCurrencyCodeAndCreatedDateBetweenAndTransactionStatus(currencyCode, searchRequest.getDateFrom(), searchRequest.getDateTo(), TransactionStatus.CAPTURED, pageable);
//            else if (searchRequest.getStatus().equalsIgnoreCase("FAILED")) {
//                List<String> statuses = List.of(TransactionStatus.FAILED.toString(), TransactionStatus.REJECTED.toString(), TransactionStatus.CANCELLED.toString());
//                orders = this.orderRepo.findAllByCurrencyCodeAndCreatedDateBetweenAndTransactionStatusIn(currencyCode, searchRequest.getDateFrom(), searchRequest.getDateTo(), statuses, pageable);
//            } else {
//                List<String> statuses = List.of(TransactionStatus.FAILED.toString(), TransactionStatus.SUCCESS.toString(), TransactionStatus.REJECTED.toString(), TransactionStatus.CANCELLED.toString());
//                orders = this.orderRepo.findAllByCurrencyCodeAndCreatedDateBetweenAndTransactionStatusNotIn(currencyCode, searchRequest.getDateFrom(), searchRequest.getDateTo(), TransactionStatus.CAPTURED, pageable);
//            }
//        } else {
//            if (searchRequest.getStatus().equalsIgnoreCase("ALL"))
//                orders = this.orderRepo.findAllByCurrencyCodeAndMerchantAndCreatedDateBetween(currencyCode, new User(searchRequest.getUserName()), searchRequest.getDateFrom(), searchRequest.getDateTo(), pageable);
//            else if (searchRequest.getStatus().equalsIgnoreCase("SUCCESS"))
//                orders = this.orderRepo.findAllByCurrencyCodeAndMerchantAndCreatedDateBetweenAndTransactionStatus(currencyCode, new User(searchRequest.getUserName()), searchRequest.getDateFrom(), searchRequest.getDateTo(), TransactionStatus.CAPTURED, pageable);
//            else if (searchRequest.getStatus().equalsIgnoreCase("FAILED")) {
//                List<String> statuses = List.of(TransactionStatus.FAILED.toString(), TransactionStatus.REJECTED.toString(), TransactionStatus.CANCELLED.toString());
//                orders = this.orderRepo.findAllByCurrencyCodeAndMerchantAndCreatedDateBetweenAndTransactionStatusIn(currencyCode, new User(searchRequest.getUserName()), searchRequest.getDateFrom(), searchRequest.getDateTo(), statuses, pageable);
//            } else {
//                List<String> statuses = List.of(TransactionStatus.FAILED.toString(), TransactionStatus.SUCCESS.toString(), TransactionStatus.REJECTED.toString(), TransactionStatus.CANCELLED.toString());
//                orders = this.orderRepo.findAllByCurrencyCodeAndMerchantAndCreatedDateBetweenAndTransactionStatusNotIn(currencyCode, new User(searchRequest.getUserName()), searchRequest.getDateFrom(), searchRequest.getDateTo(), TransactionStatus.CAPTURED, pageable);
//            }
//        }
//        return orders.getContent().isEmpty() ? List.of() : orders.getContent();
//    }
//
//    @Override
//    public Orders getByOrderId(String orderId) {
//        return this.orderRepo.findById(orderId).orElse(null);
//    }
//
//    @Override
//    public Orders getByAcquirerOrderId(String acquirerOrderId) {
//        return this.orderRepo.findByAcquirerOrderId(acquirerOrderId);
//    }
//
//    @Override
//    public Orders getByMerchantAndOrdRequestId(User merchant, String ordRequestId) {
////        return this.orderRepo.findByOrdRequestId(ordRequestId);
//        return this.orderRepo.findByMerchantAndOrdRequestId(merchant, ordRequestId);
//    }
//
//    @Override
//    public Orders getOrderByTransactionId(String transactionId) {
//        return this.orderRepo.findByTransactionId(transactionId);
//    }
//
//    @Override
//    public Orders getOrderByOrderRequestId(String orderId) {
//        return this.orderRepo.findByOrdRequestId(orderId);
//    }
//
//    @Override
//    public List<LineGraphModel> getSummary(SearchRequest searchRequest) {
//        if (searchRequest.getUserName().isEmpty())
//            return this.orderRepo.getOrderSummaryByDateRange(searchRequest.getDateFrom(), searchRequest.getDateTo(), searchRequest.getCurrencyCode());
//        else
//            return this.orderRepo.getOrderSummaryByDateRange(searchRequest.getUserName(), searchRequest.getDateFrom(), searchRequest.getDateTo(), searchRequest.getCurrencyCode());
//
//    }
//
//    @Override
//    public Document getTodayTransactionStatsByMerchant(SearchRequest req) {
//        // Step 1: Get all merchant IDs created by the user
//        Query userQuery = new Query();
//        userQuery.addCriteria(Criteria.where("role").is("MERCHANT")
//                .and("createdBy").is(req.getUserName()));
//        userQuery.fields().include("_id");
//
//        List<User> users = mongoTemplate.find(userQuery, User.class, "users");
//        if (users.isEmpty()) {
//            // No merchants found for this user
//            return new Document("totalCount", 0).append("totalAmount", 0.0);
//        }
//
//        List<String> merchantIds = users.stream()
//                .map(User::getUserId)
//                .collect(Collectors.toList());
//
//        // Step 2: Build aggregation for today's transactions
//        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
//        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
//        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
//
//        MatchOperation match = Aggregation.match(
//                Criteria.where("merchant.$id").in(merchantIds)
//                        .and("createdDate").gte(startOfDay).lt(endOfDay)
//        );
//
//        GroupOperation group = Aggregation.group().count().as("totalCount")
//                .sum("payableAmount").as("totalAmount");
//
//        Aggregation aggregation = Aggregation.newAggregation(match, group);
//
//        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "orders", Document.class);
//
//        Document stats = results.getUniqueMappedResult();
//
//        // Return 0 if no transactions found
//        if (stats == null) {
//            stats = new Document("totalCount", 0).append("totalAmount", 0.0);
//        }
//
//        return stats;
//    }
//    @Override
//    public List<Document> getTransactionCountAndSumGroupByStatus(SearchRequest req) {
//        // Step 1: Get all merchant IDs created by the user
//        List<String> merchantIds=null;
//        Query userQuery = new Query();
//        if(req.getUserName() != null){
//            userQuery.addCriteria(Criteria.where("role").is("MERCHANT")
//                    .and("createdBy").is(req.getUserName()));
//            userQuery.fields().include("_id");
//
//            List<User> users = mongoTemplate.find(userQuery, User.class, "users");
//            if (users.isEmpty()) {
//                // No merchants found for this user
//                  merchantIds = users.stream()
//                        .map(User::getUserId)
//                        .collect(Collectors.toList());
//            }
//log.info("List of merchantIds: "+merchantIds);
//        }
//
//        Criteria criteria = new Criteria();
//
//        // Merchant filter (optional)
//        if (merchantIds != null && !merchantIds.isEmpty()) {
//            criteria = criteria.and("merchant.$ref").is("users")
//                    .and("merchant.$id").is(merchantIds);
//        }
//        // Conditionally add date filter
//        if (req.getDateFrom() != null && req.getDateTo() != null) {
//            criteria = criteria.and("createdDate").gte(req.getDateFrom()).lte(req.getDateTo());
//        }
////        else if (dateFrom != null) {
////            criteria = criteria.and("createdDate").gte(dateFrom);
////        } else if (dateTo != null) {
////            criteria = criteria.and("createdDate").lte(dateTo);
////        }
//
//        // Match stage
//        MatchOperation matchStage = Aggregation.match(criteria);
//
//        // Group stage
//        GroupOperation groupStage = Aggregation.group("transactionStatus")
//                .count().as("totalTransactions")
//                .sum("payableAmount").as("totalPayableAmount");
//
//        // Project stage
//        ProjectionOperation projectStage = Aggregation.project()
//                .and("_id").as("transactionStatus")
//                .andInclude("totalTransactions", "totalPayableAmount");
//
//        // Build pipeline
//        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);
//
//        // Execute and return as List<Document>
//        return mongoTemplate.aggregate(aggregation, "orders", Document.class).getMappedResults();
//    }
//
//
//    @Override
//    public List<Orders> getOrderByStatus(String string) {
//        return this.orderRepo.findByTransactionStatus(string);
//    }
//
//    @Override
//    public Map<String, String> getPieChartSummary(String userName) {
//
//        return null;
//    }
//
//    @Override
//    public List<Orders> getAllOrders(SearchRequest searchRequest) {
//        List<String> transactionStatus = new ArrayList<>();
//        if (searchRequest.getStatus().equalsIgnoreCase("Success")) {
//            transactionStatus.add(TransactionStatus.SUCCESS.toString());
//            transactionStatus.add(TransactionStatus.CAPTURED.toString());
//        } else if (searchRequest.getStatus().equalsIgnoreCase("Failed")) {
//            transactionStatus.add(TransactionStatus.FAILED.toString());
//            transactionStatus.add(TransactionStatus.REJECTED.toString());
//            transactionStatus.add(TransactionStatus.CANCELLED.toString());
//        } else if (searchRequest.getStatus().equalsIgnoreCase("Pending")) {
//            transactionStatus.add(TransactionStatus.PENDING.toString());
//            transactionStatus.add(TransactionStatus.AUTHENTICATED.toString());
//            transactionStatus.add(TransactionStatus.AUTHORISED.toString());
//
//        } else {
//            transactionStatus.add(TransactionStatus.SUCCESS.toString());
//            transactionStatus.add(TransactionStatus.FAILED.toString());
//            transactionStatus.add(TransactionStatus.PENDING.toString());
//            transactionStatus.add(TransactionStatus.ATTEMPTED.toString());
//            transactionStatus.add(TransactionStatus.CAPTURED.toString());
//            transactionStatus.add(TransactionStatus.REJECTED.toString());
//            transactionStatus.add(TransactionStatus.CANCELLED.toString());
//            transactionStatus.add(TransactionStatus.AUTHENTICATED.toString());
//            transactionStatus.add(TransactionStatus.AUTHORISED.toString());
//        }
//
//        if (searchRequest.getUserName().isEmpty()) {
//            return this.orderRepo.findByCurrencyCodeAndCreatedDateBetweenAndTransactionStatusIn(searchRequest.getCurrencyCode(), searchRequest.getDateFrom(), searchRequest.getDateTo(), transactionStatus);
//        } else {
//            return this.orderRepo.findByMerchantAndCurrencyCodeAndCreatedDateBetweenAndTransactionStatusIn(searchRequest.getUserName(), searchRequest.getCurrencyCode(), searchRequest.getDateFrom(), searchRequest.getDateTo(), transactionStatus);
//        }
//    }
//
//    @Override
//    public List<Orders> getCurrentDayCapturedOrders(String userId) {
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.setUserName(userId);
//        TransactionStatus transactionStatus = TransactionStatus.CAPTURED;
//        return this.orderRepo.findByMerchantAndCreatedDateBetweenAndTransactionStatus(searchRequest.getUserName(), searchRequest.getDateFrom(), searchRequest.getDateTo(), transactionStatus);
//    }
//
//    public List<Orders> getPendingOrdersByAcquirerAndTimeRange(String acquirerCode, LocalDateTime startTime, LocalDateTime endTime) {
//        return orderRepo.findByAcquirerCodeAndTransactionStatusAndCreatedDateBetween(acquirerCode, TransactionStatus.PENDING.toString(), startTime, endTime);
//    }
//
//    public List<Orders> getOrdersByAcquirerAndTimeRange(String acquirerCode, LocalDateTime startTime, LocalDateTime endTime) {
//        return orderRepo.findByAcquirerCodeAndCreatedDateBetween(acquirerCode, startTime, endTime);
//    }
//
//
//    @Override
//    public Page<Orders> getAllByMerchant(User user, Integer start, Integer size) {
//        Pageable pageable = PageRequest.of(start, size);
//        return this.orderRepo.findAllByMerchant(user, pageable);
//    }
//
//    @Override
//    public Orders getOrderByIdAndMerchant(String orderId, User user) {
//        return this.orderRepo.findByOrderIdAndMerchant(orderId, user);
//    }
//
//    @Override
//    public Orders getOrderById(String orderId) {
//        return this.orderRepo.findById(orderId).orElse(null);
//    }
//
//}
