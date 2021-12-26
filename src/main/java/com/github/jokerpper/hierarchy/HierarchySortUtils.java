package com.github.jokerpper.hierarchy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author joker-pper 2021-12-26
 */
public class HierarchySortUtils {

    //Suppresses default constructor, Don't let anyone instantiate this class.
    private HierarchySortUtils() {
    }

    /**
     * 对数据列表进行排序
     *
     * @param sourceList
     * @param comparator
     * @param <T>
     */
    public static <T> void sort(final List<T> sourceList, final Comparator<T> comparator) {
        if (sourceList == null || sourceList.isEmpty()) {
            return;
        }
        Collections.sort(sourceList, comparator);
    }

    /**
     * 对数据列表及子数据列表进行排序
     *
     * @param sourceList
     * @param getChildrenFunction 获取children函数,必传
     * @param comparator
     * @param <T>
     */
    public static <T> void sortWithChildren(final List<T> sourceList, final Function<T, List<T>> getChildrenFunction, final Comparator<T> comparator) {
        Objects.requireNonNull(getChildrenFunction, "getChildrenFunction must be not null");
        if (sourceList == null || sourceList.isEmpty()) {
            return;
        }
        Collections.sort(sourceList, comparator);
        for (T source : sourceList) {
            resolveSortElement(source, getChildrenFunction, comparator);
        }
    }

    /**
     * 进行排序当前数据的子数据列表
     *
     * @param source
     * @param getChildrenFunction
     * @param comparator
     * @param <T>
     */
    private static <T> void resolveSortElement(final T source, final Function<T, List<T>> getChildrenFunction, Comparator<T> comparator) {
        List<T> children = HierarchyHelper.resolveAndGetChildren(getChildrenFunction, source);
        if (children == null || children.isEmpty()) {
            return;
        }
        Collections.sort(children, comparator);
        for (T child : children) {
            resolveSortElement(child, getChildrenFunction, comparator);
        }
    }

}
