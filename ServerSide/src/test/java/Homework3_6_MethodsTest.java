import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.geekbrains.Homework3_6_Methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Homework3_6_MethodsTest {
    private Homework3_6_Methods test;

    @BeforeEach
    public void init() {
        test = new Homework3_6_Methods();
    }


    @ParameterizedTest
    @MethodSource("getDataForTest")
    public void getElementsAfterLastFourTest(int[] arr) {
        int[] arr2 = test.getElementsAfterLastFour(arr);
        System.out.println(Arrays.toString(arr2));
    }

    public static Stream<Arguments> getDataForTest() {
        int[] arr = {1, 3, 5, 4, 7, 2, 1};
        int[] arr2 = {4, 3, 4, 7, 6, 2, 1};
        int[] arr3 = {2, 3, 5, 7, 4, 2, 4};
        int[] arr4 = {1, 3, 5, 7, 3, 2, 1};
        List<Arguments> list = new ArrayList<>();
        list.add(Arguments.arguments(arr));
        list.add(Arguments.arguments(arr2));
        list.add(Arguments.arguments(arr3));
        list.add(Arguments.arguments(arr4));
        return list.stream();
    }

    @ParameterizedTest
    @MethodSource("getDataForTest2")
    public void checkElementsOneFourTest(int[] arr) {
        System.out.println(test.checkElementsOneFour(arr));
    }

    public static Stream<Arguments> getDataForTest2() {
        int[] arr = {4, 4, 4, 4, 4, 4, 4};
        int[] arr2 = {1, 1, 1, 1, 1, 1, 1};
        int[] arr3 = {2, 3, 5, 7, 4, 2, 9};
        int[] arr4 = {1, 3, 5, 7, 3, 2, 5};
        List<Arguments> list = new ArrayList<>();
        list.add(Arguments.arguments(arr));
        list.add(Arguments.arguments(arr2));
        list.add(Arguments.arguments(arr3));
        list.add(Arguments.arguments(arr4));
        return list.stream();
    }
}
