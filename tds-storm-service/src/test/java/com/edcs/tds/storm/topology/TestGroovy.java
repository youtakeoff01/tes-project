package com.edcs.tds.storm.topology;

import com.edcs.tds.storm.core.exception.GroovyException;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.math.BigDecimal;

/**
 * Created by CaiSL2 on 2017/10/30.
 */
public class TestGroovy {
    public static void main(String[] args) throws  GroovyException {

        StringBuffer scriptText = new StringBuffer(
                "  int c = a + b;System.out.println(c);System.out.println(Math.abs(c)); if(c > 6){return  false;};if(c==-5){return true;};//kkkk");

        StringBuffer scriptText1 = new StringBuffer(
                "   if(a<b){System.out.println(2343)}else{System.out.println(8888)}");

        GroovyShell gs = new GroovyShell();
        Script script = gs.parse(scriptText1.toString());
        Binding shellContext;
        shellContext = new Binding();
        shellContext.setProperty("a", 3);
        String str = "4.35";
        shellContext.setProperty("b", str);
       // shellContext.setProperty("c", null);


        script.setBinding(shellContext);

        long st = System.currentTimeMillis();

        script.run();
        long et = System.currentTimeMillis();

       // System.out.println("Calc time used:" + (et - st) + " ms, result:" + bool);


    }

}
