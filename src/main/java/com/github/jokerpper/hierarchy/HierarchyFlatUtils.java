package com.github.jokerpper.hierarchy;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author joker-pper 2021-01-03
 */
public class HierarchyFlatUtils {

    //Suppresses default constructor, Don't let anyone instantiate this class.
    private HierarchyFlatUtils() {
    }

    public static class HierarchyFlatFunctions<T, V, R> {

        /**
         * 判断是否为root函数
         * 必选
         */
        private Function<V, Boolean> isRootFunction;

        /**
         * 获取 pid函数
         * 必选
         */
        private Function<T, V> getPidFunction;

        /**
         * 获取 id函数
         * 必选
         */
        private Function<T, V> getIdFunction;

        /**
         * 获取children函数
         * 可选,存在时读取对应元素的子元素
         */
        private Function<T, List<T>> getChildrenFunction;

        /**
         * 转换函数
         * 可选
         */
        private Function<T, R> transferFunction;

        /**
         * 过滤条件
         * 可选
         */
        private Predicate<T> filterPredicate;

        /**
         * 是否启用转换
         * 可选,未指定时当transferFunction不为空为true
         */
        private Supplier<Boolean> isEnableTransfer;

        /**
         * 是否返回全部相关的子元素
         * 可选,默认false,(false时只返回pid所对应的子元素)
         */
        private Supplier<Boolean> isWithAllChildren;

        /**
         * 是否以root元素作为根
         * 可选,默认false,当开启时必须存在root且只允许存在一个
         */
        private Supplier<Boolean> isWithRoot;

        public Function<V, Boolean> getIsRootFunction() {
            return isRootFunction;
        }

        public void setIsRootFunction(Function<V, Boolean> isRootFunction) {
            this.isRootFunction = isRootFunction;
        }

        public Function<T, V> getGetPidFunction() {
            return getPidFunction;
        }

        public void setGetPidFunction(Function<T, V> getPidFunction) {
            this.getPidFunction = getPidFunction;
        }

        public Function<T, V> getGetIdFunction() {
            return getIdFunction;
        }

        public void setGetIdFunction(Function<T, V> getIdFunction) {
            this.getIdFunction = getIdFunction;
        }

        public Function<T, List<T>> getGetChildrenFunction() {
            return getChildrenFunction;
        }

        public void setGetChildrenFunction(Function<T, List<T>> getChildrenFunction) {
            this.getChildrenFunction = getChildrenFunction;
        }

        public Function<T, R> getTransferFunction() {
            return transferFunction;
        }

        public void setTransferFunction(Function<T, R> transferFunction) {
            this.transferFunction = transferFunction;
        }

        public Predicate<T> getFilterPredicate() {
            return filterPredicate;
        }

        public void setFilterPredicate(Predicate<T> filterPredicate) {
            this.filterPredicate = filterPredicate;
        }

        public Supplier<Boolean> getIsEnableTransfer() {
            return isEnableTransfer;
        }

        public void setIsEnableTransfer(Supplier<Boolean> isEnableTransfer) {
            this.isEnableTransfer = isEnableTransfer;
        }

        public Supplier<Boolean> getIsWithAllChildren() {
            return isWithAllChildren;
        }

        public void setIsWithAllChildren(Supplier<Boolean> isWithAllChildren) {
            this.isWithAllChildren = isWithAllChildren;
        }

        public Supplier<Boolean> getIsWithRoot() {
            return isWithRoot;
        }

        public void setIsWithRoot(Supplier<Boolean> isWithRoot) {
            this.isWithRoot = isWithRoot;
        }
    }

    /**
     * 将源数据列表中相关符合的数据列表进行返回 (同时支持树形数据的元素)
     * <p>
     * 若开启返回root元素,则root元素默认为第一个元素
     * 若有设置返回全部子元素时会导致结果乱序
     * <p>
     *
     * <p>
     * 若对结果有排序需要,可通过 HierarchySortUtils 进行排序
     * <p>
     *
     * @param sourceList 源数据列表
     * @param functions
     * @param <T>
     * @param <R>
     * @param <V>
     * @return
     * @see HierarchySortUtils
     */
    public static <T, R, V> List<R> getHierarchyFlatResult(final List<T> sourceList,
                                                           final HierarchyFlatFunctions<T, V, R> functions) {
        return getHierarchyFlatResult(sourceList, functions, null);
    }

