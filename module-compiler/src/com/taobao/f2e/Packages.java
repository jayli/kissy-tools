package com.taobao.f2e;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * KISSY Packages config.
 *
 * @author yiminghe@gmail.com
 * @since 2012-08-06
 */
public class Packages {

    //map about module name and its des ,for cache
    private static HashMap<String, Module> moduleCache = new HashMap<String, Module>();

    /**
     * package encoding
     */
    private String[] encodings = {"utf-8"};

    /**
     * package base urls
     */
    private String[] baseUrls = {
            //"d:/code/kissy_git/kissy-tools/module-compiler/test/kissy/"
    };

    public String[] getEncodings() {
        return encodings;
    }

    public void setEncodings(String[] encodings) {
        this.encodings = encodings;
    }

    public String[] getBaseUrls() {
        return baseUrls;
    }

    public void setBaseUrls(String[] baseUrls) {

        ArrayList<String> re = new ArrayList<String>();
        for (String base : baseUrls) {
            base = FileUtils.escapePath(base).trim();
            if (!base.endsWith("/")) {
                base += "/";
            }
            re.add(base);
        }
        this.baseUrls = re.toArray(new String[re.size()]);
    }

    public Module getModule(String moduleName) {
        Module m = this.getModuleFromCache(moduleName);
        if (m != null) {
            return m;
        }
        Packages packages = this;
        String[] baseUrls = packages.getBaseUrls();
        String[] encodings = packages.getEncodings();
        String path = packages.getModuleFullPath(moduleName);
        String baseUrl = path.replaceFirst("(?i)" + moduleName + ".js$", "");
        int index = ArrayUtils.indexOf(baseUrls, baseUrl);
        if (index == -1 || index >= encodings.length) {
            index = 0;
        }
        String encoding = encodings[index];
        m = constructModule(encoding, path, baseUrl, moduleName);
        this.setModuleToCache(moduleName, m);
        return m;
    }

    private Module getModuleFromCache(String moduleName) {
        return moduleCache.get(moduleName);
    }

    private void setModuleToCache(String moduleName, Module module) {
        moduleCache.put(moduleName, module);
    }

    private Module constructModule(String encoding, String path,
                                   String packageBase, String moduleName) {
        Module module = new Module();
        module.setEncoding(encoding);
        module.setFullpath(path);
        module.setPackageBase(packageBase);
        module.setName(moduleName);
        return module;
    }

    private String getModuleFullPath(String moduleName) {
        Packages packages = this;
        String r = FileUtils.escapePath(moduleName);
        String[] baseUrls = packages.getBaseUrls();
        if (r.charAt(0) == '/') {
            r = r.substring(1);
        }
        if (!r.endsWith(".js") && !r.endsWith(".JS")) {
            r += ".js";
        }
        String path = "";
        for (String baseUrl : baseUrls) {
            path = baseUrl + r;
            if (new File(path).exists()) {
                break;
            }
        }
        return path;
    }


    public Module getModuleFromPath(String path) {
        String name;
        String[] encodings = this.getEncodings();
        String encoding = encodings[0];
        path = FileUtils.escapePath(path);
        String[] baseUrls = this.getBaseUrls();
        int finalIndex = -1, curIndex = -1;
        String finalBase = "";
        int packageIndex = -1;
        int finalPackageIndex = -1;
        Module m = null;
        for (String baseUrl : baseUrls) {
            packageIndex++;
            curIndex = path.indexOf(baseUrl, 0);
            if (curIndex > finalIndex) {
                finalIndex = curIndex;
                finalPackageIndex = packageIndex;
                finalBase = baseUrl;
            }
        }
        if (finalIndex != -1) {
            if (finalPackageIndex < encodings.length) {
                encoding = encodings[finalPackageIndex];
            }
            name = FileUtils.removeSuffix(path.substring(finalBase.length()));
            m = constructModule(encoding, path, finalBase, name);
            String moduleNodeName = m.getModuleNameFromNode();
            if (moduleNodeName != null) {
                name = moduleNodeName;
                m.setName(name);
            }
        }
        return m;
    }
}