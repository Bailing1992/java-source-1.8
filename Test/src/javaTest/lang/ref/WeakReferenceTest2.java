package javaTest.lang.ref;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class WeakReferenceTest2 {


    public  static  void main(String [] args) throws InterruptedException {
        System.out.println("result:" + test());

    }

    private static String test(){
        String a = new String("a");
        //System.out.println(a);
        WeakReference<String> b = new WeakReference<String>(a);
        //System.out.println(b.get());
        WeakHashMap<String, Integer> weakMap = new WeakHashMap<String, Integer>();
        weakMap.put(b.get(), 1);
        a = null;
        System.out.println("GC前b.get()：" + b.get());
        System.out.println("GC前weakMap：" + weakMap);
        System.gc();
        System.out.println("GC后b.get()：" + b.get());
        System.out.println("GC后weakMap：" + weakMap);
        String c = "";
        try{
            c = b.get().replace("a", "b");
            System.out.println("C:"+c);
            return c;
        }catch(Exception e){
            c = "c";
            System.out.println("Exception");
            return c;
        }finally{
            c += "d";
            return c + "e";
        }
    }

}
