package com.taobao.f2e;


public class Test {

    public static void testKISSY1_3() {
        Main main = new Main();
        main.setOutput("d:\\code\\kissy_git\\kissy-tools\\" +
                "module-compiler\\tests\\tb_kissy_1.3\\" +
                "build\\biz\\page\\run.combo.js");
        main.setOutputDependency("d:\\code\\kissy_git\\kissy-tools\\" +
                "module-compiler\\tests\\tb_kissy_1.3\\" +
                "build\\biz\\page\\run.dep.js");
        main.setRequire("biz/page/run");
        main.getPackages().setBaseUrls(new String[]{"d:\\code\\kissy_git\\kissy-tools\\" +
                "module-compiler\\tests\\tb_kissy_1.3\\src\\"});
        main.run();
    }


    public static void testCombo() {
        ExtractDependency main = new ExtractDependency();
        main.setOutput("d:\\code\\kissy_git\\kissy-tools\\" +
                "module-compiler\\tests\\kissy_combo\\dep.js");
        main.getPackages().setBaseUrls(new String[]{
                "d:\\code\\kissy_git\\kissy-tools\\module-compiler\\tests\\kissy_combo\\"
        });
        main.run();
    }

    public static void testKISSY1_3_combo() {
        ExtractDependency main = new ExtractDependency();
        main.setOutput("d:\\code\\kissy_git\\kissy-tools\\" +
                "module-compiler\\tests\\tb_kissy_1.3\\build-combo\\biz\\dep.js");
        main.setFixModuleName(true);
        main.getPackages().setBaseUrls(new String[]{
                "d:\\code\\kissy_git\\kissy-tools\\module-compiler\\tests\\tb_kissy_1.3\\build-combo\\"
        });

        main.run();
    }

    public static void main(String[] args) {
        testCombo();
    }
}
