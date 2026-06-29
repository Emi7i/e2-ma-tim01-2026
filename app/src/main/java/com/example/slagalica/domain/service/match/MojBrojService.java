package com.example.slagalica.domain.service.match;

import com.example.slagalica.domain.model.match.games.common.OnSessionUpdateListener;
import com.example.slagalica.domain.model.match.games.mojbroj.MojBrojSessionData;
import com.example.slagalica.repository.impl.MojBrojRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MojBrojService {
    @Inject
    public MojBrojService(MojBrojRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    private final Random random = new Random();
    private final MojBrojRepository sessionRepository;

    public int generateGoalNumber() {
        return 100 + random.nextInt(900);
    }

    public List<Integer> generateOperands() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 4; i++) numbers.add(generateSingleDigit());
        numbers.add(generateSmallDoubleDigit());
        numbers.add(generateLargeDoubleDigit());
        return numbers;
    }

    private int generateSingleDigit() {
        return 1 + random.nextInt(9);
    }

    private int generateSmallDoubleDigit() {
        int[] options = {10, 15, 20};
        return options[random.nextInt(options.length)];
    }

    private int generateLargeDoubleDigit() {
        int[] options = {25, 50, 75, 100};
        return options[random.nextInt(options.length)];
    }

    public CompletableFuture<Void> updateSessionData(String matchId, MojBrojSessionData data) {
        return sessionRepository.updateSessionData(matchId, data);
    }

    public CompletableFuture<MojBrojSessionData> getSessionData(String matchId) {
        return sessionRepository.getSessionData(matchId);
    }

    public CompletableFuture<Void> delete(String matchId) {
        return sessionRepository.delete(matchId);
    }

    public void observeSessionData(String matchId, OnSessionUpdateListener<MojBrojSessionData> listener) {
        sessionRepository.observeSessionData(matchId, listener);
    }

    /**
     * Validates and evaluates an expression made of the given tokens against the
     * available operands. Throws IllegalArgumentException for any invalid expression:
     * - token not one of (, ), +, -, *, / or a number from availableOperands
     * - operand used more times than it appears in availableOperands
     * - malformed expression (mismatched parens, operator misuse, etc.)
     * - division by zero
     * - non-integer result
     */
    public int evaluateExpression(List<String> tokens, List<Integer> availableOperands) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty expression");
        }

        validateOperandUsage(tokens, availableOperands);

        Deque<Double> values = new ArrayDeque<>();
        Deque<String> ops = new ArrayDeque<>();

        for (String token : tokens) {
            if (isNumber(token)) {
                values.push(Double.parseDouble(token));
            } else if (token.equals("(")) {
                ops.push(token);
            } else if (token.equals(")")) {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    applyOp(values, ops.pop());
                }
                if (ops.isEmpty()) {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
                ops.pop(); // remove "("
            } else if (isOperator(token)) {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(token)) {
                    applyOp(values, ops.pop());
                }
                ops.push(token);
            } else {
                throw new IllegalArgumentException("Invalid token: " + token);
            }
        }

        while (!ops.isEmpty()) {
            String op = ops.pop();
            if (op.equals("(")) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            applyOp(values, op);
        }

        if (values.size() != 1) {
            throw new IllegalArgumentException("Malformed expression");
        }

        double result = values.pop();
        if (result != Math.floor(result) || Double.isInfinite(result)) {
            throw new IllegalArgumentException("Result is not an integer");
        }

        return (int) Math.round(result);
    }

    /**
     * Checks that every numeric token used is available in availableOperands,
     * respecting multiplicity (each operand usable at most once, duplicates allowed
     * if they appear multiple times in availableOperands).
     */
    private void validateOperandUsage(List<String> tokens, List<Integer> availableOperands) {
        List<Integer> remaining = new ArrayList<>(availableOperands);
        for (String token : tokens) {
            if (isNumber(token)) {
                int value = (int) Double.parseDouble(token);
                if (!remaining.remove(Integer.valueOf(value))) {
                    throw new IllegalArgumentException("Operand not available or already used: " + token);
                }
            }
        }
    }

    private boolean isNumber(String token) {
        if (token == null || token.isEmpty()) return false;
        for (char c : token.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    private int precedence(String op) {
        if (op.equals("+") || op.equals("-")) return 1;
        if (op.equals("*") || op.equals("/")) return 2;
        return 0;
    }

    private void applyOp(Deque<Double> values, String op) {
        if (values.size() < 2) {
            throw new IllegalArgumentException("Malformed expression");
        }
        double b = values.pop();
        double a = values.pop();
        switch (op) {
            case "+": values.push(a + b); break;
            case "-": values.push(a - b); break;
            case "*": values.push(a * b); break;
            case "/":
                if (b == 0) throw new IllegalArgumentException("Division by zero");
                values.push(a / b);
                break;
            default: throw new IllegalArgumentException("Unknown operator: " + op);
        }
    }
}
