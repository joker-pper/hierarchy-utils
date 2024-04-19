package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.jokerpper.hierarchy.model.Menu;
import com.github.jokerpper.hierarchy.support.HierarchyValidateHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HierarchySamplesTest {

    private List<Menu> menuList;

    @Before
    public void setUp() throws Exception {
        //查询当前用户的菜单列表(可模拟数据)
        //menuList = menuService.findAllByUserId(1);
        menuList = HierarchyMetadata.getDefaultMenuList();
    }

    /**
     * 场景1: 通过原数据结构返回树形数据
     */
    @Test
    public void hierarchyResultByDefault() {
        //默认根元素为-1 (当前所有一级菜单的pid为-1,可根据实际定义根元素使用)
        Integer rootId = -1;

        //排序(需注意业务属性值是否为空),可选
        Comparator<Menu> comparator = Comparator.comparingInt(Menu::getSort);

        HierarchyUtils.HierarchyFunctions<Menu, Integer, Menu> defaultFunctions = new HierarchyUtils.HierarchyFunctions<>();

        //获取pid
        defaultFunctions.setGetPidFunction(data -> data.getPid());

        //获取id
        defaultFunctions.setGetIdFunction(data -> data.getId());

        //验证是否为root
        defaultFunctions.setIsRootFunction(id -> Objects.equals(rootId, id));

        //设置children
        defaultFunctions.setSetChildrenFunction((parent, children) -> parent.setChildren(children));

        //是否返回root元素(未设置时默认false,开启时root元素必须存在)
        defaultFunctions.setIsWithRoot(() -> false);

        //过滤条件(可选,用来筛选数据)
        defaultFunctions.setFilterPredicate(menu -> true);

        //获取结果(注意: 原数据列表会被改变,如有需要请在执行前进行备份原数据..)
        List<Menu> hierarchyResult = HierarchyUtils.getHierarchyResult(
                menuList,
                defaultFunctions,
                comparator
        );
        System.out.println(JSONObject.toJSONString(hierarchyResult));

        //验证与期望结果是否一致(比较一致时通过验证)
        Assert.assertTrue(Objects.equals(HierarchyMetadata.getDefaultMenuTreeList(), hierarchyResult));

        //验证原数据已被改变...
        Assert.assertNotEquals(HierarchyMetadata.getDefaultMenuList(), menuList);

    }

    /**
     * 场景2: 若原数据结构未定义children或不使用原数据结构，可通过转换数据结构（使用Map / JSONObject等）进行返回树形数据
     */
    @Test
    public void hierarchyResultByTransfer() {
        //默认根元素为-1 (当前所有一级菜单的pid为-1,可根据实际定义根元素使用)
        Integer rootId = -1;

        //排序(需注意业务属性值是否为空),可选
        Comparator<Menu> comparator = Comparator.comparingInt(Menu::getSort);

        HierarchyUtils.HierarchyFunctions<Menu, Integer, JSONObject> transferFunctions = new HierarchyUtils.HierarchyFunctions<>();

        //获取pid
        transferFunctions.setGetPidFunction(data -> data.getPid());

        //获取id
        transferFunctions.setGetIdFunction(data -> data.getId());

        //验证是否为root
        transferFunctions.setIsRootFunction(id -> Objects.equals(rootId, id));

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
        transferFunctions.setSetChildrenFunction((parent, children) -> parent.put("children", children));

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

        //验证与期望结果是否一致(字符串一致时通过验证)
        Assert.assertEquals(JSON.toJSONString(HierarchyMetadata.getDefaultMenuTransferSampleTreeList()), JSON.toJSONString(transferResults));

        //验证原数据没有被改变...
        Assert.assertEquals(HierarchyMetadata.getDefaultMenuList(), menuList);


    }


    /**
     * 场景3-1: 返回源数据列表中id为rootId的元素或pid为rootId且id能整除2的全部子元素的数据列表 （支持将树形数据打平及过滤）
     */
    @Test
    public void hierarchyFlatResult() {
        //用于筛选指定id作为根
        Integer rootId = 1;

        HierarchyFlatUtils.HierarchyFlatFunctions<Menu, Integer, Menu> functions = new HierarchyFlatUtils.HierarchyFlatFunctions<>();

        //获取pid
        functions.setGetPidFunction(data -> data.getPid());

        //获取id
        functions.setGetIdFunction(data -> data.getId());

        //验证是否为root
        functions.setIsRootFunction(id -> Objects.equals(rootId, id));

        //是否返回root元素(未设置时默认false,开启时root元素必须存在)
        functions.setIsWithRoot(() -> true);

        //是否返回全部的子元素(未设置时默认false,即默认只返回root元素的直接子元素)
        functions.setIsWithAllChildren(() -> true);

        //设置获取children(如果当前数据列表为递归树数据,需要进行设置,否则子级数据会被跳过)
        functions.setGetChildrenFunction((data) -> data.getChildren());

        //过滤条件(可选,用来筛选数据)
        functions.setFilterPredicate(menu -> menu.getId() % 2 == 0 || Objects.equals(rootId, menu.getId()));

        //排序(需注意业务属性值是否为空),可选 -- 用于排序当前要处理的数据列表
        Comparator<Menu> comparator = Comparator.comparingInt(Menu::getSort);

        //获取打平后的结果(如果为递归树数据,children会被保留)
        List<Menu> matchResults = HierarchyFlatUtils.getHierarchyFlatResult(
                menuList,
                functions,
                comparator
        );

        //对返回结果排序(需注意业务属性值是否为空),可选
        HierarchySortUtils.sort(matchResults, comparator);
        System.out.println("matchResults: " + JSONObject.toJSONString(matchResults));

        //验证是否为期望的排序结果(忽略,用于test验证输出结果是否正确)
        HierarchyValidateHelper.assertSameSortedResult(matchResults, comparator);

        //验证与期望结果是否一致(互相全部包含时通过验证)
        Assert.assertTrue(HierarchyMetadata.getDefaultMenuFlatSampleList().containsAll(matchResults));
        Assert.assertTrue(matchResults.containsAll(HierarchyMetadata.getDefaultMenuFlatSampleList()));


        //验证原数据没有被改变...
        Assert.assertEquals(HierarchyMetadata.getDefaultMenuList(), menuList);


        //------------------------------------------------
        //            通过递归树数据验证结果
        //------------------------------------------------

        //获取打平后的结果(如果为递归树数据,children会被保留)
        List<Menu> matchResultsWithTreeList = HierarchyFlatUtils.getHierarchyFlatResult(
                HierarchyMetadata.getDefaultMenuTreeList(),
                functions,
                comparator
        );
        HierarchySortUtils.sort(matchResultsWithTreeList, comparator);

        System.out.println("matchResultsWithTreeList: " + JSONObject.toJSONString(matchResultsWithTreeList, SerializerFeature.DisableCircularReferenceDetect));

        //验证结果一致(不考虑children存在的情况)
        Assert.assertEquals(matchResultsWithTreeList.stream().map(Menu::getId).collect(Collectors.toList()), matchResults.stream().map(Menu::getId).collect(Collectors.toList()));
        Assert.assertEquals(matchResults.stream().map(Menu::getId).collect(Collectors.toList()), matchResultsWithTreeList.stream().map(Menu::getId).collect(Collectors.toList()));

    }

    /**
     * 场景3-2: 【转换数据示例】返回源数据列表中id为rootId的元素或pid为rootId且id能整除2的全部子元素的数据列表 （支持将树形数据打平及过滤）
     */
    @Test
    public void hierarchyFlatResultByTransfer() {
        //用于筛选指定id作为根
        Integer rootId = 1;

        HierarchyFlatUtils.HierarchyFlatFunctions<Menu, Integer, JSONObject> functions = new HierarchyFlatUtils.HierarchyFlatFunctions<>();

        //获取pid
        functions.setGetPidFunction(data -> data.getPid());

        //获取id
        functions.setGetIdFunction(data -> data.getId());

        //验证是否为root
        functions.setIsRootFunction(id -> Objects.equals(rootId, id));

        //是否返回root元素(未设置时默认false,开启时root元素必须存在)
        functions.setIsWithRoot(() -> true);

        //是否返回全部的子元素(未设置时默认false,即默认只返回root元素的直接子元素)
        functions.setIsWithAllChildren(() -> true);

        //设置获取children(如果当前数据列表为递归树数据,需要进行设置,否则子级数据会被跳过)
        functions.setGetChildrenFunction((data) -> data.getChildren());

        //设置转换函数
        functions.setTransferFunction(menu -> {
            //转换数据
            return (JSONObject) JSON.toJSON(menu);
        });

        //过滤条件(可选,用来筛选数据)
        functions.setFilterPredicate(menu -> menu.getId() % 2 == 0 || Objects.equals(rootId, menu.getId()));

        //排序(需注意业务属性值是否为空),可选 -- 用于排序当前要处理的数据列表
        Comparator<Menu> comparator = Comparator.comparingInt(Menu::getSort);

        //获取打平后的结果(如果为递归树数据,children会被保留)
        List<JSONObject> matchResults = HierarchyFlatUtils.getHierarchyFlatResult(
                menuList,
                functions,
                comparator
        );

        Comparator<JSONObject> resultComparator = Comparator.comparing((data) -> data.getInteger("sort"));

        //对返回结果排序(需注意业务属性值是否为空),可选
        HierarchySortUtils.sort(matchResults, resultComparator);
        System.out.println("matchResults: " + JSONObject.toJSONString(matchResults));

        //转换为menu list比较
        List<Menu> convertMatchResults = JSONObject.parseArray(JSONObject.toJSONString(matchResults), Menu.class);

        //验证与期望结果是否一致(互相全部包含时通过验证)
        Assert.assertTrue(HierarchyMetadata.getDefaultMenuFlatSampleList().containsAll(convertMatchResults));
        Assert.assertTrue(convertMatchResults.containsAll(HierarchyMetadata.getDefaultMenuFlatSampleList()));


        //------------------------------------------------
        //            通过递归树数据验证结果
        //------------------------------------------------


        //获取打平后的结果(如果为递归树数据,children会被保留)
        List<JSONObject> matchResultsWithTreeList = HierarchyFlatUtils.getHierarchyFlatResult(
                HierarchyMetadata.getDefaultMenuTreeList(),
                functions,
                comparator
        );
        HierarchySortUtils.sort(matchResultsWithTreeList, resultComparator);

        System.out.println("matchResultsWithTreeList: " + JSONObject.toJSONString(matchResultsWithTreeList, SerializerFeature.DisableCircularReferenceDetect));

        //转换为menu list比较
        List<Menu> convertMatchResultsWithTreeList = JSONObject.parseArray(JSONObject.toJSONString(matchResultsWithTreeList), Menu.class);

        //验证结果一致(不考虑children存在的情况)
        Assert.assertEquals(convertMatchResultsWithTreeList.stream().map(Menu::getId).collect(Collectors.toList()), convertMatchResults.stream().map(Menu::getId).collect(Collectors.toList()));
        Assert.assertEquals(convertMatchResults.stream().map(Menu::getId).collect(Collectors.toList()), convertMatchResultsWithTreeList.stream().map(Menu::getId).collect(Collectors.toList()));

    }


    /**
     * 场景4: 通过递归树数据返回pid为2且包含自己的递归树数据
     */
    @Test
    public void hierarchyResultByAppointRootId() {
        final List<Menu> menuList = HierarchyMetadata.getDefaultMenuTreeList();

        //用于筛选指定id作为根
        Integer rootId = 2;

        //排序(需注意业务属性值是否为空),可选
        Comparator<Menu> comparator = Comparator.comparingInt(Menu::getSort);

        HierarchyUtils.HierarchyFunctions<Menu, Integer, Menu> defaultFunctions = new HierarchyUtils.HierarchyFunctions<>();

        //获取pid
        defaultFunctions.setGetPidFunction(data -> data.getPid());

        //获取id
        defaultFunctions.setGetIdFunction(data -> data.getId());

        //验证是否为root
        defaultFunctions.setIsRootFunction(id -> Objects.equals(rootId, id));

        //设置children
        defaultFunctions.setSetChildrenFunction((parent, children) -> parent.setChildren(children));

        //设置获取children(如果当前数据列表为递归树数据,需要进行设置,否则子级数据会被跳过)
        defaultFunctions.setGetChildrenFunction((data) -> data.getChildren());

        //是否返回root元素(未设置时默认false,开启时root元素必须存在)
        defaultFunctions.setIsWithRoot(() -> true);

        //过滤条件(可选,用来筛选数据)
        defaultFunctions.setFilterPredicate(menu -> true);

        //获取结果(注意: 原数据列表会被改变,如有需要请在执行前进行备份原数据..)
        List<Menu> hierarchyResult = HierarchyUtils.getHierarchyResult(
                menuList,
                defaultFunctions,
                comparator
        );
        System.out.println(JSONObject.toJSONString(hierarchyResult));

        //验证原数据已被改变...
        Assert.assertNotEquals(HierarchyMetadata.getDefaultMenuTreeList(), menuList);


        //------------------------------------------------
        //            通过平铺数据验证结果一致
        //------------------------------------------------

        //设置获取children(此时平铺数据无需获取子级数据)
        defaultFunctions.setGetChildrenFunction(null);
        List<Menu> hierarchyResult2 = HierarchyUtils.getHierarchyResult(
                HierarchyMetadata.getDefaultMenuList(),
                defaultFunctions,
                comparator
        );
        Assert.assertEquals(hierarchyResult, hierarchyResult2);

    }

}
