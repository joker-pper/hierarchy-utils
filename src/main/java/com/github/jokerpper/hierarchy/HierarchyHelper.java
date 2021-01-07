package com.github.jokerpper.hierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author joker-pper 2021-01-03
 */
public class HierarchyHelper {

    /**
     * 将sourceList中的所有元素(包含子元素)全部放入allSourceList中
     *
     * @param sourceList      源数据列表
     * @param allSourceList   全部数据列表
     * @param filterPredicate 过滤条件
     * @param <T>
     */
    static <T> void resolveToAllSourceListWithPredicate(final List<T> sourceList, final List<T> allSourceList
            , final Predicate<T> filterPredicate) {
        for (T source : sourceList) {
            if (filterPredicate.test(source)) {
                allSourceList.add(source);
            }
        }
    }

    /**
     * 将sourceList中的所有元素(包含子元素)全部放入allSourceList中
     *
     * @param sourceList          源数据列表
     * @param allSourceList       全部数据列表
     * @param getChildrenFunction 获取children函数
     * @param <T>
     */
    static <T> void resolveToAllSourceList(final List<T> sourceList, final List<T> allSourceList
            , final Function<T, List<T>> getChildrenFunction) {
        for (T source : sourceList) {
            allSourceList.add(source);
            List<T> sourceChildrenList = resolveGetChildren(getChildrenFunction, source);
            if (sourceChildrenList != null && !sourceChildrenList.isEmpty()) {
                resolveToAllSourceList(sourceChildrenList, allSourceList, getChildrenFunction);
            }
        }
    }

    /**
     * 将sourceList中的所有元素(包含子元素)全部放入allSourceList中
     *
     * @param sourceList          源数据列表
     * @param allSourceList       全部数据列表
     * @param getChildrenFunction 获取children函数
     * @param filterPredicate     过滤条件
     * @param <T>
     */
    static <T> void resolveToAllSourceListWithPredicate(final List<T> sourceList, final List<T> allSourceList
            , final Function<T, List<T>> getChildrenFunction, final Predicate<T> filterPredicate) {
        for (T source : sourceList) {
            if (filterPredicate.test(source)) {
                allSourceList.add(source);
            }
            List<T> sourceChildrenList = resolveGetChildren(getChildrenFunction, source);
            if (sourceChildrenList != null && !sourceChildrenList.isEmpty()) {
                resolveToAllSourceListWithPredicate(sourceChildrenList, allSourceList, getChildrenFunction, filterPredicate);
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
    static <T> List<T> resolveGetChildren(final Function<T, List<T>> getChildrenFunction, final T source) {
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
    static <T> void resolveSetChildren(final BiConsumer<T, List<T>> setChildrenFunction, final T source, final List<T> children) {
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
     * 获取元素id所对应的子元素(但不包含root pid)
     *
     * @param toResolveSourceList 待处理的数据元素列表
     * @param getPidFunction      获取 pid 函数
     * @param isRootPidFunction   是否为 root pid 函数
     * @param <T>
     * @param <V>
     * @return
     */
    static <T, V> Map<V, List<T>> initAndGetIdChildrenResultMap(final List<T> toResolveSourceList
            , final Function<T, V> getPidFunction
            , final Function<V, Boolean> isRootPidFunction) {

        Map<V, List<T>> resultMap = new HashMap<>(32);
        for (T toResolveSource : toResolveSourceList) {
            //获取pid
            V pid = getPidFunction.apply(toResolveSource);
            if (isRootPidFunction.apply(pid)) {
                //为root pid时跳过
                continue;
            }

            //将pid对应的数据列表放入map中
            List<T> pidSourceList = resultMap.get(pid);
            if (pidSourceList == null) {
                pidSourceList = new ArrayList<>(32);
                resultMap.put(pid, pidSourceList);
            }
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
