//package com.pv.couseae.services;
//
//import com.zenithpay.zenithpay.payin.Transaction.models.LineGraphModel;
//import com.zenithpay.zenithpay.payin.Transaction.models.Orders;
//import com.zenithpay.zenithpay.payin.Transaction.models.PayinRequest;
//import com.zenithpay.zenithpay.payin.Transaction.models.PayinRequestCrypto;
//import com.zenithpay.zenithpay.user.models.User;
//import com.zenithpay.zenithpay.utils.SearchRequest;
//import com.zenithpay.zenithpay.utils.SearchRequestCrypto;
//import org.bson.Document;
//import org.springframework.data.domain.Page;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//public interface OrderService {
//    Orders addOrder(Orders orders);
//    void updateOrder(Orders orders);
//    Orders getByOrderId(String orderId);
//    Orders getByAcquirerOrderId(String acquirerOrderId);
//
//    Orders getByMerchantAndOrdRequestId(User merchant, String ordRequestId);
//
//    Orders getOrderByTransactionId(String clientReferenceNo);
//
//    Orders getOrderByOrderRequestId(String orderId);
//
//    Orders getOrderByIdAndMerchant(String orderId, User user);
//    Orders getOrderById(String orderId);
//
//
//    Page<Orders> getAllByMerchant(User user, Integer start, Integer size);
//
//
//    Page<Orders> searchAllOrders(SearchRequest searchRequest);
//
//    Page<PayinRequest> PayinTxnSearch(SearchRequest searchRequest);
//
//    List<PayinRequest> PayinTxnSearchForExcel(SearchRequest searchRequest);
//
//    Page<PayinRequestCrypto> PayinCryptoTxnSearch(SearchRequestCrypto searchRequest);
//
//    List<Orders> searchAllOrdersList(SearchRequest searchRequest);
//
//
//
//    List<LineGraphModel> getSummary(SearchRequest searchRequest) ;
//
//    Document getTodayTransactionStatsByMerchant(SearchRequest req);
//
//    List<Document> getTransactionCountAndSumGroupByStatus(SearchRequest req);
//
//    List<Orders> getOrderByStatus(String string);
//
//    Map<String, String> getPieChartSummary(String userName);
//
//    List<Orders> getAllOrders(SearchRequest searchRequest);
//
//    List<Orders> getCurrentDayCapturedOrders(String userId);
//
//    List<Orders> getPendingOrdersByAcquirerAndTimeRange(String cashfree, LocalDateTime minus, LocalDateTime now);
//
//    List<Orders> getOrdersByAcquirerAndTimeRange(String cashfree, LocalDateTime minus, LocalDateTime now);
//}
