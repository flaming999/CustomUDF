package com.warehouse.hive.udf;

import com.alibaba.fastjson.JSON;
import com.jayway.jsonpath.JsonPath;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Description(name = "parseJsonWithPath",
    value = "_FUNC_(jsonString, jsonPath) - Returns the queried result matched the given jsonPath, in Array format.")
public class ParseJsonWithPath extends UDF {
    static class HashCache<K, V> extends LinkedHashMap<K, V> {
        private static final int CACHE_SIZE = 16;
        private static final int INIT_SIZE = 32;
        private static final float LOAD_FACTOR = 0.6f;

        HashCache() {
            super(INIT_SIZE, LOAD_FACTOR);
        }

        private static final long serialVersionUID = 1;

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > CACHE_SIZE;
        }
    }

    static Map<String, JsonPath> jsonCache = new HashCache<String, JsonPath>();

    private ArrayList<Text> result = new ArrayList<Text>();

    public List<Text> evaluate(String jsonStr, String jsonPath) {
        result.clear();

        JsonPath path = jsonCache.get(jsonPath);

        if(path == null) {
            path = JsonPath.compile(jsonPath);
            jsonCache.put(jsonPath, path);
        }

        Object res;
        try {
            res = path.read(jsonStr);
        } catch (Exception e) {
            return result;
        }

        if(res == null) return result;

        if(res instanceof List<?>) {
            for(Object o: (List<?>)res) {
                Text t = new Text();
                if(o instanceof Map<?,?>) {
                    t.set(JSON.toJSONString(o));
                } else {
                    if (o == null) {
                        t = null;
                    } else {
                        t.set(String.valueOf(o));
                    }
                }
                result.add(t);
            }
        } else {
            Text t = new Text();
            if(res instanceof Map<?,?>) {
                t.set(JSON.toJSONString(res));
            } else {
                t.set(String.valueOf(res));
            }

            result.add(t);
        }

        return result;
    }
}