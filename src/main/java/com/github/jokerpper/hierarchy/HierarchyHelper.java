package com.github.jokerpper.hierarchy;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author joker-pper 2021-01-03
 */
public class HierarchyHelper {

    //Suppresses default constructor, Don't let anyone instantiate this class.
    private HierarchyHelper() {
    }

    /**
     * 获取新list
     *
     * @param sourceList
     * @param <T>
     * @return
     */
    static <T> List<T> getNewList(final Collection<T> sourceList) {
        return new ArrayList<>(sourceList);
    }


    /**
     * 通过sourceList、获取children函数和过滤条件获取匹配的结果列表
     *
     * @param sourceList          源数据列表
     * @param getChildrenFunction 获取children函数,可选
     * @param filterPredicate     过滤条件,可选
     * @param <T>
     * @return
     */
    static <T> List<T> getApplySourceList(final List<T> sourceList, final Function<T, List<T>> getChildrenFunction, final Predicate<T> filterPredicate) {
        final List<T> applySourceList;
        boolean hasGetChildrenFunction = getChildrenFunction != null;
        if (hasGetChildrenFunction) {
            if (filterPredicate != null) {
                applySourceList = getApplySourceListWithChildrenAndPredicate(sourceList, getChildrenFunction, filterPredicate);
            } else {
                applySourceList = getApplySourceListWithChildren(sourceList, getChildrenFunction);
            }
        } else {
            if (filterPredicate != null) {
                applySourceList = getApplySourceListWithPredicate(sourceList, filterPredicate);
            } else {
                applySourceList = getNewList(sourceList);
            }
        }
        return applySourceList;
    }


    /**
     * 通过sourceList和过滤条件获取匹配的结果列表
     *
     * @param sourceList      源数据列表
     * @param filterPredicate 过滤条件
     * @param <T>
     */
    static <T> List<T> getApplySourceListWithPredicate(final List<T> sourceList, final Predicate<T> filterPredicate) {
        int size = sourceList.size();
        int capacity = (int) (size * 0.8);
        capacity = Math.max(capacity, 16);
        final List<T> applySourceList = new ArrayList<>(capacity);
        for (T source : sourceList) {
            if (filterPredicate.test(source)) {
                applySourceList.add(source);
            }
        }
        return applySourceList;
    }

    /**
     * 通过sourceList和获取children函数获取匹配的结果列表
     *
     * @param sourceList          源数据列表
     * @param getChildrenFunction 获取children函数
     * @param <T>
     * @return
     */
    static <T> List<T> getApplySourceListWithChildren(final List<T> sourceList, final Function<T, List<T>> getChildrenFunction) {
        int size = sourceList.size();
        int capacity = (int) (size * 1.8);
        capacity = Math.max(capacity, 256);
        final List<T> resultList = new ArrayList<>(capacity);
        resolveSourceList(sourceList, resultList, getChildrenFunction);
        return resultList;
    }

    /**
     * 通过sourceList、获取children函数和过滤条件获取匹配的结果列表
     *
     * @param sourceList          源数据列表
     * @param getChildrenFunction 获取children函数
     * @param filterPredicate     过滤条件
     * @param <T>
     * @return
     */
    static <T> List<T> getApplySourceListWithChildrenAndPredicate(final List<T> sourceList
            , final Function<T, List<T>> getChildrenFunction, final Predicate<T> filterPredicate) {
        int size = sourceList.size();
        int capacity = (int) (size * 1.8);
        capacity = Math.max(capacity, 256);
        final List<T> resultList = new ArrayList<>(capacity);
        resolveSourceListWithPredicate(sourceList, resultList, getChildrenFunction, filterPredicate);
        return resultList;
    }

    /**
     * 将sourceList中的所有元素(包含子元素)全部放入结果数据列表中
     *
     * @param sourceList          源数据列表
     * @param resultList          结果数据列表
     * @param getChildrenFunction 获取children函数
     * @param <T>
     */
    private static <T> void resolveSourceList(final List<T> sourceList, final List<T> resultList
            , final Function<T, List<T>> getChildrenFunction) {
        for (T source : sourceList) {
            resultList.add(source);
            List<T> sourceChildrenList = resolveAndGetChildren(getChildrenFunction, source);
            if (sourceChildrenList != null && !sourceChildrenList.isEmpty()) {
                resolveSourceList(sourceChildrenList, resultList, getChildrenFunction);
            }
        }
    }

