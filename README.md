# hierarchy-utils
    用于构建/查找具有层级关系树形数据的工具库,以解决业务中常见的树形数据处理需求
    支持自定义过滤数据、排序及转换数据等


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


### 1.通过原数据结构返回树形数据

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
        defaultFunctions.setSetChildrenFunction((parent, children) -> {
            parent.setChildren(children);
        });

        //是否返回root元素(未设置时默认false,开启时root元素必须存在)
        defaultFunctions.setIsWithRoot(() -> false);

        //过滤条件(可选,用来筛选数据)
        defaultFunctions.setFilterPredicate(menu -> true);

        List<Menu> hierarchyResult = HierarchyUtils.getHierarchyResult(
                menuList,
                defaultFunctions,
                comparator
        );
        System.out.println(JSONObject.toJSONString(hierarchyResult));

``` 

### 2.原数据结构未定义children,通过转换数据结构返回树形数据

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
### 3.返回源数据列表中id为rootId的元素或pid为rootId且id能整除2的全部子元素的数据列表

``` 

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

        //过滤条件(可选,用来筛选数据)
        functions.setFilterPredicate(menu -> menu.getId() % 2 == 0 || Objects.equals(rootId, menu.getId()));

        //排序(需注意业务属性值是否为空),可选
        Comparator<Menu> comparator = Comparator.comparingInt(Menu::getSort);

        List<Menu> matchResults = HierarchyFlatUtils.getHierarchyFlatResult(
                menuList,
                functions,
                comparator
        );

        //对返回结果排序(需注意业务属性值是否为空),可选
        HierarchySortUtils.sort(matchResults, comparator);
        System.out.println(JSONObject.toJSONString(matchResults));


``` 

### 4.对返回结果list排序

``` 
    //对返回结果排序
    HierarchySortUtils.sort(list, comparator); 
        
    //对返回结果及子元素排序
    HierarchySortUtils.sortWithChildren(list, childrenFunction, comparator);  

``` 