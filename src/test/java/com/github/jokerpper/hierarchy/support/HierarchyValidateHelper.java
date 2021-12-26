package com.github.jokerpper.hierarchy.support;

import com.github.jokerpper.hierarchy.model.Menu;
import org.junit.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HierarchyValidateHelper {

    public static void assertSameSortedResult(List<Menu> actualResults, Comparator<Menu> comparator) {
        List<Menu> expectedResults = ObjectUtils.decode(ObjectUtils.serialize(actualResults));
        SortUtils.sort(expectedResults, comparator);
        Assert.assertEquals(expectedResults, actualResults);
    }

    static class SortUtils {
        private static void sort(List<Menu> expectedResults, Comparator<Menu> comparator) {
            Collections.sort(expectedResults, comparator);
            expectedResults.forEach(it -> resolveSortElement(it, comparator));
        }

        static void resolveSortElement(Menu menu, Comparator<Menu> comparator) {
            List<Menu> children = menu.getChildren();
            if (children == null || children.isEmpty()) {
                return;
            }
            Collections.sort(children, comparator);
            children.forEach(it -> resolveSortElement(it, comparator));
        }

    }

}
