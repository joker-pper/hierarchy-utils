package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSONObject;
import com.github.jokerpper.hierarchy.model.Menu;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class HierarchyFlatUtilsTest extends HierarchyBaseTest {

    /**
     * 场景: 返回源数据列表中id为rootId的元素或pid为rootId且id能整除2的全部子元素的数据列表
     */
    @Test
    public void t() {

        Integer rootId = 1;

        HierarchyFlatUtils.HierarchyFlatFunctions<Menu, Integer, Menu> functions = new HierarchyFlatUtils.HierarchyFlatFunctions<>();

        //获取pid
        functions.setGetPidFunction(data -> data.getPid());

        //获取id
        functions.setGetIdFunction(data -> data.getId());

        //验证是否为root pid
        functions.setIsRootPidFunction(pid -> Objects.equals(rootId, pid));

        //是否返回root元素(未设置时默认false,开启时root元素必须存在)
        functions.setIsWithRoot(() -> true);

        //是否返回全部的子元素(未设置时默认false,即默认只返回root元素的直接子元素)
        functions.setIsWithAllChildren(() -> true);

        //过滤条件(可选,用来筛选数据)
        functions.setFilterPredicate(menu -> menu.getId() % 2 == 0 || Objects.equals(rootId, menu.getId()));

        //排序(需注意业务属性值是否为空),可选
        Comparator<Menu> comparator = new Comparator<Menu>() {
            @Override
            public int compare(Menu o1, Menu o2) {
                return Integer.compare(o1.getSort(), o2.getSort());
            }
        };

        List<Menu> matchResults = HierarchyFlatUtils.getHierarchyFlatResult(
                menuList,
                functions,
                comparator
        );

        System.out.println(JSONObject.toJSONString(matchResults));

    }

    /**
     * simple test
     */
    @Test
    public void testWithMenu() {

        Integer rootId = 1;

        HierarchyFlatUtils.HierarchyFlatFunctions<Menu, Integer, LinkedHashMap> functions = new HierarchyFlatUtils.HierarchyFlatFunctions<>();

        //获取pid
        functions.setGetPidFunction(data -> data.getPid());

        //获取id
        functions.setGetIdFunction(data -> data.getId());

        //验证是否为root pid
        functions.setIsRootPidFunction(pid -> Objects.equals(rootId, pid));

        //设置返回全部的子元素
        functions.setIsWithAllChildren(() -> true);

        //设置开启转换
        functions.setIsEnableTransfer(() -> true);

        //转换
        functions.setTransferFunction(menu -> {
            LinkedHashMap result = new LinkedHashMap();
            result.put("id", menu.getId());
            result.put("name", menu.getName());
            result.put("pid", menu.getPid());
            result.put("sort", menu.getSort());
            return result;
        });

        //排序(需注意业务属性值是否为空),可选
        Comparator<Menu> comparator = new Comparator<Menu>() {
            @Override
            public int compare(Menu o1, Menu o2) {
                return Integer.compare(o1.getSort(), o2.getSort());
            }
        };

        /**  验证root元素不存在 (默认)  **/

        List<LinkedHashMap> defaultResults = HierarchyFlatUtils.getHierarchyFlatResult(
                menuList,
                functions,
                comparator
        );
        Assert.assertEquals(0, defaultResults.stream().filter(it -> Objects.equals(it.get("id"), rootId)).count());


        /**  验证root元素存在  **/

        functions.setIsWithRoot(() -> true);
        List<LinkedHashMap> withRootResults = HierarchyFlatUtils.getHierarchyFlatResult(
                menuList,
                functions,
                comparator
        );
        Assert.assertEquals(1, withRootResults.stream().filter(it -> Objects.equals(it.get("id"), rootId)).count());


        /**  验证设置不包含全部子元素及不包含root元素  **/
        functions.setIsWithAllChildren(() -> false);
        functions.setIsWithRoot(() -> false);

        List<LinkedHashMap> withOutRootAndWithoutAllChildrenResults = HierarchyFlatUtils.getHierarchyFlatResult(
                menuList,
                functions,
                comparator
        );

        //所有元素pid都为root pid
        Assert.assertEquals(new HashSet<>(Arrays.asList(rootId)), withOutRootAndWithoutAllChildrenResults.stream()
                .map(it -> (Integer) it.get("pid")).collect(Collectors.toSet()));

        //所有元素id与源数据的rootId子元素的id一致
        Assert.assertEquals(menuList.stream().filter(it -> Objects.equals(it.getPid(), rootId))
                        .map(it -> it.getId()).collect(Collectors.toSet()),
                withOutRootAndWithoutAllChildrenResults.stream()
                        .map(it -> (Integer) it.get("id")).collect(Collectors.toSet()));



        /** 验证数据源为null/空 **/
        Assert.assertNotNull(HierarchyFlatUtils.getHierarchyFlatResult(null, functions
                , null));
        Assert.assertEquals(true, HierarchyFlatUtils.getHierarchyFlatResult(null, functions
                , null).isEmpty());

        Assert.assertEquals(true, HierarchyFlatUtils.getHierarchyFlatResult(new ArrayList<>(), functions
                , null) != null);
        Assert.assertEquals(true, HierarchyFlatUtils.getHierarchyFlatResult(new ArrayList<>(), functions
                , null).isEmpty());

        /** 验证数据源和comparator **/
        Assert.assertNotNull(HierarchyFlatUtils.getHierarchyFlatResult(new ArrayList<>(), functions
                , comparator));
        Assert.assertNotNull(HierarchyFlatUtils.getHierarchyFlatResult(menuList, functions
                , null));

    }


    @Test(expected = NullPointerException.class)
    public void testFunctionsNull() {
        HierarchyFlatUtils.getHierarchyFlatResult(null, null
                , null);
    }

    @Test
    public void testWithGetChildrenFunction() {
        Integer rootId = 1;
        HierarchyFlatUtils.HierarchyFlatFunctions<Menu, Integer, Menu> functions = MenuResolver.getFlatFunctions(rootId);
        Comparator<Menu> comparator = MenuResolver.getComparator();

        //设置过滤条件
        functions.setFilterPredicate((data) -> true);

        //设置返回全部children
        functions.setIsWithAllChildren(() -> true);


        //获取通过源数据列表处理的结果
        List<Menu> defaultFlatResults = HierarchyFlatUtils.getHierarchyFlatResult(
                menuList,
                functions,
                comparator
        );

        //获取通过树形数据处理的结果
        List<Menu> treeResults = MenuResolver.getResolvedWithChildrenMenuList(rootId);

        //设置获取children
        functions.setGetChildrenFunction((data) -> data.getChildren());

        List<Menu> flatWithTreeResults = HierarchyFlatUtils.getHierarchyFlatResult(
                treeResults,
                functions,
                comparator
        );

        //验证结果一致
        Assert.assertEquals(defaultFlatResults, flatWithTreeResults);

    }
}