package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.jokerpper.hierarchy.model.Menu;
import com.github.jokerpper.hierarchy.support.HierarchyValidateHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class HierarchySamplesTest {

    private List<Menu> menuList;

    @Before
    public void setUp() throws Exception {
        //查询当前用户的菜单列表(可模拟数据)
        //menuList = menuService.findAllByUserId(1);
        menuList = JSONObject.parseArray(HierarchyDataSource.MENU_TEXT, Menu.class);
    }

    /**
     * 场景一: 通过原数据结构返回树形数据
     */
    @Test
    public void hierarchyResultByDefault() {
        //默认根元素为-1 (当前所有一级菜单的pid为-1,可根据实际定义根元素使用)
        Integer rootId = -1;

        //排序(需注意业务属性值是否为空),可选
        Comparator<Menu> comparator = new Comparator<Menu>() {
            @Override
            public int compare(Menu o1, Menu o2) {
                return Integer.compare(o1.getSort(), o2.getSort());
            }
        };

        if (new Random().nextBoolean()) {
            comparator = new Comparator<Menu>() {
                @Override
                public int compare(Menu o1, Menu o2) {
                    return Integer.compare(o1.getId(), o2.getId());
                }
            };
        }

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

        //验证是否为期望的排序结果(忽略,用于test验证输出结果是否正确)
        HierarchyValidateHelper.assertSameSortedResult(defaultResults, comparator);
    }

    /**
     * 场景二: 原数据结构未定义children,通过转换数据结构返回树形数据
     */
    @Test
    public void hierarchyResultByTransfer() {
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
     * 场景三: 返回源数据列表中id为rootId的元素或pid为rootId且id能整除2的全部子元素的数据列表
     */
    @Test
    public void hierarchyFlatResult() {
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

        //对返回结果排序(需注意业务属性值是否为空),可选
        HierarchySortUtils.sort(matchResults, comparator);

        System.out.println(JSONObject.toJSONString(matchResults));

        //验证是否为期望的排序结果(忽略,用于test验证输出结果是否正确)
        HierarchyValidateHelper.assertSameSortedResult(matchResults, comparator);
    }

}
