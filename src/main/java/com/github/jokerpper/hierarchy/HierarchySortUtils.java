/*
 *
 * Copyright (c) 2021-2xxx, joker-pper (https://github.com/joker-pper).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
     * @param sourceList 源数据列表，不能存在为Null的子元素
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
     * @param sourceList          源数据列表，不能存在为Null的子元素
     * @param getChildrenFunction 获取children函数，必传
     * @param comparator          比较器
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
     * @param source              源数据，不能为Null
     * @param getChildrenFunction 获取children函数，必传
     * @param comparator          比较器
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
