package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSONObject;
import com.github.jokerpper.hierarchy.model.Menu;
import org.junit.After;
import org.junit.Before;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public abstract class HierarchyBaseTest {

    protected String menuText = "[" +
            "{'id': 1, name: '父级', 'pid': -1, 'sort': 1}, " +
            "{'id': 2, name: '子级', 'pid' : 1, 'sort': 99}" +
            "{'id': 3, name: '子级', 'pid' : 2, 'sort': 1}," +
            "{'id': 4, name: '子级', 'pid' : 2, 'sort': 22}," +
            "{'id': 5, name: '子级', 'pid' : 2, 'sort': 5}," +
            "{'id': 6, name: '子级', 'pid' : 1, 'sort': 98}" +
            "{'id': 7, name: '子级', 'pid' : 1, 'sort': 92}" +
            "]";

    protected static List<Menu> menuList;

    @Before
    public void setUp() throws Exception {
        //查询当前用户的菜单列表(可模拟数据)
        //menuList = menuService.findAllByUserId(1);
        menuList = JSONObject.parseArray(menuText, Menu.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    public static class MenuResolver {

        /**
         * 获取menu base comparator
         * @return
         */
        static Comparator<Menu> getComparator() {
            return new Comparator<Menu>() {
                @Override
                public int compare(Menu o1, Menu o2) {
                    return Integer.compare(o1.getSort(), o2.getSort());
                }
            };
        }

        /**
         * 获取menu base functions
         * @param rootId
         * @return
         */
        static HierarchyUtils.HierarchyFunctions<Menu, Integer, Menu> getFunctions(Integer rootId) {
            HierarchyUtils.HierarchyFunctions<Menu, Integer, Menu> baseFunctions = new HierarchyUtils.HierarchyFunctions<>();

            //获取pid
            baseFunctions.setGetPidFunction(data -> data.getPid());

            //获取id
            baseFunctions.setGetIdFunction(data -> data.getId());

            //验证是否为root pid
            baseFunctions.setIsRootPidFunction(pid -> Objects.equals(rootId, pid));

            //设置children
            baseFunctions.setSetChildrenFunction((parent, children) -> {
                parent.setChildren(children);
            });

            return baseFunctions;
        }

        /**
         * 获取menu base flat functions
         * @param rootId
         * @return
         */
        static HierarchyFlatUtils.HierarchyFlatFunctions<Menu, Integer, Menu> getFlatFunctions(Integer rootId) {
            HierarchyFlatUtils.HierarchyFlatFunctions<Menu, Integer, Menu> functions = new HierarchyFlatUtils.HierarchyFlatFunctions<>();

            //获取pid
            functions.setGetPidFunction(data -> data.getPid());

            //获取id
            functions.setGetIdFunction(data -> data.getId());

            //验证是否为root pid
            functions.setIsRootPidFunction(pid -> Objects.equals(rootId, pid));
            return functions;
        }

        /**
         * 获取处理过的树形数据
         * @param pid
         * @return
         */
        static List<Menu> getResolvedWithChildrenMenuList(Integer pid) {
            return HierarchyUtils.getHierarchyResult(menuList, getFunctions(pid), getComparator());
        }

    }

}
