//package com.strategyengine.strategyengine.parser;
//
//
//import com.strategyengine.strategyengine.model.ActionRule;
//import com.strategyengine.strategyengine.model.Condition;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Service
//public class ScriptParserService {
//
//    private static final Pattern RULE_PATTERN = Pattern.compile(
//            "(BUY|SELL)\\s+WHEN\\s+(\\w+)(\\((\\d+)\\))?\\s*(>|<|==|!=|>=|<=)\\s*(\\w+)?(\\((\\d+)\\))?|\\d+"
//    );
//
//    public List<ActionRule> parse(String script) {
//        List<ActionRule> rules = new ArrayList<>();
//        String[] lines = script.split("\\n");
//
//        for (String line : lines) {
//            line = line.trim();
//            if (line.isEmpty()) continue;
//
//            Matcher matcher = RULE_PATTERN.matcher(line);
//
//            if (matcher.find()) {
//                String action = matcher.group(1);          // BUY or SELL
//                String left = matcher.group(2);            // e.g., SMA
//                Integer leftArg = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : null;
//                String operator = matcher.group(5);        // >, <, ==, etc.
//                String right = matcher.group(6);
//                Integer rightArg = matcher.group(8) != null ? Integer.parseInt(matcher.group(8)) : null;
//
//                // If right is a constant value like 30
//                if (right == null) {
//                    right = "VALUE";
//                    rightArg = extractRightValue(line);
//                }
//
//                Condition condition = Condition.builder()
//                        .left(left)
//                        .leftArg(leftArg)
//                        .operator(operator)
//                        .right(right)
//                        .rightArg(rightArg)
//                        .build();
//
//                ActionRule rule = ActionRule.builder()
//                        .action(action)
//                        .condition(condition)
//                        .build();
//
//                rules.add(rule);
//            } else {
//                throw new IllegalArgumentException("Invalid DSL syntax: " + line);
//            }
//        }
//
//        return rules;
//    }
//
//    private int extractRightValue(String line) {
//        Matcher numberMatcher = Pattern.compile("(\\d+)$").matcher(line);
//        if (numberMatcher.find()) {
//            return Integer.parseInt(numberMatcher.group(1));
//        }
//        throw new IllegalArgumentException("Could not parse numeric value in: " + line);
//    }
//}


package com.strategyengine.strategyengine.parser;

import com.strategyengine.strategyengine.model.CompositeRule;
import com.strategyengine.strategyengine.model.Condition;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScriptParserService {

    public List<CompositeRule> parse(String script) {
        List<CompositeRule> rules = new ArrayList<>();
        String[] lines = script.split("\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+WHEN\\s+");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid syntax: " + line);

            String action = parts[0].trim();
            String conditionExpr = parts[1].trim();
            String[] conditionParts = conditionExpr.split("(?i)\s+AND\s+");

            List<Condition> conditions = new ArrayList<>();

            for (String part : conditionParts) {
                Pattern pattern = Pattern.compile("(\\w+)(\\((\\d+)\\))?\\s*(>|<|==|!=|>=|<=)\\s*(\\w+)?(\\((\\d+)\\))?|\\d+");
                Matcher matcher = pattern.matcher(part.trim());
                if (matcher.find()) {
                    String left = matcher.group(1);
                    Integer leftArg = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : null;

                    String operator = matcher.group(4);
                    String right = matcher.group(5);
                    Integer rightArg = matcher.group(7) != null ? Integer.parseInt(matcher.group(7)) : null;

                    if (right == null) {
                        right = "VALUE";
                        rightArg = Integer.parseInt(part.replaceAll("[^0-9]+", ""));
                    }

                    conditions.add(Condition.builder()
                            .left(left)
                            .leftArg(leftArg)
                            .operator(operator)
                            .right(right)
                            .rightArg(rightArg)
                            .build());
                } else {
                    throw new IllegalArgumentException("Invalid condition: " + part);
                }
            }

            rules.add(CompositeRule.builder().action(action).conditions(conditions).build());
        }
        return rules;
    }
}
