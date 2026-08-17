// Seed UAT (zenithuat) users, roles, and related collections for local API tests.
// Login password for seeded users: Test@1234
const HASH = "$2a$10$n1ySu.pt0u5axIT8UNX6k.BjY2ngVLP7PSXRWaYBUSUhWXcm7ordy";
const now = new Date();
const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
const lastWeek = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

function upsert(coll, id, doc) {
  db.getCollection(coll).updateOne(
    { _id: id },
    { $set: Object.assign({ _id: id }, doc) },
    { upsert: true }
  );
}

function userDoc(id, fullName, phone, role, extra) {
  const base = {
    userId: id,
    contactNumber: phone,
    isContactVerified: true,
    isEmailVerified: true,
    accountBalance: 0,
    holdBalance: NumberDecimal("0"),
    fullName: fullName,
    businessName: fullName,
    password: HASH,
    isVerified: true,
    role: role,
    status: true,
    isAccountNonExpired: true,
    isCrypto: false,
    isAccountNonLocked: true,
    isCredentialsNonExpired: true,
    isPayoutEnabled: true,
    isPayoutEnabledViaApp: true,
    isPayoutBlocked: false,
    isPayinEnabled: true,
    isPayoutGstEnabled: false,
    isPayinGstEnabled: false,
    isFeeReturnOnRefund: false,
    createdDate: now,
    lastModifiedDate: now,
    _class: "com.pv.couseae.entities.User"
  };
  return Object.assign(base, extra || {});
}

function userRef(id) {
  return { $ref: "users", $id: id };
}

// JWT from production dashboard uses this subject — must exist or merchant/list NPEs.
upsert("users", "admin@courseae.com", userDoc(
  "admin@courseae.com", "CourseAE Admin", "9999990000", "ADMIN",
  { businessName: "CourseAE" }
));

upsert("users", "testadmin@local.test", userDoc(
  "testadmin@local.test", "Test Admin", "9999999999", "ADMIN",
  { businessName: "Couseae Admin" }
));
upsert("users", "testsubadmin@local.test", userDoc(
  "testsubadmin@local.test", "Test Subadmin", "9999999998", "SUBADMIN", {}
));
upsert("users", "testfinance@local.test", userDoc(
  "testfinance@local.test", "Test Finance", "9999999994", "FINANCE", {}
));
upsert("users", "testauditor@local.test", userDoc(
  "testauditor@local.test", "Test Auditor", "9999999993", "AUDITOR", {}
));
upsert("users", "testviewer@local.test", userDoc(
  "testviewer@local.test", "Test Merchant Viewer", "9999999992", "MERCHANT_VIEWER",
  { createdBy: "testmerchant@local.test" }
));
upsert("users", "testreseller@local.test", userDoc(
  "testreseller@local.test", "Test Reseller", "9999999996", "RESELLER", {}
));
upsert("users", "testmerchant@local.test", userDoc(
  "testmerchant@local.test", "Test Merchant", "9999999997", "MERCHANT",
  {
    businessName: "Local Test Merchant",
    appKey: "test-merchant-app",
    secretKey: "test-merchant-secret",
    accountBalance: 10000
  }
));
upsert("users", "testsubmerchant@local.test", userDoc(
  "testsubmerchant@local.test", "Test Submerchant", "9999999995", "SUBMERCHANT",
  { createdBy: "testmerchant@local.test" }
));
upsert("users", "devendra@payvang.com", userDoc(
  "devendra@payvang.com", "Devendra Sharma", "9716184021", "MERCHANT",
  {
    businessName: "Payvang Test Merchant",
    appKey: "ZEpNIaTHy20260712080032636",
    secretKey: "NCb+aCsOPig2sbMDbjhOP4xnd2ZPuzjt1f6s6o2nB4g=",
    accountBalance: 10000
  }
));

[
  ["shopone@local.test", "Shop One", "9999990001", "Shop One Pvt"],
  ["shoptwo@local.test", "Shop Two", "9999990002", "Shop Two LLP"],
  ["shopthree@local.test", "Shop Three", "9999990003", "Shop Three Inc"]
].forEach(function (row, i) {
  upsert("users", row[0], userDoc(row[0], row[1], row[2], "MERCHANT", {
    businessName: row[3],
    appKey: "app-" + row[0].split("@")[0],
    secretKey: "secret-" + row[0].split("@")[0],
    accountBalance: 2500 + i * 500
  }));
});

upsert("currency", "INR", {
  currencyId: "INR",
  currencyName: "Indian Rupee",
  currencyCode: "INR",
  currencyDecimalPlace: 2,
  symbol: "Rs",
  _class: "com.pv.couseae.entities.Currency"
});
upsert("currency", "USD", {
  currencyId: "USD",
  currencyName: "US Dollar",
  currencyCode: "USD",
  currencyDecimalPlace: 2,
  symbol: "$",
  _class: "com.pv.couseae.entities.Currency"
});

