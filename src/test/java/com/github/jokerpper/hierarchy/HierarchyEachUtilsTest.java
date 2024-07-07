package com.github.jokerpper.hierarchy;

import com.github.jokerpper.hierarchy.model.Menu;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HierarchyEachUtilsTest extends HierarchyBaseTest {

    /**
     * 检查数据
     */
    @Test
    public void recursionEachCheck() {
        Integer rootId = -1;
        List<Menu> treeResults = MenuResolver.getResolvedWithChildrenMenuList(rootId);
        HierarchyEachUtils.recursionEach(treeResults, Menu::getChildren, new HierarchyEachUtils.EachCallback<Menu>() {

            @Override
            public boolean withParentList() {
                //设置启用
                return true;
            }

            @Override
            public void beforeEach(List<Menu> sourceList) {
                Assert.assertNotNull(sourceList);
                Assert.assertFalse(sourceList.isEmpty());
            }

            @Override
            public void each(int level, Menu current, Menu parent, List<Menu> parentList, List<Menu> children, boolean hasExistChildren) {

                if (hasExistChildren) {
                    Assert.assertNotNull(children);
                } else {
                    Assert.assertNull(children);
                }

                if (level == HierarchyEachUtils.FIRST_LEVEL) {
                    //第一层级时 -- 没有真实存在的父元素
                    Assert.assertNull(parent);
                    Assert.assertNull(parentList);
                } else {
                    Assert.assertNotNull(parent);
                    //启用父级列表时非第一层级一定不为Null且不为空列表
                    Assert.assertNotNull(parentList);
                    Assert.assertFalse(parentList.isEmpty());
                }
            }
        });


        treeResults = MenuResolver.getResolvedWithChildrenMenuList(rootId);
        HierarchyEachUtils.recursionEach(treeResults, Menu::getChildren, new HierarchyEachUtils.EachCallback<Menu>() {

            @Override
            public boolean withParentList() {
                //设置不启用
                return false;
            }

            @Override
            public void beforeEach(List<Menu> sourceList) {
                Assert.assertNotNull(sourceList);
                Assert.assertFalse(sourceList.isEmpty());
            }

            @Override
            public void each(int level, Menu current, Menu parent, List<Menu> parentList, List<Menu> children, boolean hasExistChildren) {

                if (hasExistChildren) {
                    Assert.assertNotNull(children);
                } else {
                    Assert.assertNull(children);
                }

                //未启用父级列表时一定为Null
                Assert.assertNull(parentList);

                if (level == HierarchyEachUtils.FIRST_LEVEL) {
                    //第一层级时 -- 没有真实存在的父元素
                    Assert.assertNull(parent);
                } else {
                    Assert.assertNotNull(parent);
                }
            }
        });

    }



    /**
     * 简单业务示例
     */
    @Test
    public void recursionEachSample() {
        Integer rootId = -1;
        List<Menu> treeResults = MenuResolver.getResolvedWithChildrenMenuList(rootId);
        HierarchyEachUtils.recursionEach(treeResults, Menu::getChildren, new HierarchyEachUtils.EachCallback<Menu>() {

            @Override
            public boolean withParentList() {
                //设置启用，后续用于获取遍历元素的所有父级列表
                return true;
            }

            @Override
            public void beforeEach(List<Menu> sourceList) {
               //对源数据列表进行处理，比如有多个进行排个序？
            }

            @Override
            public void each(int level, Menu current, Menu parent, List<Menu> parentList, List<Menu> children, boolean hasExistChildren) {

                if (hasExistChildren) {
                    //排序子元素 -- 降序
                    children.sort(Comparator.comparing(Menu::getSort).reversed());
                }

                if (level == HierarchyEachUtils.FIRST_LEVEL) {
                    //第一层级时 -- 没有真实存在的父元素
                    System.out.println(String.format("level: %s, id: %s, sort: %s, parent id: %s, path: %s", level, current.getId(), current.getSort(), "-", current.getId()));
                } else {
                    System.out.println(String.format("level: %s, id: %s, sort: %s, parent id: %s, path: %s", level, current.getId(), current.getSort(), parent.getId(), Stream.of(parentList, Collections.singletonList(current)).flatMap(Collection::stream).map(Menu::getId).map(String::valueOf)
                            .collect(Collectors.joining("-"))));

                }
            }
        });

        System.out.println("--------------------------------------------------");

        treeResults = MenuResolver.getResolvedWithChildrenMenuList(rootId);
        HierarchyEachUtils.recursionEach(treeResults, Menu::getChildren, new HierarchyEachUtils.EachCallback<Menu>() {

            @Override
            public boolean withParentList() {
                //设置启用，后续用于获取遍历元素的所有父级列表
                return true;
            }

            @Override
            public void each(int level, Menu current, Menu parent, List<Menu> parentList, List<Menu> children, boolean hasExistChildren) {

                if (hasExistChildren) {
                    //排序子元素 -- 升序
                    children.sort(Comparator.comparing(Menu::getSort));
                }

                if (level == HierarchyEachUtils.FIRST_LEVEL) {
                    //第一层级时 -- 没有真实存在的父元素
                    System.out.println(String.format("level: %s, id: %s, sort: %s, parent id: %s, path: %s", level, current.getId(), current.getSort(), "-", current.getId()));
                } else {
                    System.out.println(String.format("level: %s, id: %s, sort: %s, parent id: %s, path: %s", level, current.getId(), current.getSort(), parent.getId(), Stream.of(parentList, Collections.singletonList(current)).flatMap(Collection::stream).map(Menu::getId).map(String::valueOf)
                            .collect(Collectors.joining("-"))));

                }
            }
        });

        System.out.println("--------------------------------------------------");

        //默认不启用父级列表
        treeResults = MenuResolver.getResolvedWithChildrenMenuList(rootId);
        HierarchyEachUtils.recursionEach(treeResults, Menu::getChildren, (level, current, parent, parentList, children, hasExistChildren) -> {

            if (hasExistChildren) {
                //排序子元素 -- 升序
                children.sort(Comparator.comparing(Menu::getSort));
            }

            if (level == HierarchyEachUtils.FIRST_LEVEL) {
                //第一层级时 -- 没有真实存在的父元素
                System.out.println(String.format("level: %s, id: %s, sort: %s, parent id: %s", level, current.getId(), current.getSort(), "-"));
            } else {
                System.out.println(String.format("level: %s, id: %s, sort: %s, parent id: %s", level, current.getId(), current.getSort(), parent.getId()));
            }
        });
    }


}