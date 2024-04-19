package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSONObject;
import com.github.jokerpper.hierarchy.model.Menu;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.stream.Collectors;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HierarchyUtilsTest extends HierarchyBaseTest {

    /**
     * simple test
     */
    @Test
    public void testWithLinkHashMap() {

        List<LinkedHashMap> menuList = HierarchyMetadata.getDefaultMenuList(LinkedHashMap.class);

        Object rootId = 1;

        HierarchyUtils.HierarchyFunctions<LinkedHashMap, Object, LinkedHashMap> functions = new HierarchyUtils.HierarchyFunctions<>();

        //获取pid
        functions.setGetPidFunction(data -> data.get("pid"));

        //获取id
        functions.setGetIdFunction(data -> data.get("id"));

        //验证是否为root
        functions.setIsRootFunction(id -> Objects.equals(rootId, id));

        //设置children
        functions.setSetChildrenFunction((parent, children) -> {
            parent.put("children", children);
        });

        Comparator<LinkedHashMap> comparator = Comparator.comparingInt(o -> (int) o.get("sort"));

        /**  验证root元素不存在 (默认)  **/

        List<LinkedHashMap> defaultResults = HierarchyUtils.getHierarchyResult(
                menuList,
                functions,
                comparator
        );
        Assert.assertEquals(0, defaultResults.stream().filter(it -> Objects.equals(it.get("id"), rootId)).count());

        //所有元素pid都为root id
        Assert.assertEquals(new HashSet<>(Arrays.asList(rootId)), defaultResults.stream()
                .map(it -> (Integer) it.get("pid")).collect(Collectors.toSet()));

        /**  验证root元素存在  **/

        functions.setIsWithRoot(() -> true);
        List<LinkedHashMap> withRootResults = HierarchyUtils.getHierarchyResult(
                menuList,
                functions,
                comparator
        );

        //结果size为1(只包含root元素,子元素被包含在root中)
        Assert.assertEquals(1, withRootResults.size());

        //存在id为rootId的元素
        Assert.assertEquals(1, withRootResults.stream().filter(it -> Objects.equals(it.get("id"), rootId)).count());


        /**  验证转换前和转换后的json内容一致  **/

        //设置转换函数
        functions.setTransferFunction(data -> new LinkedHashMap(data));

        functions.setIsEnableTransfer(() -> false);
        List<LinkedHashMap> beforeTransferResults = HierarchyUtils.getHierarchyResult(
                menuList,
                functions,
                comparator
        );

        functions.setIsEnableTransfer(() -> true);
        List<LinkedHashMap> afterTransferResults = HierarchyUtils.getHierarchyResult(
                menuList,
                functions,
                comparator
        );

        Assert.assertEquals("结果不一致", JSONObject.toJSONString(beforeTransferResults), JSONObject.toJSONString(afterTransferResults));

        /** 验证数据源为null/空 **/
        Assert.assertNotNull(HierarchyUtils.getHierarchyResult(null, functions
                , null));
        Assert.assertEquals(true, HierarchyUtils.getHierarchyResult(null, functions
                , null).isEmpty());

        Assert.assertEquals(true, HierarchyUtils.getHierarchyResult(new ArrayList<>(), functions
                , null) != null);
        Assert.assertEquals(true, HierarchyUtils.getHierarchyResult(new ArrayList<>(), functions
                , null).isEmpty());

        /** 验证数据源和comparator **/
        Assert.assertNotNull(HierarchyUtils.getHierarchyResult(new ArrayList<>(), functions
                , comparator));
        Assert.assertNotNull(HierarchyUtils.getHierarchyResult(menuList, functions
                , null));
    }


    @Test
    public void testWithTreeList() {
        List<Menu> menuList = HierarchyMetadata.getDefaultMenuTreeList();
        String beforeJson = JSONObject.toJSONString(menuList);

        HierarchyUtils.HierarchyFunctions<Menu, Integer, Menu> functions = MenuResolver.getFunctions(-1);
        Comparator<Menu> comparator = MenuResolver.getComparator();

        //设置获取children
        functions.setGetChildrenFunction((data) -> data.getChildren());

        //通过源数据列表处理的结果
        List<Menu> defaultResults = HierarchyUtils.getHierarchyResult(menuList, functions, comparator);

        String afterJson = JSONObject.toJSONString(defaultResults);

        //验证结果一致
        Assert.assertEquals(menuList.size(), defaultResults.size());
        Assert.assertEquals(beforeJson, afterJson);
    }

    @Test
    public void testWithTreeListWithOutGetChildrenFunction() {
        List<Menu> menuList = HierarchyMetadata.getDefaultMenuTreeList();
        String beforeJson = JSONObject.toJSONString(menuList);

        HierarchyUtils.HierarchyFunctions<Menu, Integer, Menu> functions = MenuResolver.getFunctions(-1);
        Comparator<Menu> comparator = MenuResolver.getComparator();

        //设置获取children
        functions.setGetChildrenFunction(null);

        //通过源数据列表处理的结果
        List<Menu> defaultResults = HierarchyUtils.getHierarchyResult(menuList, functions, comparator);

        String afterJson = JSONObject.toJSONString(defaultResults);

        Assert.assertTrue(defaultResults.get(0).getChildren() == null || defaultResults.get(0).getChildren().isEmpty());
        Assert.assertNotEquals(beforeJson, afterJson);
    }


    @Test(expected = NullPointerException.class)
    public void testFunctionsNull() {
        HierarchyUtils.getHierarchyResult(null, null
                , null);
    }

    @Test
    public void testWithGetChildrenFunction() {
        List<Menu> menuList = HierarchyMetadata.getDefaultMenuList(Menu.class);

        HierarchyUtils.HierarchyFunctions<Menu, Integer, Menu> functions = MenuResolver.getFunctions(-1);
        Comparator<Menu> comparator = MenuResolver.getComparator();

        //通过源数据列表处理的结果
        List<Menu> defaultResults = HierarchyUtils.getHierarchyResult(menuList, functions, comparator);
        String defaultResultsText = JSONObject.toJSONString(defaultResults);

        //设置获取children
        functions.setGetChildrenFunction((data) -> data.getChildren());

        //获取通过树形数据处理的结果
        List<Menu> withGetChildrenResults = HierarchyUtils.getHierarchyResult(
                JSONObject.parseArray(defaultResultsText, Menu.class),
                functions,
                comparator
        );

        //验证结果一致
        Assert.assertEquals(defaultResults, withGetChildrenResults);
    }


}