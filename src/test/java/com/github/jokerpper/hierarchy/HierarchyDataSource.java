package com.github.jokerpper.hierarchy;

public class HierarchyDataSource {

    public static final String MENU_TEXT = "[" +
            "{'id': 1, name: '一级', 'pid': -1, 'sort': 1}, " +
            "{'id': 2, name: '二级(1-2)', 'pid' : 1, 'sort': 99}" +
            "{'id': 3, name: '三级(1-2-3)', 'pid' : 2, 'sort': 1}," +
            "{'id': 4, name: '三级(1-2-4)', 'pid' : 2, 'sort': 22}," +
            "{'id': 5, name: '三级(1-2-5)', 'pid' : 2, 'sort': 5}," +
            "{'id': 6, name: '二级(1-6)', 'pid' : 1, 'sort': 98}," +
            "{'id': 7, name: '二级(1-7)', 'pid' : 1, 'sort': 92}," +
            "{'id': 8, name: '四级(1-2-3-8)', 'pid' : 3, 'sort': 91}" +
            "]";

    public static final Integer ROOT_PID = -1;
}
