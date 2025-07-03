package com.strategyengine.strategyengine.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompositeRule {
    private String action; // BUY or SELL
    private List<Condition> conditions;
}