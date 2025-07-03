//package com.strategyengine.strategyengine.model;
//
//
//import lombok.*;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Condition {
//
//    private String left;      // e.g., "SMA"
//    private Integer leftArg;  // e.g., 10 for SMA(10)
//    private String operator;  // >, <, ==, etc.
//    private String right;     // "SMA", "RSI", "VALUE"
//    private Integer rightArg; // 20 for SMA(20) or 30 for VALUE
//}

package com.strategyengine.strategyengine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Condition {
    private String left;
    private Integer leftArg;
    private String operator;
    private String right;
    private Integer rightArg;
}