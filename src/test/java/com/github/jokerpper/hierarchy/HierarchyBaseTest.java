package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSONObject;
import com.github.jokerpper.hierarchy.model.Menu;
import org.junit.After;
import org.junit.Before;

import java.util.List;

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

    protected List<Menu> menuList;

    @Before
    public void setUp() throws Exception {
        //查询当前用户的菜单列表(可模拟数据)
        //menuList = menuService.findAllByUserId(1);
        menuList = JSONObject.parseArray(menuText, Menu.class);
    }

    @After
    public void tearDown() throws Exception {

    }

}
