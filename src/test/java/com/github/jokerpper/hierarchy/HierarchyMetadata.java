package com.github.jokerpper.hierarchy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.jokerpper.hierarchy.model.Menu;

import java.io.*;
import java.util.List;

public class HierarchyMetadata {

    static class Constants {
        final static String MENU_DEFAULT_JSON = "src/test/resources/metadata/menu-default.json";
        final static String MENU_DEFAULT_TREE_JSON = "src/test/resources/metadata/menu-default-tree.json";

        /**
         * 示例结果json
         */

        final static String MENU_DEFAULT_TRANSFER_SAMPLE_TREE_JSON = "src/test/resources/metadata/menu-default-transfer-sample-tree.json";
        final static String MENU_DEFAULT_FLAT_SAMPLE_JSON = "src/test/resources/metadata/menu-default-flat-sample.json";
    }

    public static List<Menu> getDefaultMenuList() {
        return getDefaultMenuList(Menu.class);
    }

    public static <T> List<T> getDefaultMenuList(Class<T> type) {
        return JSON.parseArray(HierarchyMetadataUtils.getAsString(Constants.MENU_DEFAULT_JSON), type);
    }

    public static List<Menu> getDefaultMenuTreeList() {
        return JSON.parseArray(HierarchyMetadataUtils.getAsString(Constants.MENU_DEFAULT_TREE_JSON), Menu.class);
    }

    public static List<JSONObject> getDefaultMenuTransferSampleTreeList() {
        return JSON.parseArray(HierarchyMetadataUtils.getAsString(Constants.MENU_DEFAULT_TRANSFER_SAMPLE_TREE_JSON), JSONObject.class);
    }

    public static List<Menu> getDefaultMenuFlatSampleList() {
        return JSON.parseArray(HierarchyMetadataUtils.getAsString(Constants.MENU_DEFAULT_FLAT_SAMPLE_JSON), Menu.class);
    }


    static class HierarchyMetadataUtils {

        /**
         * 获取字符内容
         *
         * @param path
         * @return
         */
        static String getAsString(String path) {
            try {
                Reader reader = new FileReader(path);
                StringWriter out = new StringWriter();
                copy(reader, out);
                return out.toString();
            } catch (IOException e) {
                throw new RuntimeException(String.format("get as string error, path: %s", path), e);
            }
        }

        /**
         * 复制reader到writer中
         *
         * @param in
         * @param out
         * @return
         * @throws IOException
         */
        private static int copy(Reader in, Writer out) throws IOException {
            try {
                int byteCount = 0;
                char[] buffer = new char[2048];

                int bytesRead;
                for (; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
                return byteCount;
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                }

                try {
                    out.close();
                } catch (IOException ex) {
                }

            }
        }

    }


}
