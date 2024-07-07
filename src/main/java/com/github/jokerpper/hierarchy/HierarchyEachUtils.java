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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author joker-pper 2024-07-07
 */
public class HierarchyEachUtils {

    /**
     * 第一层级
     */
    public final static int FIRST_LEVEL = 0;

    //Suppresses default constructor, Don't let anyone instantiate this class.
    private HierarchyEachUtils() {
    }

    /**
     * 递归遍历
     *
     * @param sourceList          源数据列表，不能存在为Null的子元素
     * @param getChildrenFunction 获取子级函数
     * @param callback            回调函数
     * @param <T>                 源数据类型
     */
    public static <T> void recursionEach(final List<T> sourceList, final Function<T, List<T>> getChildrenFunction, final EachCallback<T> callback) {
        Objects.requireNonNull(getChildrenFunction, "getChildrenFunction must be not null");
        if (sourceList == null || sourceList.isEmpty()) {
            return;
        }

        callback.beforeEach(sourceList);

        boolean withParentList = callback.withParentList();
        for (T source : sourceList) {
            recursionEach(source, null, getChildrenFunction, null, FIRST_LEVEL, callback, withParentList);
        }
    }

    /**
     * 递归遍历处理
     *
     * @param source              源数据
     * @param parent              父级元素
     * @param getChildrenFunction 获取子级函数
     * @param parentList          父级列表
     * @param level               层级
     * @param callback            回调函数
     * @param withParentList      是否启用父级列表
     * @param <T>                 源数据类型
     */
    private static <T> void recursionEach(final T source, final T parent, final Function<T, List<T>> getChildrenFunction, final List<T> parentList, final int level, final EachCallback<T> callback, final boolean withParentList) {
        List<T> children = HierarchyHelper.resolveAndGetChildren(getChildrenFunction, source);
        boolean hasExistChildren = children != null && !children.isEmpty();

        //遍历执行自定义逻辑
        callback.each(level, source, parent, parentList, children, hasExistChildren);

        if (!hasExistChildren) {
            return;
        }

        //获取当前元素的子级所要用的父级列表
        List<T> currentParentList = null;
        try {
            if (withParentList) {
                if (level == FIRST_LEVEL) {
                    //第一层级时
                    currentParentList = new ArrayList<>(1);
                } else {
                    currentParentList = new ArrayList<>(parentList.size() + 1);
                    currentParentList.addAll(parentList);
                }
                currentParentList.add(source);
            }

            for (T child : children) {
                recursionEach(child, source, getChildrenFunction, currentParentList, level + 1, callback, withParentList);
            }
        } finally {
            if (currentParentList != null) {
                currentParentList.clear();
            }
        }
    }

    interface EachCallback<T> {

        /**
         * 是否启用父级列表，默认不启用，如有需要进行开启
         *
         * @return boolean值
         */
        default boolean withParentList() {
            return false;
        }

        /**
         * 遍历前执行自定义逻辑
         * <p>
         * 注：如果源数据存在多个元素且有排序需求，可在此进行操作
         *
         * @param sourceList 源数据列表 不为Null且个数大于0的列表
         */
        default void beforeEach(List<T> sourceList) {

        }

        /**
         * 遍历执行自定义逻辑
         *
         * @param level            层级
         * @param current          当前元素 不为Null
         * @param parent           直属父级元素 非根元素时不为Null
         * @param parentList       父级列表 启用父级列表后且非根元素时不为Null，反之一直为Null
         * @param children         当前子级列表 可能为Null，存在子级列表时一定是不为Null且个数大于0的列表
         * @param hasExistChildren 是否存在子级列表
         */
        void each(int level, T current, T parent, List<T> parentList, List<T> children, boolean hasExistChildren);
    }
}
