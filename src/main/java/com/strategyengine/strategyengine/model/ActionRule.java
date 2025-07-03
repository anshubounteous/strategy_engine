package com.strategyengine.strategyengine.model;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionRule {

    private String action;     // "BUY" or "SELL"
    private Condition condition;
}
