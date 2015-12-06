package com.baidu.fis.util;

import com.alibaba.fastjson.JSONObject;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.Set;


/**
 * Created by xuchenhui on 2015/5/25.
 */
public class MapCache {
    // 缓存并操控map表
    public static JSONObject map = null;
    //public void setMap(JSONObject newMap){ map = newMap; }
    public void reloadMap(){
        String dir = Settings.getMapDir();
        if (map != null){
            System.out.println("Reload all map files in " + dir + "[" + map.hashCode() + "]");
        }

        try{
            map = loadAllMap(dir);
            System.out.println("Reload finished all maps [" + map.hashCode() + "]");
        }catch(Exception e){
            // 捕获可能的异常，不影响下次map的重新加载，否则导致当前线程退出，就不会再加载了。
            System.err.println("Failed to reload all maps: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public JSONObject getMap(){
        return map;
    }
    // 向后兼容旧方法
    public JSONObject getMap(String id){
        return map;
    }
    public void resetMap(){
        map.clear();
    }

    private static JSONObject mergeJSONObjects(JSONObject json1, JSONObject json2) {
        try {
            json1.putAll(json2);
        } catch (Exception e) {
            throw new RuntimeException("JSON Exception" + e);
        }
        return json1;
    }
    // 重新读取所有的map文件并生成map表
    protected JSONObject loadAllMap(String filePath) throws Exception{
        JSONObject newMap = new JSONObject();

        System.out.println("Load map files in : " + filePath);

        File root = new File(filePath);

        if (!root.exists() || !root.isDirectory()) {

            System.out.println("Map dir is not exists or is not an directory. `" + filePath + "`");

            return newMap;
        }

        File[] files = root.listFiles();

        for(File file:files) {
            if (!file.isDirectory() && file.canRead()) {
                String fileName = file.getName();

                if (fileName.matches(".*\\.json")){
                    JSONObject json = this.loadJson(file);

                    if (json != null) {
                        System.out.println("Load map file : " + fileName);
                        newMap = json;
                    }
                }
            }
        }

        return newMap;
    }
    protected JSONObject loadJson(File file) throws Exception {
        FileInputStream input = null;

        try {
            if (file.canRead()) {
                //System.out.println("Read map file : " + file.toPath());
                input = new FileInputStream(file);
            }
        } catch (Exception ex) {
            System.out.println("Error while load " + file.getAbsolutePath() + ".\n Error:" + ex.getMessage());
            input = null;
        }

        if (input == null) {
            return null;
        }

        try{
            String data = readStream(input);
            if (data != null) {
                return JSONObject.parseObject(data);
            }
        }catch(Exception e){
            String msg = "Error while parse JSON file: " + file.getName() + e.getMessage();
            System.err.println(msg);
            throw new Exception(msg);
        }
        return null;
    }
    protected String readStream(InputStream input) {
        String data = null;
        try {
            String enc = Settings.getString("encoding", "UTF-8");
            BufferedReader in = new BufferedReader(new UnicodeReader(input, enc));
            data = "";
            String inputLine;
            while ((inputLine = in.readLine()) != null){
                data += inputLine;
            }
            in.close();
        } catch (Exception ex) {
            // do nothing.
        }

        return data;
    }

    // 通过id获取map表节点
    public JSONObject getNode(String key, String type){
        JSONObject node, info;

        // 尝试读取
        try{
            node = map.getJSONObject(type);
            info = node.getJSONObject(key);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return info;
    }
    public JSONObject getNode(String key) {
        return getNode(key, "res");
    }

    /// 初始化方法
    public void init(ServletContext context){
        // 首次实例化加载
        if (map == null){
            reloadMap();
        }
    }

    // 单例模式
    protected MapCache() {}
    protected static MapCache instance = null;
    public static synchronized MapCache getInstance() {
        if (instance == null) {
            instance = new MapCache();
        }
        return instance;
    }
    public static synchronized void setInstance(MapCache inst){

        if(instance != null){
            System.err.println("MapCache has been created, so ignore setInstance.");
        }else{
            instance = inst;
            System.out.println("MapCache setInstance: " + inst.getClass().getName());
        }
    }
}