    /**
     * 将源数据列表中相关符合的数据列表进行返回 (同时支持树形数据的元素)
     * <p>
     * 若开启返回root元素,则root元素默认为第一个元素
     * 若有设置返回全部子元素时会导致结果乱序
     * <p>
     *
     * <p>
     * 若对结果有排序需要,可通过 HierarchySortUtils 进行排序
     * <p>
     *
     * @param sourceList 源数据列表
     * @param functions
     * @param comparator 可选 存在时会对筛选后的源数据列表进行排序
     * @param <T>
     * @param <R>
     * @param <V>
     * @return
     * @see HierarchySortUtils
     */
    public static <T, R, V> List<R> getHierarchyFlatResult(final List<T> sourceList,
                                                           final HierarchyFlatFunctions<T, V, R> functions,
                                                           final Comparator<T> comparator) {
        //检查参数
        Objects.requireNonNull(functions, "functions must be not null");
        Function<V, Boolean> isRootFunction = functions.getIsRootFunction();
        Function<T, V> getPidFunction = functions.getGetPidFunction();
        Function<T, V> getIdFunction = functions.getGetIdFunction();
        Function<T, List<T>> getChildrenFunction = functions.getGetChildrenFunction();
        Function<T, R> transferFunction = functions.getTransferFunction();
        Predicate<T> filterPredicate = functions.getFilterPredicate();

        Objects.requireNonNull(isRootFunction, "is root function must be not null");
        Objects.requireNonNull(getPidFunction, "get pid function must be not null");
        Objects.requireNonNull(getIdFunction, "get id function must be not null");

        //是否启用转换
        boolean isEnableTransfer = HierarchyHelper.getBooleanValue(functions.getIsEnableTransfer(), transferFunction != null);
        if (isEnableTransfer) {
            Objects.requireNonNull(transferFunction, "when enable transfer, transfer function must be not null");
        }

        //是否返回全部的子元素(未设置时默认false)
        boolean isWithAllChildren = HierarchyHelper.getBooleanValue(functions.getIsWithAllChildren(), false);
        //是否返回root元素(未设置时默认false,开启时root元素必须存在)
        boolean isWithRoot = HierarchyHelper.getBooleanValue(functions.getIsWithRoot(), false);

        //检查数据是否为空
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> toResolveSourceList = null;
        Map<V, List<T>> toResolveSourceIdChildrenMap = null;

        try {
            //获取当前要处理的元素列表
            toResolveSourceList = HierarchyHelper.getApplySourceList(sourceList, getChildrenFunction, filterPredicate);

            //进行排序数据列表
            if (comparator != null && toResolveSourceList.size() > 1) {
                Collections.sort(toResolveSourceList, comparator);
            }

            //获取元素id所对应的子元素(但不包含root pid)
            toResolveSourceIdChildrenMap = HierarchyHelper.initAndGetIdChildrenResultMap(toResolveSourceList, getIdFunction, getPidFunction, isRootFunction);

            //处理数据
            List<R> rootList = isWithRoot ? new ArrayList<>(2) : null;
            List<R> results = new ArrayList<>(1024);

            if (!isEnableTransfer) {
                for (T toResolveSource : toResolveSourceList) {
                    resolveHierarchyWithoutEnableTransfer(results, toResolveSource
                            , toResolveSourceIdChildrenMap, rootList
                            , isRootFunction, getPidFunction
                            , getIdFunction, isWithAllChildren);
                }
            } else {
                for (T toResolveSource : toResolveSourceList) {
                    R transferResult = HierarchyHelper.getTransferResult(transferFunction, toResolveSource);
                    resolveHierarchyWithEnableTransfer(results, toResolveSource, transferResult
                            , toResolveSourceIdChildrenMap, rootList
                            , isRootFunction, getPidFunction
                            , getIdFunction, transferFunction
                            , isWithAllChildren);
                }
            }

            if (!isWithRoot) {
                //不包含root时直接返回
                return results;
            }

            //检查rootList是否合法
            HierarchyHelper.checkRootList(rootList);

            //将root元素添加到开始位置
            results.addAll(0, rootList);

            return results;
        } finally {

            if (toResolveSourceList != null) {
                toResolveSourceList.clear();
            }

            if (toResolveSourceIdChildrenMap != null) {
                toResolveSourceIdChildrenMap.clear();
            }

        }
    }