    /**
     * 将sourceList中的所有元素(包含子元素)全部放入结果数据列表中
     *
     * @param sourceList          源数据列表
     * @param resultList          结果数据列表
     * @param getChildrenFunction 获取children函数
     * @param filterPredicate     过滤条件
     * @param <T>
     */
    private static <T> void resolveSourceListWithPredicate(final List<T> sourceList, final List<T> resultList
            , final Function<T, List<T>> getChildrenFunction, final Predicate<T> filterPredicate) {
        for (T source : sourceList) {
            if (filterPredicate.test(source)) {
                resultList.add(source);
            }
            List<T> sourceChildrenList = resolveAndGetChildren(getChildrenFunction, source);
            if (sourceChildrenList != null && !sourceChildrenList.isEmpty()) {
                resolveSourceListWithPredicate(sourceChildrenList, resultList, getChildrenFunction, filterPredicate);
            }
        }
    }

    /**
     * 获取当前元素的子元素
     *
     * @param getChildrenFunction 获取children函数
     * @param source
     * @param <T>
     * @return
     */
    static <T> List<T> resolveAndGetChildren(final Function<T, List<T>> getChildrenFunction, final T source) {
        return getChildrenFunction.apply(source);
    }

    /**
     * 设置当前元素的子元素
     *
     * @param setChildrenFunction
     * @param source
     * @param children
     * @param <T>
     */
    static <T> void resolveAndSetChildren(final BiConsumer<T, List<T>> setChildrenFunction, final T source, final List<T> children) {
        setChildrenFunction.accept(source, children);
    }

    /**
     * 获取转换后的结果
     *
     * @param transferFunction
     * @param source
     * @param <T>
     * @param <R>
     * @return
     */
    static <T, R> R getTransferResult(final Function<T, R> transferFunction, final T source) {
        return transferFunction.apply(source);
    }

    /**
     * 获取元素id所对应的子元素(但不包含root)
     *
     * @param toResolveSourceList 待处理的数据元素列表
     * @param getIdFunction       获取 id 函数
     * @param getPidFunction      获取 pid 函数
     * @param isRootFunction      是否为 root函数
     * @param <T>
     * @param <V>
     * @return
     */
    static <T, V> Map<V, List<T>> initAndGetIdChildrenResultMap(
            final List<T> toResolveSourceList,
            Function<T, V> getIdFunction,
            final Function<T, V> getPidFunction,
            final Function<V, Boolean> isRootFunction) {

        Map<V, List<T>> resultMap = new HashMap<>(toResolveSourceList.size());
        for (T toResolveSource : toResolveSourceList) {
            //获取id
            V id = getIdFunction.apply(toResolveSource);
            if (isRootFunction.apply(id)) {
                //为root时跳过
                continue;
            }

            //获取pid
            V pid = getPidFunction.apply(toResolveSource);

            //将pid对应的数据列表放入map中
            List<T> pidSourceList = resultMap.computeIfAbsent(pid, k -> new ArrayList<>(32));
            pidSourceList.add(toResolveSource);
        }
        return resultMap;
    }


    /**
     * 获取boolean值
     *
     * @param supplier
     * @param defaultValue
     * @return
     */
    static boolean getBooleanValue(Supplier<Boolean> supplier, boolean defaultValue) {
        return supplier != null ? Boolean.TRUE.equals(supplier.get()) : defaultValue;
    }

    /**
     * 添加root元素
     *
     * @param rootList
     * @param data
     * @param withCheck 是否检查
     * @param <T>
     */
    static <T> void addRoot(final List<T> rootList, final T data, final boolean withCheck) {
        //如果调用时rootList不为空则已存在root元素
        boolean hasManyRoot = !rootList.isEmpty();
        rootList.add(data);
        if (withCheck && hasManyRoot) {
            throw new IllegalArgumentException("has many root, please check it: " + rootList);
        }
    }

    /**
     * 添加root元素
     *
     * @param rootList
     * @param data
     * @param <T>
     */
    static <T> void addRoot(final List<T> rootList, final T data) {
        addRoot(rootList, data, true);
    }

    /**
     * 检查rootList是否合法
     *
     * @param rootList
     * @param <T>
     */
    static <T> void checkRootList(final List<T> rootList) {
        if (rootList.isEmpty()) {
            //必须存在root
            throw new IllegalArgumentException("can't found root, please check param config or source data must be has only one root");
        }

        if (rootList.size() > 1) {
            //root size必须为1
            throw new IllegalArgumentException("has many root, please check param config or source data must be has only one root");
        }
    }
}
