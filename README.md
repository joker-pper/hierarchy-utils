# hierarchy-utils

[![Java support](https://img.shields.io/badge/Java-8+-green?logo=java&logoColor=white)](https://openjdk.java.net/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.joker-pper/hierarchy-utils.svg?label=Maven%20Central)](https://search.maven.org/search?q=io.github.joker-pper:hierarchy-utils)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![codecov](https://codecov.io/gh/joker-pper/hierarchy-utils/branch/main/graph/badge.svg)](https://codecov.io/gh/joker-pper/hierarchy-utils)

    用于构建/查找具有层级关系树形数据的工具库,以解决业务中常见的树形数据处理需求
    支持自定义过滤数据、排序及转换数据、打平递归树数据、递归遍历树形数据处理等

## 快速使用

```xml
        <dependency>
            <groupId>io.github.joker-pper</groupId>
            <artifactId>hierarchy-utils</artifactId>
            <version>${version}</version>
        </dependency>
```

## 说明

    不依赖其他组件，如要使用fastjson / fastjson2等需自行引用。

## 使用示例


### 数据结构 - 菜单

``` 
@Data
public class Menu {
    private Integer id;
    private String name;
    private Integer pid;
    private Integer sort;
    private List<Menu> children;
}
``` 

### 数据源

        //查询当前用户的菜单列表
        List<Menu> menuList = menuService.findAllByUserId();
        //通过json转换
        List<Menu> menuList = JSONObject.parseArray(menuText, Menu.class);;


### 场景1: 通过原数据结构返回树形数据 (原数据为全部铺平的数据列表)

``` 
       
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

``` 

### 场景2: 若原数据结构未定义children或不使用原数据结构，可通过转换数据结构（使用Map / JSONObject等）进行返回树形数据

``` 
    
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

        
``` 

### 场景3-1: 返回源数据列表中id为rootId的元素或pid为rootId且id能整除2的全部子元素的数据列表 （支持将树形数据打平及过滤）

``` 
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
        System.out.println(JSONObject.toJSONString(matchResults));


``` 

### 场景3-2: 【转换数据示例】返回源数据列表中id为rootId的元素或pid为rootId且id能整除2的全部子元素的数据列表 （支持将树形数据打平及过滤）

``` 
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
        System.out.println(JSONObject.toJSONString(matchResults));


``` 

### 场景4: 通过递归树数据源进行返回pid为2且包含自己的递归树数据

``` 
        
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
        
```

### 其他

#### 如何对返回结果list排序的方法

``` 
    //对返回结果排序 （只排序当前列表，不会递归排序子元素）
    HierarchySortUtils.sort(list, comparator); 
        
    //对返回结果及子元素排序（递归排序）
    HierarchySortUtils.sortWithChildren(list, childrenFunction, comparator);  

``` 

#### 递归遍历树形数据处理 （1.0.2及以上版本）

``` 
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
``` 

## 其他

  若该项目对您有所帮助，请不吝点赞，谢谢！
  