    private static <T, R, V> void resolveHierarchyWithoutEnableTransfer(final List<R> results, final T toResolveSource
            , final Map<V, List<T>> toResolveSourceIdChildrenMap, final List<R> rootList
            , final Function<V, Boolean> isRootFunction, final Function<T, V> getPidFunction
            , final Function<T, V> getIdFunction, final boolean isWithAllChildren) {
        V id = getIdFunction.apply(toResolveSource);
        V pid = getPidFunction.apply(toResolveSource);
        R transferResult = (R) toResolveSource;
        boolean isRoot = isRootFunction.apply(id);
        if (isRoot) {
            //启用root时且当前元素为root放入rootList
            if (rootList != null) {
                HierarchyHelper.addRoot(rootList, transferResult);
            }
            return;
        }

        boolean isRootDirectChild = isRootFunction.apply(pid);
        if (isRootDirectChild) {
            //是root直接子元素时
            results.add(transferResult);
            //处理相关children
            if (isWithAllChildren) {
                resolveWithAllChildren(results, toResolveSourceIdChildrenMap, getIdFunction, id);
            }
        }
    }

    private static <T, R, V> void resolveHierarchyWithEnableTransfer(final List<R> results, final T toResolveSource, final R transferResult
            , final Map<V, List<T>> toResolveSourceIdChildrenMap, final List<R> rootList
            , final Function<V, Boolean> isRootFunction, final Function<T, V> getPidFunction
            , final Function<T, V> getIdFunction, Function<T, R> transferFunction
            , final boolean isWithAllChildren) {

        V id = getIdFunction.apply(toResolveSource);
        V pid = getPidFunction.apply(toResolveSource);
        boolean isRoot = isRootFunction.apply(id);
        if (isRoot) {
            //启用root时且当前元素为root放入rootList
            if (rootList != null) {
                HierarchyHelper.addRoot(rootList, transferResult);
            }
            return;
        }

        boolean isRootDirectChild = isRootFunction.apply(pid);
        if (isRootDirectChild) {
            //是root直接子元素时
            results.add(transferResult);

            //处理相关children
            if (isWithAllChildren) {
                resolveWithAllChildren(results, toResolveSourceIdChildrenMap
                        , getIdFunction, transferFunction
                        , id);
            }
        }
    }

    private static <T, R, V> void resolveWithAllChildren(final List<R> results, final Map<V, List<T>> toResolveSourceIdChildrenMap
            , final Function<T, V> getIdFunction, final V id) {
        List<T> currentSourceChildrenList = toResolveSourceIdChildrenMap.get(id);
        if (currentSourceChildrenList != null && !currentSourceChildrenList.isEmpty()) {
            for (T currentSourceChild : currentSourceChildrenList) {
                //添加各子元素的子元素到结果中
                resolveWithAllChildren(results, toResolveSourceIdChildrenMap, getIdFunction, getIdFunction.apply(currentSourceChild));
            }
            results.addAll((List<R>) currentSourceChildrenList);
        }
    }


    private static <T, R, V> void resolveWithAllChildren(final List<R> results, final Map<V, List<T>> toResolveSourceIdChildrenMap
            , final Function<T, V> getIdFunction
            , final Function<T, R> transferFunction
            , final V id) {
        List<T> currentSourceChildrenList = toResolveSourceIdChildrenMap.get(id);
        if (currentSourceChildrenList != null && !currentSourceChildrenList.isEmpty()) {
            List<R> transferChildrenList = new ArrayList<>(currentSourceChildrenList.size());
            for (T currentSourceChild : currentSourceChildrenList) {
                //添加各子元素的子元素到结果中
                resolveWithAllChildren(results, toResolveSourceIdChildrenMap
                        , getIdFunction, transferFunction
                        , getIdFunction.apply(currentSourceChild));
                R transferChildResult = HierarchyHelper.getTransferResult(transferFunction, currentSourceChild);
                transferChildrenList.add(transferChildResult);
            }
            results.addAll(transferChildrenList);
        }
    }

}

