package com.github.jokerpper.hierarchy;

import com.github.jokerpper.hierarchy.model.Menu;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public abstract class HierarchyBaseTest {

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

            //验证是否为root
            baseFunctions.setIsRootFunction(id -> Objects.equals(rootId, id));

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

            //验证是否为root
            functions.setIsRootFunction(id -> Objects.equals(rootId, id));
            return functions;
        }

        /**
         * 获取处理过的树形数据
         *
         * @param rootId
         * @return
         */
        static List<Menu> getResolvedWithChildrenMenuList(Integer rootId) {
            return HierarchyUtils.getHierarchyResult(HierarchyMetadata.getDefaultMenuList(), getFunctions(rootId), getComparator());
        }

    }

}
