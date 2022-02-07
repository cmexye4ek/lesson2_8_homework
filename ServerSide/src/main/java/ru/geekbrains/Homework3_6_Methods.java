package ru.geekbrains;

import java.util.Arrays;

public class Homework3_6_Methods {

    public int[] getElementsAfterLastFour(int[] arr) {
        int[] tempArr;
        if (Arrays.stream(arr).anyMatch(x -> x == 4)) {
            for (int i = arr.length -1; i >= 0; i--) {
                if (arr[i] == 4) {
                    tempArr = Arrays.copyOfRange(arr, i + 1, arr.length);
                    return tempArr;
                }
            }

        } else {
            throw new RuntimeException();
        }
        return null;
    }

    public boolean checkElementsOneFour(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 1 && arr[i] != 4) {
                return false;
            }
        }
        return true;
    }

}
