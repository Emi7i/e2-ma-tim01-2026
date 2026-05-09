package com.example.slagalica.domain.service.match;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MojBrojService {
    @Inject
    public MojBrojService() {}

    private final Random random = new Random();

    public int generateGoalNumber() {
        return 100 + random.nextInt(999);
    }

    public int[] generateOperands() {
        int[] numbers = new int[6];
        for (int i = 0; i < 4; i++) numbers[i] = generateSingleDigit();
        numbers[4] = generateSmallDoubleDigit();
        numbers[5] = generateLargeDoubleDigit();
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
}
