package com.akanksha.fraud.consumer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Explainable demo rules. Replace with calibrated rules/models for production use. */
public final class FraudDetector {
    private final BigDecimal highAmountThreshold;
    public FraudDetector(BigDecimal highAmountThreshold) { this.highAmountThreshold = highAmountThreshold; }

    public List<String> reasons(Map<String, Object> tx) {
        List<String> reasons = new ArrayList<>();
        Object amountValue = tx.get("amount");
        if (amountValue == null) {
            reasons.add("MISSING_AMOUNT");
        } else {
            try {
                BigDecimal amount = new BigDecimal(amountValue.toString());
                if (amount.signum() < 0) reasons.add("NEGATIVE_AMOUNT");
                if (amount.compareTo(highAmountThreshold) >= 0) reasons.add("HIGH_AMOUNT");
            } catch (NumberFormatException e) {
                reasons.add("INVALID_AMOUNT");
            }
        }
        if (blank(tx.get("transactionId"))) reasons.add("MISSING_TRANSACTION_ID");
        if (blank(tx.get("accountId"))) reasons.add("MISSING_ACCOUNT_ID");
        return reasons;
    }
    public boolean isSuspicious(Map<String, Object> tx) { return !reasons(tx).isEmpty(); }
    private boolean blank(Object value) { return value == null || value.toString().trim().isEmpty(); }
}