upsert("locationCountry", "IN", {
  countryId: "IN",
  countryName: "India",
  countryCode: "IN",
  countryPhoneCode: "91",
  countryNumericCode: "356",
  _class: "com.pv.couseae.entities.LocationCountry"
});
upsert("locationCountry", "US", {
  countryId: "US",
  countryName: "United States",
  countryCode: "US",
  countryPhoneCode: "1",
  countryNumericCode: "840",
  _class: "com.pv.couseae.entities.LocationCountry"
});
upsert("LocationCity", "IN-DEL", {
  cityId: "IN-DEL",
  cityName: "New Delhi",
  _class: "com.pv.couseae.entities.LocationCity"
});

upsert("acquirer", "ACQ-HDFC", {
  acquirerId: "ACQ-HDFC",
  fullName: "HDFC Dummy",
  acquirerCode: "HDFC",
  isPayin: true,
  isPayout: true,
  status: true,
  acquirerPgId: "dummy-pg-id",
  acquirerPgKey: "dummy-pg-key",
  payinWebhookUrl: "http://localhost:8085/payinwebhook/hdfcpayinwebhook",
  _class: "com.pv.couseae.entities.Acquirer"
});
upsert("acquirer", "ACQ-JUSPAY", {
  acquirerId: "ACQ-JUSPAY",
  fullName: "Juspay Dummy",
  acquirerCode: "JUSPAY",
  isPayin: true,
  isPayout: false,
  status: true,
  _class: "com.pv.couseae.entities.Acquirer"
});

upsert("MerchantWallet", "testmerchant@local.test", {
  merchantId: "testmerchant@local.test",
  balance: 15000.5,
  currency: "INR",
  lastUpdated: now,
  _class: "com.pv.couseae.entities.MerchantWallet"
});
upsert("MerchantWallet", "shopone@local.test", {
  merchantId: "shopone@local.test",
  balance: 4200,
  currency: "INR",
  lastUpdated: now,
  _class: "com.pv.couseae.entities.MerchantWallet"
});
upsert("MerchantCryptoWallet", "cw-merchant-usdt", {
  merchantId: "testmerchant@local.test",
  coin: "USDT",
  network: "TRC20",
  balance: NumberDecimal("250.000000"),
  lastUpdated: now,
  _class: "com.pv.couseae.entities.MerchantCryptoWallet"
});

upsert("fee_rules", "FEE-PAYIN-GLOBAL", {
  ruleId: "FEE-PAYIN-GLOBAL",
  merchantId: null,
  txnType: "PAYIN",
  feeType: "PERCENT",
  feeValue: NumberDecimal("1.5"),
  capMin: NumberDecimal("1"),
  capMax: NumberDecimal("50"),
  commissionPercent: NumberDecimal("0.2"),
  isActive: true,
  createdAt: now,
  updatedAt: now,
  _class: "com.pv.couseae.entities.FeeRule"
});
upsert("limit_rules", "LIM-PAYIN-MERCHANT", {
  merchantId: "testmerchant@local.test",
  txnType: "PAYIN",
  perTxnMin: "10",
  perTxnMax: "100000",
  dailyLimit: "500000",
  monthlyLimit: "5000000",
  isActive: true,
  createdAt: now,
  updatedAt: now,
  _class: "com.pv.couseae.entities.LimitRule"
});

upsert("api_master", "API-SESSION-HDFC", {
  aggregatorCode: "HDFC",
  apiName: "CREATE_SESSION",
  baseUrl: "http://localhost:8085/hdfc-stub",
  endpoint: "/session",
  httpMethod: "POST",
  type: "Payin",
  merchantId: "dummy-mid",
  secretKey: "dummy-secret",
  active: true,
  environment: "UAT",
  createdBy: "admin@courseae.com",
  createdAt: now,
  _class: "com.pv.couseae.entities.ApiMaster"
});

upsert("paymentType", "PT-UPI", {
  paymentTypeId: "PT-UPI",
  paymentTypeName: "UPI",
  paymentTypeCode: "UPI",
  country: { $ref: "locationCountry", $id: "IN" },
  currency: { $ref: "currency", $id: "INR" },
  _class: "com.pv.couseae.entities.PaymentType"
});
upsert("paymentType", "PT-CARD", {
  paymentTypeId: "PT-CARD",
  paymentTypeName: "Card",
  paymentTypeCode: "CARD",
  country: { $ref: "locationCountry", $id: "IN" },
  currency: { $ref: "currency", $id: "INR" },
  _class: "com.pv.couseae.entities.PaymentType"
});

