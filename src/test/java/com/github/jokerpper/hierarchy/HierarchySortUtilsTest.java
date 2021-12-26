package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSONObject;
import com.github.jokerpper.hierarchy.model.Menu;
import com.github.jokerpper.hierarchy.support.HierarchyValidateHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class HierarchySortUtilsTest {

    private List<Menu> menuList;

    @Before
    public void setUp() throws Exception {
        menuList = JSONObject.parseArray(HierarchyDataSource.MENU_TEXT, Menu.class);
    }

    @Test
    public void testSort() {
        Comparator<Menu> comparator = Comparator.comparing(Menu::getId);
        List<Menu> sourceList = menuList;
        HierarchySortUtils.sort(sourceList, comparator);
        HierarchyValidateHelper.assertSameSortedResult(sourceList, comparator);
    }

    @Test
    public void testSortWithChildren() {
        Comparator<Menu> comparator = Comparator.comparing(Menu::getId);
        Function<Menu, List<Menu>> getChildrenFunction = (parent) -> parent.getChildren();
        List<Menu> sourceList = HierarchyBaseTest.MenuResolver.getResolvedWithChildrenMenuList(HierarchyDataSource.ROOT_PID);
        HierarchySortUtils.sortWithChildren(sourceList, getChildrenFunction, comparator);
        HierarchyValidateHelper.assertSameSortedResult(sourceList, comparator);
    }


    @Test
    public void testSortWithEmptyList() {
        Comparator<Menu> comparator = Comparator.comparing(Menu::getId);
        HierarchySortUtils.sort(null, comparator);
        List<Menu> sourceList = Collections.emptyList();
        HierarchySortUtils.sort(sourceList, comparator);
    }


    @Test
    public void testSortWithChildrenAndEmptyList() {
        Comparator<Menu> comparator = Comparator.comparing(Menu::getId);
        Function<Menu, List<Menu>> getChildrenFunction = (parent) -> parent.getChildren();
        HierarchySortUtils.sortWithChildren(null, getChildrenFunction, comparator);
        List<Menu> sourceList = Collections.emptyList();
        HierarchySortUtils.sortWithChildren(sourceList, getChildrenFunction, comparator);
    }


    @Test
    public void testSortWithChildrenByOther() {
        Comparator<Menu> comparator = Comparator.comparing(Menu::getId);
        Function<Menu, List<Menu>> getChildrenFunction = (parent) -> parent.getChildren();
        List<Menu> sourceList = HierarchyBaseTest.MenuResolver.getResolvedWithChildrenMenuList(HierarchyDataSource.ROOT_PID);
        for (Menu source : sourceList) {
            if (source.getChildren() == null) {
                source.setChildren(Collections.emptyList());
            }
        }
        HierarchySortUtils.sortWithChildren(sourceList, getChildrenFunction, comparator);
    }


}