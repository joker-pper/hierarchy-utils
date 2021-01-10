package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.jokerpper.hierarchy.model.Menu;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.stream.Collectors;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HierarchyUtilsTest extends HierarchyBaseTest {

    /**
     * 场景一: 通过原数据结构返回树形数据
     */
    @Test
    public void t1() {
        //默认根元素为-1 (当前所有一级菜单的pid为-1,可根据实际定义根元素使用)
        Integer rootId = -1;

        //排序(需注意业务属性值是否为空),可选
        Comparator<Menu> comparator = new Comparator<Menu>() {
            @Override
            public int compare(Menu o1, Menu o2) {
                return Integer.compare(o1.getSort(), o2.getSort());
            }
        };

        HierarchyUtils.HierarchyFunctions<Menu, Integer, Menu> defaultFunctions = new HierarchyUtils.HierarchyFunctions<>();

        //获取pid
        defaultFunctions.setGetPidFunction(data -> data.getPid());

        //获取id
        defaultFunctions.setGetIdFunction(data -> data.getId());

        //验证是否为root pid
        defaultFunctions.setIsRootPidFunction(pid -> Objects.equals(rootId, pid));

        //设置children
        defaultFunctions.setSetChildrenFunction((parent, children) -> {
            parent.setChildren(children);
        });

        //是否返回root元素(未设置时默认false,开启时root元素必须存在)
        defaultFunctions.setIsWithRoot(() -> false);

        //过滤条件(可选,用来筛选数据)
        defaultFunctions.setFilterPredicate(menu -> true);

        List<Menu> defaultResults = HierarchyUtils.getHierarchyResult(
                menuList,
                defaultFunctions,
                comparator
        );

        System.out.println(JSONObject.toJSONString(defaultResults));


    }


    /**
     * 场景二: 原数据结构未定义children,通过转换数据结构返回树形数据
     */
    @Test
    public void t2() {
        //默认根元素为-1 (当前所有一级菜单的pid为-1,可根据实际定义根元素使用)
        Integer rootId = -1;

        //排序(需注意业务属性值是否为空),可选
        Comparator<Menu> comparator = new Comparator<Menu>() {
            @Override
            public int compare(Menu o1, Menu o2) {
                return Integer.compare(o1.getSort(), o2.getSort());
            }
        };

        HierarchyUtils.HierarchyFunctions<Menu, Integer, JSONObject> transferFunctions = new HierarchyUtils.HierarchyFunctions<>();

        //获取pid
        transferFunctions.setGetPidFunction(data -> data.getPid());

        //获取id
        transferFunctions.setGetIdFunction(data -> data.getId());

        //验证是否为root pid
        transferFunctions.setIsRootPidFunction(pid -> Objects.equals(rootId, pid));

        //设置转换函数
        transferFunctions.setTransferFunction(menu -> {
            //转换数据
            JSONObject result = (JSONObject) JSON.toJSON(menu);

            //可定义或移除属性
            result.put("title", menu.getName());
            result.put("order", menu.getSort());

            result.put("newName", menu.getName());
            result.remove("name");

            return result;
        });

        //设置children
        transferFunctions.setSetChildrenFunction((parent, children) -> {
            parent.put("children", children);
        });

        //是否返回root元素(未设置时默认false,开启时root元素必须存在)
        transferFunctions.setIsWithRoot(() -> false);

        //过滤条件(可选,用来筛选数据)
        transferFunctions.setFilterPredicate(menu -> true);

        List<JSONObject> transferResults = HierarchyUtils.getHierarchyResult(
                menuList,
                transferFunctions,
                comparator
        );

        System.out.println(JSONObject.toJSONString(transferResults));

    }

    /**
     * simple test
     */
    @Test
    public void testWithLinkHashMap() {

        List<LinkedHashMap> menuList = JSONObject.parseArray(menuText, LinkedHashMap.class);

        Object rootId = 1;

        HierarchyUtils.HierarchyFunctions<LinkedHashMap, Object, LinkedHashMap> functions = new HierarchyUtils.HierarchyFunctions<>();

        //获取pid
        functions.setGetPidFunction(data -> data.get("pid"));

        //获取id
        functions.setGetIdFunction(data -> data.get("id"));

        //验证是否为root pid
        functions.setIsRootPidFunction(pid -> Objects.equals(rootId, pid));

        //设置children
        functions.setSetChildrenFunction((parent, children) -> {
            parent.put("children", children);
        });

        Comparator comparator = new Comparator<LinkedHashMap>() {
            @Override
            public int compare(LinkedHashMap o1, LinkedHashMap o2) {
                return Integer.compare((int) o1.get("sort"), (int) o2.get("sort"));
            }
        };

        /**  验证root元素不存在 (默认)  **/

        List<LinkedHashMap> defaultResults = HierarchyUtils.getHierarchyResult(
                menuList,
                functions,
                comparator
        );
        Assert.assertEquals(0, defaultResults.stream().filter(it -> Objects.equals(it.get("id"), rootId)).count());

        //所有元素pid都为root pid
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

    @Test(expected = NullPointerException.class)
    public void testFunctionsNull() {
        HierarchyUtils.getHierarchyResult(null, null
                , null);
    }

    @Test
    public void testWithGetChildrenFunction() {

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