upsert("businessDetails", "BD-TESTMERCHANT", {
  businessName: "Local Test Merchant",
  companyRegistrationNo: "U12345DL2020PTC000001",
  gstVat: "07AAAAA0000A1Z5",
  panSsn: "AAAAA0000A",
  setupIntegrationFees: 0,
  settlementFees: 0,
  wireTransferFees: 0,
  minimumSettlementAmount: 100,
  businessEmail: "testmerchant@local.test",
  phone: "9999999997",
  websiteUrl: "https://merchant.local.test",
  businessAddress: "1 Test Street",
  postalCode: 110001,
  businessType: "Retail",
  businessSubType: "Retail",
  user: userRef("testmerchant@local.test"),
  _class: "com.pv.couseae.entities.BusinessDetails"
});

upsert("userAccount", "UA-TESTMERCHANT-INR", {
  userAccountId: "UA-TESTMERCHANT-INR",
  user: userRef("testmerchant@local.test"),
  currency: { $ref: "currency", $id: "INR" },
  amountBalance: 15000.5,
  _class: "com.pv.couseae.entities.UserAccount"
});

upsert("payoutSettings", "PS-TESTMERCHANT", {
  payoutSettingsId: "PS-TESTMERCHANT",
  user: userRef("testmerchant@local.test"),
  AcquirerProfile: "HDFC-DEFAULT",
  acquirerPriority: 1,
  acquirerProfilePriority: 1,
  minimumAmount: 10,
  maximumAmount: 100000,
  _class: "com.pv.couseae.entities.PayoutSettings"
});

upsert("payoutIpWhitelist", "IPWL-1", {
  payoutIpWhitelistId: "IPWL-1",
  user: userRef("testmerchant@local.test"),
  ipAddress: "127.0.0.1",
  systemName: "local-dev",
  ipAddressDesc: "localhost",
  _class: "com.pv.couseae.entities.PayoutIpWhitelist"
});

upsert("resellerMapping", "RM-SHOPONE", {
  resellerMerchantId: "RM-SHOPONE",
  merchantId: userRef("shopone@local.test"),
  merchantFullName: "Shop One",
  merchantUserName: "shopone@local.test",
  resellerId: userRef("testreseller@local.test"),
  isFixCharge: false,
  vendorCharge: 0.5,
  _class: "com.pv.couseae.entities.ResellerMapping"
});

upsert("email_master", "EM-WELCOME", {
  emailCode: "WELCOME",
  fromEmail: "noreply@local.test",
  subject: "Welcome",
  bodyTemplate: "Hello {{name}}",
  smtpHost: "localhost",
  smtpPort: 1025,
  status: "ACTIVE",
  createdDate: now,
  _class: "com.pv.couseae.entities.EmailMaster"
});

[
  { id: "ORD-DUMMY-001", status: "SUCCESS", type: "ORDER", amt: 499.00, when: yesterday, method: "UPI" },
  { id: "ORD-DUMMY-002", status: "FAILED", type: "ORDER", amt: 120.00, when: yesterday, method: "CARD" },
  { id: "ORD-DUMMY-003", status: "PENDING", type: "ORDER", amt: 2500.00, when: now, method: "UPI" },
  { id: "ORD-DUMMY-004", status: "SUCCESS", type: "ORDER", amt: 89.50, when: lastWeek, method: "UPI" },
  { id: "ORD-DUMMY-005", status: "SUCCESS", type: "ORDER", amt: 999.00, when: now, method: "CARD", merchant: "shopone@local.test" }
].forEach(function (t) {
  const mid = t.merchant || "testmerchant@local.test";
  upsert("payin_requests", t.id, {
    orderId: t.id,
    merchantId: mid,
    aggregatorCode: "HDFC",
    txnId: "TXN-" + t.id,
    paymentMethod: t.method,
    paymentMethodType: t.method,
    paymentMode: t.method,
    amount: NumberDecimal(String(t.amt)),
    paidAmount: t.status === "SUCCESS" ? NumberDecimal(String(t.amt)) : NumberDecimal("0"),
    currency: "INR",
    firstName: "Dummy",
    lastName: "Buyer",
    customerEmail: "buyer@local.test",
    customerMobile: "9000000001",
    cardLastFourDigits: t.method === "CARD" ? "4242" : null,
    transactionStatus: t.status,
    transactionType: t.type,
    statusMessage: t.status,
    callbackSent: t.status === "SUCCESS",
    ipAddress: "127.0.0.1",
    createdBy: mid,
    initiatedAt: t.when,
    createdOn: t.when,
    updatedAt: now,
    settled: t.status === "SUCCESS",
    charges: NumberDecimal("5"),
    gst: NumberDecimal("0.90"),
    netsettlementamount: NumberDecimal(String(Math.max(t.amt - 5.9, 0))),
    _class: "com.pv.couseae.entities.PayinRequest"
  });
});

print("seeded zenithuat");
print("users=" + db.users.countDocuments());
print("adminPresent=" + db.users.countDocuments({ userId: "admin@courseae.com", role: "ADMIN" }));
print("roles=" + db.users.distinct("role"));
print("payin_requests=" + db.payin_requests.countDocuments());
print("MerchantWallet=" + db.MerchantWallet.countDocuments());
print("acquirer=" + db.acquirer.countDocuments());
