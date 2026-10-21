package com.akanksha.fraud.consumer;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class FraudDetectorTest {
    private final FraudDetector detector = new FraudDetector(new BigDecimal("1000.00"));
    @Test void flagsAmountAtThreshold() {
        Map<String,Object> tx = tx("1000.00");
        assertTrue(detector.isSuspicious(tx));
        assertTrue(detector.reasons(tx).contains("HIGH_AMOUNT"));
    }
    @Test void acceptsOrdinaryTransaction() {
        assertFalse(detector.isSuspicious(tx("12.50")));
    }
    @Test void flagsMissingAmount() {
        Map<String,Object> tx = tx("10");
        tx.remove("amount");
        assertTrue(detector.reasons(tx).contains("MISSING_AMOUNT"));
    }
    private Map<String,Object> tx(String amount) {
        Map<String,Object> tx = new HashMap<>();
        tx.put("transactionId","tx-1"); tx.put("accountId","acct-1"); tx.put("amount",amount);
        return tx;
    }
}