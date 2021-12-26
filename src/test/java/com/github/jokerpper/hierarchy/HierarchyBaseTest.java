package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSONObject;
import com.github.jokerpper.hierarchy.HierarchyDataSource;
import com.github.jokerpper.hierarchy.HierarchyFlatUtils;
import com.github.jokerpper.hierarchy.HierarchyUtils;
import com.github.jokerpper.hierarchy.model.Menu;
import org.junit.After;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public abstract class HierarchyBaseTest {

    protected static String menuText = HierarchyDataSource.MENU_TEXT;

    protected static List<Menu> getSourceMenuList() {
       return JSONObject.parseArray(menuText, Menu.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    public static class MenuResolver {

        /**
         * 获取menu base comparator
         *
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
         *
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
         *
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
         *
         * @param pid
         * @return
         */
        static List<Menu> getResolvedWithChildrenMenuList(Integer pid) {
            return HierarchyUtils.getHierarchyResult(getSourceMenuList(), getFunctions(pid), getComparator());
        }

    }

}
