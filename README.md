# hierarchy-utils
    用于构建树形数据的通用工具库,支持过滤数据、排序及自定义转换数据


##使用


###数据结构 - 菜单

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

###1.通过原数据结构返回树形数据
``` 
        //查询当前用户的菜单列表(可模拟数据)
        List<Menu> dataList = menuService.findAllByUserId(1);

        //默认根元素为-1 (当前所有一级菜单的pid为-1,可根据实际定义根元素使用)
        Integer rootId = -1;

        //排序
        Comparator<Menu> comparator = new Comparator<Menu>() {
            @Override
            public int compare(Menu o1, Menu o2) {
                return Integer.compare(o1.getSort(), o2.getSort());
            }
        };

      
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

        List<Menu> defaultResults = HierarchyUtils.getHierarchyResult(
                dataList,
                defaultFunctions,
                comparator
        );

        System.out.println(JSONObject.toJSONString(defaultResults));

``` 

###2.原数据结构未定义children,通过转换数据结构返回树形数据
``` 

        //查询当前用户的菜单列表(可模拟数据)
        List<Menu> dataList = menuService.findAllByUserId(1);

        //默认根元素为-1 (当前所有一级菜单的pid为-1,可根据实际定义根元素使用)
        Integer rootId = -1;

        //排序
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
            JSONObject result = (JSONObject)JSON.toJSON(menu);

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

        List<JSONObject> transferResults = HierarchyUtils.getHierarchyResult(
                dataList,
                transferFunctions,
                comparator
        );

        System.out.println(JSONObject.toJSONString(transferResults));

``` 
###3.返回源数据列表中id为rootId的元素或pid为rootId且id能整除2的全部子元素的数据列表
``` 

        //查询当前用户的菜单列表(可模拟数据)
        List<Menu> dataList = menuService.findAllByUserId(1);

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

        Comparator<Menu> comparator = new Comparator<Menu>() {
            @Override
            public int compare(Menu o1, Menu o2) {
                return Integer.compare(o1.getSort(), o2.getSort());
            }
        };

        List<Menu> matchResults = HierarchyFlatUtils.getHierarchyFlatResult(
                dataList,
                functions,
                comparator
        );

        System.out.println(JSONObject.toJSONString(matchResults));

``` 