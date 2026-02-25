package com.notification.mock.domain;

import java.util.concurrent.ThreadLocalRandom;

public enum DispatchStatus {

    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    ERROR("ERROR");

    private final String value;

    DispatchStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 랜덤으로 발송 결과 상태를 반환합니다.
     * SUCCESS / FAILURE / ERROR 중 하나가 선택됩니다.
     */
    public static DispatchStatus random() {
        DispatchStatus[] statuses = values();
        int index = ThreadLocalRandom.current().nextInt(statuses.length);
        return statuses[index];
    }
}
